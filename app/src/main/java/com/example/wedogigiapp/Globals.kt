package com.example.wedogigiapp

import android.app.Application
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
Coordenadas GPS y ZONAS

           {
                print(“MAD")
                let defaults = UserDefaults.standard
                defaults.set("40.521787" , forKey: "lat")
                defaults.set("-3.890979" , forKey: "long")
                defaults.set("1" , forKey: "gps")
                defaults.synchronize()
            }

          {
                print(“AGP")
                let defaults = UserDefaults.standard
                defaults.set("36.738841" , forKey: "lat")
                defaults.set("-4.553855" , forKey: "long")
                defaults.set("2" , forKey: "gps")
                defaults.synchronize()
            }
        case 3:
            do{
                print(“SFO")
                let defaults = UserDefaults.standard
                defaults.set("37.529534," , forKey: "lat")
                defaults.set("-122.264849" , forKey: "long")
                defaults.set("3" , forKey: "gps")
                defaults.synchronize()
            }
        case 4:
            do{
                print(“HND")
                let defaults = UserDefaults.standard
                defaults.set("35.6712461," , forKey: "lat")
                defaults.set("139.7185629" , forKey: "long")
                defaults.set("4" , forKey: "gps")
                defaults.synchronize()
            }


https://apex.wedoteam.io/ords/pdb1/wedodevops/api/demozones
{"items":[
{"id":"MALAGA","name":"Malaga","description":null,"active":"Y","contact":"Iván Sampedro","contactemail":"ivan.sampedro@oracle.com","contact2":null,"contact2email":null,"longitude":-3.702628,"latitude":40.416721},
{"id":"TOKYO","name":"Tokyo","description":null,"active":"Y","contact":"Iván Sampedro","contactemail":"ivan.sampedro@oracle.com","contact2":null,"contact2email":null,"longitude":35.682135,"latitude":139.744555},
{"id":"MADRID","name":"Madrid","description":null,"active":"Y","contact":"Iván Sampedro","contactemail":"ivan.sampedro@oracle.com","contact2":null,"contact2email":null,"longitude":-4.423019,"latitude":36.713357}],"hasMore":false,"limit":25,"offset":0,"count":3,"links":[{"rel":"self","href":"https://apex.wedoteam.io/ords/pdb1/wedodevops/api/demozones"},{"rel":"describedby","href":"https://apex.wedoteam.io/ords/pdb1/wedodevops/metadata-catalog/api/item"},{"rel":"first","href":"https://apex.wedoteam.io/ords/pdb1/wedodevops/api/demozones"}]}

 */

class Globals : Application() {

    // aplicacion inicializada se usa para poner código que solo se debe ejecutar 1 vez (init de aplicacion)
    var appInitialized = 0
    var zones = arrayOf("Istanbul", "Madrid", "Málaga", "San Francisco", "Tokyo")
    var zonesId4OrdersAPI = arrayOf("istanbul", "madrid", "malaga", "sanfrancisco", "tokyo")
    //var zonesId = arrayOf("IST", "MAD", "AGP", "SFO", "HND")
    var zoneCurrent = 0
    var useHQCoords = 1
    // variables para las coordenadas (pueden cambiar si se usa current location)
    // Se inicializa con los valores de Madrid
    var zoneCurrentLong = "-3.890979"
    var zoneCurrentLat = "40.521787"
    var zonesLong = arrayOf("0", "-3.890979", "-4.553855", "-122.264849", "139.7185629")
    var zonesLat = arrayOf("0", "40.521787", "36.738841", "37.529534", "35.6712461")
    var userCurrentLong = "-3.890979"
    var userCurrentLat = "40.521787"

    // variables para la gestión de ordenes (paso de variables globales entre actividades)
    var currentOrderId = ""
    var currentOrderStatus = ""
    var currentOrderDetails = ""
    var currentOrderPrice = ""
    var currentOrderPhone = ""
    var currentOrderLong = ""
    var currentOrderLat = ""

    // constantes de estado de la orden
    val statusOutForDelivery = "PIZZA OUT FOR DELIVERY"
    val statusDelivered = "PIZZA DELIVERED"
    val statusPaid = "PIZZA PAID"

    fun getData() {
        val demozonesIDList   = kotlin.collections.ArrayList<String>()
        val demozonesNameList = kotlin.collections.ArrayList<String>()
        val demozonesLongList = kotlin.collections.ArrayList<String>()
        val demozonesLatList  = kotlin.collections.ArrayList<String>()

        Log.d("TRAZA DEMOZONE", "getData from " + "https://apex.wedoteam.io/ords/pdb1/wedodevops/api/demozones")
        val retrofitClient = NetworkUtils
            .getRetrofitInstance("https://apex.wedoteam.io/ords/pdb1/wedodevops/api/")
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val callback = endpoint.getDemozones()

        callback.enqueue(object : Callback<ordsDemozones> {
            override fun onFailure(call: Call<ordsDemozones>, t: Throwable) {
                Log.e ("ERROR QUERY DEMOZONE","Error: " + t.message)
                if (!t.message!!.contains("BEGIN_ARRAY"))
                    Toast.makeText(baseContext, t.message, Toast.LENGTH_SHORT).show()
                //recuperadasOrdenes = true
            }

            override fun onResponse(call: Call<ordsDemozones>, response: Response<ordsDemozones>) {
                demozonesIDList.clear()
                demozonesNameList.clear()
                demozonesLongList.clear()
                demozonesLatList.clear()

                Log.d("TRAZA RESPONSE", "Response: " + response.body().toString())
                response.body()?.items?.forEach {
                    Log.d("RECUPERADA UNA Demozone ", it.id+" name = ["+it.name+"]")
                    demozonesIDList.add(it.id)
                    demozonesNameList.add(it.name)
                    demozonesLongList.add(it.longitude)
                    demozonesLatList.add(it.latitude)
                }

                zones             = demozonesNameList.toTypedArray()
                zonesId4OrdersAPI = demozonesIDList.toTypedArray()
                zonesLong         = demozonesLongList.toTypedArray()
                zonesLat          = demozonesLatList.toTypedArray()

                Log.d("TRAZA DEMOZONES", "Demozones[zones]:             " + zones)
                Log.d("TRAZA DEMOZONES", "Demozones[zonesId4OrdersAPI]: " + zonesId4OrdersAPI)
                Log.d("TRAZA DEMOZONES", "Demozones[zonesLong]:         " + zonesLong)
                Log.d("TRAZA DEMOZONES", "Demozones[zonesLat]:          " + zonesLat)
            }
        })
    }
}