package top.sanqii.finance

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.sanqii.finance.room.store.Record
import top.sanqii.finance.ui.screen.*
import top.sanqii.finance.ui.theme.BillsStartColor
import top.sanqii.finance.ui.theme.IncomesStartColor
import top.sanqii.finance.utils.DataStoreUtil
import java.time.LocalDate

private const val ANIMATE_DURATION_MILLIS = 700

// 页面导航栏目
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FinanceNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val context = LocalContext.current
    val allIncomes by context.database.getRecordDao().queryAllIncomes().collectAsState(emptyList())
    val allBills by context.database.getRecordDao().queryAllBills().collectAsState(emptyList())
    AnimatedNavHost(
        navController = navController,
        startDestination = BaseScreen.Overview.name,
        modifier = modifier
    ) {
        // 主页导航
        composable(
            BaseScreen.Overview.name,
            enterTransition = { enterTrans(this, BaseScreen.Overview) },
            exitTransition = { exitTrans(this, BaseScreen.Overview) },
            popEnterTransition = { enterTrans(this, BaseScreen.Overview) },
            popExitTransition = { exitTrans(this, BaseScreen.Overview) }
        ) {
            OverviewBody(
                allIncomes = allIncomes,
                allBills = allBills,
                controller = navController,
                onRecordClick = { id, color ->
                    navController.navigate("Detail/${id}/${color.toArgb()}")
                },
                onClickSeeAllAccounts = { navController.navigate(BaseScreen.Accounts.name) },
                onClickSeeAllIncomes = { navController.navigate(BaseScreen.Incomes.name) },
                onClickSeeAllBills = { navController.navigate(BaseScreen.Bills.name) }
            )
        }
        // 收入页导航
        composable(
            BaseScreen.Incomes.name,
            enterTransition = { enterTrans(this, BaseScreen.Incomes) },
            exitTransition = { exitTrans(this, BaseScreen.Incomes) },
            popEnterTransition = { enterTrans(this, BaseScreen.Incomes) },
            popExitTransition = { exitTrans(this, BaseScreen.Incomes) }
        ) {
            RecordsBody(
                allIncomes,
                title = stringResource(R.string.incomeScreenCircleLabel),
                startColor = IncomesStartColor, negative = false
            ) { id, color ->
                navController.navigate("Detail/${id}/${color.toArgb()}")
            }
        }
        // 支出页导航
        composable(
            BaseScreen.Bills.name,
            enterTransition = { enterTrans(this, BaseScreen.Bills) },
            exitTransition = { exitTrans(this, BaseScreen.Bills) },
            popEnterTransition = { enterTrans(this, BaseScreen.Bills) },
            popExitTransition = { exitTrans(this, BaseScreen.Bills) }
        ) {
            RecordsBody(
                allBills,
                title = stringResource(R.string.billScreenCircleLabel),
                startColor = BillsStartColor, negative = true
            ) { id, color ->
                navController.navigate("Detail/${id}/${color.toArgb()}")
            }
        }
        // 详细信息页导航
        composable(
            "Detail/{id}/{color}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.IntType
                },
                navArgument("color") {
                    type = NavType.IntType
                }
            ),
            enterTransition = { enterTrans(this, BaseScreen.Incomes) },
            exitTransition = { exitTrans(this, BaseScreen.Incomes) },
            popEnterTransition = { enterTrans(this, BaseScreen.Incomes) },
            popExitTransition = { exitTrans(this, BaseScreen.Incomes) }
        ) {
            val id = it.arguments?.getInt("id")
            val color = it.arguments?.getInt("color") ?: IncomesStartColor.toArgb()
            id?.let { notNullId ->
                val record by context.database.getRecordDao().queryRecordById(notNullId)
                    .collectAsState(
                        Record(
                            id = 0L,
                            amount = 0f,
                            date = LocalDate.now(),
                            type = "空类型",
                            isIncome = true
                        )
                    )
                // 此处判断不能去掉，否则界面发生重组时程序会因删除数据后(此时record为null)找不到而数据崩溃
                if (record != null) {
                    SingleRecordBody(
                        record,
                        Color(color),
                        !record.isIncome,
                        canDelete = true
                    ) { item ->
                        navController.popBackStack()
                        MainScope().launch {
                            withContext(Dispatchers.IO) {
                                context.database.getRecordDao().deleteRecords(item)
                                // 删除database中的数据
                                val lastRid =
                                    DataStoreUtil.get<Long>(context.dataStore, "lastRid").first()
                                // 更新dataStore缓存中的数据
                                if (item.id <= lastRid) {
                                    val deleteList =
                                        DataStoreUtil.get<String>(context.dataStore, "deleteList")
                                            .first()
                                    DataStoreUtil.put(
                                        context.dataStore,
                                        "deleteList",
                                        "$deleteList+${item.id}"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        //账户页导航
        composable(
            BaseScreen.Accounts.name,
            enterTransition = { enterTrans(this, BaseScreen.Accounts) },
            exitTransition = { exitTrans(this, BaseScreen.Accounts) },
            popEnterTransition = { enterTrans(this, BaseScreen.Accounts) },
            popExitTransition = { exitTrans(this, BaseScreen.Accounts) }
        ) {
            AccountsBody()
        }
        //设置页导航
        composable(
            BaseScreen.Settings.name,
            enterTransition = { enterTrans(this, BaseScreen.Settings) },
            exitTransition = { exitTrans(this, BaseScreen.Settings) },
            popEnterTransition = { enterTrans(this, BaseScreen.Settings) },
            popExitTransition = { exitTrans(this, BaseScreen.Settings) }
        ) {
            SettingsBody(navController)
        }
        //帮助页导航
        composable(
            InnerScreen.Help.name,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Left,
                    animationSpec = tween(ANIMATE_DURATION_MILLIS)
                )
            }, exitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(ANIMATE_DURATION_MILLIS)
                )
            }
        ) {
            HelpBody()
        }
        //用户中心页导航
        composable(
            InnerScreen.UserCenter.name,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Left,
                    animationSpec = tween(ANIMATE_DURATION_MILLIS)
                )
            }, exitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(ANIMATE_DURATION_MILLIS)
                )
            }
        ) {
            UserCenterBody(navController)
        }
        //注册页导航
        composable(
            InnerScreen.Register.name,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Left,
                    animationSpec = tween(ANIMATE_DURATION_MILLIS)
                )
            }, exitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(ANIMATE_DURATION_MILLIS)
                )
            }
        ) {
            RegisterBody { navController.popBackStack() }
        }
        //登录页导航
        composable(
            InnerScreen.Login.name,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Left,
                    animationSpec = tween(ANIMATE_DURATION_MILLIS)
                )
            }, exitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(ANIMATE_DURATION_MILLIS)
                )
            }
        ) {
            LoginBody { navController.popBackStack() }
        }
        //修改密码页导航
        composable(
            InnerScreen.ChangePassword.name,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Left,
                    animationSpec = tween(ANIMATE_DURATION_MILLIS)
                )
            }, exitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(ANIMATE_DURATION_MILLIS)
                )
            }
        ) {
            ChangePasswordBody { navController.popBackStack() }
        }
        //新建记录页导航
        composable(
            InnerScreen.NewRecord.name,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Left,
                    animationSpec = tween(ANIMATE_DURATION_MILLIS)
                )
            }, exitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(ANIMATE_DURATION_MILLIS)
                )
            }
        ) {
            NewRecordBody(navController)
        }
    }
}

