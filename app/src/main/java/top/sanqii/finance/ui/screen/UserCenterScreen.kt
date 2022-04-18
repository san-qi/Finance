package top.sanqii.finance.ui.screen

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
import kotlinx.coroutines.withContext
import top.sanqii.finance.InnerScreen
import top.sanqii.finance.dataStore
import top.sanqii.finance.database
import top.sanqii.finance.utils.DataStoreUtil

private val DEFAULT_PADDING = 12.dp

@Composable
fun UserCenterBody(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Column(
        Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val isLogin: Boolean by DataStoreUtil.get<Boolean>(
            LocalContext.current.dataStore,
            "isLogin"
        )
            .collectAsState(false)

        val items = mapOf(
            "注册" to { navController.navigate(InnerScreen.Register.name) },
            "登录" to { navController.navigate(InnerScreen.Login.name) },
            "修改密码" to { navController.navigate(InnerScreen.ChangePassword.name) },
            "退出登录" to {
                scope.launch(Dispatchers.IO) {
                    DataStoreUtil.put(context.dataStore, "id", "")
                    DataStoreUtil.put(context.dataStore, "isLogin", false)
                    val recordDao = context.database.getRecordDao()
                    val records = recordDao.queryAllRecords().first()
                    withContext(Dispatchers.IO) {
                        recordDao.deleteRecords(*records.toTypedArray())
                    }
                    DataStoreUtil.put(context.dataStore, "deleteList", "")
                    DataStoreUtil.put(context.dataStore, "lastRid", 0L)
                    withContext(Dispatchers.Main) {
                        navController.popBackStack()
                    }
                }
                Unit
            }
        )

        if (isLogin) {
            items.forEach {
                UserCenterItem(it)
            }
        } else {
            items.minus(listOf("修改密码", "退出登录")).forEach {
                UserCenterItem(it)
            }
        }

    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserCenterItem(item: Map.Entry<String, () -> Unit>) {
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
