package top.sanqii.finance.ui.screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import top.sanqii.finance.dataStore
import top.sanqii.finance.database
import top.sanqii.finance.ui.components.*
import top.sanqii.finance.utils.DataStoreUtil
import top.sanqii.finance.utils.ReplyJson
import top.sanqii.finance.utils.RetrofitClient
import top.sanqii.finance.utils.UserJson

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginBody(onSucceed: () -> Unit) {
    CenterRow {
        Column(
            modifier = Modifier.fillMaxHeight(.8f),
            verticalArrangement = Arrangement.Center
        ) {
            val context = LocalContext.current
            val localKeyboardController = LocalSoftwareKeyboardController.current
            val (idField, psdField) = remember { FocusRequester.createRefs() }
            var id by remember { mutableStateOf("") }
            var idIsError by remember { mutableStateOf(false) }
            var psd by remember { mutableStateOf("") }
            var psdIsError by remember { mutableStateOf(false) }
            var state by remember { mutableStateOf(10) }

            // 弹出对话框
            var showDialog by remember { mutableStateOf(false) }
            var message by remember { mutableStateOf("") }
            AnimatedVisibility(showDialog) {
                FinanceAlertDialog(
                    onDismiss = { showDialog = false },
                    bodyText = message
                )
            }

            val onSubmit: (id: String, psd: String) -> Unit = { _id, _psd ->
                RetrofitClient.getService(context).login(UserJson(_id.toLong(), _psd)).enqueue(
                    object : Callback<ReplyJson> {
                        override fun onResponse(
                            call: Call<ReplyJson>,
                            response: Response<ReplyJson>
                        ) {
                            // Log.d("RESPONSE_SUCCESS", response.toString())
                            state = if (response.code() == 200) {
                                0
                            } else if (response.code() == 502) {
                                message = "服务器未开启,该功能暂时无法使用"
                                showDialog = true
                                10
                            } else {
                                if (response.body()!!.code == 11) {
                                    message = "用户不存在,请重试"
                                    showDialog = true
                                    1
                                } else {
                                    message = "密码错误,请重试"
                                    showDialog = true
                                    2
                                }
                            }
                        }

                        override fun onFailure(call: Call<ReplyJson>, t: Throwable) {
                            message = "发生了不可知的错误,请稍后重试"
                            showDialog = true
                            state = 10
                        }
                    }
                )
            }
            val submitInterceptor: () -> Unit = {
                localKeyboardController?.hide()
                when (id.length < 5) {
                    true -> state = 1
                    false -> when (psd.length < 7) {
                        true -> state = 2
                        false -> onSubmit(id, psd)
                    }
                }
            }
            when (state) {
                0 -> {
                    idIsError = false
                    psdIsError = false
                    Log.d("LOGIN", "Login Succeed!")
                    LaunchedEffect(Dispatchers.IO) {
                        DataStoreUtil.put(context.dataStore, "isLogin", true)
                        val oldId = DataStoreUtil.get<String>(context.dataStore, "id").first()
                        // 当用户切换帐号时,清空本地数据,防止数据产生冲突
                        if (oldId != id) {
                            DataStoreUtil.put(context.dataStore, "id", id)
                            val recordDao = context.database.getRecordDao()
                            val records = recordDao.queryAllRecords().first()
                            withContext(Dispatchers.IO) {
                                recordDao.deleteRecords(*records.toTypedArray())
                            }
                            DataStoreUtil.put(context.dataStore, "deleteList", "")
                            DataStoreUtil.put(context.dataStore, "lastRid", 0L)
                        }
                        onSucceed()
                    }
                }
                // 用户不匹配时
                1 -> {
                    idIsError = true
                    psdIsError = false
                }
                // 密码不匹配时
                2 -> {
                    idIsError = false
                    psdIsError = true
                }
                // 初始状态
                10 -> {
                    idIsError = false
                    psdIsError = false
                }
            }

            Column(
                modifier = Modifier.fillMaxHeight(0.25f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                IdOutlineText(
                    idField = idField,
                    id = id,
                    idIsError = idIsError,
                    onInputChange = { id = it },
                    onFocusChange = { idIsError = it },
                    keyboardActions = KeyboardActions(
                        onNext = {
                            psdField.requestFocus()
                        }
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                PasswordOutlineText(
                    isDone = true,
                    psdField = psdField,
                    psd = psd,
                    psdIsError = psdIsError,
                    onInputChange = { psd = it },
                    onFocusChange = { psdIsError = it },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            submitInterceptor()
                        }
                    )
                )
            }

            SubmitButton(text = "登录") {
                submitInterceptor()
            }
        }
    }
}