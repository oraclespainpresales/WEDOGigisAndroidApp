package com.example.wedogigiapp

import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*
import java.io.ObjectInput

/*
data class Posts(
    @SerializedName("userId")
    var userId : Int,
    @SerializedName("id")
    var id : Int,
    @SerializedName("title")
    var title : String,
    @SerializedName("body")
    var body : String
)
*/

data class Orders(
    val customer: Customer,
    val customerAdress: CustomerAdress,
    val dateTimeOrderTaken: String,
    val orderId: String,
    val pizzaOrdered: PizzaOrdered,
    val status: String,
    val takenByEmployee: String,
    val totalPrice: String
)

data class ordsDemozones(
    val items: List<Demozones>
)

data class Demozones(
    val id: String,
    val name: String,
    val longitude: String,
    val latitude: String
)


interface Endpoint {
    /*
    @GET("posts")
    fun getPosts() : Call<List<Posts>>
    */
    //POST con headers para que se pueda pasar en el body una query
    //Se usa para poder enviar un where a la consulta de getOrders
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("getOrder")
    fun getOrders(@Body body: String) : Call<List<Orders>>

    @GET("demozones")
    fun getDemozones() : Call<ordsDemozones>
}

