package com.example.wedogigiapp

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_gpsconfiguration.*
//import com.sun.xml.internal.bind.v2.schemagen.Util.equalsIgnoreCase
import android.widget.Spinner
import android.widget.RadioButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.multidex.MultiDex
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.instantapps.ActivityCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener


class GPSConfiguration : AppCompatActivity(), AdapterView.OnItemSelectedListener
    , GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    com.google.android.gms.location.LocationListener
{

    // para la localización actual del dispositivo
    private lateinit var mGoogleApiClient: GoogleApiClient
    private var mLocationManager: LocationManager? = null
    lateinit var mLocation: Location
    private var mLocationRequest: LocationRequest? = null
    private val listener: com.google.android.gms.location.LocationListener? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

    lateinit var locationManager: LocationManager


    //Spinner
    var zones = arrayOf("")


    var spinner:Spinner? = null
    var textView_msg:TextView? = null
    //END Spinner



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gpsconfiguration)

        // recuperar el valor de la zona actual
        val g = application as Globals

        //Spinner
        textView_msg = this.msg

        spinner = this.spinnerGPS
        spinner!!.setOnItemSelectedListener(this)

        // Create an ArrayAdapter using a simple spinner layout and languages array
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, g.zones)
        // Set layout to use when the list of choices appear
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        spinner!!.setAdapter(aa)
        //END Spinner

        //to change title of activity
        val actionBar = supportActionBar
        actionBar!!.title = "GPS Configuration"



        //Log.d("GPS CONFIG (INIT)", "g.zonesId[g.zoneCurrent])="+g.zones[g.zoneCurrent])
        //Log.d("GPS CONFIG (INIT)", "g.zoneCurrentLong)="+g.zoneCurrentLong)
        //Log.d("GPS CONFIG (INIT)", "g.zoneCurrentLat)="+g.zoneCurrentLat)
        spinner!!.setSelection(obtenerPosicionItem(spinner!!, g.zones[g.zoneCurrent]));

        //set del radio button
        val radioButton = findViewById<View>(R.id.radioDemoZone) as RadioButton

        val radioButton2 = findViewById<View>(R.id.radioCurrentPos) as RadioButton
        val grupoRadioButtonUSeLocalization  = findViewById<View>(R.id.radioGroup) as RadioGroup

        if (g.useHQCoords==0) {
            radioButton.setChecked(false)
            radioButton2.setChecked(true)
        }

        // inicializar para acceder a la localizacion actual
        MultiDex.install(this)
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()

        mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        checkLocation()

    }

    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
        val g = application as Globals
        //textView_msg!!.text = "Selected : "+g.zones[position]
        g.zoneCurrent = position
        //Log.d("TRAZA JESUS (3) ", "Variable global (seleccionada en el combo)"+g.zonesId[g.zoneCurrent])
        // establecer el spiner al valor de la zona actual
        spinner!!.setSelection(obtenerPosicionItem(spinner!!, g.zones[g.zoneCurrent]));
        // se cambian las ccodenadas a las de la nueva zona. Solo en caso de que se estén usando las de HQ, no currentPos
        if (g.useHQCoords==1) {
            g.zoneCurrentLong = g.zonesLong[g.zoneCurrent]
            g.zoneCurrentLat = g.zonesLat[g.zoneCurrent]
        }


    }

    override fun onNothingSelected(arg0: AdapterView<*>) {

    }

    override fun onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    override fun onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    override fun onConnectionSuspended(p0: Int) {

        mGoogleApiClient.connect();
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i("TAG", "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    override fun onLocationChanged(location: Location) {


        var msg = "Updated Location: Latitude " + location.longitude.toString() + location.longitude;
        var g = application as Globals
        g.userCurrentLat = ""+location.latitude
        g.userCurrentLong = ""+location.longitude
        // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();


    }

    override fun onConnected(p0: Bundle?) {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //------------------
            Log.d("ONCONNECTED", "ContextCompat.checkSelfPermission")

            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

            //------------------


            //return;
        }


        startLocationUpdates();

        var fusedLocationProviderClient :
                FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient .getLastLocation()
            .addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                    mLocation = location;
                    // recuperar el valor de la zona actual
                    var g = application as Globals
                    g.userCurrentLat = ""+mLocation.latitude
                    g.userCurrentLong = ""+mLocation.longitude

                }
            })
    }



    // función para inicializar el Spinner al valor de la zona actual
    //Método para obtener la posición de un ítem del spinner
    fun obtenerPosicionItem(spinner: Spinner, zonaActual: String): Int {
        //Creamos la variable posicion y lo inicializamos en 0
        var posicion = 0
        //Recorre el spinner en busca del ítem que coincida con el parametro `String fruta`
        //que lo pasaremos posteriormente
        for (i in 0 until spinner.count) {
            //Almacena la posición del ítem que coincida con la búsqueda
            if (spinner.getItemAtPosition(i).toString().equals(zonaActual, ignoreCase = true)) {
                posicion = i
            }
        }
        //Devuelve un valor entero (si encontro una coincidencia devuelve la
        // posición 0 o N, de lo contrario devuelve 0 = posición inicial)
        return posicion
    }

    //Este método se ejecutará cuando se presionen los botones del radio
    fun clickRAdioHQ(view: View) {
        // recuperar el valor de la zona actual
        var g = application as Globals
        g.useHQCoords = 1
        /*
        g.zoneCurrentLong = g.zonesLong[g.zoneCurrent]
        g.zoneCurrentLat = g.zonesLat[g.zoneCurrent]
        */
    }
    fun clickRAdioCurrentPos(view: View) {
        // recuperar el valor de la zona actual
        var g = application as Globals
        g.useHQCoords = 0
        g.userCurrentLat = ""+mLocation.latitude
        g.userCurrentLong = ""+mLocation.longitude

    }

    private fun checkLocation(): Boolean {

        if(!isLocationEnabled()) {
            showAlert();
        }
        return isLocationEnabled();
    }

    private fun isLocationEnabled(): Boolean {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
            .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
            .setPositiveButton("Location Settings", DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(myIntent)
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { paramDialogInterface, paramInt -> })
        dialog.show()
    }

    protected fun startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL)
            .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
            mLocationRequest, this);
    }

}
