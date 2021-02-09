package com.bitran.ubired

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.eduardohm.ingenmtryx.dika.Util.URL.URL_USUARIO
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_busqueda.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class Busqueda : AppCompatActivity() {

    var id: String? =null
    var recyclerView: RecyclerView? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_busqueda)
        setSupportActionBar(findViewById(R.id.toolbar))
        id=intent.getStringExtra("idusuario")
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Buscando", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            Buscar(editTextTextEmailAddressBusqueda.text.toString(),id!!)
        }
        buttonBuscar.setOnClickListener {
            Buscar(editTextTextEmailAddressBusqueda.text.toString(),id!!)
        }



    }
    //Encuentra user registrado
    private fun Buscar(correo: String,id:String){

        var hashMap : HashMap<String, String> = HashMap<String, String> ()
        hashMap.put("correo", correo)
        val jsonData = JSONObject(hashMap as Map<*, *>);
        val u= URL_USUARIO
        AndroidNetworking.post(u.GetUsuario())// UPDATE LAT LOG
            .addJSONObjectBody(jsonData)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val respuesta = response.getString("respuesta")
                        if (respuesta == "200") {
                            val arrayProductos = response.getJSONArray("data")
                            var empList = ArrayList<Employe>()
                            var u = Employe()
                            for (i in 0 until arrayProductos.length()) {
                                u.emp_id = arrayProductos.getJSONObject(i).getString("idusuario")
                                u.emp_name = arrayProductos.getJSONObject(i).getString("nombre")
                                u.emp_idotro = id
                                u.emp_photo = R.drawable.ic_baseline_account_circle_24
                                empList?.add(u)
                                u = Employe()
                            }

                            Toast.makeText(
                                this@Busqueda,
                                "Token actualizado. ",
                                Toast.LENGTH_LONG
                            ).show()

                            // val llm = LinearLayoutManager(this@Busqueda)
                            // llm.orientation = LinearLayoutManager.VERTICAL
                            // list.setLayoutManager(llm)
                            // list.setAdapter(adapter)
                          //  recyclerView = findViewById<View>(R.id.my_recycler_viewB) as RecyclerView
                          //  val manager = LinearLayoutManager(this@Busqueda)
                          //  recyclerView!!.setLayoutManager(manager)
                           // recyclerView!!.setHasFixedSize(true)
                           // recyclerView!!.setAdapter(EmplyeAdapter(this@Busqueda,empList))

                            my_recycler_viewB.apply {
                                layoutManager = LinearLayoutManager(this@Busqueda)
                                adapter = EmplyeAdapter(this@Busqueda, empList)

                            }


                        } else {
                            Toast.makeText(
                                this@Busqueda,
                                "No hay ningun dato disponible.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                            this@Busqueda,
                            "No hay ningun dato disponible."+e,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

                override fun onError(anError: ANError) {
                    Toast.makeText(
                        this@Busqueda,
                        "Error: " + anError.errorDetail,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    class EmplyeAdapter(context: Context, emps: List<Employe>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var context: Context
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
                builder.setMessage("¿ENVIAR UNA LA SOLICITUD DE AMISTAD?")
                builder.setPositiveButton("ENVIAR") { dialog, which ->
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val date = Date()
                    val fecha = dateFormat.format(date)
                    InsertSolicitud(fecha, employe.emp_id.toString(), employe.emp_idotro!!)
                }
                builder.setNegativeButton("CANCELAR") { dialog, which ->
                    Toast.makeText(context,
                        android.R.string.no, Toast.LENGTH_SHORT).show()
                }

                builder.show()


            }
        }
        override fun getItemCount(): Int {
            if (employes != null)
                return employes.size
            return 0;
            //return employes.size
        }

        internal class EmployeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var lbl_name: TextView
            var img_emp: ImageView



            init {
                lbl_name = itemView.findViewById(R.id.textGuardia) as TextView
                img_emp = itemView.findViewById(R.id.imageEstadoGuardia) as ImageView

            }
        }
        private fun InsertSolicitud(fecha: String, idusuario1: String, idusuario2: String){

            var hashMap : HashMap<String, String> = HashMap<String, String> ()
            hashMap.put("fecha", fecha)
            hashMap.put("idusuario1", idusuario2)
            hashMap.put("idusuario2", idusuario1)
            val jsonData = JSONObject(hashMap as Map<*, *>);
            val u= URL_USUARIO
            AndroidNetworking.post(u.InsertSolicitud())// UPDATE LAT LOG
                .addJSONObjectBody(jsonData)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        try {
                            val respuesta = response.getString("respuesta")
                            if (respuesta == "200") {
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("SOLICITUD DE AMISTAD")
                                builder.setMessage("SE ENVIO LA SOLICITUD DE AMISTAD")
                                builder.setPositiveButton("ACEPTAR") { dialog, which ->
                                }
                                builder.show()
                                val i= Intent(context, MainActivity2::class.java)
                                val b = Bundle()
                                b.putString("idusuario", idusuario2)
                                i.putExtras(b)
                                context.startActivity(i)
                                Toast.makeText(
                                    context,
                                    "Solicitud Enviada ",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "No hay ningun dato disponible.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: JSONException) {
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