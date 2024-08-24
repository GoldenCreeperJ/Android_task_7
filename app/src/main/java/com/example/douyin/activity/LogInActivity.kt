package com.example.douyin.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.douyin.DyApplication.Companion.myId
import com.example.douyin.DyApplication.Companion.retrofit
import com.example.douyin.dyRetrofit.easyEnqueue
import com.example.douyin.dyRetrofit.easyEnqueueForUser
import com.example.douyin.ui.theme.DouYinTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class LogInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DouYinTheme {
                val pageType = remember { mutableStateOf(true) }
                val email = remember { mutableStateOf("") }
                val password = remember { mutableStateOf("") }
                val showPassword = remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                Scaffold(topBar = {
                    TopAppBar(title = {}, navigationIcon = {
                        IconButton(onClick = {
                            startActivity(Intent(this, MainActivity::class.java))
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null
                            )
                        }
                    })
                }) {
                    Column(
                        Modifier
                            .padding(it)
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = if (pageType.value) "登录发现更多精彩" else "注册您的新账号",
                            fontSize = 40.sp
                        )
                        Text(
                            text = "请遵守抖音用户协议和隐私政策以及运营商服务协议，运营商户将对拟提供的信息进行验证",
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = email.value,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Email") },
                            singleLine = true,
                            onValueChange = { email.value = it })
                        OutlinedTextField(
                            value = password.value,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Password") },
                            singleLine = true,
                            onValueChange = { password.value = it },
                            visualTransformation = if (showPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            isError = password.value.length in 1..4,
                            supportingText = { if (password.value.length in 1..4) Text(text = "密码不少于5位") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    showPassword.value = !showPassword.value
                                }) {
                                    Icon(
                                        if (showPassword.value) Icons.Default.Lock else Icons.Outlined.Lock,
                                        contentDescription = null
                                    )
                                }
                            },
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (pageType.value) {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        retrofit.login(
                                            username = email.value,
                                            password = password.value
                                        )
                                            .easyEnqueueForUser { _, user ->
                                                getSharedPreferences(
                                                    "data",
                                                    MODE_PRIVATE
                                                ).apply {
                                                    edit().putString(
                                                        "id",
                                                        user.socialObject.id
                                                    ).apply()
                                                }
                                                myId = user.socialObject.id
                                                startActivity(
                                                    Intent(
                                                        this@LogInActivity,
                                                        MainActivity::class.java
                                                    )
                                                )
                                            }
                                    }
                                } else {
                                    coroutineScope.launch {
                                        retrofit.register(
                                            username = email.value,
                                            password = password.value
                                        )
                                            .easyEnqueue {
                                                pageType.value = true
                                                null
                                            }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = password.value.length > 4 && email.value.isNotEmpty()
                        ) {
                            Text(
                                text = if (pageType.value) "登录" else "注册",
                                fontSize = 20.sp
                            )
                        }
                        Text(
                            text = if (pageType.value) "点击注册新账号" else "点击登录已有账号",
                            fontSize = 10.sp,
                            modifier = Modifier.clickable {
                                pageType.value = !pageType.value
                                email.value = ""
                                password.value = ""
                                showPassword.value = false
                            }
                        )
                    }
                }
            }
        }
    }
}
