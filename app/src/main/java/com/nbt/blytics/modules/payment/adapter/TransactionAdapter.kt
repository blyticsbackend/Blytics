package com.nbt.blytics.modules.payment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowHomeTransactionBinding
import com.nbt.blytics.modules.transactionhistory.TransactionResponse
import com.nbt.blytics.utils.*
import java.text.SimpleDateFormat

class TransactionAdapter(val context: Context, val list:MutableList<TransactionResponse.Data.Txn>, val callback:(Int) ->Unit) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    class TransactionViewHolder(val mBinding: RowHomeTransactionBinding) :
        RecyclerView.ViewHolder(mBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowHomeTransactionBinding>(
            inflater, R.layout.row_home_transaction, parent, false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val data = list[position]
        holder.mBinding.txtUserName.text = data.userName
        holder.mBinding.amount.text ="${Constants.DEFAULT_CURRENCY} ${data.amount}"
        holder.mBinding.date.text = UtilityHelper.txnDateFormat(data.date)
        holder.mBinding.time.text = UtilityHelper.txnTimeFormat(data.date)
        holder.mBinding.txtReference.text = "Reference:${data.reference}"
        holder.itemView.setOnClickListener{
            if(data.userId!="27")  //do not callback for user 27
            callback(position)
        }

       // Glide.with(context).clear(holder.mBinding.imgUser)
        holder.mBinding.imgUser.setImage(R.drawable.dummy_user)
        if(data.userImage.isNotEmpty()) {
            holder.mBinding.imgUser.setImage(data.userImage)
        }

        holder.mBinding.statusFailed.hide()
        if(data.status.equals("Success", true)) {
            if (data.type.equals(Constants.TxnHistoryType.DEBIT.name, true)) {
              //  holder.mBinding.status.setTextColor(context.resources.getColor(R.color.light_red))
               holder.mBinding.lytCard.setBackgroundResource(R.drawable.img_red_shadow_bg)
              //  holder.mBinding.status.text = context.resources.getString(R.string.status_send)
                holder.mBinding.amount.text ="-${Constants.DEFAULT_CURRENCY} ${data.amount}"

            } else {
               // holder.mBinding.status.setTextColor(context.resources.getColor(R.color.teal_700))
                holder.mBinding.lytCard.setBackgroundResource(R.drawable.img_green_shadow_bg)
               // holder.mBinding.status.text = context.resources.getString(R.string.status_received)
                holder.mBinding.amount.text ="${Constants.DEFAULT_CURRENCY} ${data.amount}"

            }
        }else{
            holder.mBinding.statusFailed.show()
           //data.status
           // holder.mBinding.status.setTextColor(context.resources.getColor(R.color.light_red))
            holder.mBinding.lytCard.setBackgroundResource(R.drawable.img_red_shadow_bg)
        }
    }

    override fun getItemCount(): Int = list.size


}




