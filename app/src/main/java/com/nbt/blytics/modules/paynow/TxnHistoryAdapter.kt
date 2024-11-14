package com.nbt.blytics.modules.paynow

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowTxnBinding
import com.nbt.blytics.modules.transactionhistory.TransactionResponse
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.setImage
import com.nbt.blytics.utils.show

/**
 * Created bynbton 11-11-2021
 */
class TxnHistoryAdapter (val context: Context, val list:MutableList<TransactionResponse.Data.Txn>, val callback:(Int)->Unit):
    RecyclerView.Adapter<TxnHistoryAdapter.TxnViewHolder>() {
    class TxnViewHolder (val binding :RowTxnBinding):RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TxnViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowTxnBinding>(
            inflater, R.layout.row_txn, parent, false
        )
        return TxnViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TxnViewHolder, position: Int) {
        val data = list[position]

        var counter =0
        for(i in list.indices){
            if(list[i].isSelected){
                counter+=1
            }

        }
        if(counter>2){
            list[position].isSelected= false
            Toast.makeText(context, "maximum 2 payees.", Toast.LENGTH_SHORT).show()
        }
        holder.binding.apply {

            if(data.isSelected){

                holder.binding.imgCheck.show()
    ;
            }else{
                holder.binding.imgCheck.hide()
            }


            if(data.userImage.isNotBlank()) {
                txtShort.setImage(data.userImage)
            }else{
                txtShort.setImage(R.drawable.dummy_user)
            }
            txtName.text = data.userName
            holder.itemView.setOnClickListener {
                callback(position)
            }
        }
    }


    override fun getItemCount(): Int =list.size
}