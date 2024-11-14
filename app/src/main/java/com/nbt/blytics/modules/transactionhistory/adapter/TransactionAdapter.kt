package com.nbt.blytics.modules.transactionhistory.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowSingleTransactionHistoryBinding
import com.nbt.blytics.modules.transactionhistory.TransactionResponse
import com.nbt.blytics.utils.*

/**
 * Created bynbton 30-10-2021
 */
class TransactionAdapter(
    val context: Context,
    val list: MutableList<TransactionResponse.Data.Txn>,
    val isForSingleTxn: Boolean = false,
    val callback: (Int) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    class TransactionViewHolder(val binding: RowSingleTransactionHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowSingleTransactionHistoryBinding>(
            inflater, R.layout.row_single_transaction_history, parent, false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val data = list[position]
        //Glide.with(context).clear(holder.binding.imgUser)
        holder.binding.apply {
            imgUser.setImage(R.drawable.dummy_user)
            if (data.userImage.isNotEmpty()) {
                imgUser.setImage(data.userImage)
            }
            txtUserName.text = data.userName
            amount.text = "${Constants.DEFAULT_CURRENCY} ${data.amount}"
            status.text = data.type
            date.text = UtilityHelper.txnDateFormat(data.date)
            time.text = UtilityHelper.txnTimeFormat(data.date)

        }

        holder.binding.statusFailed.hide()
        if (data.status.equals("Success", true)) {
            if (data.type.equals(Constants.TxnHistoryType.DEBIT.name, true)) {
                holder.binding.status.setTextColor(context.resources.getColor(R.color.light_red))
                holder.binding.lytCard.setBackgroundResource(R.drawable.img_red_shadow_bg)
                holder.binding.status.text = context.resources.getString(R.string.status_send)
            } else {
                holder.binding.status.setTextColor(context.resources.getColor(R.color.teal_700))
                holder.binding.lytCard.setBackgroundResource(R.drawable.img_green_shadow_bg)
                holder.binding.status.text = context.resources.getString(R.string.status_received)
            }

            if (isForSingleTxn) {
                if (data.type.equals(Constants.TxnHistoryType.DEBIT.name, true)) {

                    holder.binding.status.hide()
                    holder.binding.lytCard.setBackgroundResource(R.drawable.img_red_shadow_bg)
                    holder.binding.amount.text ="-${Constants.DEFAULT_CURRENCY} ${data.amount}"

                    holder.binding.status.setTextColor(context.resources.getColor(R.color.app_green_light))
                    holder.binding.status.setOnClickListener {
                        callback(position)
                    }
                }else{
                    holder.binding.status.show()
                    holder.binding.lytCard.setBackgroundResource(R.drawable.img_green_shadow_bg)
                    holder.binding.amount.text ="${Constants.DEFAULT_CURRENCY} ${data.amount}"

                    holder.binding.status.setTextColor(context.resources.getColor(R.color.teal_700))
                    holder.binding.status.text = context.resources.getString(R.string.status_received)
                }

            } else {
                holder.itemView.setOnClickListener {
                    callback(position)
                }
            }
        } else {
            holder.binding.statusFailed.show()
            holder.binding.status.text = ""//data.status
            holder.binding.status.setTextColor(context.resources.getColor(R.color.light_red))
            holder.binding.lytCard.setBackgroundResource(R.drawable.img_red_shadow_bg)
        }




    }

    override fun getItemCount(): Int = list.size
}