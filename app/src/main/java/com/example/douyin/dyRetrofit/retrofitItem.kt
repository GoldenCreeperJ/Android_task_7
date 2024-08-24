package com.example.douyin.dyRetrofit

data class ResponseDates(val base:HashMap<String,Any>, val data:Any)

data class TimeStamp(val createdAt:String, val updateAt:String, val deleteAt:String)

data class SocialObject(val id :String, val userName:String, val avatarUrl:String)

data class User(val socialObject: SocialObject, val timeStamp: TimeStamp)

data class Video(val id: String, val userId:String, val videoUrl:String, val coverUrl:String, val title:String, val description:String, val visitCount:Int,
                 var likeCount:Int, val commentCount:Int, val timeStamp: TimeStamp
)

data class Comment(val id: String,val userId:String,val videoId:String,val parentId:String,val likeCount: Int,val childCount:Int,val content:String,val timeStamp: TimeStamp)
