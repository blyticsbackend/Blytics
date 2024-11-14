package com.nbt.blytics.modules.allaccount

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowAllAccountBinding
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.SharePreferences
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.show


class AllAccountAdapter(
    val context: Context,
    val list: MutableList<AllAccountModel.AllAccountData>,
    val acType: String,
    val callback: (String, String, String) -> Unit
) :
    RecyclerView.Adapter<AllAccountAdapter.AllAccountViewHolder>() {
    inner class AllAccountViewHolder(val binding: RowAllAccountBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllAccountViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowAllAccountBinding>(
            inflater, R.layout.row_all_account, parent, false
        )
        return AllAccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AllAccountViewHolder, position: Int) {
        val data = list[position]


        holder.binding.apply {
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
        }
        happyThemeChanges(holder.binding)

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

}




