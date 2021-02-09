
package com.bitran.ubired

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bitran.ubired.ble.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_ble_operations.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.selector
import org.jetbrains.anko.yesButton
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class BleOperationsActivity : AppCompatActivity() {

    private lateinit var device: BluetoothDevice
    private var id:String= null.toString()
    var mainHandler = Handler(Looper.getMainLooper())
    private val dateFormatter = SimpleDateFormat("MMM d, HH:mm:ss", Locale.US)
    private val characteristics by lazy {
        ConnectionManager.servicesOnDevice(device)?.flatMap { service ->
            service.characteristics ?: listOf()
        } ?: listOf()
    }
    private val characteristicProperties by lazy {
        characteristics.map { characteristic ->
            characteristic to mutableListOf<CharacteristicProperty>().apply {
                if (characteristic.isNotifiable()) add(CharacteristicProperty.Notifiable)
                if (characteristic.isIndicatable()) add(CharacteristicProperty.Indicatable)
                if (characteristic.isReadable()) add(CharacteristicProperty.Readable)
                if (characteristic.isWritable()) add(CharacteristicProperty.Writable)
                if (characteristic.isWritableWithoutResponse()) {
                    add(CharacteristicProperty.WritableWithoutResponse)
                }
            }.toList()
        }.toMap()
    }
    private val characteristicAdapter: CharacteristicAdapter by lazy {
        CharacteristicAdapter(characteristics) { characteristic ->
            showCharacteristicOptions(characteristic)
        }
    }
    private var notifyingCharacteristics = mutableListOf<UUID>()

    override fun onCreate(savedInstanceState: Bundle?) {
        ConnectionManager.registerListener(connectionEventListener)
        super.onCreate(savedInstanceState)
        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            ?: error("Missing BluetoothDevice from MainActivity!")

        setContentView(R.layout.activity_ble_operations)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
            title =" getString(R.string.ble_playground)"
        }
        setupRecyclerView()
        request_mtu_button.setOnClickListener {
            if (mtu_field.text.isNotEmpty() && mtu_field.text.isNotBlank()) {
                mtu_field.text.toString().toIntOrNull()?.let { mtu ->
                    log("Requesting for MTU value of $mtu")
                    ConnectionManager.requestMtu(device, mtu)
                } ?: log("Invalid MTU value: ${mtu_field.text}")
            } else {
                log("Please specify a numeric value for desired ATT MTU (23-517)")
            }
            hideKeyboard()
        }
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                id = task.result?.token.toString()
                ac=1
            })

        checkSMSStatePermissionA()
      //  id= intent.getParcelableExtra("id")
        //    ?: error("Missing BluetoothDevice from MainActivity!")
        Back()
    }

    override fun onDestroy() {
        ConnectionManager.unregisterListener(connectionEventListener)
        ConnectionManager.teardownConnection(device)
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        characteristics_recycler_view.apply {
            adapter = characteristicAdapter
            layoutManager = LinearLayoutManager(
                this@BleOperationsActivity,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = characteristics_recycler_view.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun log(message: String) {
        val formattedMessage = String.format("%s: %s", dateFormatter.format(Date()), message)
        runOnUiThread {
            val currentLogText = if (log_text_view.text.isEmpty()) {
                "Beginning of log."
            } else {
                log_text_view.text
            }
            log_text_view.text = "$currentLogText\n$formattedMessage"
            log_scroll_view.post { log_scroll_view.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun showCharacteristicOptions(characteristic: BluetoothGattCharacteristic) {
        characteristicProperties[characteristic]?.let { properties ->
            selector("Select an action to perform", properties.map { it.action }) { _, i ->
                when (properties[i]) {
                    CharacteristicProperty.Readable -> {
                        log("Reading from ${characteristic.uuid}")
                        ConnectionManager.readCharacteristic(device, characteristic)
                    }
                    CharacteristicProperty.Writable, CharacteristicProperty.WritableWithoutResponse -> {
                        showWritePayloadDialog(characteristic)
                    }
                    CharacteristicProperty.Notifiable, CharacteristicProperty.Indicatable -> {
                        if (notifyingCharacteristics.contains(characteristic.uuid)) {
                            log("Disabling notifications on ${characteristic.uuid}")
                            ConnectionManager.disableNotifications(device, characteristic)
                        } else {
                            log("Enabling notifications on ${characteristic.uuid}")
                            ConnectionManager.enableNotifications(device, characteristic)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showWritePayloadDialog(characteristic: BluetoothGattCharacteristic) {
        val hexField = layoutInflater.inflate(R.layout.edittext_hex_payload, null) as EditText
        alert {
            customView = hexField
            isCancelable = false
            yesButton {
                with(hexField.text.toString()) {
                    if (isNotBlank() && isNotEmpty()) {
                        val bytes = hexToBytes()
                        log("Writing to ${characteristic.uuid}: ${bytes.toHexString()}")
                        ConnectionManager.writeCharacteristic(device, characteristic, bytes)
                    } else {
                        log("Please enter a hex payload to write to ${characteristic.uuid}")
                    }
                }
            }
            noButton {}
        }.show()
        hexField.showKeyboard()
    }
    fun Back(){
        val intent = Intent()
        intent.setAction(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }

    var i=0
    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onDisconnect = {
                runOnUiThread {///////////////////////////////////////////////////////////////////////////////////////////////DESCONEXION
                    alert {
                        title = "SE ESTA ENVIANDO LA UBICACION"
                        message = "¿DESACTIVAR LA ALARMA?"
                        positiveButton("OK") {
                            ac=0
                            i=0
                            mainHandler = Handler(Looper.getMainLooper())
                            onBackPressed() //
                        }
                    }.show()

                }
                mainHandler.post(object : Runnable {
                    override fun run() {

                        getDeviceLocation(i)
                        i++
                        mainHandler.postDelayed(this, 10000)/// 10 segundos
                        if(ac==0)
                            mainHandler.removeCallbacks(this)
                    }
                })


            }

            onCharacteristicRead = { _, characteristic ->
                log("Read from ${characteristic.uuid}: ${characteristic.value.toHexString()}")
            }

            onCharacteristicWrite = { _, characteristic ->
                log("Wrote to ${characteristic.uuid}")
            }

            onMtuChanged = { _, mtu ->
                log("MTU updated to $mtu")
            }

            onCharacteristicChanged = { _, characteristic ->
                log("Value changed on ${characteristic.uuid}: ${characteristic.value.toHexString()}")
            }

            onNotificationsEnabled = { _, characteristic ->
                log("Enabled notifications on ${characteristic.uuid}")
                notifyingCharacteristics.add(characteristic.uuid)
            }

            onNotificationsDisabled = { _, characteristic ->
                log("Disabled notifications on ${characteristic.uuid}")
                notifyingCharacteristics.remove(characteristic.uuid)
            }
        }
    }

    private enum class CharacteristicProperty {
        Readable,
        Writable,
        WritableWithoutResponse,
        Notifiable,
        Indicatable;

        val action
            get() = when (this) {
                Readable -> "Read"
                Writable -> "Write"
                WritableWithoutResponse -> "Write Without Response"
                Notifiable -> "Toggle Notifications"
                Indicatable -> "Toggle Indications"
            }
    }

    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }
    private fun checkSMSStatePermissionA() {
        val permissionCheck: Int = ContextCompat.checkSelfPermission(
            this, Manifest.permission.SEND_SMS
        )
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.i("Mensaje", "No se tiene permiso para enviar SMS.")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 225)
        } else {
            Log.i("Mensaje", "Se tiene permiso para enviar SMS!")
        }
    }


    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun EditText.showKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        requestFocus()
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun String.hexToBytes() =
        this.chunked(2).map { it.toUpperCase(Locale.US).toInt(16).toByte() }.toByteArray()

    private fun InserLocalizacionH(
        lat: String,
        lon: String,
        fecha: String,
        hora: String,
        detalle: String,
        tipo: String,
        token: String
    ){

        var hashMap : HashMap<String, String> = HashMap<String, String> ()
        hashMap.put("lat", lat)
        hashMap.put("lon", lon)
        hashMap.put("fecha", fecha)
        hashMap.put("hora", hora)
        hashMap.put("detalle", detalle)
        hashMap.put("tipo", tipo)
        hashMap.put("token", token)
        val jsonData = JSONObject(hashMap as Map<*, *>);
        val u= URL
        AndroidNetworking.post(u.InsertUbicacionH())// UPDATE LAT LOG
            .addJSONObjectBody(jsonData)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val respuesta = response.getString("respuesta")
                        if (respuesta == "200") {
                            val arrayUser = response.getJSONObject("datad")
                            val nombre = arrayUser.getString("nombre")
                            val arrayProductos = response.getJSONArray("data")
                            idh = response.getString("id")
                            Log.i("TAGG","VALIDA: "+idh)
                            for (i in 0 until arrayProductos.length()) {
                                val n = arrayProductos.getJSONObject(i).getString("telefono")
                                if (n != null) {
                                    val sms: SmsManager = SmsManager.getDefault()
                                    sms.sendTextMessage(
                                        n,
                                        null,
                                        "ALERTA EL USUARIO " + nombre + " ACTIVO EL SISTEMA DE EMERGENCIA \n" + "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon,
                                        null,
                                        null
                                    )
                                }
                            }

                            // val idusuario = arrayProductos.getString("idusuario")
                            Toast.makeText(
                                application,
                                "UBICACION ACTIVADA",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                application,
                                "No hay ningun dato disponible.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        //Toast.makeText(
                        //   this@MapsActivity,
                        //  "Error: " + e.message,
                        //  Toast.LENGTH_SHORT
                        //).show()
                    }

                }

                override fun onError(anError: ANError) {
                    Toast.makeText(
                        application,
                        "Error: " + anError.errorDetail,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    var idh:String = null.toString()
    private fun InserLocalizacion(
        lat: String,
        lon: String,
        fecha: String,
        hora: String,
        idh: String
    ){

        var hashMap : HashMap<String, String> = HashMap<String, String> ()
        hashMap.put("lat", lat)
        hashMap.put("lon", lon)
        hashMap.put("fecha", fecha)
        hashMap.put("hora", hora)

        hashMap.put("idhistorial", idh)
        val jsonData = JSONObject(hashMap as Map<*, *>);
        val u= URL
        AndroidNetworking.post(u.InsertUbicacion())// UPDATE LAT LOG
            .addJSONObjectBody(jsonData)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val respuesta = response.getString("respuesta")
                        if (respuesta == "200") {

                            Toast.makeText(
                                application,
                                "ALARMA ACTIVADA",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {
                            Toast.makeText(
                                application,
                                "No hay ningun dato disponible.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                            this@BleOperationsActivity,
                            "Error: " + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

                override fun onError(anError: ANError) {
                    Toast.makeText(
                        application,
                        "Error: " + anError.errorDetail,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    var ac= 1
    private fun getDeviceLocation(n1:Int) {

        val mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            mfusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    //lastLocation = location
                    val latitudOrigen = location.latitude
                    val longitudOrigen = location.longitude
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val date = Date()
                    val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
                    val hora = simpleDateFormat.format(date)
                    val fecha = dateFormat.format(date)
                    if(n1==0){
                        InserLocalizacionH(
                            latitudOrigen.toString(),
                            longitudOrigen.toString(),
                            fecha,
                            hora,
                            "ALARMA ACTIVADA",
                            "DESCONEXION DEL DISPOSITIVO",
                            id!!
                        )
                    }else{
                        InserLocalizacion(
                            latitudOrigen.toString(),
                            longitudOrigen.toString(),
                            fecha!!,
                            hora!!,
                            idh
                        )
                    }

                    Toast.makeText(
                        baseContext,
                        "Lat: " + latitudOrigen + "; Long: " + longitudOrigen,
                        Toast.LENGTH_LONG
                    ).show()




                }
            }
        } catch (e: SecurityException) {
            Log.e("ds", "getDeviceLocation: SecurityException: " + e.message)
        }
    }

}
