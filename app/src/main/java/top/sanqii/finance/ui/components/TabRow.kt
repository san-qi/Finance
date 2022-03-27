package top.sanqii.finance.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.sanqii.finance.BaseScreen
import java.util.*

private val TabHeight = 56.dp
private const val InactiveTabOpacity = 0.60f
private const val TabFadeInAnimationDuration = 150
private const val TabFadeInAnimationDelay = 100
private const val TabFadeOutAnimationDuration = 100


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TabRow(
    allBaseScreens: List<BaseScreen>,
    currentBaseScreen: BaseScreen?,
    onTabSelected: (BaseScreen) -> Unit,
    showBackButton: Boolean,
    onBackPressed: () -> Unit
) {
    Surface(
        Modifier
            .height(TabHeight)
            .fillMaxWidth()
    ) {
        Row(Modifier.selectableGroup()) {
            AnimatedVisibility(visible = showBackButton) {
                Box(
                    Modifier
                        .padding(vertical = 16.dp)
                        .height(TabHeight)
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            }
            allBaseScreens.forEach {
                Tab(
                    text = it.getTitle(),
                    icon = it.icon,
                    selected = currentBaseScreen == it
                ) { onTabSelected(it) }
            }
        }
    }
}

@Composable
private fun Tab(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onSelected: () -> Unit
) {
    val color = MaterialTheme.colors.onSurface
    val durationMillis = if (selected) TabFadeInAnimationDuration else TabFadeOutAnimationDuration
    val animSpec = remember {
        tween<Color>(
            durationMillis = durationMillis,
            easing = LinearEasing,
            delayMillis = TabFadeInAnimationDelay
        )
    }
    val tabTintColor by animateColorAsState(
        targetValue = if (selected) color else color.copy(alpha = InactiveTabOpacity),
        animationSpec = animSpec
    )
    val modifier = if (selected)
        Modifier
            .padding(16.dp)
            .animateContentSize()
            .height(TabHeight)
    else
        Modifier
            .padding(16.dp)
            .animateContentSize()
            .height(TabHeight)
            .selectable(
                selected = selected,
                onClick = onSelected,
                role = Role.Tab,
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = rememberRipple(
                    bounded = false,
                    radius = Dp.Unspecified,
                    color = Color.Unspecified
                )
            )
    Row(modifier) {
        Icon(imageVector = icon, tint = tabTintColor, contentDescription = null)
        AnimatedVisibility(selected) {
            Spacer(Modifier.width(12.dp))
            Text(
                text = text.uppercase(Locale.getDefault()),
                color = tabTintColor,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

    }
}