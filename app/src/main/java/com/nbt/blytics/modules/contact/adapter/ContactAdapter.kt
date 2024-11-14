package com.nbt.blytics.modules.contact.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowContactBinding
import com.nbt.blytics.modules.contact.model.ContactData

/**
 * Created bynbton 26-10-2021
 */

class ContactAdapter(val context: Context, val list: MutableList<ContactData>, val callback: (Int) -> Unit) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {
    class ContactViewHolder(val binding: RowContactBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowContactBinding>(inflater, R.layout.row_contact, parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val data = list[position]
        holder.binding.apply {
            txtName.text = data.name
            if (data.phoneNumber.isNotEmpty()) txtContact.text = data.phoneNumber[0].replace(" ", "")
            else txtContact.text = ""
        }
        holder.itemView.setOnClickListener {
            callback(position)
        }
    }
    override fun getItemCount(): Int = list.size
}