package com.nbt.blytics.modules.payee.payment

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowPaymentHistoryBinding
import com.nbt.blytics.utils.setImage

class RequestMoneyAdapter(
    val context: Context,
    val list: MutableList<RequestMoneyResponse.Data>,
    val callback: (Int) -> Unit
) :
    RecyclerView.Adapter<RequestMoneyAdapter.RequestViewHolder>() {
    class RequestViewHolder(val binding: RowPaymentHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowPaymentHistoryBinding>(
            inflater, R.layout.row_payment_history, parent, false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val data = list[position]
        holder.binding.txtUserName.text = data.requestByUserName
        holder.binding.imgUser.setImage(R.drawable.dummy_user)
        if (data.imageUrl.isNotEmpty()) {
            holder.binding.imgUser.setImage(data.imageUrl)
        }
        holder.itemView.setOnClickListener {
            callback(position)
        }
    }

    override fun getItemCount(): Int = list.size

}