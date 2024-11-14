package com.nbt.blytics.modules.transactiondetails.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowTransactionDetailsBinding
import com.nbt.blytics.modules.transactiondetails.models.TranscationDetailsModel

/**
 * Created bynbton 26-06-2021
 */
class TransactionDetailsAdapter(
    val context: Context,
    val list: MutableList<TranscationDetailsModel>,val callback:(Int,Int)->Unit
) :
    RecyclerView.Adapter<TransactionDetailsAdapter.TransactionDetailsViewHolder>() {
    class TransactionDetailsViewHolder(val binding: RowTransactionDetailsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionDetailsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowTransactionDetailsBinding>(
            inflater, R.layout.row_transaction_details, parent, false
        )
        return TransactionDetailsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionDetailsViewHolder, position: Int) {
        val data = list[position]

        holder.binding.txtTransactionDay.text = data.day

        val adapter = SubTrasactionDataAdapter(context,data.list){
            callback(position,it)
        }
        holder.binding.rvSubTransaction.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        holder.binding.rvSubTransaction.adapter = adapter



    }

    override fun getItemCount(): Int = list.size


}