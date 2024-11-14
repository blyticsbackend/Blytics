package com.nbt.blytics.modules.chargesdetails

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.ChargesDetailsFragmentBinding
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.transactionhistory.TransactionRequest
import com.nbt.blytics.utils.EndlessRecyclerViewScrollListener
import com.nbt.blytics.utils.SharePreferences
import com.nbt.blytics.utils.show

class ChargesDetailsFragment : BaseFragment<ChargesDetailsFragmentBinding, ChargesDetailsViewModel>() {

    private val chargesDetailsViewModel: ChargesDetailsViewModel by viewModels()
    private lateinit var binding: ChargesDetailsFragmentBinding
    private var chargeList: MutableList<UserChargerResponse.Data.Final> = mutableListOf()
    private lateinit var adapter: ChargeAdapter
    private val limit ="12"
    private var transactUserId:String =""
    companion object{
       val TNX_USER_ID ="transactUserId"
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        (requireContext() as BconfigActivity).setToolbarTitle("Charges")
        setAdapter()
        hideLoading()
        observer()
        transactUserId = requireArguments().getString(TNX_USER_ID, "")
    }

    override fun onResume() {
        super.onResume()
        showLoading()
        chargeList.clear()
        adapter.notifyDataSetChanged()
        chargesDetailsViewModel.userCharge(
            UserChargeRequest(
                pref().getStringValue(SharePreferences.USER_ID, ""),
                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                transactUserId

            )
        )
    }

    private fun setAdapter() {
        adapter = ChargeAdapter(requireContext(), chargeList)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvCharges.layoutManager = layoutManager
        binding.rvCharges.adapter = adapter
        val endlessRecyclerView = object : EndlessRecyclerViewScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int) {
                chargesDetailsViewModel.userCharge(
                    UserChargeRequest(
                        pref().getStringValue(SharePreferences.USER_ID, ""),
                        pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                        pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                        transactUserId,
                        page = (page + 1).toString(),
                        offset = limit

                    )
                )
            }
        }
        binding.rvCharges.addOnScrollListener(endlessRecyclerView)
    }

    private fun observer() {
        chargesDetailsViewModel.observerResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is UserChargerResponse -> {
                    hideLoading()
                    chargeList.addAll(it.data.finalList)
                    adapter.notifyDataSetChanged()
                    if(chargeList.isEmpty()){
                        binding.txtNoTxn.show()
                    }
                    chargesDetailsViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    chargesDetailsViewModel.observerResponse.value = null
                }
                is Throwable -> {
                    showToast(it.message.toString())
                    hideLoading()
                    chargesDetailsViewModel.observerResponse.value = null
                }

            }
        })
    }

    override fun getLayoutId(): Int = R.layout.charges_details_fragment

    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): ChargesDetailsViewModel = chargesDetailsViewModel

}