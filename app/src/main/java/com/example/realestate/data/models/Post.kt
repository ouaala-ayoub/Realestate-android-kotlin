package com.example.realestate.data.models

import com.google.gson.annotations.SerializedName

data class Post(

    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("description")
    var description: String? = null,

    @SerializedName("media")
    var media: List<String>,

    @SerializedName("type")
    var type: String,

    @SerializedName("price")
    var price: Number,

    @SerializedName("location")
    var location: PostLocationData,

    @SerializedName("ownerId")
    var ownerId: String,

    @SerializedName("likes")
    val likes: Int = 0,

    @SerializedName("category")
    var category: String,

    @SerializedName("details")
    var details: Details? = null

)
