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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import top.sanqii.finance.InnerScreen
import top.sanqii.finance.dataStore
import top.sanqii.finance.utils.DataStoreUtil

private val DEFAULT_PADDING = 12.dp

@Composable
fun UserCenterBody(navController: NavController) {
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
        )

        if (isLogin) {
            items.forEach {
                UserCenterItem(it)
            }
        } else {
            items.minus("修改密码").forEach {
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
