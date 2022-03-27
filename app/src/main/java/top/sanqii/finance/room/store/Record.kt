package top.sanqii.finance.room.store

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "RECORDS")
data class Record(
    val amount: Float,
    val date: LocalDate,
    val type: String,
    val isIncome: Boolean,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L
)