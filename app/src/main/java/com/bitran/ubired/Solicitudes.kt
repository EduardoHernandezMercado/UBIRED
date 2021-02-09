package com.bitran.ubired

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.eduardohm.ingenmtryx.dika.Util.URL.URL_USUARIO
import kotlinx.android.synthetic.main.content_solicitudes.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Solicitudes : AppCompatActivity() {

    var id: String? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitudes)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "SOLICITUDES DISPONIBLES", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        id=intent.getStringExtra("idusuario")
        GetSolicitudes(id!!)
    }
    private fun GetSolicitudes(idusuario: String){

        var hashMap : HashMap<String, String> = HashMap<String, String> ()
        hashMap.put("idusuario", idusuario)
        val jsonData = JSONObject(hashMap as Map<*, *>);
        val u= URL_USUARIO
        AndroidNetworking.post(u.GetSolicitud())// UPDATE LAT LOG
            .addJSONObjectBody(jsonData)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val respuesta = response.getString("respuesta")
                        if (respuesta == "200") {
                            val arrayProductos = response.getJSONArray("data")
                            val arrayS = response.getJSONArray("datad")
                            var empList = ArrayList<Employe>()
                            var u=Employe()
                            for (i in 0 until arrayProductos.length()) {
                                u.emp_id = arrayProductos.getJSONObject(i).getString("id")
                                u.emp_idsolitud = arrayS.getJSONObject(i).getString("idsolicitud")
                                u.emp_name= arrayProductos.getJSONObject(i).getString("nombre")
                                u.emp_photo=R.drawable.ic_baseline_account_circle_24
                                empList?.add(u)
                                u=Employe()
                            }

                            my_recycler_view.apply {
                                layoutManager = LinearLayoutManager(this@Solicitudes)
                                adapter = EmplyeAdapter(this@Solicitudes,empList,idusuario)
                            }

                            Toast.makeText(
                                this@Solicitudes,
                                "Token actualizado. ",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this@Solicitudes,
                                "No hay ningun dato disponible.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                    }

                }

                override fun onError(anError: ANError) {
                    Toast.makeText(
                        this@Solicitudes,
                        "Error: " + anError.errorDetail,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    class EmplyeAdapter(context: Context, emps: List<Employe>,idusuario:String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var context: Context
        var id=idusuario
        var employes: List<Employe>
        var TAG = "EmpAdapter"
        init {
            this.context = context
            this.employes = emps
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(context)
            return EmployeHolder(inflater.inflate(R.layout.my_text, parent, false))

        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val employe = employes[position]
            val eh = holder as EmployeHolder

            eh.lbl_name.setText(employe.emp_name)
            eh.img_emp.setImageResource(employe.emp_photo!!)
            eh.itemView.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("SOLICITUD DE AMISTAD")
                builder.setMessage("¿ACEPTAR LA SOLICITUD?")
                builder.setPositiveButton("ACEPTAR") { dialog, which ->
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val date = Date()
                    val fecha = dateFormat.format(date)
                    RegistroFamilia(fecha,"1",id,employe.emp_id!!,employe.emp_idsolitud!!)
                    Toast.makeText(context,
                        android.R.string.yes, Toast.LENGTH_SHORT).show()
                }
                builder.setNeutralButton("ELIMINAR") { dialog, which ->
                    DeleteSolicitud(employe.emp_idsolitud!!,id)
                    Toast.makeText(context,
                        "ELIMINADA", Toast.LENGTH_SHORT).show()
                }
                builder.setNegativeButton("CANCELAR") { dialog, which ->
                    Toast.makeText(context,
                        android.R.string.no, Toast.LENGTH_SHORT).show()
                }

                builder.show()

            }
        }
        override fun getItemCount(): Int {
            return employes.size
        }

        internal class EmployeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var lbl_name: TextView
            var img_emp: ImageView



            init {
                lbl_name = itemView.findViewById(R.id.textGuardia) as TextView
                img_emp = itemView.findViewById(R.id.imageEstadoGuardia) as ImageView

            }
        }
        private fun RegistroFamilia(fecha:String,permiso:String,idusuario1:String,idusuario2:String,idsolicitud:String){

            var hashMap : HashMap<String, String> = HashMap<String, String> ()
            hashMap.put("fecha",fecha)
            hashMap.put("permiso",permiso)
            hashMap.put("idusuario1",idusuario1)
            hashMap.put("idusuario2",idusuario2)
            hashMap.put("idsolicitud",idsolicitud)
            val jsonData = JSONObject(hashMap as Map<*, *>);
            val u= URL_USUARIO
            AndroidNetworking.post(u.InsertFamilia())// Insert user
                .addJSONObjectBody(jsonData)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        try {
                            val respuesta = response.getString("respuesta")
                            if (respuesta == "200") {
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("REGISTRO EXITOSO")
                                builder.setMessage("SE A AGREGADO LA LISTA DE FAMILIA")
                                builder.setPositiveButton("ACEPTAR") { dialog, which ->
                                }
                                builder.show()
                                val i= Intent(context, MainActivity2::class.java)
                                val b = Bundle()
                                b.putString("idusuario", id)
                                i.putExtras(b)
                                context.startActivity(i)
                                Toast.makeText(
                                    context,
                                    "Registro exitoso",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: JSONException) {
                            Toast.makeText(
                                context,
                                "Error: " + e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }

                    override fun onError(anError: ANError) {
                        Toast.makeText(
                            context,
                            "Error: " + anError.errorDetail,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
        private fun DeleteSolicitud(idsolicitud: String,id:String){

            var hashMap : HashMap<String, String> = HashMap<String, String> ()
            hashMap.put("idsolicitud",idsolicitud)
            val jsonData = JSONObject(hashMap as Map<*, *>);
            val u= URL_USUARIO
            AndroidNetworking.post(u.DeleteUserSolicitud())// Insert user
                .addJSONObjectBody(jsonData)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        try {
                            val respuesta = response.getString("respuesta")
                            if (respuesta == "200") {
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("ELIMINACION")
                                builder.setMessage("SE ELIMINO DE LA LISTA DE FAMILIA")
                                builder.setPositiveButton("ACEPTAR") { dialog, which ->
                                }
                                builder.show()
                                val i= Intent(context, MainActivity2::class.java)
                                val b = Bundle()
                                b.putString("idusuario", id)
                                i.putExtras(b)
                                context.startActivity(i)
                                Toast.makeText(
                                    context,
                                    "Eliminado Exitoso",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: JSONException) {
                            Toast.makeText(
                                context,
                                "Error: " + e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }

                    override fun onError(anError: ANError) {
                        Toast.makeText(
                            context,
                            "Error: " + anError.errorDetail,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}