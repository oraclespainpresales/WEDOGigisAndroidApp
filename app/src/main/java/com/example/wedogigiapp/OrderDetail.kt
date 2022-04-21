package com.example.wedogigiapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
//import com.sun.xml.internal.ws.streaming.XMLStreamWriterUtil.getOutputStream


//import com.sun.xml.internal.ws.streaming.XMLStreamWriterUtil.getOutputStream
//import jdk.nashorn.internal.runtime.ScriptingFunctions.readLine



class OrderDetail : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        //recuperar las variables globales
        val g = application as Globals

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        // Get the Intent that started this activity and extract the string
        val message = intent.getStringExtra(EXTRA_MESSAGE)

        // Capture the layout's TextView and set the string as its text
        val textView = findViewById<TextView>(R.id.textChangeOrder).apply {
            text = "# Order : " + message
        }

        val detalleOrden = findViewById<TextView>(R.id.detailOrder).apply {
            text = "Order Detail:"+ System.getProperty("line.separator")+
                    g.currentOrderDetails + System.getProperty("line.separator")+
                    "Price: " + g.currentOrderPrice + System.getProperty("line.separator")+
                    "Phone: " + g.currentOrderPhone + System.getProperty("line.separator")+
                    "Location: GPS coord. provided," + System.getProperty("line.separator")+
                    g.currentOrderLong+", "+g.currentOrderLat
        }

        //to change title of activity
        val actionBar = supportActionBar
        actionBar!!.title = "Pizza Payment"

        //inicializar text payment method
        val textView2 = findViewById<TextView>(R.id.paymentResult).apply {
            text = "Select payment method"
        }
    }

    /** Called when the user taps the VISA image */
    fun goVISA(view: View) {

        val okCall = callRestAPIUpdateOrder("VISA")

    }
    /** Called when the user taps the AMEX image */
    fun goAMEX(view: View) {

        val okCall = callRestAPIUpdateOrder("AMEX")

    }
    /** Called when the user taps the VISA image */
    fun goMC(view: View) {

        val okCall = callRestAPIUpdateOrder("MC")

    }
    /** Called when the user taps the VISA image */
    fun goCASH(view: View) {

        val okCall = callRestAPIUpdateOrder("CASH")

    }


    fun callRestAPIUpdateOrder(paymentMethod: String) : String{

        var retorno = "0"

        // Capture the layout's TextView and set the string as its text
        val textView = findViewById<TextView>(R.id.paymentResult).apply {
            text = "Making Payment"
        }

        val thread = Thread(Runnable {
            try {
                //Your code goes here
                //recuperar las variables globales
                val g = application as Globals
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra(EXTRA_MESSAGE, "PAYMENT")
                }
                //invocar servicio REST de cambio de STATUS
                Log.d("TRAZA DEMOZONE", "getData from " + "https://"+ g.zonesId4OrdersAPI[g.zoneCurrent]+"-gigispizza.wedoteam.io/")

                val url = URL("https://"+ g.zonesId4OrdersAPI[g.zoneCurrent]+"-gigispizza.wedoteam.io/changeStatus")
                //val url = URL("https://madrid-gigispizza.wedoteam.io/changeStatus")
                val con = url.openConnection() as HttpURLConnection
                con.setRequestMethod("PUT")
                con.setRequestProperty("Content-Type", "application/json")
                con.setRequestProperty("Accept", "application/json")
                con.doOutput = true
                val jsonInputString = "{${'"'}orderId${'"'} : ${'"'}"+g.currentOrderId+"${'"'},"+
                        "${'"'}status${'"'} : ${'"'}"+g.statusPaid+"${'"'}}"
                val input = jsonInputString

                try {
                    val outputStream: DataOutputStream = DataOutputStream(con.outputStream)
                    outputStream.write(jsonInputString.toByteArray())
                    outputStream.flush()
                } catch (exception: Exception) {
                    Log.d("CHANGE ORDEN STATUS", "Se produce una excepcion:"+exception.toString())
                }

                var responseLine: String? = null
                //read response
                BufferedReader(
                    InputStreamReader(con.inputStream, "utf-8")
                ).use({ br ->
                    val response = StringBuilder()
                    responseLine = br.readLine()
                    if (responseLine!!.contains("1"))
                        responseLine = "Payment OK"
                    else
                        responseLine= "Payment No OK"
                })
                // Capture the layout's TextView and set the string as its text
                val textView = findViewById<TextView>(R.id.paymentResult).apply {
                    text = responseLine
                }
                //startActivity(intent)
                //END invocar servicio REST de cambio de STATUS
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        thread.start()
        return retorno
    }

    /** Called when the user taps the Finish button */
    fun close(view: View) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, "PAYMENT OK")
        }
        startActivity(intent)
    }
}
