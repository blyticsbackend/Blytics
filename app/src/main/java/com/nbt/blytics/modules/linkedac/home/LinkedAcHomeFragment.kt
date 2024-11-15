package com.nbt.blytics.modules.linkedac.home

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.LinkedAcHomeFragmentBinding
import com.nbt.blytics.modules.allaccount.AllAccountFragment
import com.nbt.blytics.modules.linkedac.manageac.ManageLinkedFragment
import com.nbt.blytics.modules.payment.adapter.TransactionAdapter
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.transactionhistory.TransactionRequest
import com.nbt.blytics.modules.transactionhistory.TransactionResponse
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.SharePreferences
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.show

class LinkedAcHomeFragment : BaseFragment<LinkedAcHomeFragmentBinding, LinkedAcHomeViewModel>() {
    private lateinit var adapterTnx: TransactionAdapter
    private val txnList = mutableListOf<TransactionResponse.Data.Txn>()

    private val linkedAcHomeViewModel: LinkedAcHomeViewModel by viewModels()
    private lateinit var binding: LinkedAcHomeFragmentBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        hideLoading()
        observer()
        getTxn()
        happyThemeChanges()
        (requireContext() as BconfigActivity).setToolbarTitle("Linked account")
        binding.apply {
            AllAccountFragment.SELECTED_LINKED_AC?.apply {
                txtCardNumber.text = accNo
                txtCardNumberBack.text = accNo
                txtCvcBack.text = "123"

                txtDateBack.text = "23/2024"

                txtPayeeName.text = createdFor
                txtUserName.text = createdFor
                txtUserNameBack.text =createdFor
                txtPayeeNameBack.text = createdFor

                txtRelation.text = relation
                txtRelationBack.text = relation
            }


        }
        setTransactionRecycler()
        (activity as BaseActivity).showLoadingTransperant()
        flipCard(requireContext(), binding.cardFront, binding.cardBack)
        binding.txtManageAc.setOnClickListener {

            findNavController().navigate(
                R.id.action_linkedAcHomeFragment3_to_manageLinkedFragment, bundleOf(
                    ManageLinkedFragment.UUID to   AllAccountFragment.SELECTED_LINKED_AC!!.accUuid,ManageLinkedFragment.AccId to  AllAccountFragment.SELECTED_LINKED_AC!!.accId
                )
            )
        }

        binding.imgInfo.setOnClickListener {
            (activity as BaseActivity).showLoadingTransperant()
            flipCard(requireContext(), binding.cardBack, binding.cardFront)
        }
        binding.imgInfoEnd.setOnClickListener {
            (activity as BaseActivity).showLoadingTransperant()
            flipCard(requireContext(), binding.cardFront, binding.cardBack)
        }

    }


    private fun observer() {
        linkedAcHomeViewModel.observerResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is TransactionResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        txnList.clear()
                        txnList.addAll(it.data!!.txnList)
                        adapterTnx.notifyDataSetChanged()
                        if(txnList.isEmpty()){
                            binding.txtNoTxn.show()
                        }else{
                            binding.txtNoTxn.hide()
                        }
                    }
                    linkedAcHomeViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    linkedAcHomeViewModel.observerResponse.value = null
                }
                is Throwable -> {
                    linkedAcHomeViewModel.observerResponse.value = null
                    showToast(it.message.toString())
                    hideLoading()
                }
            }
        })
    }

    private fun getTxn() {
        showLoading()
        linkedAcHomeViewModel.transactionHistory(
            TransactionRequest(
                pref().getStringValue(SharePreferences.USER_ID, ""),
                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                AllAccountFragment.SELECTED_LINKED_AC!!.accUuid,
                frequentPayee = "False",
                reverse = 1,
                bank = "Internal"
            )
        )
    }

    private fun setTransactionRecycler() {
        adapterTnx = TransactionAdapter(requireContext(), txnList) { position ->
            val data = txnList[position]
         /*   val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.TRANSACTION_DETAILS.name)
            intent.putExtra(SingleTransactionFragment.NAME, data.userName)
            intent.putExtra(SingleTransactionFragment.TOTAL_AMOUNT_SENT, data.amount)
            intent.putExtra(SingleTransactionFragment.TOTAL_AMOUNT_RECEIVER, data.amount)
            intent.putExtra(SingleTransactionFragment.USER_IMG_URL, data.userImage)
            intent.putExtra(SingleTransactionFragment.TRANSACTION_USER_ID, data.userId)
            intent.putExtra(SingleTransactionFragment.BANK_TYPE, data.bankType)
            startActivity(intent)*/
            // findNavController().navigate()
        }
        binding.rvTransaction.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvTransaction.adapter = adapterTnx
    }


    private fun flipCard(context: Context, visibleView: View, inVisibleView: View) {
        try {
            visibleView.show()
            val scale = context.resources.displayMetrics.density
            val cameraDist = 11000 * scale
            visibleView.cameraDistance = cameraDist
            inVisibleView.cameraDistance = cameraDist
            val flipOutAnimatorSet =
                AnimatorInflater.loadAnimator(
                    context,
                    R.animator.front_animator
                ) as AnimatorSet
            flipOutAnimatorSet.setTarget(inVisibleView)
            val flipInAnimatorSet =
                AnimatorInflater.loadAnimator(
                    context,
                    R.animator.back_animator
                ) as AnimatorSet
            flipInAnimatorSet.setTarget(visibleView)
            flipOutAnimatorSet.start()
            flipInAnimatorSet.start()
            flipInAnimatorSet.doOnEnd {

            }
            flipOutAnimatorSet.doOnEnd {
                (activity as BaseActivity).hideLoadingTransperant()
                inVisibleView.hide()
            }
        } catch (e: Exception) {
            // logHandledException(e)
        }
    }


    private fun happyThemeChanges() {


        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.debitImageFront.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.bg_debit_cart_orange
                    )
                )

                binding.debitImageBack.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.bg_debit_cart_orange
                    )
                )
                binding.topLayout.setBackgroundResource(R.color.black)
                binding.card1.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_dark))
                binding.card2.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_dark))

            }
            Configuration.UI_MODE_NIGHT_NO -> {

                binding.debitImageFront.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.bg_debit_cart
                    )
                )

                binding.debitImageBack.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.bg_debit_cart
                    )
                )

                binding.topLayout.setBackgroundResource(R.color.gray_bg)
                binding.card1.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_light))
                binding.card2.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_light))

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {

                binding.debitImageFront.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.bg_debit_cart
                    )
                )
                binding.debitImageBack.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.bg_debit_cart
                    )
                )
                binding.topLayout.setBackgroundResource(R.color.gray_bg)
                binding.card1.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_light))
                binding.card2.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_light))

            }
        }


    }


    override fun getLayoutId(): Int = R.layout.linked_ac_home_fragment
    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): LinkedAcHomeViewModel = linkedAcHomeViewModel

}