package com.bitran.ubired

import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONException
import org.json.JSONObject

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    var marcador: Marker?=null
    var miPosicion:LatLng? = null
    var im: Bitmap?=null
    var idusuario: String? =null
    val mainHandler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        idusuario=intent.getStringExtra("idusuario")
        Glide.with(baseContext)
            .asBitmap()
            .load(URL.GetImagenMarca())
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    im= getRoundedCornerBitmap(resource, 120)?.let { Bitmap.createScaledBitmap(it, 120, 120, false) }
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        setUpMapS()
        mainHandler.post(object : Runnable {
            override fun run() {
                GetUbucacion(idusuario!!)
                mainHandler.postDelayed(this, 10000)/// 5 segundos
              //  if(ac==0)
                //    mainHandler.removeCallbacks(this)
            }
        })


    }
    private fun setUpMapS() {
        //Analiza el permisos de acceso a la ubicación
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                212121
            )
            return
        }
        getDeviceLocation()
        mMap.isMyLocationEnabled = true //Marcador de ubicación actual
        //mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN //Tipo de mapa
        mMap.uiSettings.isMyLocationButtonEnabled = false

        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {

            override fun onMarkerDragStart(marker: Marker) {
                // TODO Auto-generated method stub
                //Here your code
            }

            override fun onMarkerDragEnd(marker: Marker) {
                // TODO Auto-generated method stub
                // UpdateMarket(marker.position.latitude.toString(),marker.position.longitude.toString(),"1")
                //   AgregaMarcadorA(marker.position.latitude,marker.position.longitude,"UBITATION",im!!,"DIRECCION")

                // Toast.makeText(baseContext, "Lat: " + mMap.cameraPosition.target.latitude + "; Long: " + mMap.cameraPosition.target.longitude, Toast.LENGTH_LONG).show()

            }

            override fun onMarkerDrag(marker: Marker) {
                // TODO Auto-generated method stub

            }
        })


    }

    private fun getDeviceLocation() {
        Log.d("MAP--", "getDeviceLocation: obteniendo la ubicación actual de los dispositivos")
        val mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            mfusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    //lastLocation = location
                    val latitudOrigen = location.latitude
                    val longitudOrigen = location.longitude
                    miPosicion = LatLng(latitudOrigen, longitudOrigen)
                    //    AgregaMarcadorA(location.latitude,location.longitude,"UBITATION","DIRECCION")
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miPosicion, 17.0f))
                   // UpdateMarket(miPosicion!!.latitude.toString(),miPosicion!!.longitude.toString(),"1")
                    Toast.makeText(
                        baseContext,
                        "Lat: " + miPosicion!!.latitude + "; Long: " + miPosicion!!.longitude,
                        Toast.LENGTH_LONG
                    ).show()

                }
            }
        } catch (e: SecurityException) {
            Log.e("ds", "getDeviceLocation: SecurityException: " + e.message)
        }
    }
    private fun AgregaMarcadorA(lat: Double, log: Double, alarma: String, name: String,fecha:String) {

        val info = InfoWindowData(
            alarma, name,
            fecha,
            im!!
        )
        val cordenadas = LatLng(lat, log)
        val Ubicacion = CameraUpdateFactory.newLatLngZoom(cordenadas, 16f)
        val customInfoWindow = CustomInfoWindowGoogleMap(this)

         mMap!!.setInfoWindowAdapter(customInfoWindow)
        if (marcador != null)
            marcador!!.remove()
        marcador = mMap.addMarker(
            MarkerOptions()
                .position(cordenadas)
                .title(name)
                .icon(BitmapDescriptorFactory.fromBitmap(im))

        )
        marcador!!.setTag(0)
        marcador!!.tag = info
        marcador!!.showInfoWindow()
        mMap.animateCamera(Ubicacion)
       // val sydney = LatLng(lat!!.toDouble(), log!!.toDouble())
       // mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(cordenadas))


    }
    private fun GetUbucacion(id: String){

        var hashMap : HashMap<String, String> = HashMap<String, String> ()
        hashMap.put("idusuario", id)
        val jsonData = JSONObject(hashMap as Map<*, *>);
        val u= URL
        AndroidNetworking.post(u.GetUbicacion())// UPDATE LAT LOG
            .addJSONObjectBody(jsonData)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val respuesta = response.getString("respuesta")
                        if (respuesta == "200") {
                            val arrayProductos = response.getJSONObject("data")
                            val arrayUser = response.getJSONObject("datad")
                            val lat: String = arrayProductos.getString("lat").toString()
                            val lon: String = arrayProductos.getString("lon").toString()
                            val hora: String = arrayProductos.getString("hora").toString()
                            val fecha: String = arrayProductos.getString("fecha").toString()
                            val alarma: String = arrayProductos.getString("detalle").toString()
                            val name: String = arrayUser.getString("nombre").toString()
                            AgregaMarcadorA(lat.toDouble(), lon.toDouble(), alarma, name,hora+" "+fecha)
                            Toast.makeText(
                                this@MapsActivity,
                                "Validado.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MapsActivity,
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
                        this@MapsActivity,
                        "Error: " + anError.errorDetail,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    private fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Int): Bitmap? {
        val output = Bitmap.createBitmap(
            bitmap.width, bitmap
                .height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(
            0, 0, bitmap.width,
            bitmap.height
        )
        val rectF = RectF(rect)
        paint.setAntiAlias(true)
        canvas.drawARGB(0, 0, 0, 0)
        paint.setColor(color)
        canvas.drawRoundRect(rectF, pixels.toFloat(), pixels.toFloat(), paint)
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

}