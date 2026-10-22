package com.example.lab10ejercicio.data

import com.google.gson.annotations.SerializedName

data class PostModel(
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("title")
    var title: String,
    @SerializedName("body")
    var body: String,
    @SerializedName("userId")
    var userId: Int = 1
)