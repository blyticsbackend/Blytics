package com.nbt.blytics.modules.payee.payment

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowPaymentHistoryBinding
import com.nbt.blytics.modules.transactionhistory.TransactionResponse
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.setImage
import com.nbt.blytics.utils.show

/**
 * Created bynbton 14-10-2021
 */
class PaymentHistoryAdapter(
    val context: Context,
    val list: MutableList<TransactionResponse.Data.Txn>,
    val callback: (Int) -> Unit
) :
    RecyclerView.Adapter<PaymentHistoryAdapter.HistoryViewHolder>() {
    class HistoryViewHolder(val binding: RowPaymentHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater =    LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowPaymentHistoryBinding>(
            inflater, R.layout.row_payment_history, parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val data = list[position]
        holder.binding.txtUserName.text = data.userName
        Log.d("Pyament=", data.userImage)
        holder.binding.imgUser.setImage(R.drawable.dummy_user)
        if(data.userImage.isNotEmpty()) {
            holder.binding.imgUser.setImage(data.userImage)
        }
        if(data.isSelected){
            holder.binding.imgCheck.show()
        }else{
            holder.binding.imgCheck.hide()
        }

        holder.itemView.setOnClickListener {
            callback(position)
        }
    }
    override fun getItemCount(): Int = list.size
    interface ClickListener {
        fun onClick(view: View?, position: Int)
        fun onLongClick(view: View?, position: Int)
    }

}

