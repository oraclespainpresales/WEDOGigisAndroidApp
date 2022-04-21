package com.example.wedogigiapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
//import android.widget.ArrayAdapter
//import android.R
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.*
import android.widget.*


//API calls
//import android.support.v7.app.AppCompatActivity
//import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.baoyz.swipemenulistview.*
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_order_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.lang.Thread as Thread1

import kotlinx.coroutines.*


const val EXTRA_MESSAGE = "com.example.wedogigiapp.MESSAGE"

class MainActivity : AppCompatActivity() {
    private val TAG = "MainTask"

    //variables para recoger los valores al hacer una accion de swipe
    val ordenesIdList      = kotlin.collections.ArrayList<String>()
    val ordenesStatusList  = kotlin.collections.ArrayList<String>()
    val ordenesDetailsList = kotlin.collections.ArrayList<String>()
    val ordenesPriceList   = kotlin.collections.ArrayList<String>()
    val ordenesPhoneList   = kotlin.collections.ArrayList<String>()
    val ordenesLongList    = kotlin.collections.ArrayList<String>()
    val ordenesLatList     = kotlin.collections.ArrayList<String>()
    // indica que se ha llamado al servicio de recuperacion de ordenes
    //var recuperadasOrdenes = false

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        //recuperar las variables globales
        val g = application as Globals
        g.getData()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupPermissions()

        val listView      = findViewById<View>(R.id.listView) as SwipeMenuListView
        val pullToRefresh = findViewById<View>(R.id.pullToRefresh) as SwipeRefreshLayout;

        pullToRefresh.setEnabled(false)
        pullToRefresh.setOnRefreshListener {
            getData()                    // refresh your list contents somehow
            pullToRefresh.isRefreshing = false   // reset the SwipeRefreshLayout (stop the loading spinner)
        }

        listView.setOnScrollListener(object: AbsListView.OnScrollListener {
            override fun onScrollStateChanged(absListView: AbsListView, i: Int) {}

            override fun onScroll(absListView: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                Log.d("frist visible", "firstVisibleItem: " + firstVisibleItem.toString())
                if (firstVisibleItem == 0)
                    pullToRefresh.setEnabled(true);
                else
                    pullToRefresh.setEnabled(false);
                //listView.setMenuCreator(createMenu(g, firstVisibleItem))
            }
        })

        //recuperar las ordenes
        getData()

        //ejecutar codigo de inicialización de la aplicación (solo 1 vez)
        // de momento hasta que el servicio de demozones no esté disponible, no se ejecuta
        //if (g.appInitialized == 0) {
        //    initApp()
        //    g.appInitialized = 1
        //}

//---

        listView.setMenuCreator(object: SwipeMenuCreator{
            override fun create(menu: SwipeMenu?) {
                var menuType = menu as SwipeMenu
                Log.d("MENU TYPE", "menutype: " + menuType.viewType.toString())
                val openItem = SwipeMenuItem(
                    applicationContext
                )
                // set item width
                openItem.width = 200
                // set item title fontsize
                openItem.titleSize = 18
                // set item title font color
                openItem.titleColor = Color.WHITE

                when (menuType.viewType){
                    0 -> {

                        openItem.background = ColorDrawable(
                            Color.rgb(0x00, 0x66,0xff)
                        )
                        // set item title
                        openItem.title = "Deliver"
                    }
                    1 -> {
                        openItem.background = ColorDrawable(
                            Color.rgb(0x29, 0x88,0x2E)
                        )
                        // set item title
                        openItem.title = "Payment"
                    }
                }

                // add to menu
                menu.addMenuItem(openItem)
                // create "route" item
                val deleteItem = SwipeMenuItem(
                    applicationContext
                )
                // set item background
                deleteItem.background = ColorDrawable(
                    Color.rgb(
                        0xF9,
                        0x3F, 0x25
                    )
                )
                // set item width
                deleteItem.width = 190
                // set item title
                deleteItem.title = "Route"
                // set item title fontsize
                deleteItem.titleSize = 18
                // set item title font color
                deleteItem.titleColor = Color.WHITE
                // add to menu
                menu.addMenuItem(deleteItem)
            }
        })

