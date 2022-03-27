package top.sanqii.finance.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.Serializable

// 用于存储本地首选项,即用dataStore存储缓存
object DataStoreUtil {
    suspend fun <T> put(dataStore: DataStore<Preferences>, key: String, value: T) {
        withContext(Dispatchers.IO) {
            dataStore.edit {
                when (value) {
                    is Int -> it[intPreferencesKey(key)] = value
                    is Long -> it[longPreferencesKey(key)] = value
                    is Double -> it[doublePreferencesKey(key)] = value
                    is Float -> it[floatPreferencesKey(key)] = value
                    is Boolean -> it[booleanPreferencesKey(key)] = value
                    is String -> it[stringPreferencesKey(key)] = value
                    else -> throw IllegalArgumentException("This type can be saved into DataStore")
                }
            }
        }
    }

    inline fun <reified T> get(dataStore: DataStore<Preferences>, key: String): Flow<T>
            where T : Comparable<T>,
                  T : Serializable {
        return dataStore.data.map<Preferences, T> {
            when (T::class) {
                Int::class -> {
                    it[intPreferencesKey(key)] ?: 0
                }
                Long::class -> {
                    it[longPreferencesKey(key)] ?: 0L
                }
                Double::class -> {
                    it[doublePreferencesKey(key)] ?: 0.0
                }
                Float::class -> {
                    it[floatPreferencesKey(key)] ?: 0f
                }
                Boolean::class -> {
                    it[booleanPreferencesKey(key)] ?: false
                }
                String::class -> {
                    it[stringPreferencesKey(key)] ?: ""
                }
                else -> {
                    throw IllegalArgumentException("This type can be get into DataStore")
                }
            } as T
        }
    }
}