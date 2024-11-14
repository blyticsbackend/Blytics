package com.nbt.blytics.modules.singletransaction

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowPaymentHistoryBinding
import com.nbt.blytics.databinding.RowSingleTransactionHistoryBinding

/**
 * Created bynbton 19-10-2021
 */
/*
class TransactionAdapter (val context: Context, val list: MutableList<TransactionModel>, val callback:(Int)->Unit) :
    RecyclerView.Adapter<TransactionAdapter.HistoryViewHolder>() {
    class HistoryViewHolder (val binding: RowSingleTransactionHistoryBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowSingleTransactionHistoryBinding>(
            inflater, R.layout.row_single_transaction_history, parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val data = list[position]
        holder.binding.status.text = data.status
        holder.binding.amount.text =data.amount
        holder.binding.date.text = data.date

        if(data.status=="Received"){
            holder.binding.status.setTextColor(context.resources.getColor(R.color.teal_700))
        }else{
            holder.binding.status.setTextColor(context.resources.getColor(R.color.light_red))
        }
        holder.itemView.setOnClickListener{
            callback(position)
        }

    }

    override fun getItemCount(): Int  = list.size


}*/
