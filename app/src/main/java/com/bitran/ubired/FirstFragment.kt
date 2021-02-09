package com.bitran.ubired

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.eduardohm.ingenmtryx.dika.Util.URL.URL_USUARIO
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import org.json.JSONException
import org.json.JSONObject

class FirstFragment : Fragment() {// Inicia sesion y valida si ya tiene cuenta activa

    var token:String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //FirebaseApp.initializeApp(activity);
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("TAG", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                token = task.result?.token.toString()
                Log.d("TAG", token)
                var builder =  AlertDialog.Builder(activity);
                    builder.setView(R.layout.progress);
                    var dialog = builder.create()
                   dialog.show();
                ValidaSesion(token,dialog)

            })
        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            //findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            val correo=view.findViewById<EditText>(R.id.editTextTextEmailAddress).text
            val pass=view.findViewById<EditText>(R.id.editTextTextPassword).text
            var builder =  AlertDialog.Builder(activity);
            builder.setView(R.layout.progress);
            var dialog = builder.create()
            dialog.show();
            Inicio(correo.toString(),token,pass.toString(),dialog)// Inicia sesion

        }
        view.findViewById<Button>(R.id.button_cambio).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

    }
    private fun ValidaSesion(token:String,v:Dialog){

        var hashMap : HashMap<String, String> = HashMap<String, String> ()
        hashMap.put("token",token)
        val jsonData = JSONObject(hashMap as Map<*, *>);
        val u= URL_USUARIO
        AndroidNetworking.post(u.GetUserValidaToken())// UPDATE LAT LOG
            .addJSONObjectBody(jsonData)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val respuesta = response.getString("respuesta")
                        if (respuesta == "200") {
                            val arrayProductos = response.getJSONObject ("data")
                            val idusuario = arrayProductos.getString("idusuario")
                            val intent = Intent(activity, MainActivity::class.java)/// Validacion correcta
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            val b = Bundle()
                            b.putString("idusuario", idusuario)
                            b.putString("token", token)
                            intent.putExtras(b)
                            startActivity(intent)
                            Toast.makeText(
                                activity,
                                "Validado.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            v.dismiss()
                            Toast.makeText(
                                activity,
                                "INICIA SESIÓN",
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
                        activity,
                        "Error: " + anError.errorDetail,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    private fun Inicio(correo:String,token:String,password:String,v:Dialog){

        var hashMap : HashMap<String, String> = HashMap<String, String> ()
        hashMap.put("token",token)
        hashMap.put("correo",correo)
        hashMap.put("password",password)
        val jsonData = JSONObject(hashMap as Map<*, *>);
        val u= URL_USUARIO
        AndroidNetworking.post(u.GetUserPassword())// UPDATE LAT LOG
            .addJSONObjectBody(jsonData)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val respuesta = response.getString("respuesta")
                        if (respuesta == "200") {
                            val arrayProductos = response.getJSONObject ("data")
                            val idusuario = arrayProductos.getString("idusuario")
                            val intent = Intent(activity, MainActivity::class.java)/// Validacion correcta desde correo y pass
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            val b = Bundle()
                            b.putString("idusuario", idusuario)
                            b.putString("token", token)
                            intent.putExtras(b)
                            startActivity(intent)
                            Toast.makeText(
                                activity,
                                "INICIO",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            v.dismiss()
                            Toast.makeText(
                                activity,
                                "VERIFIQUE SUS DATOS ERROR AL INICIAR SESION",
                                Toast.LENGTH_LONG
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
                        activity,
                        "Error: " + anError.errorDetail,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}