package top.sanqii.finance.utils

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import top.sanqii.finance.dataStore

interface ApiService {
    @POST("register")
    fun register(@Body newUserJson: NewUserJson): Call<ReplyJson>

    @POST("login")
    fun login(@Body userJson: UserJson): Call<ReplyJson>

    @POST("password")
    fun password(@Body password: Password): Call<ReplyJson>

    @POST("upload")
    fun upload(@Body recordJson: List<RecordJson>): Call<ReplyJson>

    @POST("delete")
    fun delete(@Query("rid") rid: Long, @Body deleteList: List<Long>): Call<ReplyJson>

    @GET("download")
    fun download(@Query("rid") rid: Long): Call<ReplyJson>
}

// 用于网络通信
object RetrofitClient {
    private var okHttpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getClient(context: Context): Retrofit {
        if (retrofit == null) {
            okHttpClient = OkHttpClient().newBuilder().addInterceptor(Interceptor { chain ->
                val resp = chain.proceed(chain.request())
                val cookies = resp.headers("Set-Cookie")
                if (!cookies.isNullOrEmpty()) {
                    var cookieStr = ""
                    for (str in cookies) {
                        cookieStr += "$str;"
                    }
                    MainScope().launch {
                        withContext(Dispatchers.IO) {
                            DataStoreUtil.put(context.dataStore, "cookie", cookieStr)
                        }
                    }
                }
                resp
            }).addInterceptor(Interceptor { chain ->
                val cookieStr = runBlocking {
                    DataStoreUtil.get<String>(context.dataStore, "cookie").first()
                }
                chain.proceed(
                    chain.request().newBuilder().addHeader("Cookie", cookieStr).build()
                )
            }).build()

            retrofit = Retrofit.Builder().baseUrl("https://sanqii.top/finance/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient!!)
                .build()
        }
        return retrofit!!
    }

    fun getService(context: Context): ApiService {
        return getClient(context).create(ApiService::class.java)
    }
}