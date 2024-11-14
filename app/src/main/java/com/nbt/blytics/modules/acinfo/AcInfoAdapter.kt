package com.nbt.blytics.modules.acinfo

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowAcInfoBinding
import com.nbt.blytics.databinding.RowAcInfoBottomBinding
import com.nbt.blytics.databinding.RowAcInfoCenterBinding
import com.nbt.blytics.modules.payee.schedule.RecentScheduleRes
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.show

class AcInfoAdapter(
    val context: Context,
    val list: MutableList<AcInfoResponse.Data>,
    val isShareVisible: Boolean = false,
    val callback: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class AcViewHolderTop(val binding: RowAcInfoBinding) : RecyclerView.ViewHolder(binding.root)
    class AcViewHolderCenter(val binding: RowAcInfoCenterBinding) :
        RecyclerView.ViewHolder(binding.root)

    class AcViewHolderBottom(val binding: RowAcInfoBottomBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<RowAcInfoBinding>(inflater, R.layout.row_ac_info, parent, false)
            return AcViewHolderTop(binding)
        } else if (viewType == 1) {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<RowAcInfoBottomBinding>(inflater, R.layout.row_ac_info_bottom, parent, false)
            return AcViewHolderBottom(binding)
        } else {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<RowAcInfoCenterBinding>(inflater, R.layout.row_ac_info_center, parent, false)
            return AcViewHolderCenter(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = list[position]
        holder.itemView.setOnClickListener {
            callback(position)
        }

        when (holder.itemViewType) {
            0 -> {
                val holder = holder as AcViewHolderTop
                holder.binding.apply {
                    acNumber.text = data.acc_no
                    acType.text = data.acc_type
                    txtAmount.text = "${Constants.DEFAULT_CURRENCY} ${data.amount}"
                    imgShare.setOnClickListener {
                        var details = ""
                        val sharingIntent = Intent(Intent.ACTION_SEND)
                        sharingIntent.type = "text/plain"
                        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Account Details")
                        if (data.acc_type.equals("linked", true)) {
                            details = "A/C Created By: ${data.create_by}\nA/C Created For: ${data.create_for}\nA/C Type: ${data.acc_type}\nA/C No.: ${data.acc_no}\nBank Code: ${data.bank_code}"
                        } else {
                            details = "A/C Holder Name: ${data.acc_holder_name}\nA/C No.: ${data.acc_no}\nA/C Type: ${data.acc_type}\n" + "Bank Code: ${data.bank_code}"
                        }
                        sharingIntent.putExtra(Intent.EXTRA_TEXT, details)
                        context.startActivity(Intent.createChooser(sharingIntent, "Sharing Option"))
                    }

                    acName.text = data.acc_holder_name
                    Log.d("onBindViewHolder", "onBindViewHolder: ${data.acc_holder_name}")
                    if (isShareVisible) {
                        imgShare.show()
                        rb.hide()
                    } else {
                        imgShare.hide()
                        rb.show()
                    }/*if(list.size==1){
                        dividerLine.hide()
                    }else{
                        dividerLine.show()
                    }*/
                    if (list.isNotEmpty()) {
                        if (position == list.size - 1) {
                            dividerLine.hide()
                        } else {
                            dividerLine.show()
                        }
                    }
                }
            }

            1 -> {
                val holder = holder as AcViewHolderBottom
                holder.binding.apply {
                    acNumber.text = data.acc_no
                    acType.text = data.acc_type
                    txtAmount.text = "${Constants.DEFAULT_CURRENCY} ${data.amount}"
                    imgShare.setOnClickListener {
                        var details = ""
                        val sharingIntent = Intent(Intent.ACTION_SEND)
                        sharingIntent.type = "text/plain"
                        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Account Details")
                        if (data.acc_type.equals("linked", true)) {
                            details = "A/C Created By: ${data.create_by}\nA/C Created For: ${data.create_for}\nA/C Type: ${data.acc_type}\nA/C No.: ${data.acc_no}\nBank Code: ${data.bank_code}"
                        } else {
                            details = "A/C Holder Name: ${data.acc_holder_name}\nA/C No.: ${data.acc_no}\nA/C Type: ${data.acc_type}\n" + "Bank Code: ${data.bank_code}"
                        }
                        sharingIntent.putExtra(Intent.EXTRA_TEXT, details)
                        context.startActivity(Intent.createChooser(sharingIntent, "Sharing Option"))
                    }
                    acName.text = data.acc_holder_name
                    if (isShareVisible) {
                        imgShare.show()
                        rb.hide()
                    } else {
                        imgShare.hide()
                        rb.show()
                    }
                    if (list.isNotEmpty()) {
                        if (position == list.size - 1) {
                            dividerLine.hide()
                        } else {
                            dividerLine.show()
                        }
                    }
                }
            }

            else -> {
                val holder = holder as AcViewHolderCenter
                holder.binding.apply {
                    acNumber.text = data.acc_no
                    acType.text = data.acc_type
                    txtAmount.text = "${Constants.DEFAULT_CURRENCY} ${data.amount}"
                    imgShare.setOnClickListener {
                        var details = ""
                        val sharingIntent = Intent(Intent.ACTION_SEND)
                        sharingIntent.type = "text/plain"
                        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Account Details")
                        if (data.acc_type.equals("linked", true)) {
                            acName.text = data.acc_holder_name
                            details = "A/C Created By: ${data.create_by}\nA/C Created For: ${data.create_for}\nA/C Type: ${data.acc_type}\nA/C No.: ${data.acc_no}\nBank Code: ${data.bank_code}"
                        } else {
                            details = "A/C Holder Name: ${data.acc_holder_name}\nA/C No.: ${data.acc_no}\nA/C Type: ${data.acc_type}\n" + "Bank Code: ${data.bank_code}"
                        }
                        sharingIntent.putExtra(Intent.EXTRA_TEXT, details)
                        context.startActivity(Intent.createChooser(sharingIntent, "Sharing Option"))
                    }
                    // for saving account
                    acName.text = data.acc_holder_name

                    if (isShareVisible) {
                        imgShare.show()
                        rb.hide()
                    } else {
                        imgShare.hide()
                        rb.show()
                    }
                    if (list.isNotEmpty()) {
                        if (position == list.size - 1) {
                            dividerLine.hide()
                        } else {
                            dividerLine.show()
                        }
                    }
                }
            }
        }

        /*  if(data.acc_type.equals("linked",true)){
      lblAcHolderName.hide()
      txtAcHolderName.hide()
      lblCreateBy.show()
      lblRelation.show()
      lblCreateFor.show()
      txtRelation.show()
      txtAcCreateBy.show()
      txtAcCreateFor.show()
  }else{
      lblAcHolderName.show()
      txtAcHolderName.show()
      lblCreateBy.hide()
      lblRelation.hide()
      lblCreateFor.hide()
      txtRelation.hide()
      txtAcCreateBy.hide()
      txtAcCreateFor.hide()
  }*//*     txtAcHolderName.text = data.acc_holder_name
             txtAcNo.text = data.acc_no
             txtAmount.text= "${Constants.DEFAULT_CURRENCY} ${data.amount}"
             txtBackCode.text = data.bank_code
             txtAcType.text= data.acc_type
             txtRelation.text= data.relation
             txtAcCreateFor.text= data.create_for
             txtAcCreateBy.text= data.create_by
  */
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return 0
        } else if (position == list.size - 1) {
            return 1
        }
        return -1
    }
    fun deleteItem(i: Int) {
        //list.removeAt(i)
        notifyDataSetChanged()
    }
    fun addItem(i: Int, item: RecentScheduleRes.Data.Schedule) {
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int = list.size
}