//屏幕切换动画
@OptIn(ExperimentalAnimationApi::class)
fun enterTrans(
    scope: AnimatedContentScope<NavBackStackEntry>,
    currentScreen: BaseScreen,
): EnterTransition {
    val rank = screenRank(currentScreen)
    val initialRank = screenRank(scope.initialState.destination.route)
    return if (rank >= initialRank)
        scope.slideIntoContainer(
            AnimatedContentScope.SlideDirection.Left,
            animationSpec = tween(ANIMATE_DURATION_MILLIS)
        )
    else
        scope.slideIntoContainer(
            AnimatedContentScope.SlideDirection.Right,
            animationSpec = tween(ANIMATE_DURATION_MILLIS)
        )
}

@OptIn(ExperimentalAnimationApi::class)
fun exitTrans(
    scope: AnimatedContentScope<NavBackStackEntry>,
    currentScreen: BaseScreen,
): ExitTransition {
    val rank = screenRank(currentScreen)
    val targetRank = screenRank(scope.targetState.destination.route)
    return if (rank <= targetRank)
        scope.slideOutOfContainer(
            AnimatedContentScope.SlideDirection.Left,
            animationSpec = tween(ANIMATE_DURATION_MILLIS)
        )
    else
        scope.slideOutOfContainer(
            AnimatedContentScope.SlideDirection.Right,
            animationSpec = tween(ANIMATE_DURATION_MILLIS)
        )
}