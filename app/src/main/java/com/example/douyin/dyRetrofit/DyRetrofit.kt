package com.example.douyin.dyRetrofit

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import java.io.File

interface DyRetrofit {
    @FormUrlEncoded
    @POST("m1/4782108-0-default/user/register")
    fun register(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<ResponseDates>

    @FormUrlEncoded
    @POST("m1/4782108-0-default/user/login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("code") code: String? = null
    ): Call<ResponseDates>

    @FormUrlEncoded
    @POST("m1/4782108-0-default/user/image/search")
    fun searchByImage(@Field("data") data: File? = null): Call<ResponseDates>

    @GET("m1/4782108-0-default/user/info")
    fun getInfo(@Query("user_id") userId: String? = null): Call<ResponseDates>

    @PUT("m1/4782108-0-default/user/avatar/upload")
    fun uploadAvatar(@Field("data") data: File? = null): Call<ResponseDates>

    @GET("m1/4782108-0-default/user/auth/mfa/qrcode")
    fun getMfa(): Call<ResponseDates>

    @FormUrlEncoded
    @POST("m1/4782108-0-default/user/auth/mfa/bind")
    fun bindMfa(
        @Field("code") code: String? = null,
        @Field("secret") secret: String? = null
    ): Call<ResponseDates>

    @GET("m1/4782108-0-default/video/feed/")
    fun getVideo(@Query("latest_time") latestTime: String? = null): Call<ResponseDates>

    @POST("m1/4782108-0-default/video/publish")
    fun publishVideo(
        @Field("data") data: File? = null,
        @Field("title") title: String? = null,
        @Field("description") description: String? = null
    ): Call<ResponseDates>

    @GET("m1/4782108-0-default/video/list")
    fun getVideoListById(
        @Query("user_id") userId: String,
        @Query("page_num") pageNum: Int,
        @Query("page_size") pageSize: Int
    ): Call<ResponseDates>

    @GET("m1/4782108-0-default/video/popular")
    fun getPopularVideoList(
        @Query("page_num") pageNum: Int? = null,
        @Query("page_size") pageSize: Int? = null
    ): Call<ResponseDates>

    @FormUrlEncoded
    @POST("m1/4782108-0-default/video/search")
    fun searchVideo(
        @Field("keywords") keywords: String,
        @Field("page_size") pageSize: Int,
        @Field("page_num") pageNum: Int,
        @Field("from_date") fromDate: Int? = null,
        @Field("to_date") toDate: Int? = null,
        @Field("username") username: String? = null
    ): Call<ResponseDates>

    @FormUrlEncoded
    @POST("m1/4782108-0-default/like/action")
    fun likeAction(
        @Field("video_id") videoId: String? = null,
        @Field("comment_id") commentId: String? = null,
        @Field("action_type") actionType: String? = null
    ): Call<ResponseDates>

    @GET("m1/4782108-0-default/like/list")
    fun getLikeList(
        @Query("user_id") userId: String? = null,
        @Query("page_size") pageSize: Int? = null,
        @Query("page_num") pageNum: Int? = null
    ): Call<ResponseDates>

    @FormUrlEncoded
    @POST("m1/4782108-0-default/comment/publish")
    fun publishComment(
        @Field("video_id") videoId: String? = null,
        @Field("comment_id") commentId: String? = null,
        @Field("content") content: String
    ): Call<ResponseDates>

    @FormUrlEncoded
    @DELETE("m1/4782108-0-default/comment/delete")
    fun deleteComment(
        @Field("video_id") videoId: String? = null,
        @Field("comment_id") commentId: String? = null
    ): Call<ResponseDates>

    @GET("m1/4782108-0-default/comment/list")
    fun getCommentList(
        @Query("video_id") videoId: String? = null,
        @Query("comment_id") commentId: String? = null,
        @Query("page_size") pageSize: Int? = null,
        @Query("page_num") pageNum: Int? = null
    ): Call<ResponseDates>

    @FormUrlEncoded
    @POST("m1/4782108-0-default/relation/action")
    fun relationAction(
        @Field("to_user_id") toUserId: String,
        @Field("action_type") actionType: Int
    ): Call<ResponseDates>

    @GET("m1/4782108-0-default/following/list")
    fun getFollowingList(
        @Query("user_id") userId: String,
        @Query("page_size") pageSize: Int? = null,
        @Query("page_num") pageNum: Int? = null
    ): Call<ResponseDates>

    @GET("m1/4782108-0-default/follower/list")
    fun getFollowerList(
        @Query("user_id") userId: String,
        @Query("page_size") pageSize: Int? = null,
        @Query("page_num") pageNum: Int? = null
    ): Call<ResponseDates>

    @GET("m1/4782108-0-default/friends/list")
    fun getFriendsList(
        @Query("page_size") pageSize: Int? = null,
        @Query("page_num") pageNum: Int? = null
    ): Call<ResponseDates>

}