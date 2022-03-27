package top.sanqii.finance.utils

import top.sanqii.finance.room.store.Record
import java.time.LocalDate

/**
 * User to network submit.
 */
data class UserJson(val uid: Long, val password: String)

data class NewUserJson(val uid: Long, val password: String, val email: String)

data class Password(val password: String)

data class ReplyJson(
    val code: Int,
    val data: List<RecordJson>,
    val details: String
)

data class RecordJson(
    val id: Long,
    val amount: Float,
    val date: String,
    val record_type: String,
    val is_income: Boolean,
)

fun recordJson2Entity(recordJson: RecordJson): Record {
    return recordJson.run {
        Record(
            id = id,
            amount = amount,
            date = LocalDate.parse(date),
            type = record_type,
            isIncome = is_income
        )
    }
}

fun entity2RecordJson(record: Record): RecordJson {
    return record.run {
        RecordJson(
            id = id,
            amount = amount,
            date = date.toString(),
            record_type = type,
            is_income = isIncome
        )
    }
}