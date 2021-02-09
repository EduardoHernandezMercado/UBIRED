package com.bitran.ubired

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.eduardohm.ingenmtryx.dika.Util.URL.URL_USUARIO
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import org.json.JSONException
import org.json.JSONObject

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    var token:String = ""
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_inicio).setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        view.findViewById<Button>(R.id.button_registro).setOnClickListener {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w("TAG", "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }
                    token = task.result?.token.toString()
                    Log.w("TAG", "getInstanceId failed "+token)
                    val correo=view.findViewById<EditText>(R.id.editTextTextEmailAddress2).text
                    val pass=view.findViewById<EditText>(R.id.editTextTextPassword2).text
                    val nombre=view.findViewById<EditText>(R.id.editTextTextPersonName).text
                    val telefono=view.findViewById<EditText>(R.id.editTextPhone).text
                    Registro(correo.toString(),token,pass.toString(),nombre.toString(),telefono.toString())
                })
        }
    }
    private fun Registro(correo:String,token:String,password:String,nombre:String,telefono:String){

        var hashMap : HashMap<String, String> = HashMap<String, String> ()
        hashMap.put("token",token)
        hashMap.put("nombre",nombre)
        hashMap.put("correo",correo)
        hashMap.put("password",password)
        hashMap.put("telefono",telefono)
        val jsonData = JSONObject(hashMap as Map<*, *>);
        val u= URL_USUARIO
        AndroidNetworking.post(u.InsertUser())// Insert user
            .addJSONObjectBody(jsonData)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val respuesta = response.getString("respuesta")
                        if (respuesta == "200") {

                            val idusuario = response.getString("data")
                            val intent = Intent(activity, MainActivity::class.java)/// Validacion correcta desde correo y pass
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            val b = Bundle()
                            b.putString("idusuario", idusuario)
                            b.putString("token", token)
                            intent.putExtras(b)
                            startActivity(intent)
                            Toast.makeText(
                                activity,
                                "Registro exitoso",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                activity,
                                "Error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                          activity,
                         "Error: " + e.message,
                          Toast.LENGTH_SHORT
                        ).show()
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