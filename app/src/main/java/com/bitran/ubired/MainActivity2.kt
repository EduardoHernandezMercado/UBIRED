package com.bitran.ubired

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.eduardohm.ingenmtryx.dika.Util.URL.URL_USUARIO
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_main2.*
import org.json.JSONException
import org.json.JSONObject


class MainActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "UBI RED", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        Glide.with(baseContext)
            .asBitmap()
            .load(URL.GetImagenMarca())
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imagePerfil.setImageBitmap(resource?.let { Bitmap.createScaledBitmap(it, 280, 280, false) })
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
        var id = intent.getStringExtra("idusuario")
        button_familia.setOnClickListener {
            Intent(this@MainActivity2, Contactos::class.java).also {////Inicia la conexion
                it.putExtra("idusuario", id!!)
                startActivity(it)
            }
        }
        button_buscar.setOnClickListener {
            Intent(this@MainActivity2, Busqueda::class.java).also {////Inicia la conexion
                it.putExtra("idusuario", id!!)
                startActivity(it)
            }
        }
        button_solicitud.setOnClickListener {
            Intent(this@MainActivity2, Solicitudes::class.java).also {////Inicia la conexion
                it.putExtra("idusuario", id!!)
                startActivity(it)
            }
        }
        button_csesion.setOnClickListener {

            Cerrar(id)
        }
    }

    private fun Cerrar(idusuario: String) {

        var hashMap: HashMap<String, String> = HashMap<String, String>()
        hashMap.put("idusuario", idusuario)
        val jsonData = JSONObject(hashMap as Map<*, *>);
        val u = URL_USUARIO
        AndroidNetworking.post(u.DeleteTokenUser())// UPDATE LAT LOG
            .addJSONObjectBody(jsonData)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val respuesta = response.getString("respuesta")
                        if (respuesta == "200") {
                            f()
                            Toast.makeText(
                                this@MainActivity2,
                                "Token actualizado. ",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MainActivity2,
                                "No hay ningun dato disponible.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                            this@MainActivity2,
                            "No hay ningun dato disponible." + e,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

                override fun onError(anError: ANError) {
                    Toast.makeText(
                        this@MainActivity2,
                        "Error: " + anError.errorDetail,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    fun f() {
        Intent(this@MainActivity2, Inicio::class.java).also {////Inicia la conexion
            startActivity(it)
            finish()
        }
    }
}

