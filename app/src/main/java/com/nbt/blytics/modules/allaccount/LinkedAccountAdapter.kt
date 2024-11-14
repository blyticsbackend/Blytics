package com.nbt.blytics.modules.allaccount

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.*
import com.nbt.blytics.modules.acinfo.AcInfoAdapter
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.show


class LinkedAccountAdapter(
    val context: Context,
    val list: MutableList<LinkedAccResponse.AccList>,
    val callback: (Int, View) -> Unit
)  :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class AcViewHolderTop(val binding: RowAcInfoBinding) : RecyclerView.ViewHolder(binding.root)
    class AcViewHolderCenter(val binding: RowAcInfoCenterBinding) : RecyclerView.ViewHolder(binding.root)
    class AcViewHolderBottom(val binding: RowAcInfoBottomBinding) : RecyclerView.ViewHolder(binding.root)




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType==0){
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<RowAcInfoBinding>(
                inflater, R.layout.row_ac_info, parent, false
            )
            return AcInfoAdapter.AcViewHolderTop(binding)
        }else if(viewType ==1){
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<RowAcInfoBottomBinding>(
                inflater, R.layout.row_ac_info_bottom, parent, false
            )
            return AcInfoAdapter.AcViewHolderBottom(binding)
        }else {

            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<RowAcInfoCenterBinding>(
                inflater, R.layout.row_ac_info_center, parent, false
            )
            return AcInfoAdapter.AcViewHolderCenter(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = list[position]

        when(holder.itemViewType){
            0 ->{
                val holder = holder as AcInfoAdapter.AcViewHolderTop
                holder.binding.apply {
                    acNumber.text = data.accNo
                    acType.text= "Linked account"
                    txtAmount.text= "${Constants.DEFAULT_CURRENCY} ${data.amount}"

                    imgShare.hide()
                    rb.show()

                    if(list.isNotEmpty()){
                        if(position == list.size-1){
                            dividerLine.hide()
                        }else{
                            dividerLine.show()
                        }
                    }



                }
            }
            1 -> {
                val holder = holder as AcInfoAdapter.AcViewHolderBottom
                holder.binding.apply {
                    acNumber.text = data.accNo
                    acType.text = "Linked account"
                    txtAmount.text = "${Constants.DEFAULT_CURRENCY} ${data.amount}"

                    imgShare.hide()
                    rb.show()

                    if(list.isNotEmpty()){
                        if(position == list.size-1){
                            dividerLine.hide()
                        }else{
                            dividerLine.show()
                        }
                    }
                }
            }
            else -> {
                val holder = holder as AcInfoAdapter.AcViewHolderCenter
                holder.binding.apply {
                    acNumber.text = data.accNo
                    acType.text = "Linked account"
                    txtAmount.text = "${Constants.DEFAULT_CURRENCY} ${data.amount}"

                    imgShare.hide()
                    rb.show()
                    if(list.isNotEmpty()){
                        if(position == list.size-1){
                            dividerLine.hide()
                        }else{
                            dividerLine.show()
                        }
                    }

                }
                //happyThemeChanges(holder.binding)

            }
        }
        holder.itemView.setOnClickListener {
            callback(position, holder.itemView)
        }


        /* holder.binding.apply {
             if (acType == Constants.CURRENT_ACC) {
                 textPurpose.hide()
             } else {
                 textPurpose.show()
             }
             if (data.default) {
                 SharePreferences.getInstance(context)
                     .setStringValue(SharePreferences.DEFAULT_ACCOUNT, data.acc_no)
                 SharePreferences.getInstance(context)
                     .setStringValue(SharePreferences.DEFAULT_PURPOSE, data.purpose)
                 check.visibility = View.VISIBLE
                 uncheck.visibility = View.GONE
             } else {
                 check.visibility = View.GONE
                 uncheck.visibility = View.VISIBLE
             }
             textAccount.text = data.acc_no
             textPurpose.text = data.purpose
             allAccountCard.setOnClickListener {
                 callback(data.acc_no, data.purpose, data.acc_uuid)

             }
         }*/
        /*   */

    }


    private fun happyThemeChanges(binding: RowAllAccountBinding) {

        when (context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.uncheck.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.check.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.orange_dark
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.uncheck.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.check.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.blue_dark
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.uncheck.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.check.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.blue_dark
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )

            }
        }


    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        if(position ==0){
            return 0
        }else if( position ==list.size-1){
            return 1
        }
        return -1

    }

}




