package top.sanqii.finance.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun BaseOutlineText(
    label: String = "",
    isDone: Boolean = false,
    placeholder: String,
    inputPattern: Regex,
    leadingIcon: ImageVector,
    objField: FocusRequester,
    obj: String,
    objIsError: Boolean,
    onInputChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    keyboardActions: KeyboardActions
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(objField)
            .onFocusChanged { if (it.isFocused) onFocusChange(false) },
        value = obj,
        onValueChange = {
            if (inputPattern.matches(it)) onInputChange(it)
        },
        singleLine = true,
        isError = objIsError,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(leadingIcon, null) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = if (isDone) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = keyboardActions
    )
}

@Composable
fun IdOutlineText(
    idField: FocusRequester,
    id: String,
    idIsError: Boolean,
    onInputChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    keyboardActions: KeyboardActions
) {
    BaseOutlineText(
        label = "帐号",
        placeholder = "由5-12位数字构成",
        inputPattern = Regex("[0-9]{0,12}"),
        leadingIcon = Icons.Filled.Person,
        objField = idField,
        obj = id,
        objIsError = idIsError,
        onInputChange = onInputChange,
        onFocusChange = onFocusChange,
        keyboardActions = keyboardActions
    )
}

@Composable
fun EmailOutlineText(
    emailField: FocusRequester,
    email: String,
    emailIsError: Boolean,
    onInputChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    keyboardActions: KeyboardActions
) {
    BaseOutlineText(
        label = "邮箱",
        isDone = true,
        placeholder = "请输入正确的邮箱格式",
        //格式需要额外校验，否则用户输入不了
        inputPattern = Regex(".*"),
        leadingIcon = Icons.Filled.Email,
        objField = emailField,
        obj = email,
        objIsError = emailIsError,
        onInputChange = onInputChange,
        onFocusChange = onFocusChange,
        keyboardActions = keyboardActions
    )
}

@Composable
fun PasswordOutlineText(
    label: String = "密码",
    isDone: Boolean = false,
    psdField: FocusRequester,
    psd: String,
    psdIsError: Boolean,
    onInputChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    keyboardActions: KeyboardActions
) {
    val psdPlaceholder = "由7-15位大小写字母或数字构成"
    var psdVisible by remember { mutableStateOf(false) }
    val psdVisibleTrans =
        if (psdVisible) VisualTransformation.None else PasswordVisualTransformation()

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(psdField)
            .onFocusChanged {
                if (it.isFocused) {
                    onFocusChange(false)
                } else {
                    psdVisible = false
                }
            },
        value = psd,
        onValueChange = {
            val pattern = Regex("[a-zA-Z0-9]{0,15}")
            if (pattern.matches(it)) onInputChange(it)
        },
        singleLine = true,
        isError = psdIsError,
        label = { Text(label) },
        placeholder = { Text(psdPlaceholder) },
        leadingIcon = { Icon(Icons.Filled.Lock, null) },
        trailingIcon = {
            IconButton(onClick = { psdVisible = !psdVisible }) {
                if (psdVisible)
                    Icon(Icons.Filled.Visibility, null)
                else
                    Icon(Icons.Filled.VisibilityOff, null)
            }
        },
        visualTransformation = psdVisibleTrans,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = if (isDone) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = keyboardActions
    )
}

@Composable
fun SubmitButton(text: String, onClick: () -> Unit) {
    CenterRow(Modifier.padding(vertical = 32.dp)) {
        OutlinedButton(onClick = { onClick() }) {
            Text(text = text, style = MaterialTheme.typography.overline)
        }
    }
}