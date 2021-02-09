
package com.bitran.ubired

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
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
import com.bitran.ubired.ble.ConnectionEventListener
import com.bitran.ubired.ble.ConnectionManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2
var mainHandler = Handler(Looper.getMainLooper())
@Suppress("DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {

    /*******************************************
     * Properties
     *******************************************/

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private var isScanning = false
        set(value) {
            field = value
            runOnUiThread { scan_button.text = if (value) "Stop Scan" else "Start Scan" }
        }

    private val scanResults = mutableListOf<ScanResult>()
    private val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults) { result ->
            if (isScanning) {
                stopBleScan()
            }
            with(result.device) {
                Timber.w("Connecting to $address")
                ConnectionManager.connect(this, this@MainActivity)
            }
        }
    }

    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    /*******************************************
     * Activity function overrides
     *******************************************/

    var id:String= null.toString()
    var i=0
    val REQUEST_CODE = 10101
    var PwrKeyShortPress=true
    override fun onCreate(savedInstanceState: Bundle?) {///////////////////////////////////////////////////////////////////////////////////////////
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->

            mainHandler.post(object : Runnable {
                override fun run() {

                    getDeviceLocation(i)
                    i++
                    mainHandler.postDelayed(this, 10000)/// 10 segundos
                    if (ac == 0)
                        mainHandler.removeCallbacks(this)
                }
            })
        }
        findViewById<FloatingActionButton>(R.id.fabC).setOnClickListener { view ->
            ac=0
            i=0
            mainHandler = Handler(Looper.getMainLooper())
        }
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("TAG", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                id = task.result?.token.toString()
                ac = 1

            })
        if (checkDrawOverlayPermission()) {
            //startService(Intent(this, PowerButtonService::class.java))
        }

        checkSMSStatePermissionA()

        scan_button.setOnClickListener { if (isScanning) stopBleScan() else startBleScan() }

        button_admin.setOnClickListener {
            Intent(this@MainActivity, MainActivity2::class.java).also {////Inicia la conexion
                it.putExtra("idusuario", intent.getStringExtra("idusuario"))
                startActivity(it)
            }
        }


        setupRecyclerView()

    }


    fun checkDrawOverlayPermission(): Boolean {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        return if (!Settings.canDrawOverlays(this)) {
            /** if not construct intent to request permission  */
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            /** request permission via start activity for result  */
            startActivityForResult(intent, REQUEST_CODE)
            false
        } else {
            true
        }
    }








    override fun onResume() {
        super.onResume()
        ConnectionManager.registerListener(connectionEventListener)
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }
    override fun onBackPressed() {
       // Log.d("CDA", "onBackPressed Called")
        val setIntent = Intent(Intent.ACTION_MAIN)
        setIntent.addCategory(Intent.CATEGORY_HOME)
        setIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(setIntent)
    }
    @TargetApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
               // startService(Intent(this, PowerButtonService::class.java))
            }
        }

        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    startBleScan()
                }
            }
        }
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

    override fun onPause() {
        super.onPause()

    }

    /*******************************************
     * Private functions
     *******************************************/

    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    private fun startBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            scanResults.clear()
            scanResultAdapter.notifyDataSetChanged()
            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
        }
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }
        runOnUiThread {
            alert {
                title = "Permiso"
                message = "El sistema requiere que se otorguen a la aplicacion acceso a la ubicación para buscar dispositivos BLE"
                isCancelable = false
                positiveButton(android.R.string.ok) {
                    requestPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            }.show()
        }
    }

    private fun setupRecyclerView() {
        scan_results_recycler_view.apply {
            adapter = scanResultAdapter
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = scan_results_recycler_view.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    /*******************************************
     * Callback bodies
     *******************************************/

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                with(result.device) {
                    Timber.i("Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                }
                scanResults.add(result)
                scanResultAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Timber.e("onScanFailed: code $errorCode")
        }
    }

    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onConnectionSetupComplete = { gatt ->
                Intent(this@MainActivity, BleOperationsActivity::class.java).also {////Inicia la conexion
                    it.putExtra("id", "2")
                    it.putExtra(BluetoothDevice.EXTRA_DEVICE, gatt.device)
                    startActivity(it)

                }
                ConnectionManager.unregisterListener(this)
            }
            onDisconnect = {
                runOnUiThread {
                    alert {
                        title = "Disconnected"
                        message = "Disconnected or unable to connect to device."
                        positiveButton("OK") {}
                    }.show()
                }
            }
        }
    }

    /*******************************************
     * Extension functions
     *******************************************/

    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }

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
                            Log.i("TAGG", "VALIDA: " + idh)
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
                        Log.i("TAAAA", "ACOMMM-----")
                        val respuesta = response.getString("respuesta")
                        if (respuesta == "200") {

                            Log.i("TAAAA", "BCOMMM-----")
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
                            this@MainActivity,
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
    private fun getDeviceLocation(n1: Int) {

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
                            "ACTIVACION DE BONTON DE PANICO",
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
