package com.example.douyin.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.net.toFile
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import coil.request.videoFrameMicros
import com.example.douyin.AvatarImage
import com.example.douyin.DyApplication.Companion.myId
import com.example.douyin.DyApplication.Companion.retrofit
import com.example.douyin.IconWithLabel
import com.example.douyin.dyRetrofit.Comment
import com.example.douyin.dyRetrofit.SocialObject
import com.example.douyin.dyRetrofit.TimeStamp
import com.example.douyin.dyRetrofit.User
import com.example.douyin.dyRetrofit.Video
import com.example.douyin.dyRetrofit.easyEnqueue
import com.example.douyin.dyRetrofit.easyEnqueueForComments
import com.example.douyin.dyRetrofit.easyEnqueueForSocialObjects
import com.example.douyin.dyRetrofit.easyEnqueueForUser
import com.example.douyin.dyRetrofit.easyEnqueueForVideos
import com.example.douyin.getFilePathFromContentUri
import com.example.douyin.permissionRequesterPlus
import com.example.douyin.ui.theme.DouYinTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private lateinit var videoUri: Uri
    private lateinit var videoLauncher: ActivityResultLauncher<Intent>
    private lateinit var imageLauncher: ActivityResultLauncher<Intent>

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        videoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
                    videoUri = result.data!!.data!!
                }
            }
        imageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        retrofit.uploadAvatar(result.data!!.data!!.toFile())
                    }
                }
            }

        super.onCreate(savedInstanceState)
        setContent {
            DouYinTheme {
                val selectedPage = remember { mutableIntStateOf(0) }

                Scaffold(bottomBar = {
                    DyNavigationBar(selectedPage)
                }) {
                    Column {
                        when (selectedPage.intValue) {
                            0 -> MainPage(it)
                            1 -> FriendPage(it)
                            2 -> UploadPage(it)
                            3 -> MessagePage(it)
                            4 -> HomePage(it)
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainPage(paddingValues: PaddingValues, videos: List<Video>? = null) {
        val videoList = remember { mutableListOf<Video>().apply { videos?.let { addAll(it) } } }
        val refreshState = rememberPullToRefreshState()
        val horizontalPagerState = rememberPagerState { 2 }
        val tabIndex = remember { mutableIntStateOf(0) }
        val coroutineScope = rememberCoroutineScope()
        val lazyListState = rememberLazyListState()

        if (refreshState.isRefreshing) {
            LaunchedEffect(true) {
                videoList.clear()
                videoList.addAll(retrofit.getVideo().easyEnqueueForVideos())
                refreshState.endRefresh()
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
                Text(text = "推荐")
            }
            Tab(
                selected = tabIndex.intValue == 1,
                modifier = Modifier.fillMaxWidth(0.5f),
                onClick = {
                    tabIndex.intValue = 1
                    coroutineScope.launch { horizontalPagerState.scrollToPage(1) }
                }) {
                Text(text = "其他")
            }
        }
        HorizontalPager(
            state = horizontalPagerState,
            modifier = Modifier
                .nestedScroll(refreshState.nestedScrollConnection)
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    state = lazyListState,
                    flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)
                ) {
                    items(videoList.size) {
                        ConstraintLayout {
                            val openCommentSheet = remember { mutableStateOf(false) }
                            val commentSheetState =
                                rememberModalBottomSheetState(skipPartiallyExpanded = true)
                            val openDescriptionSheet = remember { mutableStateOf(false) }
                            val descriptionSheetState =
                                rememberModalBottomSheetState(skipPartiallyExpanded = true)
                            val commentField = remember { mutableStateOf("") }
                            val commentList = remember { mutableListOf<Comment>() }
                            val commentTarget = remember { mutableStateOf(videoList[it].id) }
                            val videoOwner = remember {
                                mutableStateOf(
                                    User(
                                        SocialObject("", "", ""),
                                        TimeStamp("", "", "")
                                    )
                                )
                            }
                            val videoView = VideoView(this@MainActivity).apply {
                                setMediaController(MediaController(this@MainActivity).apply { hide() })
                                setOnCompletionListener { this.resume() }
                            }

                            LaunchedEffect(it) {
                                videoOwner.value = retrofit.getInfo(videoList[it].userId)
                                    .easyEnqueueForUser()
                            }

                            val (video, titleText, buttonList, slider) = createRefs()
                            AndroidView(modifier = Modifier
                                .clickable {
                                    if (videoView.isPlaying) videoView.suspend() else videoView.start()
                                }
                                .constrainAs(video) {
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }, factory = { _ ->
                                videoView.apply {
                                    this.setVideoPath(videoList[it].videoUrl)
                                    this.start()
                                }
                            })
                            Text(modifier = Modifier
                                .clickable {
                                    openDescriptionSheet.value = true
                                }
                                .constrainAs(titleText) {
                                    start.linkTo(parent.start)
                                    bottom.linkTo(slider.top)
                                    end.linkTo(buttonList.start)
                                }
                                .basicMarquee(), maxLines = 1, text = videoList[it].title)
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .constrainAs(slider) {
                                        start.linkTo(parent.start)
                                        bottom.linkTo(parent.bottom)
                                        end.linkTo(parent.start)
                                    },
                                progress = { videoView.currentPosition / videoView.duration.toFloat() })
                            Column(modifier = Modifier.constrainAs(buttonList) {
                                end.linkTo(parent.end)
                                bottom.linkTo(slider.top)
                            }) {
                                AvatarImage(
                                    url = videoOwner.value.socialObject.avatarUrl,
                                    modifier = Modifier.clickable {
                                        startActivity(
                                            Intent(
                                                this@MainActivity,
                                                ProfileActivity::class.java
                                            ).apply {
                                                startActivity(
                                                    Intent(
                                                        this@MainActivity,
                                                        ProfileActivity::class.java
                                                    ).apply {
                                                        putExtra(
                                                            "userId",
                                                            videoOwner.value.socialObject.id
                                                        )
                                                    })
                                            })
                                    })
                                IconWithLabel(
                                    text = videoList[it].likeCount.toString(),
                                    icon = Icons.Default.Favorite
                                ) {
                                    coroutineScope.launch {
                                        retrofit.likeAction(videoList[it].id, actionType = "1")
                                        videoList[it].likeCount += 1
                                    }
                                }
                                IconWithLabel(
                                    text = commentList.size.toString(),
                                    icon = Icons.Default.Menu
                                ) {
                                    coroutineScope.launch {
                                        openCommentSheet.value = true
                                        commentList.clear()
                                        commentList.addAll(
                                            retrofit.getCommentList(videoList[it].id)
                                                .easyEnqueueForComments()
                                        )
                                    }
                                }
                            }
                            if (openCommentSheet.value) {
                                ModalBottomSheet(
                                    modifier = Modifier.fillMaxHeight(0.8f),
                                    onDismissRequest = {
                                        openCommentSheet.value = false
                                    },
                                    sheetState = commentSheetState,
                                    dragHandle = {
                                        Text(
                                            text = "共" + commentList.size.toString() + "个评论",
                                            fontSize = 20.sp
                                        )
                                    }
                                ) {
                                    Spacer(modifier = Modifier.fillMaxHeight(0.1f))
                                    ConstraintLayout {
                                        val (comment, textField) = createRefs()
                                        LazyColumn(modifier = Modifier.constrainAs(comment) {
                                            bottom.linkTo(textField.top)
                                        }) {
                                            items(commentList.size) {
                                                CommentContainer(commentList[it], commentTarget)
                                            }
                                        }
                                        TextField(
                                            value = commentField.value,
                                            singleLine = true,
                                            onValueChange = { commentField.value = it },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .constrainAs(textField) {
                                                    bottom.linkTo(parent.bottom)
                                                },
                                            placeholder = {
                                                Text(
                                                    text = "comment"
                                                )
                                            },
                                            trailingIcon = {
                                                IconButton(onClick = {
                                                    if (myId != "-1") {
                                                        coroutineScope.launch {
                                                            retrofit.publishComment(
                                                                commentTarget.value,
                                                                content = commentField.value
                                                            ).easyEnqueue()
                                                            commentTarget.value = commentList[it].id
                                                        }
                                                    } else {
                                                        startActivity(
                                                            Intent(
                                                                this@MainActivity,
                                                                LogInActivity::class.java
                                                            ).apply {
                                                            })
                                                    }
                                                }) {
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
                            if (openDescriptionSheet.value) {
                                ModalBottomSheet(
                                    modifier = Modifier.fillMaxHeight(0.4f),
                                    onDismissRequest = {
                                        openDescriptionSheet.value = false
                                    },
                                    sheetState = descriptionSheetState,
                                ) {
                                    Spacer(modifier = Modifier.fillMaxHeight(0.1f))
                                    Column {
                                        Text(text = videoList[it].title)
                                        Text(text = videoList[it].timeStamp.createdAt)
                                        Row {
                                            AvatarImage(url = videoOwner.value.socialObject.avatarUrl,
                                                modifier = Modifier
                                                    .clickable {
                                                        startActivity(
                                                            Intent(
                                                                this@MainActivity,
                                                                ProfileActivity::class.java
                                                            ).apply {
                                                                putExtra(
                                                                    "userId",
                                                                    videoOwner.value.socialObject.id
                                                                )
                                                            })
                                                    }
                                            )
                                            Text(text = videoOwner.value.socialObject.userName)
                                            Button(onClick = {
                                                if (myId != "-1") {
                                                    coroutineScope.launch {
                                                        retrofit.relationAction(
                                                            videoOwner.value.socialObject.id,
                                                            1
                                                        ).easyEnqueue()
                                                    }
                                                } else
                                                    startActivity(
                                                        Intent(
                                                            this@MainActivity,
                                                            LogInActivity::class.java
                                                        ).apply {
                                                        })
                                            }) {
                                                Text(text = "关注")
                                            }
                                        }
                                        Text(text = videoList[it].description)
                                    }
                                }
                            }
                        }
                    }
                }
                PullToRefreshContainer(
                    state = refreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-30).dp)
                )
            }
        }
    }

    @Composable
    fun FriendPage(paddingValues: PaddingValues) {
        Text(text = "相应接口不能提供本人好友")
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(1) {
                ListItem(headlineContent = { Text(text = "") }, leadingContent = {
                    AvatarImage(
                        url = "",
                    )
                }, modifier = Modifier.clickable {
                    startActivity(Intent(this@MainActivity, ProfileActivity::class.java).apply {

                    })
                })
            }
        }
    }

    @Composable
    @SuppressLint("CheckResult", "InlinedApi")
    fun UploadPage(paddingValues: PaddingValues) {
        val title = remember { mutableStateOf("") }
        val description = remember { mutableStateOf("") }
        val module = remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope()
        val bitmap = Bitmap.createBitmap(999, 999, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawColor(android.graphics.Color.GRAY)

        Scaffold(modifier = Modifier.padding(paddingValues = paddingValues), topBar = {
            TopAppBar(title = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(text = "upload") }
            })
        }) {
            Column {
                Row(
                    modifier = Modifier
                        .padding(it)
                        .padding(horizontal = 10.dp)
                        .fillMaxHeight(0.5f)
                ) {
                    Column {
                        OutlinedTextField(
                            value = title.value,
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .fillMaxHeight(0.2f),
                            label = { Text("Title") },
                            onValueChange = { title.value = it })
                        OutlinedTextField(
                            value = description.value,
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .fillMaxHeight(),
                            label = { Text("Description") },
                            onValueChange = { description.value = it })
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    SubcomposeAsyncImage(model = ImageRequest.Builder(this@MainActivity)
                        .data(module.value).apply {
                        videoFrameMicros(0)
                    }.build(), contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                permissionRequesterPlus(
                                    this@MainActivity,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_MEDIA_VIDEO,
                                    Build.VERSION_CODES.TIRAMISU
                                ) {
                                    videoLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply {
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        type = "video/*"
                                    })
                                }
                            }) {
                        if (painter.state is AsyncImagePainter.State.Loading || painter.state is AsyncImagePainter.State.Error) {
                            Text(text = "click to select video")
                        } else {
                            SubcomposeAsyncImageContent()
                        }
                    }
//                    AsyncImage(
//                        model = module.value, contentDescription = null,
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .clickable {
//                                permissionRequesterPlus(
//                                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                                    Manifest.permission.READ_MEDIA_VIDEO,
//                                    Build.VERSION_CODES.TIRAMISU
//                                ) {
//                                    videoLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply {
//                                        addCategory(Intent.CATEGORY_OPENABLE)
//                                        type = "video/*"
//                                    })
//                                }
//                            },
//                    )
//                    GlideImage(
//                        loading = placeholder(drawable = bitmap.toDrawable(Resources.getSystem())),
//                        failure = placeholder(drawable = bitmap.toDrawable(Resources.getSystem())),
//                        model = module.value,
//                        contentDescription = null,
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .clickable {
//                                permissionRequesterPlus(
//                                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                                    Manifest.permission.READ_MEDIA_VIDEO,
//                                    Build.VERSION_CODES.TIRAMISU
//                                ) {
//                                    videoLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply {
//                                        addCategory(Intent.CATEGORY_OPENABLE)
//                                        type = "video/*"
//                                    })
//                                }
//                            },
//                        requestBuilderTransform = {
//                            it.apply(RequestOptions.frameOf(0).apply {
//                                set(FRAME_OPTION, MediaMetadataRetriever.OPTION_CLOSEST)
//                                transform(object : BitmapTransformation() {
//                                    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
//                                        try {
//                                            messageDigest.update((context.packageName + "RotateTransform").toByte())
//                                        } catch (e: Exception) {
//                                            e.printStackTrace()
//                                        }
//                                    }
//
//                                    override fun transform(
//                                        pool: BitmapPool,
//                                        toTransform: Bitmap,
//                                        outWidth: Int,
//                                        outHeight: Int
//                                    ): Bitmap {
//                                        return toTransform
//                                    }
//                                })
//                            })
//                        }
//                    )
                }

                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    if (this@MainActivity::videoUri.isInitialized) {
                        coroutineScope.launch {
                            retrofit.publishVideo(
                                File(getFilePathFromContentUri(this@MainActivity, videoUri)!!),
                                title.value
                            )
                        }
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "请先选择视频",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {
                    Text(text = "upload")
                }
            }
        }
    }

    @Composable
    fun MessagePage(paddingValues: PaddingValues) {
        Text(text = "后端没有提供相应接口")
        LazyColumn(Modifier.padding(paddingValues)) {
            items(1) {
                ListItem(headlineContent = {
                    AvatarImage(url = "")
                },
                    overlineContent = { Text(text = "") },
                    supportingContent = { Text(text = "") },
                    leadingContent = { Text(text = "") },
                    modifier = Modifier.clickable { })
            }
        }
    }

    @Composable
    fun HomePage(paddingValues: PaddingValues) {
        val horizontalPagerState = rememberPagerState { 0 }
        val tabIndex = remember { mutableIntStateOf(0) }
        val coroutineScope = rememberCoroutineScope()
        val userInfo =
            produceState(initialValue = User(SocialObject("", "", ""), TimeStamp("", "", ""))) {
                value = retrofit.getInfo(myId)
                    .easyEnqueueForUser()
            }
        val videoList = produceState(initialValue = listOf<Video>()) {
            value = retrofit.getVideoListById(userInfo.value.socialObject.id, 0, 999)
                .easyEnqueueForVideos()
        }
        val likeList = produceState(initialValue = listOf<Video>()) {
            value = retrofit.getLikeList(userInfo.value.socialObject.id).easyEnqueueForVideos()
        }
        val followerList = produceState(initialValue = listOf<SocialObject>()) {
            value = retrofit.getFollowerList(userInfo.value.socialObject.id)
                .easyEnqueueForSocialObjects()
        }
        val followingList = produceState(initialValue = listOf<SocialObject>()) {
            value = retrofit.getFollowingList(userInfo.value.socialObject.id)
                .easyEnqueueForSocialObjects()
        }

        ModalNavigationDrawer(modifier = Modifier.padding(paddingValues), drawerContent = {
            ModalDrawerSheet {
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    NavigationDrawerItem(
                        label = { Text(text = "高级设置", fontSize = 20.sp) },
                        selected = false,
                        onClick = {
                            Toast.makeText(
                                this@MainActivity,
                                "there is nothing",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    NavigationDrawerItem(
                        label = { Text(text = "退出登录", fontSize = 20.sp) },
                        selected = false,
                        onClick = {
                            getSharedPreferences("data", Context.MODE_PRIVATE).apply {
                                edit().putString("id", "-1").apply()
                            }
                            myId = "-1"
                            startActivity(Intent(this@MainActivity, LogInActivity::class.java))
                        })
                }
            }
        }) {
            Column {
                Box(
                    modifier = Modifier
                        .background(Color.hsv(240f, 0.5f, 0.5f))
                        .fillMaxHeight(0.4f)
                        .fillMaxWidth()
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        AvatarImage(url = userInfo.value.socialObject.avatarUrl)
                        Column(verticalArrangement = Arrangement.SpaceEvenly) {
                            Text(text = userInfo.value.socialObject.userName)
                            Text(text = userInfo.value.socialObject.id)
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxHeight(0.15f)
                        .fillMaxWidth()
                ) {
                    Text(text = "关注\n${followingList.value.size}")
                    Text(text = "粉丝\n${followerList.value.size}")
                    Button(onClick = {
                        imageLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "image/*"
                        })
                    }) {
                        Text(text = "更换头像")
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
                        Text(text = "我的")
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
                            items(videoList.value.size) {
                                AsyncImage(
                                    model = videoList.value[it].coverUrl,
                                    contentDescription = null
                                )
                            }
                        } else {
                            items(likeList.value.size) {
                                AsyncImage(
                                    model = likeList.value[it].coverUrl,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun DyNavigationBar(selectedPage: MutableState<Int>) {
        NavigationBar {
            NavigationBarItem(
                selected = selectedPage.value == 0,
                onClick = {
                    if (myId != "-1")
                        selectedPage.value = 0
                    else {
                        startActivity(Intent(this@MainActivity, LogInActivity::class.java).apply {
                        })
                    }
                },
                icon = {},
                label = { Text(text = "首页", fontSize = 20.sp) }
            )
            NavigationBarItem(
                selected = selectedPage.value == 1,
                onClick = {
                    if (myId != "-1")
                        selectedPage.value = 1
                    else {
                        startActivity(Intent(this@MainActivity, LogInActivity::class.java).apply {
                        })
                    }
                },
                icon = {},
                label = { Text(text = "朋友", fontSize = 20.sp) }
            )
            NavigationBarItem(
                selected = selectedPage.value == 2,
                onClick = {
                    if (myId != "-1")
                        selectedPage.value = 2
                    else {
                        startActivity(Intent(this@MainActivity, LogInActivity::class.java).apply {
                        })
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.drawBehind {
                            drawContext.canvas.apply {
                                drawRoundRect(-55f,
                                    -15f,
                                    drawContext.size.width + 45f,
                                    drawContext.size.height + 15f,
                                    10f,
                                    10f,
                                    Paint().apply {
                                        color = Color.Red
                                        style = PaintingStyle.Stroke
                                        strokeWidth = 3f
                                    }
                                )
                                drawRoundRect(-45f,
                                    -15f,
                                    drawContext.size.width + 55f,
                                    drawContext.size.height + 15f,
                                    10f,
                                    10f,
                                    Paint().apply {
                                        color = Color.Blue
                                        style = PaintingStyle.Stroke
                                        strokeWidth = 3f
                                    }
                                )
                                drawRoundRect(-50f,
                                    -15f,
                                    drawContext.size.width + 50f,
                                    drawContext.size.height + 15f,
                                    10f,
                                    10f,
                                    Paint().apply {
                                        color = Color.Black
                                        style = PaintingStyle.Stroke
                                        strokeWidth = 5f
                                    }
                                )
                            }
                        }
                    )
                },
            )
            NavigationBarItem(
                selected = selectedPage.value == 3,
                onClick = {
                    if (myId != "-1")
                        selectedPage.value = 3
                    else {
                        startActivity(Intent(this@MainActivity, LogInActivity::class.java).apply {
                        })
                    }
                },
                icon = {},
                label = { Text(text = "消息", fontSize = 20.sp) }
            )
            NavigationBarItem(
                selected = selectedPage.value == 4,
                onClick = {
                    if (myId != "-1")
                        selectedPage.value = 4
                    else {
                        startActivity(Intent(this@MainActivity, LogInActivity::class.java).apply {
                        })
                    }
                },
                icon = {},
                label = { Text(text = "我", fontSize = 20.sp) }
            )
        }
    }

    @Composable
    fun CommentContainer(comment: Comment, commentTarget: MutableState<String>) {
        val hasChildComment = remember { mutableStateOf(false) }
        val openChildComment = remember { mutableStateOf(false) }
        val childComment = remember { mutableListOf<Comment>() }
        val coroutineScope = rememberCoroutineScope()
        val commentOwner = remember {
            mutableStateOf(
                User(
                    SocialObject("", "", ""),
                    TimeStamp("", "", "")
                )
            )
        }

        LaunchedEffect(key1 = true) {
            childComment.addAll(
                retrofit.getCommentList(commentId = comment.id)
                    .easyEnqueueForComments()
            )
            commentOwner.value = retrofit.getInfo(comment.userId).easyEnqueueForUser()
        }

        ListItem(
            headlineContent = { Text(text = comment.content) },
            overlineContent = { Text(text = commentOwner.value.socialObject.userName) },
            supportingContent = {
                Column {
                    Row {
                        Text(text = comment.timeStamp.createdAt)
                        Text(text = "回复", Modifier.clickable {
                            commentTarget.value = comment.id
                        })
                    }
                    if (hasChildComment.value) {
                        if (openChildComment.value) {
                            LazyColumn {
                                items(childComment.size) {
                                    CommentContainer(
                                        comment = childComment[it],
                                        commentTarget = commentTarget
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (openChildComment.value) "---收起---" else "---展开" + childComment.size + "条评论---",
                            Modifier.clickable { openChildComment.value = !openChildComment.value })
                    }
                }
            },
            leadingContent = {
                AvatarImage(
                    url = commentOwner.value.socialObject.avatarUrl,
                    modifier = Modifier.clickable {
                        startActivity(Intent(this@MainActivity, ProfileActivity::class.java).apply {
                            putExtra("userId", commentOwner.value.socialObject.id)
                        })
                    })
            },
            trailingContent = {
                IconWithLabel(Icons.Default.ThumbUp, comment.likeCount.toString()) {
                    coroutineScope.launch {
                        retrofit.likeAction(
                            commentId = comment.id,
                            actionType = "1"
                        )
                    }
                }
            }
        )
    }

    @Preview
    @Composable
    fun Test() {
    }
}