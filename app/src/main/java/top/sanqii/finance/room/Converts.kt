package top.sanqii.finance.room

import androidx.room.TypeConverter
import java.time.LocalDate

class Converts {
    @TypeConverter
    fun date2string(time: LocalDate): String {
        return time.toString()
    }

    @TypeConverter
    fun string2date(string: String): LocalDate {
        return LocalDate.parse(string)
    }

}