package top.sanqii.finance.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.sanqii.finance.ui.components.CenterRow

@Composable
fun HelpBody() {
    val title2text =
        mapOf(
            "注意事项" to "    同时在多个终端登录一个帐号是受支持的,并且及时使用'同步数据'功能可以让您终端的数据保持互通;当然,您也可以拥有多个帐号,同时可以在同一台终端上自由的切换帐号或注册新帐号,但当您这么做之前请先同步数据,否则您本地未同步的数据将会丢失",
            "如何修改密码" to "    使用该功能需要先在'用户中心'进行登录操作,当您完成登录后,该功能方可在'用户中心'栏目下出现并使用;同时,当您修改密码后需要再次进行登录操作,否则其它的在线功能将无法使用",
            "如何新建记录" to "    点击首页的右下角按钮即可进入'新建记录'页面,在'新建记录'页面下点击右下角按钮即可切换到新增收入或是新增支出",
            "如何删除记录" to "    从首页或其它页面中点击具体的记录,即可进入详细记录页面,在此页面下将记录从右向左滑动将会出现删除按钮,点击该按钮将会删除记录",
            "如何同步数据" to "    在使用该功能前需要先进行登录操作;当您的本地数据丢失(例如无意间清除了数据,更换手机等)时,可将之前同步的数据恢复到本地;同时,您也可以利用同一个账户将其他设备的最新数据同步到该设备中",
            "关于首页记录" to "    首页记录是会出现重复数据的,但'收入'栏目与'支出'栏目之间不会重复,只有'我的账户'栏目中存在'收入'栏目与'支出'栏目中已经出现过的记录;" +
                    "这样的安排并非多余,当您的记录过多时这会非常有用,因为'我的账户'栏目中的记录是按照日期排序的," +
                    "而其他两栏是按照金额排序的,这样的安排能使得您更能了解最近创建记录的大致情况",
        )

    var current by remember { mutableStateOf("") }
    Column(Modifier.scrollable(rememberScrollState(), Orientation.Vertical)) {
        title2text.forEach {
            CenterCard(title = it.key, body = it.value, current = current) { item ->
                current = item
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun CenterCard(
    title: String,
    body: String,
    current: String,
    onClick: (String) -> Unit = {}
) {
    Card(Modifier.padding(12.dp, 4.dp)) {
        CenterRow(
            Modifier.clickable { onClick(title) }) {
            Column(
                Modifier
                    .animateContentSize(), Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.caption
                )
                if (current == title) {
                    Spacer(Modifier.height(12.dp))
                    Text(modifier = Modifier.padding(bottom = 4.dp), text = body)
                }
            }
        }
    }
}
