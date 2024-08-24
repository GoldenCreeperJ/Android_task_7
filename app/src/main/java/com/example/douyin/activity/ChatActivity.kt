package com.example.douyin.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.douyin.AvatarImage
import com.example.douyin.ui.theme.DouYinTheme

@OptIn(ExperimentalMaterial3Api::class)
class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DouYinTheme {
                val mes = remember { mutableStateOf("") }
                Scaffold(topBar = {
                    TopAppBar(title = {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AvatarImage(url = "")
                            Text(text = "")
                        }
                    })
                }) {
                    ConstraintLayout(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(it)
                    ) {
                        val (chatList, textField) = createRefs()
                        LazyColumn(reverseLayout = true, modifier = Modifier.constrainAs(chatList) {
                            height = Dimension.fillToConstraints
                            bottom.linkTo(textField.top)
                            top.linkTo(parent.top)
                        }) {
                            items(1) {
                                Text(text = "")
                            }
                        }
                        TextField(
                            value = mes.value,
                            singleLine = true,
                            onValueChange = { mes.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .constrainAs(textField) {
                                    bottom.linkTo(parent.bottom)
                                },
                            placeholder = {
                                Text(
                                    text = "mes"
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { /*TODO*/ }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}