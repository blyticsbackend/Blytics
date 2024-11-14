package com.nbt.blytics.modules.txnstatus

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.TranscationStatusFragmentBinding
import com.nbt.blytics.modules.payamount.PayAmountFragment
import com.nbt.blytics.utils.hide

class TranscationStatusFragment :
    BaseFragment<TranscationStatusFragmentBinding, TranscationStatusViewModel>() {
    private val transcationStatusViewModel: TranscationStatusViewModel by viewModels()
    private lateinit var binding: TranscationStatusFragmentBinding
    var  isMultiPay :String?=""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()

        requireArguments().apply {
            isMultiPay = getString(PayAmountFragment.IS_MULTI_PAY)
            val message = getString(PayAmountFragment.MESSAGE)
            binding.txtTxnMessage.text = message
            /*if(message.equals("Request successful",true)){
                binding.btnGoTxnHistory.hide()
            }*/
            binding.btnGoHome.setOnClickListener {
                requireActivity().finish()
            }
            binding.imgHome.setOnClickListener {
                requireActivity().finish()
            }
            binding.btnGoTxnHistory.setOnClickListener {
                findNavController().navigate(R.id.action_transcationStatusFragment_to_transactionHistoryFragment)

            }

        }
        activity?.onBackPressedDispatcher?.addCallback(
            requireActivity(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    return
                }
            })

    }

     fun happyThemeChanges() {

        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.btnGoHome.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnGoTxnHistory.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.imgTxnStatus.setImageResource(R.drawable.img_txn_done_orange)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.btnGoHome.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnGoTxnHistory.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.imgTxnStatus.setImageResource(R.drawable.img_txn_done)

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.btnGoHome.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnGoTxnHistory.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.imgTxnStatus.setImageResource(R.drawable.img_txn_done)
            }
        }
    }
    override fun getLayoutId(): Int = R.layout.transcation_status_fragment

    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): TranscationStatusViewModel = transcationStatusViewModel


}