        listView.setOnMenuItemClickListener {position, menu, index ->
            g.currentOrderId      = ordenesIdList[position]
            g.currentOrderStatus  = ordenesStatusList[position]
            g.currentOrderDetails = ordenesDetailsList[position]
            g.currentOrderPrice   = ordenesPriceList[position]
            g.currentOrderPhone   = ordenesPhoneList[position]
            g.currentOrderLong    = ordenesLongList[position]
            g.currentOrderLat     = ordenesLatList[position]
            when (index) {
                0 -> {
                    Log.d("TRAZA JESUS", "[ESTADO DE LA ORDEN"+g.currentOrderId+"] = ["+g.currentOrderStatus+"]")
                    if (g.currentOrderStatus == g.statusDelivered) {
                        this.goOrderDetails(this, g.currentOrderId)
                    }
                    else {
                        var okCall = callRestAPIUpdateOrder()
                    }
                }
                1 -> {
                    this.goRoute(this)
                }
            }
            // false : close the menu; true : not close the menu
            false
        }


        //to change title of activity
        val actionBar = supportActionBar
        actionBar!!.title = "DZ: ("+g.zones[g.zoneCurrent]+") Orders"
        // Set action bar elevation
        actionBar.elevation = 15.0F
        // Display the app icon in action bar/toolbar
        actionBar.setDisplayShowHomeEnabled(true)
        actionBar.setLogo(R.mipmap.ic_launcher)
        actionBar.setDisplayUseLogoEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.getItemId()

        if (id == R.id.add) {
            goGPSConfiguration()
            return true
        }
        if (id == R.id.about) {
            val builder = AlertDialog.Builder(this@MainActivity)
            // Set the alert dialog title
            builder.setTitle("About Wedo Gigi's Pizza Demo")
            // Display a message on alert dialog
            builder.setMessage("Gigi's Pizza DevOps Demo version 1.0\nPlease contact WeDo team for more info")
            // Display a neutral button on alert dialog
            builder.setNeutralButton("Continue"){_,_ ->
                Toast.makeText(applicationContext,"You exit the dialog.",Toast.LENGTH_SHORT).show()
            }
            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()
            // Display the alert dialog on app interface
            dialog.show()
            return true
        }
        if (id == R.id.exit) {
            finish()
        }

