package com.nbt.blytics.modules.paynow

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.PayNowFragmentBinding
import com.nbt.blytics.modules.payamount.PayAmountFragment
import com.nbt.blytics.modules.payee.payment.PaymentHistoryAdapter
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.singletransaction.SingleTransactionFragment
import com.nbt.blytics.modules.transactionhistory.TransactionRequest
import com.nbt.blytics.modules.transactionhistory.TransactionResponse
import com.nbt.blytics.utils.*

class PayNowFragment : BaseFragment<PayNowFragmentBinding, PayNowViewModel>() {
    private val payNowViewModel: PayNowViewModel by viewModels()
    private lateinit var binding: PayNowFragmentBinding
    private lateinit var adapter: TxnHistoryAdapter
    private val txnList = mutableListOf<TransactionResponse.Data.Txn>()
    private val limit = "12"
    private var payMode: String? = ""
    companion object {
        val SELECTED_TXN_PAYEES = mutableListOf<TransactionResponse.Data.Txn>()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()
        observer()
        setAdapter()
        payMode = requireArguments().getString(Constants.PAY_MODE)
        if (payMode.equals(Constants.PayType.REQUEST_MONEY.name, true)) {
            binding.btnSend.text = "Request Money"
            binding.btnPayNow.text = "Request new"
            binding.lblStart.text = "Your payer"
            (requireActivity() as BconfigActivity).setToolbarTitle("Request Money")
        } else {
            binding.btnSend.text = "Send Money"
            binding.btnPayNow.text = "Pay new"
            binding.lblStart.text = "Your payee"
            (requireActivity() as BconfigActivity).setToolbarTitle("Send money")
        }
        if (payMode.equals(Constants.PayType.RECENT_PAYEE.name, true)) {
            binding.btnSend.hide()
            binding.btnPayNow.hide()
        }
        if (payMode.equals(Constants.PayType.SCHEDULE.name, true)) {
            (requireActivity() as BconfigActivity).setToolbarTitle("")
            binding.btnSend.hide()
            binding.btnPayNow.text = "Pay new"
            binding.lblStart.text = "Your payee"
        }
        binding.btnPayNow.setOnClickListener {
            if (payMode.equals(Constants.PayType.SCHEDULE.name, true)) {
                findNavController().navigate(R.id.contactFragment, bundleOf(Constants.PAY_MODE to Constants.PayType.SCHEDULE.name))
            } else {
                findNavController().navigate(R.id.contactFragment, bundleOf(Constants.PAY_MODE to payMode))
            }
        }
        binding.btnSend.setOnClickListener {
            SELECTED_TXN_PAYEES.clear()
            for (i in txnList.indices) {
                if (txnList[i].isSelected) {
                    SELECTED_TXN_PAYEES.add(txnList[i])
                }
            }
            if (SELECTED_TXN_PAYEES.isNotEmpty() && SELECTED_TXN_PAYEES.size < 3) {
                findNavController().navigate(R.id.payAmountFragment, bundleOf(PayAmountFragment.IS_MULTI_PAY to "yes", Constants.PAY_MODE to payMode))
            } else {
                showToast("please select payees.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        txnList.clear()
        adapter.notifyDataSetChanged()
        showLoading()
        payNowViewModel.transactionHistory(
            TransactionRequest(
                pref().getStringValue(SharePreferences.USER_ID, ""),
                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                frequentPayee = "True",
                reverse = 1,
                bank = "Internal",
                offset = limit
            )
        )
    }

    private fun setAdapter() {
        adapter = TxnHistoryAdapter(requireContext(), txnList) {
            val data = txnList[it]
            if (payMode.equals(Constants.PayType.RECENT_PAYEE.name, true)) {
                val intent = Intent(requireActivity(), BconfigActivity::class.java)
                intent.putExtra(Constants.COMING_FOR, ComingFor.TRANSACTION_DETAILS.name)
                intent.putExtra(SingleTransactionFragment.NAME, data.userName)
                intent.putExtra(SingleTransactionFragment.TOTAL_AMOUNT_SENT, data.amount)
                intent.putExtra(SingleTransactionFragment.TOTAL_AMOUNT_RECEIVER, data.amount)
                intent.putExtra(SingleTransactionFragment.USER_IMG_URL, data.userImage)
                intent.putExtra(SingleTransactionFragment.TRANSACTION_USER_ID, if (data.bankType.equals("External", true)) data.txnId else data.userId)
                intent.putExtra(SingleTransactionFragment.BANK_TYPE, data.bankType)
                startActivity(intent)
            }
        }
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvTxn.layoutManager = layoutManager
        binding.rvTxn.adapter = adapter
        binding.rvTxn.addOnItemTouchListener(
            RecyclerTouchListener(
                requireContext(),
                binding.rvTxn,
                object : PaymentHistoryAdapter.ClickListener {
                    override fun onClick(view: View?, position: Int) {
                    }
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onLongClick(view: View?, position: Int) {
                        val data = txnList[position]
                        if (payMode.equals(Constants.PayType.SCHEDULE.name, true)) {
                            val intent = Intent()
                            intent.putExtra("mobileNo", data.mobNo)
                            intent.putExtra("acc", data.bank_acc)
                            intent.putExtra("name", data.userName)
                            requireActivity().setResult(1234, intent)
                            requireActivity().finish()
                        } else if (payMode.equals(Constants.PayType.RECENT_PAYEE.name, true).not()) {
                            data.isSelected = !data.isSelected
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            )
        )

        val endlessRecyclerView = object : EndlessRecyclerViewScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int) {
                payNowViewModel.transactionHistory(
                    TransactionRequest(
                        pref().getStringValue(SharePreferences.USER_ID, ""),
                        pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                        pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                        frequentPayee = "True",
                        reverse = 1,
                        page = page + 1,
                        offset = limit
                    )
                )
            }
        }
        binding.rvTxn.addOnScrollListener(endlessRecyclerView)
    }

    private fun observer() {
        payNowViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is TransactionResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        txnList.addAll(it.data!!.txnList)
                        adapter.notifyDataSetChanged()
                    }
                    if (txnList.isEmpty()) {
                        binding.txtNoTxn.show()
                        binding.btnSend.hide()
                    } else {
                        binding.txtNoTxn.hide()
                        binding.btnSend.show()
                    }
                    if (payMode.equals(Constants.PayType.RECENT_PAYEE.name, true)) {
                        binding.btnSend.hide()
                        binding.btnPayNow.hide()
                    }
                    if (payMode.equals(Constants.PayType.SCHEDULE.name, true)) {
                        binding.btnSend.hide()
                    }
                    payNowViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    payNowViewModel.observerResponse.value = null
                }
                is Throwable -> {
                    hideLoading()
                    payNowViewModel.observerResponse.value = null
                }
            }
        }
    }

    private fun happyThemeChanges() {
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.btnSend.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.mainLayout.setBackgroundResource(R.color.b_bg_color_dark)

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.btnSend.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.mainLayout.setBackgroundResource(R.color.b_bg_color_light)

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.btnSend.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.mainLayout.setBackgroundResource(R.color.b_bg_color_light)
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.pay_now_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): PayNowViewModel = payNowViewModel
}