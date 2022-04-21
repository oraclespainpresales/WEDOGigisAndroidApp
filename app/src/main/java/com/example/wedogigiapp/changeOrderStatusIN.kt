package com.example.wedogigiapp

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.PUT

data class changeOrderStatusIN(
    val orderId: String,
    val status: String
)

