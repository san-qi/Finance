package top.sanqii.finance.ui.screen

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import top.sanqii.finance.InnerScreen
import top.sanqii.finance.dataStore
import top.sanqii.finance.database
import top.sanqii.finance.ui.components.FinanceAlertDialog
import top.sanqii.finance.utils.*
import java.io.IOException

private val DEFAULT_PADDING = 12.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsBody(navController: NavController) {
    Column(
        Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        var showDialog by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf("") }
        AnimatedVisibility(showDialog) {
            FinanceAlertDialog(
                onDismiss = { showDialog = false },
                bodyText = message
            )
        }

        val items = mapOf(
            "用户中心" to { navController.navigate(InnerScreen.UserCenter.name) },
            "用户指南" to { navController.navigate(InnerScreen.Help.name) },
            "同步数据" to {
                // 上传本地未同步数据,并下载服务器中未同步的数据
                scope.launch(Dispatchers.IO) {
                    syncData(
                        context,
                        onShowDialog = { showDialog = true },
                        onMessageChange = {
                            message = it
                        }
                    )
                }

                scope.launch(Dispatchers.IO) {
                    // 更新服务器中本地已删除的数据
                    val lastRid = DataStoreUtil.get<Long>(context.dataStore, "lastRid").first()
                    val deleteList =
                        DataStoreUtil.get<String>(context.dataStore, "deleteList").first()
                            .split("+").filter { it.isNotEmpty() }.map { it.toLong() }
                    RetrofitClient.getService(context).delete(lastRid, deleteList).enqueue(
                        object : Callback<ReplyJson> {
                            override fun onResponse(
                                call: Call<ReplyJson>,
                                response: Response<ReplyJson>
                            ) {
                                if (response.code() == 200) {
                                    scope.launch(Dispatchers.IO) {
                                        // message = "服务器数据更新完成"
                                        DataStoreUtil.put(context.dataStore, "deleteList", "")
                                    }
                                } else if (response.code() == 502) {
                                    message = "服务器未开启,该功能暂时无法使用"
                                } else if (response.body()!!.code == 5) {
                                    message = "数据删除失败,请稍后再试"
                                } else {
                                    message = "请您在用户中心登录后再使用该功能"
                                }
                                showDialog = true
                            }

                            override fun onFailure(
                                call: Call<ReplyJson>,
                                t: Throwable
                            ) {
                                // message = "服务器数据更新失败,请您稍后再试"
                                // 通常是没开网络
                                message = "发生了未知错误,请稍后重试"
                                showDialog = true
                            }
                        }
                    )
                }
                Unit
            },
            "检查更新" to {
                showDialog = true
                message = "您当前已经处于最新版本"
            })
        items.forEach {
            SettingItem(it)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingItem(item: Map.Entry<String, () -> Unit>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DEFAULT_PADDING),
        onClick = item.value
    ) {
        Row(
            Modifier.padding(DEFAULT_PADDING),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(item.key)
            Icon(imageVector = Icons.Filled.NavigateNext, contentDescription = null)
        }
    }
}

suspend fun syncData(
    context: Context,
    onShowDialog: () -> Unit,
    onMessageChange: (String) -> Unit
) {
    val lastRid = DataStoreUtil.get<Long>(context.dataStore, "lastRid").first()
    val localRecords = context.database.getRecordDao().queryRecordsByLimitId(lastRid).first()
    val recordDao = context.database.getRecordDao()
    val localRecordsJson = localRecords.map { entity2RecordJson(it) }
    // 可能会由于网络原因导致同步请求失败,需要捕获
    // 即使是suspend函数中也需要使用同步函数获取,因为两次请求之间的数据有依赖
    try {
        RetrofitClient.getService(context).upload(localRecordsJson).execute()
    } catch (e: IOException) {
        null
    }?.let { uploadRes ->
        if (uploadRes.code() == 200) {
            recordDao.deleteRecords(*localRecords.toTypedArray())
            if (recordDao.queryAllRecords().first().isEmpty()) {
                DataStoreUtil.put(context.dataStore, "lastRid", 0L)
            }
        } else {
            if (uploadRes.code() == 502) {
                onMessageChange("服务器未开启,该功能暂时无法使用")
            } else if (uploadRes.body()!!.code == 4) {
                onMessageChange("数据删除失败,请稍后再试")
            } else {
                onMessageChange("请您在用户中心登录后再使用该功能")
            }
            onShowDialog()
            return
        }
    }

    val newRid = DataStoreUtil.get<Long>(context.dataStore, "lastRid").first()
    // 可能会由于网络原因导致同步请求失败,需要捕获
    // 即使是suspend函数中也需要使用同步函数获取,因为两次请求之间的数据有依赖
    try {
        RetrofitClient.getService(context).download(newRid).execute()
    } catch (e: IOException) {
        null
    }?.let { downloadRes ->
        if (downloadRes.code() == 200) {
            val recordsJson = downloadRes.body()!!.data
            val records = recordsJson.map { recordJson2Entity(it) }
            context.database.getRecordDao()
                .insertRecords(*records.toTypedArray())
            val maxId = recordsJson.maxByOrNull { it.id }?.id ?: newRid
            DataStoreUtil.put(context.dataStore, "lastRid", maxId)
            onMessageChange("您的数据已经同步完成")
        } else if (downloadRes.code() == 502) {
            onMessageChange("服务器未开启,该功能暂时无法使用")
        } else {
            onMessageChange("请您在用户中心登录后再使用该功能")
        }
        onShowDialog()
    }
}
