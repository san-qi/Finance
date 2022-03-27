package top.sanqii.finance.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import top.sanqii.finance.R
import top.sanqii.finance.room.store.Record
import top.sanqii.finance.ui.theme.FinanceDialogThemeOverlay
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val AmountDecimalFormat = DecimalFormat("###,###,###.##")
val DateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
const val COLOR_STEP_SIZE = 0x2020

fun formatAmount(amount: Float): String = AmountDecimalFormat.format(amount)
fun formatDate(datetime: LocalDate): String = DateFormat.format(datetime)

/**
 * 生成渐变颜色序列
 * @param startColor: 起始颜色
 * @param stepSize: 步长
 * @param listSize: 序列长度
 * TODO: 可能会溢出
 */
fun generateColorSequence(startColor: Color, stepSize: Int, listSize: Int) =
    generateSequence(seed = startColor, nextFunction = {
        val newColorValue = it.toArgb() + stepSize
        Color(newColorValue)
    }).take(listSize)

/**
 * Used with accounts and bills to create the animated circle.
 */
fun <E> List<E>.extractProportions(selector: (E) -> Float): List<Float> {
    val total = this.sumOf { selector(it).toDouble() }
    return this.map { (selector(it) / total).toFloat() }
}

/**
 * 带有渐变颜色的自定义分割线
 */
@Composable
fun FinanceDivider(modifier: Modifier = Modifier, startColor: Color) {
    Row(modifier.fillMaxWidth()) {
        generateColorSequence(startColor, COLOR_STEP_SIZE / 8, 20).forEach {
            Spacer(
                modifier = Modifier
                    .weight(5f)
                    .height(1.dp)
                    .background(it)
            )
        }
    }
}

@Composable
fun RecordRow(
    modifier: Modifier = Modifier,
    record: Record,
    color: Color,
    negative: Boolean,
    canDelete: Boolean = false,
    onDelete: (Record) -> Unit = {}
) {
    BaseRow<Record>(
        modifier = modifier,
        color = color,
        title = record.type,
        subtitle = record.date,
        amount = record.amount,
        negative = negative,
        canDelete = canDelete
    ) { onDelete(record) }
}

/**
 * @param color: 每条记录前面条行对应的颜色
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> BaseRow(
    modifier: Modifier = Modifier,
    color: Color,
    title: String,
    subtitle: LocalDate,
    amount: Float,
    negative: Boolean,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    val moneySign = if (negative) "-￥" else "￥"
    val formatAmount = formatAmount(amount)

    val state = rememberSwipeableState(0)
    val coroutineScope = rememberCoroutineScope()
    val sizePx = with(LocalDensity.current) { 48.dp.toPx() }
    val anchors = mapOf(0f to 0, -sizePx to 1)

    val elevationOverlay = LocalElevationOverlay.current
    val absoluteElevation = LocalAbsoluteElevation.current
    val backgroundColor =
        elevationOverlay?.apply(MaterialTheme.colors.surface, absoluteElevation)
            ?: Color.Unspecified

    Box(
        Modifier.swipeable(
            enabled = canDelete,
            state = state,
            anchors = anchors,
            thresholds = { _, _ -> FractionalThreshold(0.3f) },
            orientation = Orientation.Horizontal

        )
    ) {
        Row(
            modifier = Modifier.matchParentSize(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                onDelete()
                coroutineScope.launch {
                    state.animateTo(0, tween(700))
                }
            }) { Icon(Icons.Filled.Delete, null) }
        }

        Row(
            modifier = modifier
                .height(68.dp)
                .offset { (IntOffset(state.offset.value.roundToInt(), 0)) }
                .background(
                    backgroundColor
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(
                Modifier
                    .size(4.dp, 36.dp)
                    .background(color)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier) {
                Text(text = title, style = MaterialTheme.typography.body1)
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(text = formatDate(subtitle), style = MaterialTheme.typography.subtitle1)
                }
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = moneySign,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Text(
                    text = formatAmount,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Spacer(Modifier.width(16.dp))
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                if (!canDelete)
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        Modifier
                            .padding(end = 12.dp)
                            .size(24.dp)
                    )
            }
        }

        Divider(color = MaterialTheme.colors.background, thickness = 1.dp, modifier = modifier)
    }
}

/**
 * 自定义消息对话框
 */
@Composable
fun FinanceAlertDialog(
    onDismiss: () -> Unit,
    bodyText: String,
    dismissOnClickOutside: Boolean = true
) {
    FinanceDialogThemeOverlay {
        AlertDialog(
            onDismissRequest = onDismiss,
            text = { Text(bodyText) },
            buttons = {
                Column {
                    Divider(
                        Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colors.onSurface.copy(.2f)
                    )
                    TextButton(
                        onClick = onDismiss,
                        shape = RectangleShape,
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(id = R.string.confirm))

                    }
                }
            },
            properties = DialogProperties(dismissOnClickOutside = dismissOnClickOutside)
        )
    }
}

@Composable
fun CenterRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        content()
    }
}