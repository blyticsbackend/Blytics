package com.nbt.blytics.modules.sqverify.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowSqVerifyBinding
import com.nbt.blytics.modules.sqverify.models.SQVerifyResponse
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.show

/**
 * Created bynbton 22-07-2021
 */
class SqVerifyAdapter(
    val context: Context,
    val list: MutableList<SQVerifyResponse.Data.QuesAn>,
    val callbackHint: (Int) -> Unit
) : RecyclerView.Adapter<SqVerifyAdapter.SqViewHolder>() {
    inner class SqViewHolder(val binding: RowSqVerifyBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SqViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<RowSqVerifyBinding>(
            inflater, R.layout.row_sq_verify, parent, false
        )
        return SqViewHolder(binding)

    }

    override fun onBindViewHolder(holder: SqViewHolder, position: Int) {
        val data = list[position]
        holder.binding.apply {
            ques.text = data.ques.toString()
            edtAns.doOnTextChanged { text, start, count, after ->
                data.userEnterAns = text.toString().trim()
            }
            btnHint.setOnClickListener {
                if(txtHint.isShown){
                    txtHint.hide()
                }else{
                    txtHint.show()
                    txtHint.text = data.hint
                }

            }

        }


        /* holder.itemView.setOnClickListener {
             callbackHint(position)
         }*/

    }



    override fun getItemCount(): Int = list.size
}