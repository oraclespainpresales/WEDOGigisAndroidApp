package com.example.wedogigiapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_route.*

class Route : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)


        //to change title of activity
        val actionBar = supportActionBar
        actionBar!!.title = "Best route to customer"

        // calcular las coordenadas origen y destino
        // recuperar el valor de la zona actual
        val g = application as Globals
        var orglat = ""
        var orglon = ""
        var destlat = ""
        var destlon = ""
        if (g.useHQCoords==0) {
            orglat = g.userCurrentLat
            orglon = g.userCurrentLong
        } else {
            orglat = g.zonesLat[g.zoneCurrent]
            orglon = g.zonesLong[g.zoneCurrent]
        }
        destlat = g.currentOrderLat
        destlon = g.currentOrderLong

        Log.d("CHANGE STATUS ORDEN", "RUTA (2)=["+"https://infra.wedoteam.io/mapviewer/calculate_route.html?orglat="+orglat+"&orglon="+orglon+"&destlat="+destlat+"&destlon="+destlon+"]")

        wvGetmeanapp.settings.javaScriptEnabled = true
        wvGetmeanapp.loadUrl("https://infra.wedoteam.io/mapviewer/calculate_route.html?orglat="+orglat+"&orglon="+orglon+"&destlat="+destlat+"&destlon="+destlon)
        //wvGetmeanapp.loadUrl("https://infra.wedoteam.io/mapviewer/calculate_route.html?orglat=40.521787&orglon=-3.890979&destlat=40.452282&destlon=-3.694093")
    }
}
