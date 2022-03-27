package top.sanqii.finance.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.sanqii.finance.R

/**
 * 自定义浮动按钮
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FinanceFAB(modifier: Modifier = Modifier, extended: Boolean, onClick: () -> Unit) {
    FinanceBaseFAB(modifier = modifier, onClick = onClick) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
        AnimatedVisibility(visible = extended) {
            Text(
                text = stringResource(id = R.string.edit),
                Modifier.padding(start = 8.dp, top = 2.dp)
            )
        }
    }
}

@Composable
fun FinanceBaseFAB(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    FloatingActionButton(modifier = modifier, onClick = onClick, backgroundColor = Color.Gray) {
        Row(Modifier.padding(horizontal = 16.dp)) {
            content()
        }
    }
}

/**
 * ScrollState的拓展函数，用于判断当前屏幕是否处于向上滑动状态
 */
@Composable
fun ScrollState.isScrollUp(): Boolean {
    var previous by remember { mutableStateOf(value) }
    return remember {
        derivedStateOf { (previous <= value).also { previous = value } }
    }.value
}