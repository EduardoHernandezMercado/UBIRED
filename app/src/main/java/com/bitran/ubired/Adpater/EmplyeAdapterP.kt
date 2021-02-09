package com.bitran.ubired

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.acos


class EmplyeAdapterP (context: Context, emps: List<Employe>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
 
       // eh.lbl_designation.setText(employe.emp_designation)
        eh.lbl_name.setText(employe.emp_name)
       // eh.lbl_salary.setText(employe.emp_salary)
        eh.img_emp.setImageResource(employe.emp_photo!!)
        eh.itemView.setOnClickListener {



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
}