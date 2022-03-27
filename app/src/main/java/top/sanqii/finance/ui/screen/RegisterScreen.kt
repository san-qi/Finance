package top.sanqii.finance.ui.screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import top.sanqii.finance.dataStore
import top.sanqii.finance.database
import top.sanqii.finance.ui.components.*
import top.sanqii.finance.utils.DataStoreUtil
import top.sanqii.finance.utils.NewUserJson
import top.sanqii.finance.utils.ReplyJson
import top.sanqii.finance.utils.RetrofitClient

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RegisterBody(onSucceed: () -> Unit) {
    CenterRow {
        Column(modifier = Modifier.fillMaxHeight(.8f), verticalArrangement = Arrangement.Center) {
            val context = LocalContext.current
            val localKeyboardController = LocalSoftwareKeyboardController.current
            val (idField, psdField, psdAgainField, emailField) = remember { FocusRequester.createRefs() }
            var id by remember { mutableStateOf("") }
            var idIsError by remember { mutableStateOf(false) }
            var psd by remember { mutableStateOf("") }
            var psdIsError by remember { mutableStateOf(false) }
            var psdAgain by remember { mutableStateOf("") }
            var psdAgainIsError by remember { mutableStateOf(false) }
            var email by remember { mutableStateOf("") }
            var emailIsError by remember { mutableStateOf(false) }
            var state by remember { mutableStateOf(10) }

            // 弹出对话框
            var showDialog by remember { mutableStateOf(false) }
            var message by remember { mutableStateOf("") }
            AnimatedVisibility(showDialog) {
                FinanceAlertDialog(
                    onDismiss = { showDialog = false },
                    bodyText = message
                )
            }

            val onSubmit: (id: String, psd: String, email: String) -> Unit = { _id, _psd, _email ->
                RetrofitClient.getService(context).register(NewUserJson(_id.toLong(), _psd, _email))
                    .enqueue(
                        object : Callback<ReplyJson> {
                            override fun onResponse(
                                call: Call<ReplyJson>,
                                response: Response<ReplyJson>
                            ) {
                                //Log.d("RESPONSE_SUCCESS", response.toString())
                                state = if (response.code() == 200) {
                                    0
                                } else if (response.code() == 502) {
                                    message = "服务器未开启,该功能暂时无法使用"
                                    showDialog = true
                                    10
                                } else {
                                    showDialog = true
                                    when (response.body()!!.code) {
                                        1 -> {
                                            message = "用户已存在,请重试"
                                            1
                                        }
                                        22 -> {
                                            message = "密码格式错误,请重试"
                                            2
                                        }
                                        23 -> {
                                            message = "邮箱格式错误,请重试"
                                            4
                                        }
                                        else -> {
                                            message = "帐号格式错误,请重试"
                                            1
                                        }
                                    }
                                }
                            }

                            override fun onFailure(call: Call<ReplyJson>, t: Throwable) {
                                message = "发生了不可知的错误,请稍后重试"
                                showDialog = true
                                state = 10
                            }

                        }
                    )
            }
            val submitInterceptor: () -> Unit = {
                localKeyboardController?.hide()
                idIsError = false
                psdIsError = false
                psdAgainIsError = false
                emailIsError = false
                if (id.length < 5) {
                    state = 1
                } else if (psd.length < 7) {
                    state = 2
                } else if (psdAgain != psd) {
                    state = 3
                } else if (!Regex("[^@\\s]+@(\\w+\\.)+\\w+").matches(email)) {
                    state = 4
                } else {
                    onSubmit(id, psd, email)
                }
            }
            when (state) {
                0 -> {
                    Log.d("REGISTER", "Register Succeed!")
                    LaunchedEffect(Dispatchers.IO) {
                        DataStoreUtil.put(context.dataStore, "isLogin", true)
                        val oldId = DataStoreUtil.get<String>(context.dataStore, "id").first()
                        // 当用户切换帐号时,清空本地数据,防止数据产生冲突
                        if (oldId != id) {
                            DataStoreUtil.put(context.dataStore, "id", id)
                            val recordDao = context.database.getRecordDao()
                            val records = recordDao.queryAllRecords().first()
                            withContext(Dispatchers.IO) {
                                recordDao.deleteRecords(*records.toTypedArray())
                            }
                            DataStoreUtil.put(context.dataStore, "deleteList", "")
                            DataStoreUtil.put(context.dataStore, "lastRid", 0L)
                        }
                        onSucceed()
                    }
                }
                // 用户不匹配时
                1 -> idIsError = true
                // 密码不匹配时
                2 -> psdIsError = true
                // 确认密码不匹配时
                3 -> psdAgainIsError = true
                // 邮箱不匹配时
                4 -> emailIsError = true
            }

            Column(
                modifier = Modifier.fillMaxHeight(.56f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                IdOutlineText(
                    idField = idField,
                    id = id,
                    idIsError = idIsError,
                    onInputChange = { id = it },
                    onFocusChange = { idIsError = it },
                    keyboardActions = KeyboardActions(
                        onNext = {
                            psdField.requestFocus()
                        }
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                PasswordOutlineText(
                    isDone = false,
                    psdField = psdField,
                    psd = psd,
                    psdIsError = psdIsError,
                    onInputChange = { psd = it },
                    onFocusChange = { psdIsError = it },
                    keyboardActions = KeyboardActions(
                        onNext = {
                            psdAgainField.requestFocus()
                        }
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                PasswordOutlineText(
                    label = "确认密码",
                    isDone = false,
                    psdField = psdAgainField,
                    psd = psdAgain,
                    psdIsError = psdAgainIsError,
                    onInputChange = { psdAgain = it },
                    onFocusChange = { psdAgainIsError = it },
                    keyboardActions = KeyboardActions(
                        onNext = {
                            emailField.requestFocus()
                        }
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                EmailOutlineText(
                    emailField = emailField,
                    email = email,
                    emailIsError = emailIsError,
                    onInputChange = { email = it },
                    onFocusChange = { emailIsError = it },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            submitInterceptor()
                        }
                    )
                )
            }

            SubmitButton(text = "注册") {
                submitInterceptor()
            }
        }
    }
}
