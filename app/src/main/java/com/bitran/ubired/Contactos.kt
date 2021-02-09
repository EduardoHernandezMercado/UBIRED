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
import kotlinx.android.synthetic.main.fragment_home.*
import org.json.JSONException
import org.json.JSONObject

class Contactos : AppCompatActivity() {

    var id:String= null.toString()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactos)
        setSupportActionBar(findViewById(R.id.toolbar))
        id=intent.getStringExtra("idusuario")
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Intent(this@Contactos, Busqueda::class.java).also {////Inicia la conexion
                it.putExtra("idusuario", id!!)
                startActivity(it)
            }
        }

        GetFamilia(id)
    }
    private fun GetFamilia(idusuario: String){

    var hashMap : HashMap<String, String> = HashMap<String, String> ()
    hashMap.put("idusuario", idusuario)
    val jsonData = JSONObject(hashMap as Map<*, *>);
    val u= URL_USUARIO
    AndroidNetworking.post(u.GetFamilia())// UPDATE LAT LOG
        .addJSONObjectBody(jsonData)
        .setPriority(Priority.MEDIUM)
        .build()
        .getAsJSONObject(object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                try {
                    val respuesta = response.getString("respuesta")
                    if (respuesta == "200") {
                        val arrayProductos = response.getJSONArray("data")
                        val arrayD = response.getJSONArray("datad")
                        var empList = ArrayList<Employe>()
                        var u=Employe()
                        for (i in 0 until arrayProductos.length()) {
                            u.emp_id = arrayProductos.getJSONObject(i).getString("id")
                            u.emp_idfamilia = arrayD.getJSONObject(i).getString("idfamilia")
                            u.emp_name= arrayProductos.getJSONObject(i).getString("nombre")
                            u.emp_photo=R.drawable.ic_baseline_account_circle_24
                            empList?.add(u)
                            u=Employe()
                        }

                        my_recycler_view.apply {
                            layoutManager = LinearLayoutManager(this@Contactos)
                            adapter = this@Contactos?.let { empList?.let { it1 -> EmplyeAdapter(it, it1) } }!!
                        }

                        Toast.makeText(
                            this@Contactos,
                            "Token actualizado. ",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@Contactos,
                            "No hay ningun dato disponible.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: JSONException) {
                }

            }

            override fun onError(anError: ANError) {
                Toast.makeText(
                    this@Contactos,
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
            builder.setTitle("ACCIONES")
            builder.setMessage("SELECIONE UNA OPCION:")
            builder.setPositiveButton("VER UBICACION") { dialog, which ->
                val i= Intent(context, MapsActivity::class.java)
                val b = Bundle()
                b.putString("idusuario", employe.emp_id)
                i.putExtras(b)
                context.startActivity(i)
            }
            builder.setNeutralButton("ELIMINAR") { dialog, which ->
                DeleteFamilia(employe.emp_idfamilia!!)
                Toast.makeText(context,
                    "ELIMINADO", Toast.LENGTH_SHORT).show()
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
    private fun DeleteFamilia(idFamilia: String){

        var hashMap : HashMap<String, String> = HashMap<String, String> ()
        hashMap.put("idsolicitud",idFamilia)
        val jsonData = JSONObject(hashMap as Map<*, *>);
        val u= URL_USUARIO
        AndroidNetworking.post(u.DeleteUserFamilia())// Insert user
            .addJSONObjectBody(jsonData)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val respuesta = response.getString("respuesta")
                        if (respuesta == "200") {

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