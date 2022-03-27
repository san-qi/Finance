package top.sanqii.finance.room.store

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface RecordDao {
    @Insert
    fun insertRecords(vararg records: Record)

    @Update
    fun updateRecords(vararg records: Record)

    @Query("DELETE FROM RECORDS WHERE id = :id")
    fun deleteRecordById(id: Long)

    @Delete
    fun deleteRecords(vararg records: Record)

    @Query("SELECT * FROM RECORDS WHERE id > :id")
    fun queryRecordsByLimitId(id: Long): Flow<List<Record>>

    @Query("SELECT * FROM RECORDS")
    fun queryAllRecords(): Flow<List<Record>>

    @Query("SELECT * FROM RECORDS WHERE isIncome<>0")
    fun queryAllIncomes(): Flow<List<Record>>

    @Query("SELECT * FROM RECORDS WHERE isIncome=0")
    fun queryAllBills(): Flow<List<Record>>

    @Query("SELECT * FROM RECORDS WHERE id = :id")
    fun queryRecordById(id: Int): Flow<Record>

    @Query("SELECT * FROM RECORDS WHERE date BETWEEN DATE(:startDate) AND DATE(:endDate)")
    fun queryRecordsLimitDate(startDate: LocalDate, endDate: LocalDate): Flow<List<Record>>

    @Query("SELECT * FROM RECORDS WHERE isIncome<>0 AND (date BETWEEN DATE(:startDate) AND DATE(:endDate))")
    fun queryIncomesLimitDate(startDate: LocalDate, endDate: LocalDate): Flow<List<Record>>

    @Query("SELECT * FROM RECORDS WHERE isIncome=0 AND (date BETWEEN DATE(:startDate) AND DATE(:endDate))")
    fun queryBillsLimitDate(startDate: LocalDate, endDate: LocalDate): Flow<List<Record>>
}