        return super.onOptionsItemSelected(item)

    }

    private fun setupPermissions() {
        val coarPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION)

        val finePermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)

        if (coarPermission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to ACCESS_COARSE_LOCATION denied")
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Permission to access location is required for this app to record audio.")
                        .setTitle("Permission required")
                            builder.setPositiveButton("OK"
                            ) { dialog, id ->
                        Log.i(TAG, "Clicked")
                        makeRequestCoarce()
                    }

                    val dialog = builder.create()
                dialog.show()
            } else {
                makeRequestCoarce()
            }
        }
        else if (finePermission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to ACCESS_FINE_LOCATION denied")
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Permission to access fine location is required for this app to record audio.")
                    .setTitle("Permission required")
                builder.setPositiveButton("OK"
                ) { dialog, id ->
                    Log.i(TAG, "Clicked")
                    makeRequestFine()
                }

                val dialog = builder.create()
                dialog.show()
            } else {
                makeRequestFine()
            }
        }
    }

    private fun makeRequestCoarce() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            1)
    }

    private fun makeRequestFine() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            2)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
                    finish();
                }
                return
            }
            2 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
                    finish();
                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun goOrderDetails(view: MainActivity, idOrder: String) {
        val message = idOrder

        val intent = Intent(this, OrderDetail::class.java).apply {
            putExtra(EXTRA_MESSAGE, message)
        }
        startActivity(intent)
    }

    /** Called when the user taps the Cross image */
    fun goGPSConfiguration() {
        val message = "GO ROUTE"
        val intent = Intent(this, GPSConfiguration::class.java).apply {
            putExtra(EXTRA_MESSAGE, message)
        }
        startActivity(intent)
    }

    /** Called when the user taps the Route image */
    fun goRoute(view: MainActivity) {
        val message = "GO ROUTE"
        val intent = Intent(this, Route::class.java).apply {
            putExtra(EXTRA_MESSAGE, message)
        }
        startActivity(intent)
    }

    fun getData() {
        pullToRefresh.isRefreshing = true
        Log.d("TRAZA JESUS", "getData")
        val g = application as Globals
        val list = kotlin.collections.ArrayList<String>()
        Log.d("TRAZA DEMOZONE", "getData from " + "https://"+ g.zonesId4OrdersAPI[g.zoneCurrent]+"-gigispizza.wedoteam.io/")
        val retrofitClient = NetworkUtils
            .getRetrofitInstance("https://"+ g.zonesId4OrdersAPI[g.zoneCurrent]+"-gigispizza.wedoteam.io/")
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val jsonQuery = "{\"orderId\": \"\",\"where\": [{" +
                "\"cond\": {" +
                "\"field\": \"status\"," +
                "\"operator\": \"LIKE\"," +
                "\"value\": \"'PIZZA OUT FOR DELIVERY'\"" +
                "}," +
                "\"relation\": \"OR\"" +
                "}," +
                "{" +
                "\"cond\": {" +
                "\"field\": \"status\"," +
                "\"operator\": \"LIKE\"," +
                "\"value\": \"'PIZZA DELIVERED'\"" +
                "}," +
                "\"relation\": \"\"}]}"

        Log.d("TRAZA DEMOZONE", "body query" + jsonQuery)
        val callback = endpoint.getOrders(jsonQuery)

        callback.enqueue(object : Callback<List<Orders>> {
            override fun onFailure(call: Call<List<Orders>>, t: Throwable) {
                pullToRefresh.isRefreshing = false
                Log.e ("ERROR QUERY DEMOZONE","Error: " + t.message)
                if (!t.message!!.contains("BEGIN_ARRAY"))
                    Toast.makeText(baseContext, t.message, Toast.LENGTH_SHORT).show()
                //recuperadasOrdenes = true
            }

            override fun onResponse(call: Call<List<Orders>>, response: Response<List<Orders>>) {
                //var aux para calcular el tipo de Pizza
                var tipoPizza = ""
                ordenesIdList.clear()
                ordenesStatusList.clear()
                ordenesDetailsList.clear()
                ordenesPriceList.clear()
                ordenesPhoneList.clear()
                ordenesLongList.clear()
                ordenesLatList.clear()
                Log.d("TRAZA RESPONSE", "Response: " + response.body().toString())
                response.body()?.forEach {
                    Log.d("RECUPERADA UNA PIZZA", it.orderId+" status = ["+it.status+"]")
                    // Aquí se carga la lista de datos los pedidos solo pizzas con estado READY FOR DELIVERY y DELIVERED
                    // no hace falta hacer el if porque se llama al API gerOrder con where del status que queremos recuperar.
                    //if ((it.status.compareTo(g.statusOutForDelivery)==0)|| (it.status.compareTo(g.statusDelivered)==0)){
                        //recuperar todos los valores de la orden
                        ordenesIdList.add(it.orderId)
                        ordenesStatusList.add(it.status)
                        tipoPizza = it.pizzaOrdered.baseType
                        if (!it.pizzaOrdered.topping1.equals("no extra toppings"))
                            tipoPizza = tipoPizza + ", "+ it.pizzaOrdered.topping1
                        if (!it.pizzaOrdered.topping2.equals("no extra toppings"))
                            tipoPizza = tipoPizza + ", "+ it.pizzaOrdered.topping2
                        if (!it.pizzaOrdered.topping3.equals("no extra toppings"))
                            tipoPizza = tipoPizza + ", "+ it.pizzaOrdered.topping3
                        ordenesDetailsList.add (tipoPizza)
                        ordenesPriceList.add (it.totalPrice)
                        ordenesPhoneList.add (it.customer.customerId.telephone)
                        ordenesLongList.add (it.customerAdress.street.long)
                        ordenesLatList.add (it.customerAdress.street.lat)

                        list.add("ORDER " + it.orderId
                                + System.getProperty("line.separator") + it.status
                                + System.getProperty("line.separator") + "Price " + it.totalPrice
                                + System.getProperty("line.separator") + "Date " + it.dateTimeOrderTaken
                        )
                    //}
                }
                //val adapter = ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_list_item_1, list)
                //Cambiamos el color de fondo de los elementos que están esperando para ser enviados
                val adapter: ArrayAdapter<String> =
                    object : ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_list_item_1, list) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val v = super.getView(position, convertView, parent)
                            val tv = v.findViewById<TextView>(android.R.id.text1)
                            Log.d("item list view","item ["+ position +"]: " + tv.text)
                            if (tv.text!!.contains(g.statusOutForDelivery)) {
                                tv.setBackgroundColor(Color.rgb(
                                    170,
                                    250,
                                    170
                                ))
                            }
                            else {
                                tv.setBackgroundColor(Color.rgb(
                                    255,
                                    255,
                                    255
                                ))
                            }

                            return v
                        }

                        override fun getViewTypeCount(): Int {
                            return 2
                        }

                        override fun getItemViewType(position: Int): Int {
                            var item = getItem(position);
                            var contenType = super.getItemViewType(position)
                            if (item!!.contains(g.statusOutForDelivery)) {
                                contenType = 0
                            }
                            else{
                                contenType = 1
                            }
                            Log.d(
                                "CONTENT TYPE",
                                "ContentType: ["+contenType+"] item [" + position + "]: " + getItem(position)
                            )
                            return contenType
                        }
                    }

                listView.adapter = adapter
                pullToRefresh.isRefreshing = false
                //recuperadasOrdenes = true
            }
        })
    }



    fun callRestAPIUpdateOrder() : String{
        var retorno = "0"
        var textView = findViewById<TextView>(R.id.textChangeOrder)

        val thread = Thread1(Runnable {
            try {
                //Your code goes here
                // Capture the layout's TextView and set the string as its text
                this@MainActivity.runOnUiThread(java.lang.Runnable {
                    //val textView = findViewById<TextView>(R.id.textChangeOrder).apply {
                    //    text = "Changing order status"
                    //}
                    textView.setText("Changing order status")
                })

                //recuperar las variables globales
                val g = application as Globals
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra(EXTRA_MESSAGE, "AMEX")
                }
                //invocar servicio REST de cambio de STATUS
                Log.d("TRAZA DEMOZONE", "getData from " + "https://"+ g.zonesId4OrdersAPI[g.zoneCurrent]+"-gigispizza.wedoteam.io/")

                val url = URL("https://"+ g.zonesId4OrdersAPI[g.zoneCurrent]+"-gigispizza.wedoteam.io/changeStatus")
                val con = url.openConnection() as HttpURLConnection
                con.setRequestMethod("PUT")
                con.setRequestProperty("Content-Type", "application/json")
                con.setRequestProperty("Accept", "application/json")
                con.doOutput = true
                val jsonInputString = "{${'"'}orderId${'"'} : ${'"'}"+g.currentOrderId+"${'"'},"+
                        "${'"'}status${'"'} : ${'"'}"+g.statusDelivered+"${'"'}}"
                val input = jsonInputString

                try {
                    val outputStream: DataOutputStream = DataOutputStream(con.outputStream)
                    outputStream.write(jsonInputString.toByteArray())
                    outputStream.flush()
                } catch (exception: Exception) {
                    Log.d("CHANGE STATUS ORDEN", "Se produce una excepcion:"+exception.toString())
                }

                var responseLine: String? = null
                //read response
                BufferedReader(
                    InputStreamReader(con.inputStream, "utf-8")
                ).use({ br ->
                    //val response = StringBuilder()
                    responseLine = br.readLine()
                    if (responseLine!!.contains("1"))
                        responseLine = "Order ready for deliver"
                    else
                        responseLine= "Error in order"
                })

                this@MainActivity.runOnUiThread(java.lang.Runnable {
                    textView.setText(responseLine)
                    //recargar pagina
                    getData()
                })
                //END invocar servicio REST de cambio de STATUS
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        thread.start()
        return retorno
    }

    fun initApp () {

        // este código no se puede ejecutar porque de el problema del handshake de SSl
        // cuando el servicio esté operativo se utilizará

        Log.d("TRAZA JESUS", "initApp")
        var retorno = "0"


        val thread = Thread1(Runnable {
            try {
                //Your code goes here


                //recuperar las variables globales
                val g = application as Globals


                //invocar servicio REST de obtencion datos de DemoZones
                val url = URL("https://apex.wedoteam.io/ords/pdb1/wedodevops/api/demozones")
                val con = url.openConnection() as HttpURLConnection
                con.setRequestMethod("GET")
                con.setRequestProperty("Content-Type", "application/json")
                con.setRequestProperty("Accept", "application/json")
                con.doOutput = true
                /*
                val jsonInputString = "{${'"'}orderId${'"'} : ${'"'}"+g.currentOrderId+"${'"'},"+
                        "${'"'}status${'"'} : ${'"'}"+g.statusDelivered+"${'"'}}"
                val input = jsonInputString
                try {
                    val outputStream: DataOutputStream = DataOutputStream(con.outputStream)
                    outputStream.write(jsonInputString.toByteArray())
                    outputStream.flush()
                } catch (exception: Exception) {
                    Log.d("CHANGE STATUS ORDEN", "Se produce una excepcion:"+exception.toString())

                }
                */

                var responseLine: String? = null
                //read response
                BufferedReader(
                    InputStreamReader(con.inputStream, "utf-8")
                ).use({ br ->
                    val response = StringBuilder()
                    responseLine = br.readLine()
                    Log.d("INIT_APP", "INFO RECUPERADA ["+responseLine+"]")
                })

                //END invocar servicio REST de obtencion info DemoZones



            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        thread.start()

    }

}

