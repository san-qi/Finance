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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import top.sanqii.finance.dataStore
import top.sanqii.finance.ui.components.CenterRow
import top.sanqii.finance.ui.components.FinanceAlertDialog
import top.sanqii.finance.ui.components.PasswordOutlineText
import top.sanqii.finance.ui.components.SubmitButton
import top.sanqii.finance.utils.DataStoreUtil
import top.sanqii.finance.utils.Password
import top.sanqii.finance.utils.ReplyJson
import top.sanqii.finance.utils.RetrofitClient

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChangePasswordBody(onSucceed: () -> Unit) {
    CenterRow {
        Column(
            modifier = Modifier.fillMaxHeight(.8f),
            verticalArrangement = Arrangement.Center
        ) {
            val context = LocalContext.current
            val localKeyboardController = LocalSoftwareKeyboardController.current
            val (psdField, psdAgainField) = remember { FocusRequester.createRefs() }
            var psd by remember { mutableStateOf("") }
            var psdIsError by remember { mutableStateOf(false) }
            var psdAgain by remember { mutableStateOf("") }
            var psdAgainIsError by remember { mutableStateOf(false) }
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

            val onSubmit: (psd: String) -> Unit = { _psd ->
                // Log.d("CHANGE_PASSWORD", _psd)
                RetrofitClient.getService(context).password(Password(_psd)).enqueue(
                    object : Callback<ReplyJson> {
                        override fun onResponse(
                            call: Call<ReplyJson>,
                            response: Response<ReplyJson>
                        ) {
                            // Log.d("CHANG_PASSWORD", response.body().toString())
                            state = if (response.code() == 200) {
                                0
                            } else if (response.code() == 502) {
                                message = "服务器未开启,该功能暂时无法使用"
                                showDialog = true
                                10
                            } else if (response.body()!!.code == 11) {
                                message = "请重新登录后再修改密码"
                                showDialog = true
                                10
                            } else {
                                message = "密码修改失败,请稍后重试"
                                showDialog = true
                                1
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
                psdIsError = false
                psdAgainIsError = false
                if (psd.length < 7) {
                    state = 1
                } else if (psdAgain != psd) {
                    state = 2
                } else {
                    onSubmit(psd)
                }
            }
            when (state) {
                0 -> {
                    Log.d("CHANGE_PASSWORD", "Change Password Succeed!")
                    LaunchedEffect(Dispatchers.IO) {
                        DataStoreUtil.put(context.dataStore, "isLogin", true)
                        onSucceed()
                    }
                }
                1 -> psdIsError = true
                2 -> psdAgainIsError = true
            }

            Column(
                modifier = Modifier.fillMaxHeight(0.25f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                PasswordOutlineText(
                    label = "新密码",
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
                    label = "确认新密码",
                    isDone = true,
                    psdField = psdAgainField,
                    psd = psdAgain,
                    psdIsError = psdAgainIsError,
                    onInputChange = { psdAgain = it },
                    onFocusChange = { psdAgainIsError = it },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            submitInterceptor()
                        }
                    )
                )
            }


            SubmitButton(text = "确认") {
                submitInterceptor()
            }
        }

    }
}