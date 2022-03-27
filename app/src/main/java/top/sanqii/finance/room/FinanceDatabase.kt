package top.sanqii.finance.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import top.sanqii.finance.room.store.Record
import top.sanqii.finance.room.store.RecordDao

@Database(entities = [Record::class], version = 2, exportSchema = false)
@TypeConverters(Converts::class)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun getRecordDao(): RecordDao

    // 单例模式访问
    companion object {
        private fun creator(application: Context): FinanceDatabase {
            return Room.databaseBuilder(
                application,
                FinanceDatabase::class.java,
                "FinanceDatabase"
            )
                .build()
        }

        @Volatile
        private var instance: FinanceDatabase? = null
        fun getInstance(application: Context): FinanceDatabase =
            instance ?: synchronized(this) {
                instance ?: creator(application).apply {
                    instance = this
                }
            }
    }
}