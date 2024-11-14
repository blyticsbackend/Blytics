package com.nbt.blytics.modules.chargesdetails

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowChargeBinding
import com.nbt.blytics.databinding.RowPaymentHistoryBinding
/*import com.nbt.blytics.modules.payee.schedule.ScheduleAdapter*/
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.UtilityHelper


class ChargeAdapter(val context: Context, val list:MutableList<UserChargerResponse.Data.Final>) :
    RecyclerView.Adapter<ChargeAdapter.ChargeViewHolder>(){
      class ChargeViewHolder(val binding:RowChargeBinding):RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ChargeViewHolder, position: Int) {
    val data  = list[position]
       holder.binding.apply {
           amount.text = "${Constants.DEFAULT_CURRENCY} ${data.chargedAmount}"
           txtChargeFor.text = data.chargedFor
           date.text = UtilityHelper.txnDateFormat(data.chargedDate)
           time.text = UtilityHelper.txnTimeFormat(data.chargedDate)

       }
    }

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChargeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowChargeBinding>(
            inflater, R.layout.row_charge, parent, false
        )
        return ChargeViewHolder(binding)
    }
}