package com.example.douyin.dyRetrofit

import android.widget.Toast
import com.example.douyin.DyApplication
import com.example.douyin.internetCheck
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun Call<ResponseDates>.fastEnqueue(func1:(response: Response<ResponseDates>)->Any?={}, func2:(t: Throwable)->Any?={}) {
    if (internetCheck()) {
        this.enqueue(object : Callback<ResponseDates> {
            override fun onResponse(call: Call<ResponseDates>, response: Response<ResponseDates>) {
                func1(response)
            }

            override fun onFailure(call: Call<ResponseDates>, t: Throwable) {
                func2(t)
            }
        })
    }
}

fun Call<ResponseDates>.easyEnqueue(func:(response: Response<ResponseDates>)->Any?={}) {
    if (internetCheck()) {
        this.enqueue(object : Callback<ResponseDates> {
            override fun onResponse(call: Call<ResponseDates>, response: Response<ResponseDates>) {
                if (response.body() != null && response.body()!!.base.isNotEmpty() && response.body()!!.base["code"] != -1)
                    func(response)
                else Toast.makeText(DyApplication.context, "数据存在错误", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<ResponseDates>, t: Throwable) {
                Toast.makeText(DyApplication.context, "网络连接失败", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

fun Call<ResponseDates>.easyEnqueueForUser(func:(response: Response<ResponseDates>, user: User)->Any?={ _, _->}): User {
    var result = User(SocialObject("", "", ""), TimeStamp("", "", ""))
    if (internetCheck()) {
        this.enqueue(object : Callback<ResponseDates> {
            override fun onResponse(call: Call<ResponseDates>, response: Response<ResponseDates>) {
                if (response.body() != null && response.body()!!.base.isNotEmpty() && response.body()!!.base["code"] != -1){
                    val u = (response.body()!!.data as Map<String, String>)
                    val user = User(
                        SocialObject(
                            u["id"]!!,
                            u["username"]!!,
                            u["avatar_url"]!!,
                        ),
                        TimeStamp(
                            u["created_at"]!!,
                            u["updated_at"]!!,
                            u["deleted_at"]!!
                        )
                    )
                    func(response, user)
                    result = user
                }
                else Toast.makeText(DyApplication.context, "数据存在错误", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<ResponseDates>, t: Throwable) {
                Toast.makeText(DyApplication.context, "网络连接失败", Toast.LENGTH_SHORT).show()
            }
        })
    }
    return result
}

fun Call<ResponseDates>.easyEnqueueForVideos(func:(response: Response<ResponseDates>)->Any?={}):List<Video> {
    var result = listOf<Video>()
    if (internetCheck()) {
        this.enqueue(object : Callback<ResponseDates> {
            override fun onResponse(call: Call<ResponseDates>, response: Response<ResponseDates>) {
                if (response.body() != null && response.body()!!.base.isNotEmpty() && response.body()!!.base["code"] != -1){
                    func(response)
                    result = (((response.body()!!.data as Map<String, *>)["items"] as ArrayList<Map<String, Any>>).map { i ->
                        Video(
                            i["id"] as String,
                            i["user_id"] as String,
                            i["video_url"] as String,
                            i["cover_url"] as String,
                            i["title"] as String,
                            i["description"] as String,
                            (i["visit_count"] as Double).toInt(),
                            (i["like_count"] as Double).toInt(),
                            (i["comment_count"] as Double).toInt(),
                            TimeStamp(
                                i["created_at"] as String,
                                i["updated_at"] as String,
                                i["deleted_at"] as String
                            )
                        )
                    })
                }
                else Toast.makeText(DyApplication.context, "数据存在错误", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<ResponseDates>, t: Throwable) {
                Toast.makeText(DyApplication.context, "网络连接失败", Toast.LENGTH_SHORT).show()
            }
        })
    }
    return result
}

fun Call<ResponseDates>.easyEnqueueForComments(func:(response: Response<ResponseDates>)->Any?={}):List<Comment> {
    var result = listOf<Comment>()
    if (internetCheck()) {
        this.enqueue(object : Callback<ResponseDates> {
            override fun onResponse(call: Call<ResponseDates>, response: Response<ResponseDates>) {
                if (response.body() != null && response.body()!!.base.isNotEmpty() && response.body()!!.base["code"] != -1){
                    func(response)
                    result = ((response.body()!!.data as Map<String, *>)["items"] as ArrayList<Map<String, Any>>).map {i->
                        Comment(
                            i["id"] as String,
                            i["user_id"] as String,
                            i["video_id"] as String,
                            i["parent_id"] as String,
                            (i["like_count"] as Double).toInt(),
                            (i["child_count"] as Double).toInt(),
                            i["content"] as String,
                            TimeStamp(
                                i["created_at"] as String,
                                i["updated_at"] as String,
                                i["deleted_at"] as String
                            )
                        )
                    }
                }
                else Toast.makeText(DyApplication.context, "数据存在错误", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<ResponseDates>, t: Throwable) {
                Toast.makeText(DyApplication.context, "网络连接失败", Toast.LENGTH_SHORT).show()
            }
        })
    }
    return result
}

fun Call<ResponseDates>.easyEnqueueForSocialObjects(func:(response: Response<ResponseDates>)->Any?={}):List<SocialObject> {
    var result = listOf<SocialObject>()
    if (internetCheck()) {
        this.enqueue(object : Callback<ResponseDates> {
            override fun onResponse(call: Call<ResponseDates>, response: Response<ResponseDates>) {
                if (response.body() != null && response.body()!!.base.isNotEmpty() && response.body()!!.base["code"] != -1){
                    func(response)
                    result = ((response.body()!!.data as Map<String, *>)["items"] as ArrayList<Map<String, String>>).map {i->
                        SocialObject(
                            i["id"] as String,
                            i["username"] as String,
                            i["avatar_url"] as String,
                        )
                    }
                }
                else Toast.makeText(DyApplication.context, "数据存在错误", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<ResponseDates>, t: Throwable) {
                Toast.makeText(DyApplication.context, "网络连接失败", Toast.LENGTH_SHORT).show()
            }
        })
    }
    return result
}
