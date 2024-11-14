package com.nbt.blytics.modules.manageaccount

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.MangeAccountFragmentBinding
import com.nbt.blytics.utils.setBgColorHappyTheme
import com.nbt.blytics.utils.setTextDrawableTint

class MangeAccountFragment : BaseFragment<MangeAccountFragmentBinding, MangeAccountViewModel>() {
    private val mangeAccountViewModel:MangeAccountViewModel by viewModels()
    private lateinit var binding: MangeAccountFragmentBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding =getViewDataBinding()
        happyThemeChanges()
    }

    private fun happyThemeChanges(){
        if(BaseActivity.isCustomMode) {
            binding.apply {
                topLayout.setBgColorHappyTheme()
                txtCurrentAc.setTextDrawableTint()
                txtLinkedAc.setTextDrawableTint()
                txtSavingAc.setTextDrawableTint()
                txtTravelAc.setTextDrawableTint()
            }
        }
    }

    override fun getLayoutId(): Int =R.layout.mange_account_fragment
    override fun getBindingVariable(): Int =0
    override fun getViewModel(): MangeAccountViewModel =mangeAccountViewModel
}