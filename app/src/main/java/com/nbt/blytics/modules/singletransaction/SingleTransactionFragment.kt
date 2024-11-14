package com.nbt.blytics.modules.singletransaction

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.SingleTransactionFragmentBinding
import com.nbt.blytics.modules.actobank.ActoBankFragment
import com.nbt.blytics.modules.chargesdetails.ChargesDetailsFragment
import com.nbt.blytics.modules.chargesdetails.UserChargerResponse
import com.nbt.blytics.modules.payamount.PayAmountFragment
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.transactionhistory.TransactionRequest
import com.nbt.blytics.modules.transactionhistory.TransactionResponse
import com.nbt.blytics.modules.transactionhistory.adapter.TransactionAdapter
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.EndlessRecyclerViewScrollListener
import com.nbt.blytics.utils.SharePreferences
import com.nbt.blytics.utils.setImage


class SingleTransactionFragment : BaseFragment<SingleTransactionFragmentBinding, SingleTransactionViewModel>() {
    private val singleTransactionViewModel: SingleTransactionViewModel by viewModels()
    private lateinit var binding: SingleTransactionFragmentBinding
    private val txnList = mutableListOf<TransactionResponse.Data.Txn>()
    private val transactionHistoryList = mutableListOf<TransactionModel>()
    private lateinit var adapter: TransactionAdapter
    private var transactUserId: String = ""
    private var bankType = ""
    private val limit = "12"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        observer()
        setAdapter()
        hideLoading()
        happyThemeChanges()
        (requireContext() as BconfigActivity).setToolbarTitle("Details")
        requireArguments().apply {
            binding.txtUserName.text = this.getString(NAME,"").split(" ")[0]
            bankType = this.getString(BANK_TYPE, "")
            if (this.getString(USER_IMG_URL, "") == "") {
                binding.imgUser.setImage(R.drawable.dummy_user)
            } else {
                binding.imgUser.setImage(this.getString(USER_IMG_URL) ?: "")
            }

            transactUserId = getString(TRANSACTION_USER_ID, "")

        }



        binding.txtBankCharge.setOnClickListener {
       findNavController().navigate(
                    R.id.chargesDetailsFragment,
                    bundleOf(ChargesDetailsFragment.TNX_USER_ID to transactUserId)
                )

        }


    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    private fun getData() {
        showLoading()
        txnList.clear()
        adapter.notifyDataSetChanged()
        singleTransactionViewModel.transactionHistory(
            if(bankType.equals("Internal",true)) {
                TransactionRequest(
                    pref().getStringValue(SharePreferences.USER_ID, ""),
                    pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                    pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                    frequentPayee = "False",
                    reverse = 1,
                    offset = limit,
                    transactuserid = transactUserId.toString(),
                    bank = if (bankType == "Internal") "Internal" else "External"
                )
            }else{
                TransactionRequest(
                    pref().getStringValue(SharePreferences.USER_ID, ""),
                    pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                    pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                    frequentPayee = "False",
                    reverse = 1,
                    offset = limit,
                    accNumber = transactUserId.toString(),
                    bank = if (bankType == "Internal") "Internal" else "External"
                )
            }
        )
    }


