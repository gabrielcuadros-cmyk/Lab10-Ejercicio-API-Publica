package com.example.lab10ejercicio.data

import retrofit2.Response
import retrofit2.http.*

interface PostApiService {
    @GET("posts")
    suspend fun selectPosts(): List<PostModel>

    @GET("posts/{id}")
    suspend fun selectPost(@Path("id") id: String): Response<PostModel>

    @POST("posts")
    suspend fun insertPost(@Body post: PostModel): Response<PostModel>

    @PUT("posts/{id}")
    suspend fun updatePost(@Path("id") id: String, @Body post: PostModel): Response<PostModel>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: String): Response<Unit>
}