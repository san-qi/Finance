package top.sanqii.finance.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import top.sanqii.finance.database
import top.sanqii.finance.ui.components.AnimatedTable
import top.sanqii.finance.ui.components.CenterRow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

// 获取给定日期所在的周对应的起始跨度
fun getDuringOfWeek(localDate: LocalDate): Pair<LocalDate, LocalDate> {
    val monday = localDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sunday = localDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    return Pair(monday, sunday)
}

// 获取给定日期所在的年对应的起始跨度
fun getDuringOfYear(localDate: LocalDate): Pair<LocalDate, LocalDate> {
    val firstDay = localDate.with(TemporalAdjusters.firstDayOfYear())
    val lastDay = localDate.with(TemporalAdjusters.lastDayOfYear())
    return Pair(firstDay, lastDay)
}

//TODO: implement this account body
@Composable
fun AccountsBody() {
    val context = LocalContext.current
    val now = LocalDate.now()

    val duringOfYear = getDuringOfYear(now)
    val recordOfYear by context.database.getRecordDao()
        .queryRecordsLimitDate(duringOfYear.first, duringOfYear.second).collectAsState(emptyList())
    val recordGroupByMonth = recordOfYear.groupBy { it.date.month.value }

    val duringOfWeek = getDuringOfWeek(now)
    val recordOfWeek by context.database.getRecordDao()
        .queryRecordsLimitDate(duringOfWeek.first, duringOfWeek.second).collectAsState(emptyList())
    val recordGroupByDay = recordOfWeek.groupBy { it.date.dayOfWeek.value }

    val timeOfMonth = listOf("一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "冬月", "腊月")
    val amountOfMonth = emptyList<Float>().toMutableList()
    for (month in 1..12) {
        amountOfMonth += recordGroupByMonth[month]?.map { it.amount }?.sum() ?: 0f
    }

    val res = getDuringOfWeek(now)
    Log.d("RESPONSE", res.toString())
    val timeOfWeek = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val amountOfDay = emptyList<Float>().toMutableList()
    for (day in 1..7) {
        amountOfDay += recordGroupByDay[day]?.map { it.amount }?.sum() ?: 0f
    }

    Column {
        Box {
            Card(
                shape = RoundedCornerShape(12),
                elevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.618f)
                    .padding(horizontal = 16.dp)
            ) {
                AnimatedTable(record = amountOfMonth, timeLabel = timeOfMonth)
            }
            CenterRow { Text("年度动态") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box {
            Card(
                shape = RoundedCornerShape(12),
                elevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.618f)
                    .padding(horizontal = 16.dp)
            ) {
                AnimatedTable(record = amountOfDay, timeLabel = timeOfWeek)
            }
            CenterRow { Text("周内动态") }
        }
    }
}
