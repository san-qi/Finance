package top.sanqii.finance.ui.screen

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import top.sanqii.finance.InnerScreen
import top.sanqii.finance.R
import top.sanqii.finance.room.store.Record
import top.sanqii.finance.ui.components.*
import top.sanqii.finance.ui.theme.AccountStartColor
import top.sanqii.finance.ui.theme.BillsStartColor
import top.sanqii.finance.ui.theme.IncomesStartColor

private val DefaultPadding = 12.dp

private const val CARD_SHOWN_ITEMS = 3

/**
 * It used by Overview  screen
 */
@OptIn(ExperimentalAnimationApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun OverviewBody(
    allIncomes: List<Record>,
    allBills: List<Record>,
    controller: NavHostController,
    onRecordClick: (Long, Color) -> Unit,
    onClickSeeAllAccounts: () -> Unit,
    onClickSeeAllIncomes: () -> Unit,
    onClickSeeAllBills: () -> Unit,
) {
    //TODO 滑动到顶部或底部时很卡
    Column(
        Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AccountCard(
            data = allIncomes.plus(allBills),
            onClickSeeAll = onClickSeeAllAccounts,
            onItemClick = onRecordClick
        )
        Spacer(Modifier.height(DefaultPadding))
        IncomeCard(
            data = allIncomes,
            onClickSeeAll = onClickSeeAllIncomes,
            onItemClick = onRecordClick
        )
        Spacer(Modifier.height(DefaultPadding))
        BillCard(
            data = allBills,
            onClickSeeAll = onClickSeeAllBills,
            onItemClick = onRecordClick
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 32.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        FinanceFAB(
            extended = true
        ) {
            controller.navigate(InnerScreen.NewRecord.name)
        }
    }
}

@Composable
private fun <T> OverviewScreenCard(
    title: String,
    amount: Float,
    data: List<T>,
    showItemSize: Int = CARD_SHOWN_ITEMS,
    negative: Boolean,
    dividerStartColor: Color,
    onClickSeeAll: () -> Unit,
    row: @Composable (T, Color) -> Unit,
) {
    Card {
        Column {
            Row(
                Modifier
                    .padding(DefaultPadding)
                    .fillMaxWidth(), Arrangement.SpaceBetween
            ) {
                //TODO: 更改标题字体
                Text(
                    text = title,
                    style = MaterialTheme.typography.h2,
                    modifier = Modifier.padding(
                        start = DefaultPadding,
                        top = DefaultPadding / 2,
                        bottom = DefaultPadding
                    )
                )
                val amountText = (if (negative) "-" else "") + "￥" + formatAmount(amount)
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.align(Alignment.Bottom)
                )
            }
            FinanceDivider(
                modifier = Modifier.padding(
                    start = DefaultPadding,
                    end = DefaultPadding
                ), dividerStartColor
            )
            Column(Modifier.padding(start = 16.dp, top = 4.dp, end = 8.dp)) {
                data.asSequence().take(showItemSize)
                    .zip(
                        generateColorSequence(
                            dividerStartColor,
                            COLOR_STEP_SIZE,
                            showItemSize
                        )
                    )
                    .forEach {
                        row(it.first, it.second)
                    }
                SeeAllButton(
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = "All $title"
                    },
                    onClick = onClickSeeAll,
                )
            }
        }
    }
}

@Composable
private fun SeeAllButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .fillMaxWidth()
    ) {
        Text(stringResource(R.string.see_all))
    }
}

@Composable
fun AccountCard(
    data: List<Record>,
    onClickSeeAll: () -> Unit,
    onItemClick: (Long, Color) -> Unit
) {
    val amount = data.map { if (it.isIncome) it.amount else -it.amount }.sum()
    OverviewScreenCard(
        title = stringResource(R.string.account),
        amount = Math.abs(amount),
        data = data.sortedByDescending { it.date },
        showItemSize = CARD_SHOWN_ITEMS * 2 - 1,
        negative = amount < 0,
        dividerStartColor = AccountStartColor,
        onClickSeeAll = onClickSeeAll
    ) { record, color ->
        RecordRow(
            modifier = Modifier.clickable { onItemClick(record.id, color) },
            record = record,
            color = color,
            negative = !record.isIncome
        )
    }

}

@Composable
fun IncomeCard(
    data: List<Record>,
    onClickSeeAll: () -> Unit,
    onItemClick: (Long, Color) -> Unit
) {
    val amount = data.map { it.amount }.sum()
    OverviewScreenCard(
        title = stringResource(R.string.income),
        amount = amount,
        onClickSeeAll = onClickSeeAll,
        data = data.sortedByDescending { it.amount },
        negative = false,
        dividerStartColor = IncomesStartColor
    ) { record, color ->
        RecordRow(
            modifier = Modifier.clickable { onItemClick(record.id, color) },
            record = record,
            color = color,
            negative = false
        )
    }
}

@Composable
fun BillCard(
    data: List<Record>,
    onClickSeeAll: () -> Unit,
    onItemClick: (Long, Color) -> Unit
) {
    val amount = data.map { it.amount }.sum()
    OverviewScreenCard(
        title = stringResource(R.string.bill),
        amount = amount,
        onClickSeeAll = onClickSeeAll,
        data = data.sortedByDescending { it.amount },
        negative = true,
        dividerStartColor = BillsStartColor
    ) { record, color ->
        RecordRow(
            modifier = Modifier.clickable { onItemClick(record.id, color) },
            record = record,
            color = color,
            negative = true
        )
    }
}