    private fun observer() {
        singleTransactionViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is UserChargerResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        binding.apply {
                            txtTotalTrascation.text = it.data.totalData.toString()
                            var totalAmt: Long = 0
                            for (i in it.data.finalList.indices) {
                                totalAmt += it.data.finalList[i].chargedAmount.toLong()
                            }
                            txtBankCharge.text = totalAmt.toString()
                        }
                    }
                    singleTransactionViewModel.observerResponse.value = null
                }
                is TransactionResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        txnList.addAll(it.data!!.txnList)
                        adapter.notifyDataSetChanged()
                        binding.txtTotalAmountSent.text ="-${Constants.DEFAULT_CURRENCY} ${it.data.amountSent}"
                        binding.txtTotalAmountReceive.text ="${Constants.DEFAULT_CURRENCY} ${it.data.amountReceive}"
                        binding.txtBankCharge.text = "-${Constants.DEFAULT_CURRENCY} ${it.data.totalCharges}"
                        binding.txtTotalTrascation.text ="${it.data.totalTxn}"

                    }
                    singleTransactionViewModel.observerResponse.value = null

                    /* singleTransactionViewModel.userCharge(
                         UserChargeRequest(
                             pref().getStringValue(SharePreferences.USER_ID, ""),
                             pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                             transactUserId,
                             pref().getStringValue(SharePreferences.USER_WALLET_UUID, "")
                         )
                     )*/
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    singleTransactionViewModel.observerResponse.value = null

                }

            }
        }
    }

    companion object {
        const val TOTAL_AMOUNT_SENT: String = "total_amt_sent"
        const val TOTAL_AMOUNT_RECEIVER: String = "total_amt_received"
        const val NAME: String = "name"
        const val USER_IMG_URL: String = "user_img_url"
        const val TRANSACTION_USER_ID = "transact_user_id"
        const val BANK_TYPE = "bank_type"
    }


    fun setAdapter() {
        adapter = TransactionAdapter(requireContext(), txnList, true) {
            val data = txnList[it]
            if(bankType.equals("Internal",true)) {
                val intent = Intent(requireContext(), BconfigActivity::class.java)
                intent.apply {
                    putExtra(PayAmountFragment.IS_MULTI_PAY, "no")
                    putExtra(PayAmountFragment.RECEIVER_NAME, data.userName)
                    putExtra(PayAmountFragment.RECEIVER_CONTACT, data.mobNo)
                    putExtra(PayAmountFragment.RECEIVER_IMG, data.userImage)
                    Log.d("onViewCreated", "setAdapter: ${data.userImage} ")
                    putExtra(Constants.PAY_MODE, Constants.PayType.SENT_MONEY.name)
                    putExtra(PayAmountFragment.RECEIVER_AMT, data.amount)
                }
                intent.putExtra(Constants.COMING_FOR, ComingFor.PAY_AMOUNT.name)
                startActivity(intent)
            }else{
                val intent = Intent(requireActivity(), BconfigActivity::class.java)
                intent.putExtra(Constants.COMING_FOR, ComingFor.AC_TO_BANK.name)
                intent.putExtra(ActoBankFragment.RECEIVER_NAME, data.userName)
                intent.putExtra(ActoBankFragment.RECEIVER_NAME, data.userName)
                intent.putExtra(ActoBankFragment.RECEIVER_AC_NUMBER, data.bank_acc)
                intent.putExtra(ActoBankFragment.BANK_CODE, data.bank_code)
                intent.putExtra(ActoBankFragment.AMOUNT, data.amount)
                startActivity(intent)

            }

        }
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rvTransaction.layoutManager = layoutManager

        binding.rvTransaction.adapter = adapter
        val endlessRecyclerView = object : EndlessRecyclerViewScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int) {
                if(bankType.equals("Internal",true)) {

                    singleTransactionViewModel.transactionHistory(
                        TransactionRequest(
                            pref().getStringValue(SharePreferences.USER_ID, ""),
                            pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                            pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                            frequentPayee = "False",
                            reverse = 1,
                            offset = limit,
                            page = page + 1,
                            transactuserid = transactUserId.toString(),
                            bank = if (bankType == "Internal") "Internal" else "External"

                        )
                    )
                }else{
                    singleTransactionViewModel.transactionHistory(
                        TransactionRequest(
                            pref().getStringValue(SharePreferences.USER_ID, ""),
                            pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                            pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                            frequentPayee = "False",
                            reverse = 1,
                            offset = limit,
                            page = page + 1,
                            accNumber = transactUserId.toString(),
                            bank = if (bankType == "Internal") "Internal" else "External"

                        )
                    )
                }

            }

        }
        binding.rvTransaction.addOnScrollListener(endlessRecyclerView)


    }
    private fun happyThemeChanges() {

        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.motionTransaction.setBackgroundResource(R.color.b_bg_color_dark)
               // binding.materialCardView.setCardBackgroundColor(resources.getColor(R.color.black))
                binding.txtTotalAmountReceive.setTextColor(resources.getColor(R.color.white))
                binding.txtTotalAmountSent.setTextColor(resources.getColor(R.color.white))
               // binding.txtBankCharge.setTextColor(resources.getColor(R.color.white))

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.motionTransaction.setBackgroundResource(R.color.b_bg_color_light)
               // binding.materialCardView.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.txtTotalAmountReceive.setTextColor(resources.getColor(R.color.b_currency_blue))
                binding.txtTotalAmountSent.setTextColor(resources.getColor(R.color.b_currency_blue))
               // binding.txtBankCharge.setTextColor(resources.getColor(R.color.b_currency_blue))

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.motionTransaction.setBackgroundResource(R.color.b_bg_color_light)
              //  binding.materialCardView.setCardBackgroundColor(resources.getColor(R.color.white))

                binding.txtTotalAmountReceive.setTextColor(resources.getColor(R.color.b_currency_blue))
                binding.txtTotalAmountSent.setTextColor(resources.getColor(R.color.b_currency_blue))
               // binding.txtBankCharge.setTextColor(resources.getColor(R.color.b_currency_blue))

            }
        }


    }

    override fun getLayoutId(): Int = R.layout.single_transaction_fragment

    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): SingleTransactionViewModel = singleTransactionViewModel


}