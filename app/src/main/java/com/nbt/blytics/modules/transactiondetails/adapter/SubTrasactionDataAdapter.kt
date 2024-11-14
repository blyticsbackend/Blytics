package com.nbt.blytics.modules.transactiondetails.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowTransactionSubDetailsBinding
import com.nbt.blytics.modules.transactiondetails.models.TranscationDetailsModel

/**
 * Created bynbton 26-06-2021
 */
class SubTrasactionDataAdapter(val context: Context,val list: MutableList<TranscationDetailsModel.Details>,val callback:(Int)->Unit) :
    RecyclerView.Adapter<SubTrasactionDataAdapter.SubDataViewHolder>() {
    class SubDataViewHolder (val binding: RowTransactionSubDetailsBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubDataViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowTransactionSubDetailsBinding>(
            inflater, R.layout.row_transaction_sub_details, parent, false
        )
        return SubDataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubDataViewHolder, position: Int) {
        val data = list[position]
        holder.binding.status.text = data.status
        holder.binding.amount.text =data.amount
        holder.binding.date.text = data.date

        if(data.status=="Received"){
            holder.binding.status.setTextColor(context.resources.getColor(R.color.teal_700))
        }else{
            holder.binding.status.setTextColor(context.resources.getColor(R.color.light_red))
        }
        holder.itemView.setOnClickListener{1
            callback(position)
        }

    }

    override fun getItemCount(): Int  = list.size


}