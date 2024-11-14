package com.nbt.blytics.modules.transactiondetails.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowAcInfoBinding
import com.nbt.blytics.databinding.RowAcInfoBottomBinding
import com.nbt.blytics.databinding.RowHomeTransactionBinding
import com.nbt.blytics.databinding.RowSingleTextTitleBinding
import com.nbt.blytics.modules.acinfo.AcInfoAdapter
import com.nbt.blytics.modules.home.HomeFragment
import com.nbt.blytics.modules.home.HomeFragment.Companion.TODAY_AMOUNT
import com.nbt.blytics.modules.transactionhistory.TransactionResponse
import com.nbt.blytics.utils.*
import java.text.SimpleDateFormat
import java.util.*

class TransactionDetailAdapter(val context: Context, val list:MutableList<TransactionResponse.Data.Txn>, val callback:(Int) ->Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class TransactionViewHolder(val mBinding: RowHomeTransactionBinding) :
        RecyclerView.ViewHolder(mBinding.root)
    class TransactionViewHolderTop(val mBinding: RowSingleTextTitleBinding) :
        RecyclerView.ViewHolder(mBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if(viewType==0){
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<RowHomeTransactionBinding>(
                inflater, R.layout.row_home_transaction, parent, false
            )
            return TransactionViewHolder(binding)
        }else {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<RowSingleTextTitleBinding>(
                inflater, R.layout.row_single_text_title, parent, false
            )
            return TransactionViewHolderTop(binding)


        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = list[position]
        Log.d("Txn--", "onBindViewHolder: $data")
        when(holder.itemViewType){
            0 ->{
              holder as TransactionViewHolder
                holder.mBinding.txtUserName.text = data.userName
                holder.mBinding.amount.text ="${Constants.DEFAULT_CURRENCY} ${data.amount}"
                holder.mBinding.date.text = UtilityHelper.txnDateFormat(data.date)
                holder.mBinding.time.text = UtilityHelper.txnTimeFormat(data.date)
                holder.mBinding.txtReference.text = "Reference:${data.reference}"



                holder.itemView.setOnClickListener{
                    if(data.userId!="27")  //do not callback for user 27
                    callback(position)
                }

                holder.mBinding.imgUser.setImage(R.drawable.dummy_user)
                if(data.userImage.isNotEmpty()) {
                    holder.mBinding.imgUser.setImage(data.userImage)
                }

                holder.mBinding.statusFailed.hide()
                if(data.status.equals("Success", true)) {
                    if (data.type.equals(Constants.TxnHistoryType.DEBIT.name, true)) {
                        holder.mBinding.lytCard.setBackgroundResource(R.drawable.img_red_shadow_bg)

                        holder.mBinding.amount.text ="-${Constants.DEFAULT_CURRENCY} ${data.amount}"

                    } else {
                        holder.mBinding.lytCard.setBackgroundResource(R.drawable.img_green_shadow_bg)

                        holder.mBinding.amount.text ="${Constants.DEFAULT_CURRENCY} ${data.amount}"

                    }
                }else{
                    holder.mBinding.statusFailed.show()
                    holder.mBinding.lytCard.setBackgroundResource(R.drawable.img_red_shadow_bg)
                }
            }
            else ->{
               holder as TransactionViewHolderTop
                holder.mBinding.txtUserName.text = data.userName
                holder.mBinding.amount.text ="${Constants.DEFAULT_CURRENCY} ${data.amount}"
                holder.mBinding.date.text = UtilityHelper.txnDateFormat(data.date)
                holder.mBinding.time.text = UtilityHelper.txnTimeFormat(data.date)
                holder.mBinding.txtReference.text = "Reference:${data.reference}"

               val strDataFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH)
               val currentDateTime = strDataFormat.format(Date())
                val currentDate = UtilityHelper.txnDateFormat(currentDateTime)
                if(currentDate.equals(UtilityHelper.txnDateFormat(data.date), true)){
                    holder.mBinding.dateTitle.text ="Today"
                    holder.mBinding.closingAmt.text=HomeFragment.TODAY_AMOUNT

                }else{
                    holder.mBinding.dateTitle.text =UtilityHelper.txnDateFormat(data.date)
                    holder.mBinding.closingAmt.text = "${Constants.DEFAULT_CURRENCY} ${data.closing_balance}"
                }

                holder.itemView.setOnClickListener{
                    if(data.userId!="27")  //do not callback for user 27
                    callback(position)
                }

                holder.mBinding.imgUser.setImage(R.drawable.dummy_user)
                if(data.userImage.isNotEmpty()) {
                    holder.mBinding.imgUser.setImage(data.userImage)
                }

                holder.mBinding.statusFailed.hide()
                if(data.status.equals("Success", true)) {
                    if (data.type.equals(Constants.TxnHistoryType.DEBIT.name, true)) {
                        holder.mBinding.amount.text ="-${Constants.DEFAULT_CURRENCY} ${data.amount}"
                        holder.mBinding.lytCard.setBackgroundResource(R.drawable.img_red_shadow_bg)


                    } else {
                        holder.mBinding.amount.text ="${Constants.DEFAULT_CURRENCY} ${data.amount}"
                        holder.mBinding.lytCard.setBackgroundResource(R.drawable.img_green_shadow_bg)

                    }
                }else{
                    holder.mBinding.statusFailed.show()
                    holder.mBinding.lytCard.setBackgroundResource(R.drawable.img_red_shadow_bg)
                }

            }
        }

    }

    override fun getItemCount(): Int = list.size


    override fun getItemViewType(position: Int): Int {
        if(list[position].lytType ==0){
            return 0
        }else {
            return 1
        }


    }
}




