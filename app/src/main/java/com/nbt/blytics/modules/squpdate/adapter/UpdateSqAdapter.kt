package com.nbt.blytics.modules.squpdate.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowSqUpdateBinding
import com.nbt.blytics.modules.squpdate.model.SqResponse

/**
 * Created bynbton 29-07-2021
 */
class UpdateSqAdapter(val context : Context, val list : MutableList<SqResponse.Question>): RecyclerView.Adapter<UpdateSqAdapter.SqViewHolder>(){
    class SqViewHolder (val binding:RowSqUpdateBinding):RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SqViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<RowSqUpdateBinding>(
            inflater, R.layout.row_sq_update, parent, false
        )
        return SqViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SqViewHolder, position: Int) {
        val data = list[position]
        holder.binding.apply {
            ques.text = data.question

            holder.itemView.setOnClickListener {
                var totalSelect = 0
                for(i in list.indices){
                    if(list[i].selected){
                        totalSelect +=1
                    }
                }
                if(totalSelect<2) {
                    data.selected = true
                }else{
                    data.selected = false
                }
                notifyDataSetChanged()
            }
            ques.isChecked = data.selected
            /*edtAns.setText(data.ans)
            edtHint.setText(data.hint)
*/
          /*  edtAns.doOnTextChanged { text, start, count, after ->
                data.ans = text.toString().trim()
            }
            edtHint.doOnTextChanged { text, start, count, after ->
                data.hint = text.toString().trim()
            }*/
        }

    }

    override fun getItemCount(): Int =list.size
}