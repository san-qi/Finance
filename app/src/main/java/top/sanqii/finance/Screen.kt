package top.sanqii.finance

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 确认每个屏幕在导航中排列的顺序
 */
fun screenRank(route: String?): Int =
    when (route) {
        null -> 0
        else -> screenRank(route)
    }

@JvmName("screenRank1")
fun screenRank(route: String): Int =
    when (route.substringBefore("/")) {
        BaseScreen.Overview.name -> 1
        BaseScreen.Incomes.name -> 2
        BaseScreen.Bills.name -> 3
        BaseScreen.Accounts.name -> 4
        BaseScreen.Settings.name -> 5
        // 返回其他表示正处于InnerScreen中
        else -> 6
    }

fun screenRank(screen: BaseScreen?): Int =
    when (screen) {
        null -> 0
        else -> screenRank(screen)
    }

@JvmName("screenRank2")
fun screenRank(screen: BaseScreen): Int =
    when (screen) {
        BaseScreen.Overview -> 1
        BaseScreen.Incomes -> 2
        BaseScreen.Bills -> 3
        BaseScreen.Accounts -> 4
        BaseScreen.Settings -> 5
    }


/**
 * 能从顶部导航栏进入的屏幕
 */
enum class BaseScreen(val icon: ImageVector) {
    Overview(
        icon = Icons.Filled.PieChart
    ),
    Incomes(
        icon = Icons.Filled.AttachMoney
    ),
    Bills(
        icon = Icons.Filled.MoneyOff
    ),
    Accounts(
        icon = Icons.Filled.Leaderboard
    ),
    Settings(
        icon = Icons.Filled.Settings
    );

    companion object {
        fun fromRoute(route: String?): BaseScreen? =
            when (route?.substringBefore("/")) {
                Overview.name -> Overview
                Incomes.name -> Incomes
                Bills.name -> Bills
                Accounts.name -> Accounts
                Settings.name -> Settings
                null -> Overview
                // else -> throw IllegalArgumentException("Route $route is not recognized.")
                // 返回其他表示正处于InnerScreen中
                else -> null
            }
    }

    fun getTitle(): String =
        when (this) {
            Overview -> "回顾"
            Incomes -> "收入"
            Bills -> "支出"
            Accounts -> "账户"
            Settings -> "设置"
        }

    fun getNext(): BaseScreen =
        when (this) {
            Overview -> Incomes
            Incomes -> Bills
            Bills -> Accounts
            Accounts -> Settings
            Settings -> Overview
        }

    fun getBefore(): BaseScreen =
        when (this) {
            Overview -> Settings
            Incomes -> Overview
            Bills -> Incomes
            Accounts -> Bills
            Settings -> Accounts
        }

}

/**
 * 不能从顶部导航栏进入的深层屏幕
 */
enum class InnerScreen {
    NewRecord,
    Help,
    UserCenter,
    Register,
    Login,
    ChangePassword,
}
