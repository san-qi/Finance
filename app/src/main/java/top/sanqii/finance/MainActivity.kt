package top.sanqii.finance

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import top.sanqii.finance.room.FinanceDatabase
import top.sanqii.finance.ui.components.TabRow
import top.sanqii.finance.ui.theme.FinanceTheme

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "datastore")
val Context.database: FinanceDatabase
    get() = FinanceDatabase.getInstance(applicationContext)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun App() {
    FinanceTheme {
        val allScreens = BaseScreen.values().toList()
        val navController = rememberAnimatedNavController()
        val backstackEntry = navController.currentBackStackEntryAsState()
        val currentScreen = BaseScreen.fromRoute(backstackEntry.value?.destination?.route)
        val scrollState = rememberScrollState()
        var offset by remember { mutableStateOf(Offset.Zero) }

        Scaffold(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset = Offset.Zero },
                        onDragEnd = {
                            //TODO:确定滑动的offset阈值
                            val offsetThreshold = 260
                            BaseScreen
                                .fromRoute(backstackEntry.value?.destination?.route)
                                ?.let {
                                    if (offset.x > offsetThreshold) {
                                        navController.cleanAndNavigate(it.getBefore().name)
                                    } else if (offset.x < -offsetThreshold) {
                                        navController.cleanAndNavigate(it.getNext().name)
                                    }
                                }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offset += Offset(dragAmount, 0f)
                        }
                    )
                }
                .scrollable(state = scrollState, orientation = Orientation.Vertical),
            topBar = {
                TabRow(
                    allBaseScreens = allScreens,
                    onTabSelected = { navController.cleanAndNavigate(it.name) },
                    currentBaseScreen = currentScreen,
                    // 用回退栈的数量来判断是否显示回退键
                    showBackButton = navController.backQueue.size > 2,
                    onBackPressed = { navController.popBackStack() }
                )
            }
        ) {
            FinanceNavHost(
                modifier = Modifier.padding(it),
                navController = navController
            )
        }
    }
}

private fun NavController.cleanAndNavigate(route: String) {
    while (this.backQueue.size > 1) this.popBackStack()
    this.navigate(route)
}
