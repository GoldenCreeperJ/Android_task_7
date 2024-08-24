package com.example.douyin.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.example.douyin.AvatarImage
import com.example.douyin.DyApplication.Companion.retrofit
import com.example.douyin.dyRetrofit.SocialObject
import com.example.douyin.dyRetrofit.TimeStamp
import com.example.douyin.dyRetrofit.User
import com.example.douyin.dyRetrofit.Video
import com.example.douyin.dyRetrofit.easyEnqueueForSocialObjects
import com.example.douyin.dyRetrofit.easyEnqueueForUser
import com.example.douyin.dyRetrofit.easyEnqueueForVideos
import com.example.douyin.ui.theme.DouYinTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DouYinTheme {
                val horizontalPagerState = rememberPagerState { 0 }
                val tabIndex = remember { mutableIntStateOf(0) }
                val coroutineScope = rememberCoroutineScope()
                val userInfo = remember { mutableStateOf(User(SocialObject("","",""), TimeStamp("","",""))) }
                val videoList = remember { mutableListOf<Video>() }
                val likeList = remember { mutableListOf<Video>() }
                val followerList = remember { mutableListOf<SocialObject>() }
                val followingList = remember { mutableListOf<SocialObject>() }

                LaunchedEffect(key1 = true){
                    userInfo.value= retrofit.getInfo(intent.getStringExtra("userId")).easyEnqueueForUser()
                    videoList.addAll(
                        retrofit.getVideoListById(userInfo.value.socialObject.id, 0, 999)
                            .easyEnqueueForVideos()
                    )
                    likeList.addAll(
                        retrofit.getLikeList(userInfo.value.socialObject.id)
                            .easyEnqueueForVideos()
                    )
                    followerList.addAll(
                        retrofit.getFollowerList(userInfo.value.socialObject.id)
                            .easyEnqueueForSocialObjects()
                    )
                    followingList.addAll(
                        retrofit.getFollowingList(userInfo.value.socialObject.id)
                            .easyEnqueueForSocialObjects()
                    )
                }

                Column {
                    Box(
                        modifier = Modifier
                            .background(Color.Blue)
                            .fillMaxHeight(0.4f)
                    ) {
                        Row {
                            AvatarImage(url = userInfo.value.socialObject.avatarUrl)
                            Column {
                                Text(text = userInfo.value.socialObject.userName)
                                Text(text = userInfo.value.socialObject.id)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxHeight(0.1f)
                    ) {
                        Text(text = "关注\n${followingList.size}")
                        Text(text = "粉丝\n${followerList.size}")
                        Button(onClick = { /*TODO*/ }) {
                            Text(text = "关注")
                        }
                    }
                    TabRow(selectedTabIndex = tabIndex.intValue) {
                        Tab(
                            selected = tabIndex.intValue == 0,
                            modifier = Modifier.fillMaxWidth(0.5f),
                            onClick = {
                                tabIndex.intValue = 0
                                coroutineScope.launch { horizontalPagerState.scrollToPage(0) }
                            }) {
                            Text(text = "作品")
                        }
                        Tab(selected = tabIndex.intValue == 1, onClick = {
                            tabIndex.intValue = 1
                            coroutineScope.launch { horizontalPagerState.scrollToPage(1) }
                        }) {
                            Text(text = "点赞")
                        }
                    }
                    HorizontalPager(state = rememberPagerState { 2 }) {
                        LazyVerticalGrid(columns = GridCells.Fixed(3)) {
                            if (tabIndex.intValue == 0) {
                                items(videoList.size) {
                                    AsyncImage(
                                        model = videoList[it].coverUrl,
                                        contentDescription = null
                                    )
                                }
                            } else {
                                items(likeList.size) {
                                    AsyncImage(model = likeList[it].coverUrl, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}