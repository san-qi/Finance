package top.sanqii.finance.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.sanqii.finance.room.store.Record
import top.sanqii.finance.ui.components.*

/**
 * It used by Incomes screen and Bills screen
 */
@Composable
fun RecordsBody(
    records: List<Record>,
    title: String,
    startColor: Color,
    negative: Boolean,
    onRecordClick: (Long, Color) -> Unit
) {
    StatementBody(
        items = records,
        startColor = startColor,
        amounts = { it.amount },
        circleLabel = title
    ) { record, color ->
        RecordRow(
            modifier = Modifier.clickable { onRecordClick(record.id, color) },
            record = record,
            color = color,
            negative = negative
        )
    }
}

/**
 * Detail screen for a single Record
 */
@Composable
fun SingleRecordBody(
    record: Record,
    startColor: Color,
    negative: Boolean,
    canDelete: Boolean = false,
    onDelete: (Record) -> Unit = {}
) {
    StatementBody(
        items = listOf(record),
        startColor = startColor,
        amounts = { it.amount },
        circleLabel = record.type
    ) { item, color ->
        RecordRow(
            record = item,
            color = color,
            negative = negative,
            canDelete = canDelete,
            onDelete = onDelete
        )
    }
}

@Composable
fun <T> StatementBody(
    modifier: Modifier = Modifier,
    items: List<T>,
    startColor: Color,
    amounts: (T) -> Float,
    circleLabel: String,
    row: @Composable (T, Color) -> Unit
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Box(Modifier.padding(16.dp)) {
            val accountsProportion = items.extractProportions { amounts(it) }
            val amountsTotal = items.map { amounts(it) }.sum()
            val circleColors =
                generateColorSequence(startColor, COLOR_STEP_SIZE, items.size).toList()
            AnimatedCircle(
                accountsProportion,
                circleColors,
                Modifier
                    .height(300.dp)
                    .align(Alignment.Center)
                    .fillMaxWidth()
            )
            Column(modifier = Modifier.align(Alignment.Center)) {
                Text(
                    text = circleLabel,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = formatAmount(amountsTotal),
                    style = MaterialTheme.typography.h2,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        if (items.isNotEmpty()) {
            Card {
                Column(modifier = Modifier.padding(12.dp)) {
                    items.asSequence()
                        .zip(generateColorSequence(startColor, COLOR_STEP_SIZE, items.size))
                        .forEach {
                            row(it.first, it.second)
                        }
                }
            }
        }
    }
}