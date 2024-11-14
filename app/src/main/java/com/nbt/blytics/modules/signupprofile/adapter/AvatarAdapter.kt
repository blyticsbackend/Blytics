package com.nbt.blytics.modules.signupprofile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowAvatarBinding
import com.nbt.blytics.modules.signupprofile.models.AvatarModel
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.setImage
import com.nbt.blytics.utils.show

/**
 * Created bynbton 13-07-2021
 */
class AvatarAdapter(val listAvatar: List<AvatarModel.Data>, val callback: (Int) -> Unit) :
    RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {
    inner class AvatarViewHolder(var binding: RowAvatarBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowAvatarBinding>(
            inflater, R.layout.row_avatar, parent, false
        )
        return AvatarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        holder.binding.apply {

            imgAvatar.setImage(listAvatar[position].image)

           /* if (listAvatar[position].isSelect) {
                imgCheck.show()
            } else {
                imgCheck.hide()
            }*/
            holder.itemView.setOnClickListener {

               listAvatar[position].isSelect = true
                callback(position)
            }
        }


    }

    override fun getItemCount(): Int = listAvatar.size
}