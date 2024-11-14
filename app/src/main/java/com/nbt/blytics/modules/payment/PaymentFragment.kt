package com.nbt.blytics.modules.payment

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.activity.main.MainActivity
import com.nbt.blytics.activity.qrcode.QrCodeActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseActivity.Companion.isCustomMode
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.callback.valueChangeLiveData
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.BottomSheetSwitchAcBinding
import com.nbt.blytics.databinding.PaymentFragmentBinding
import com.nbt.blytics.modules.acinfo.AcInfoRequest
import com.nbt.blytics.modules.acinfo.AcInfoResponse
import com.nbt.blytics.modules.home.BalanceResponse
import com.nbt.blytics.modules.home.HomeFragment
import com.nbt.blytics.modules.home.WalletAccountModel
import com.nbt.blytics.modules.payment.adapter.TransactionAdapter
import com.nbt.blytics.modules.payment.manageac.ManageFragment
import com.nbt.blytics.modules.payment.models.UserProfileInfoResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.singletransaction.SingleTransactionFragment
import com.nbt.blytics.modules.transactionhistory.TransactionRequest
import com.nbt.blytics.modules.transactionhistory.TransactionResponse
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.SharePreferences
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.setBgColorHappyTheme
import com.nbt.blytics.utils.setTextColorHappyTheme
import com.nbt.blytics.utils.show
import java.util.Locale

class PaymentFragment : BaseFragment<PaymentFragmentBinding, PaymentViewModel>() {
    private val homeViewModel: PaymentViewModel by viewModels()
    private lateinit var binding: PaymentFragmentBinding
    var accountNumber = ""
    var accountPurpose = ""
    private var btnRegular: LinearLayout? = null
    private var btnCurrentAc: LinearLayout? = null
    private var btnLinkedAc: LinearLayout? = null
    private var btnWalletAc: LinearLayout? = null
    private var addAccountButton: MaterialButton? = null
    private var lineRegular: View? = null
    private var lineCurrent: View? = null
    private var lineLinked: View? = null
    private var lintWallet: View? = null
    private lateinit var bottomSheetDialog: BottomSheetDialog
    lateinit var pref: SharePreferences
    private val txnList = mutableListOf<TransactionResponse.Data.Txn>()
    private lateinit var adapter: TransactionAdapter
    companion object {
        var IS_SWITCH_SHOWN = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        pref = SharePreferences.getInstance(requireContext())
        binding.txtAcNumber.text = "A/C No. " + pref.getStringValue(pref.DEFAULT_ACCOUNT, "")
        binding.txtAcNumberBack.text = "A/C No. " + pref.getStringValue(pref.DEFAULT_ACCOUNT, "")
        val accNum = "XXXX" + " " + "XXXX" + " " + "XXXX" + " " + "XXXX"
        binding.txtCardNumber.text = pref.getStringValue(pref.DEFAULT_ACCOUNT, "")//accNum
        binding.txtCardNumberBack.text = "5520 0606 3154 9540"
        binding.txtAcCardPurpose.text = pref.getStringValue(pref.DEFAULT_PURPOSE, "")
        binding.txtAcCardPurposeBack.text = pref.getStringValue(pref.DEFAULT_PURPOSE, "")
        setTransactionRecycler()
        happyThemeChanges()
        observer()
        binding.imgInfo.setOnClickListener {
            (activity as BaseActivity).showLoadingTransperant()
            flipCard(requireContext(), binding.frameLayoutBack, binding.frameLayoutFornt)
        }
        binding.imgInfoEnd.setOnClickListener {
            (activity as BaseActivity).showLoadingTransperant()
            flipCard(requireContext(), binding.frameLayoutFornt, binding.frameLayoutBack)
        }
        binding.btnAddWallet.setOnClickListener {
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.SELF_TRANSACTION.name)
            startActivity(intent)
        }
        binding.btnAddAccount.setOnClickListener {
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.CREATE_AC.name)
            startActivity(intent)
        }
        binding.btnManagerAc.setOnClickListener {
            val intent = Intent(requireContext(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.MANAGE_AC.name)
            intent.putExtra(ManageFragment.AC_TYPE, pref().getStringValue(SharePreferences.DEFAULT_AC_TYPE, ""))
            startActivity(intent)
        }
        binding.btnSwitchAc.setOnClickListener {
            showSwitchDialog()
        }
        binding.btnShowQr.setOnClickListener {
            val intent = Intent(requireContext(), QrCodeActivity::class.java)
            intent.putExtra(Constants.SHOW_QR, Constants.AnsYN.YES.name)
            startActivity(intent)
        }
        binding.btnWalletBank.setOnClickListener {
            val intent = Intent(requireContext(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.AC_TO_BANK.name)
            startActivity(intent)
        }
        binding.txtSeeAllTransaction.setOnClickListener {
            val intent = Intent(requireContext(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.TRANSACTION_HISTORY.name)
            startActivity(intent)
        }
        valueChangeLiveData.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    pref().apply {
                        binding.apply {
                            txtUserName.text = "${getStringValue(USER_FIRST_NAME, "")} ${getStringValue(USER_LAST_NAME, "")}"
                            txtAcHolderName.text = getStringValue(USER_FIRST_NAME, "")
                            //  valueChangeLiveData.value =false
                        }
                    }
                }
                false -> {
                    // Handle false case (if necessary)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentAc = pref().getStringValue(SharePreferences.DEFAULT_AC_TYPE, "").lowercase(Locale.getDefault()) + " account"
        binding.lblCurrentAc.text = currentAc[0].uppercase() + currentAc.lowercase().substring(1)
        binding.lblCurrentAcBack.text = currentAc[0].uppercase() + currentAc.lowercase().substring(1)
        homeViewModel.getBalance(
            pref().getStringValue(SharePreferences.USER_ID, ""),
            pref().getStringValue(SharePreferences.USER_TOKEN, ""),
            pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
        )
        if (pref().getStringValue(SharePreferences.DEFAULT_AC_TYPE, "").equals(SharePreferences.AcType.WALLET.name, true)) {
            //binding.btnManagerAc.hide()
            binding.txtAcCardPurpose.visibility = View.INVISIBLE
        } else {
            //  binding.btnManagerAc.show()
            binding.txtAcCardPurpose.visibility = View.VISIBLE
        }
    }

    private fun getTxn() {
        showLoading()
        val userId = pref().getStringValue(SharePreferences.USER_ID, "").toInt()
     //   homeViewModel.transactionHomeHistory(userId)
        homeViewModel.transactionHistory(
            TransactionRequest(
                pref().getStringValue(SharePreferences.USER_ID, ""),
                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                frequentPayee = "False",
                reverse = 1
            )
        )
    }

    private fun observer() {
        homeViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is AcInfoResponse -> {
                    hideLoading()
                    var isRegular: Boolean = false
                    var isCurrent: Boolean = false
                    var isLinkend: Boolean = false
                    for (i in it.list.indices) {
                        if (it.list[i].acc_type.equals("saving", true)) {
                            isRegular = true
                        }
                        if (it.list[i].acc_type.equals("current", true)) {
                            isCurrent = true
                        }
                        if (it.list[i].acc_type.equals("linked", true)) {
                            isLinkend = true
                        }
                    }
                    //lintWallet?.show()
                    if (isRegular) {
                        btnRegular?.show()
                        lineRegular?.show()
                    } else {
                        lineRegular?.hide()
                        btnRegular?.hide()
                    }
                    if (isCurrent) {
                        btnCurrentAc?.show()
                        lineCurrent?.show()
                    } else {
                        lineCurrent?.hide()
                        btnCurrentAc?.hide()
                    }
                    if (isLinkend) {
                        btnLinkedAc?.show()
                        lineLinked?.show()
                    } else {
                        lineLinked?.hide()
                        btnLinkedAc?.hide()
                    }
                    homeViewModel.observerResponse.value = null
                }

                is WalletAccountModel -> {
                    homeViewModel.observerResponse.value = null
                    hideLoading()
                    if (it.data.isNotEmpty()) {
                        pref().setStringValue(SharePreferences.USER_WALLET_UUID, it.data[0].acc_uuid)
                        pref().setStringValue(SharePreferences.DEFAULT_ACCOUNT, it.data[0].acc_no)
                        pref().setStringValue(SharePreferences.DEFAULT_PURPOSE, "")
                        pref().setStringValue(SharePreferences.DEFAULT_AC_TYPE, SharePreferences.AcType.WALLET.name)
                        binding.txtAcCardPurpose.visibility = View.INVISIBLE
                        HomeFragment.IS_SWITCH_SHOWN = true
                        requireActivity().finish()
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                    }
                    homeViewModel.observerResponse.value = null
                }
                is TransactionResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        txnList.clear()
                        txnList.addAll(it.data!!.txnList)
                        adapter.notifyDataSetChanged()
                    }
                    if (txnList.isEmpty()) {
                        binding.txtNoTxn.show()
                        binding.txtSeeAllTransaction.hide()
                    } else {
                        binding.txtNoTxn.hide()
                        binding.txtSeeAllTransaction.show()
                    }
                    homeViewModel.observerResponse.value = null
                }

                is BalanceResponse -> {
                    binding.txtUserAmount.text = Constants.DEFAULT_CURRENCY + it.data.balance
                    binding.txtAcCardBalance.text = Constants.DEFAULT_CURRENCY + it.data.balance
                    try {
                        (requireActivity() as MainActivity).setCurrentBalance(Constants.DEFAULT_CURRENCY + it.data.balance)
                    } catch (ex: Exception) {
                    }
                    getTxn()
                    homeViewModel.observerResponse.value = null
                }

                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    homeViewModel.observerResponse.value = null
                }
            }
        }
    }

    private fun saveProfileData(data: UserProfileInfoResponse.Data) {
        data.apply {
            pref().apply {
                setStringValue(USER_ID, userId)
                setStringValue(USER_MOBILE_NUMBER, mobNo)
                setBooleanValue(USER_MOBILE_VERIFIED, mobVerified)
                setStringValue(USER_EMAIL, email)
                setBooleanValue(USER_EMAIL_VERIFIED, emailVerified)
                setStringValue(USER_ADDRESS, address)
                setStringValue(USER_COUNTRY, country)
                setStringValue(USER_BVN, bvn)
                setBooleanValue(USER_BVN_VERIFIED, bvnVerified)
                setBooleanValue(USER_DOC_VERIFIED, docVerified)
                setStringValue(USER_PROFILE_STATUS, profileStatus)
                setStringValue(USER_FIRST_NAME, firstName)
                setStringValue(USER_LAST_NAME, lastName)
                setStringValue(USER_DOB, dob)
                setStringValue(USER_STATE, state)
                setStringValue(USER_PIN_CODE, pincode)
                if (data.avatar.avatarImage.isNotBlank()) {
                    setStringValue(USER_PROFILE_IMAGE, avatar.avatarImage)
                    (requireActivity() as MainActivity).setProfileImage(data.avatar.avatarImage)
                } else {
                    setStringValue(USER_PROFILE_IMAGE, avatar.defaultAvatar)
                    (requireActivity() as MainActivity).setProfileImage(data.avatar.defaultAvatar)
                }
            }
        }
    }

    private fun setTransactionRecycler() {
        adapter = TransactionAdapter(requireContext(), txnList) { position ->
            val data = txnList[position]
            if (data.bankType.equals("Internal", true)) {
                val intent = Intent(requireActivity(), BconfigActivity::class.java)
                intent.putExtra(Constants.COMING_FOR, ComingFor.TRANSACTION_DETAILS.name)
                intent.putExtra(SingleTransactionFragment.NAME, data.userName)
                intent.putExtra(SingleTransactionFragment.TOTAL_AMOUNT_SENT, data.amount)
                intent.putExtra(SingleTransactionFragment.TOTAL_AMOUNT_RECEIVER, data.amount)
                intent.putExtra(SingleTransactionFragment.USER_IMG_URL, data.userImage)
                intent.putExtra(SingleTransactionFragment.TRANSACTION_USER_ID, data.userId)
                intent.putExtra(SingleTransactionFragment.BANK_TYPE, data.bankType)
                startActivity(intent)
            } else {
                val intent = Intent(requireActivity(), BconfigActivity::class.java)
                intent.putExtra(Constants.COMING_FOR, ComingFor.TRANSACTION_DETAILS.name)
                intent.putExtra(SingleTransactionFragment.NAME, data.userName)
                intent.putExtra(SingleTransactionFragment.TOTAL_AMOUNT_SENT, data.amount)
                intent.putExtra(SingleTransactionFragment.TOTAL_AMOUNT_RECEIVER, data.amount)
                intent.putExtra(SingleTransactionFragment.USER_IMG_URL, data.userImage)
                intent.putExtra(SingleTransactionFragment.TRANSACTION_USER_ID, data.bank_acc)
                intent.putExtra(SingleTransactionFragment.BANK_TYPE, data.bankType)
                startActivity(intent)
            }
        }
        binding.rvTransaction.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvTransaction.adapter = adapter
    }

    private fun showSwitchDialog() {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_switch)
        btnRegular = dialog.findViewById(R.id.btn_regular)
        btnCurrentAc = dialog.findViewById(R.id.btn_current_ac)
        btnLinkedAc = dialog.findViewById(R.id.btn_linked_ac)
        btnWalletAc = dialog.findViewById(R.id.btn_wallet_ac)
        addAccountButton = dialog.findViewById(R.id.addAccountButton)
        val toolbar = dialog.findViewById<MaterialToolbar>(R.id.toolbar_top)
        lineRegular = dialog.findViewById<MaterialToolbar>(R.id.line_regular)
        lineCurrent = dialog.findViewById<MaterialToolbar>(R.id.line_current)
        lineLinked = dialog.findViewById<MaterialToolbar>(R.id.line_linked)
        lintWallet = dialog.findViewById<MaterialToolbar>(R.id.lint_wallet)
        btnRegular?.hide()
        btnCurrentAc?.hide()
        btnLinkedAc?.hide()
        lineRegular?.hide()
        lineCurrent?.hide()
        lineLinked?.hide()
        val topLayoutRelation = dialog.findViewById<ConstraintLayout>(R.id.top_layout_relation)
        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_baseline_arrow_back_24)
        toolbar.title = "           Switch account"
        toolbar.setNavigationOnClickListener {
            dialog.dismiss()
        }

        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                toolbar.setBackgroundResource(R.color.black)
                toolbar.setTitleTextColor(resources.getColor(R.color.white))
                toolbar.textAlignment = View.TEXT_ALIGNMENT_CENTER
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                toolbar.setBackgroundResource(R.color.white)
                toolbar.setTitleTextColor(resources.getColor(R.color.b_currency_blue))
                toolbar.textAlignment = View.TEXT_ALIGNMENT_CENTER
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                toolbar.setBackgroundResource(R.color.white)
                toolbar.setTitleTextColor(resources.getColor(R.color.b_currency_blue))
                toolbar.textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
        }

        btnRegular?.setOnClickListener {
            IS_SWITCH_SHOWN = true
            dialog.dismiss()
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.ALL_AC.name)
            intent.putExtra(Constants.ACC_TYPE, Constants.SAVING_ACC)
            startActivity(intent)
        }
        btnCurrentAc?.setOnClickListener {
            IS_SWITCH_SHOWN = true
            dialog.dismiss()
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.ALL_AC.name)
            intent.putExtra(Constants.ACC_TYPE, Constants.CURRENT_ACC)
            startActivity(intent)
        }
        btnLinkedAc?.setOnClickListener {
            IS_SWITCH_SHOWN = true
            dialog.dismiss()
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.ALL_AC.name)
            intent.putExtra(Constants.ACC_TYPE, Constants.LINKED_ACC)
            startActivity(intent)
        }

        addAccountButton?.setOnClickListener {
            IS_SWITCH_SHOWN = true
            dialog.dismiss()
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.CREATE_AC.name)
            startActivity(intent)
        }

        btnWalletAc?.setOnClickListener {
            dialog.dismiss()
            val selectAcName = "Wallet Account"
            HomeFragment.SELECTED_AC_TYPE = selectAcName
            binding.lblCurrentAc.text = selectAcName
            showLoading()
            homeViewModel.getAllAccount(pref().getStringValue(SharePreferences.USER_ID, ""), pref().getStringValue(SharePreferences.USER_TOKEN, ""), "wallet")
        }
        dialog.show()
        showLoading()
        homeViewModel.getAllAc(AcInfoRequest(pref().getStringValue(SharePreferences.USER_ID, ""), pref().getStringValue(SharePreferences.USER_TOKEN, "")))
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                dialog.findViewById<ConstraintLayout>(R.id.top_layout_relation).setBackgroundResource(R.color.b_bg_color_dark)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                dialog.findViewById<ConstraintLayout>(R.id.top_layout_relation).setBackgroundResource(R.color.b_bg_color_light)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                dialog.findViewById<ConstraintLayout>(R.id.top_layout_relation).setBackgroundResource(R.color.b_bg_color_light)
            }
        }
    }

    private fun accountTypeDialog() {
        bottomSheetDialog = BottomSheetDialog(requireContext()).apply {
            DataBindingUtil.inflate<BottomSheetSwitchAcBinding>(layoutInflater, R.layout.bottom_sheet_switch_ac, null, false).apply {
                setContentView(root)
                btnCancel.setOnClickListener {
                    dismiss()
                }
                //var selectAcName = ""
                btnSavingAc.setOnClickListener {
                    // selectAcName = "Saving Account"
                    /*     binding.lblCurrentAc.text = selectAcName
                         HomeFragment.SELECTED_AC_TYPE = selectAcName*/
                    /*  val bundle = bundleOf(Constants.ACC_TYPE to Constants.SAVING_ACC)
                      findNavController().navigate(R.id.allAccountFragment,bundle)*/
                }
                btnCurrentAc.setOnClickListener {
                    // selectAcName = "Current Account"
                    /*binding.lblCurrentAc.text = selectAcName
                    HomeFragment.SELECTED_AC_TYPE =selectAcName*/
                    dismiss()
                    IS_SWITCH_SHOWN = true/*   val bundle = bundleOf(Constants.ACC_TYPE to Constants.CURRENT_ACC)
                       findNavController().navigate(R.id.allAccountFragment,bundle)*/
                    val intent = Intent(requireActivity(), BconfigActivity::class.java)
                    intent.putExtra(Constants.COMING_FOR, ComingFor.ALL_AC.name)
                    intent.putExtra(Constants.ACC_TYPE, Constants.CURRENT_ACC)
                    startActivity(intent)
                }
                btnLinkedAc.setOnClickListener {
                    //selectAcName = "Linked Account"
                    /* HomeFragment.SELECTED_AC_TYPE =selectAcName
                     binding.lblCurrentAc.text = selectAcName*/
                    dismiss()
                    IS_SWITCH_SHOWN = true
                    // requireActivity().finish()
                    val intent = Intent(requireActivity(), BconfigActivity::class.java)
                    intent.putExtra(Constants.COMING_FOR, ComingFor.ALL_AC.name)
                    intent.putExtra(Constants.ACC_TYPE, Constants.LINKED_ACC)
                    startActivity(intent)
                }
                btnWalletAc.setOnClickListener {
                    val selectAcName = "Wallet Account"
                    HomeFragment.SELECTED_AC_TYPE = selectAcName
                    binding.lblCurrentAc.text = selectAcName
                    showLoading()
                    homeViewModel.getAllAccount(pref().getStringValue(SharePreferences.USER_ID, ""), pref().getStringValue(SharePreferences.USER_TOKEN, ""), "wallet")
                }
            }
            setCancelable(true)
            show()
        }
    }

    private fun happyThemeChanges() {
        if (isCustomMode) {
            binding.apply {
                topLayout.setBgColorHappyTheme()
                txtUserAmount.setTextColorHappyTheme()
                lblWalletBank.setTextColorHappyTheme()
                lblShowQr.setTextColorHappyTheme()
                lblWallet.setTextColorHappyTheme()
            }
        }

        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.debitImage.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.bg_debit_cart_orange))
                binding.debitImageBack.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.bg_debit_cart_orange))
                binding.mainLayout.setBackgroundResource(R.color.black)
                binding.card1.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_dark))
                binding.card2.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_dark))
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.debitImage.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.bg_debit_cart))
                binding.debitImageBack.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.bg_debit_cart))
                binding.mainLayout.setBackgroundResource(R.color.gray_bg)
                binding.card1.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_light))
                binding.card2.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_light))
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.debitImage.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.bg_debit_cart))
                binding.debitImageBack.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.bg_debit_cart))
                binding.mainLayout.setBackgroundResource(R.color.gray_bg)
                binding.card1.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_light))
                binding.card2.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_light))
                // binding.imgAddWallet.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }
    }
    private fun flipCard(context: Context, visibleView: View, inVisibleView: View) {
        try {
            visibleView.show()
            // imgBtn.isClickable = false
            val scale = context.resources.displayMetrics.density
            val cameraDist = 11000 * scale
            visibleView.cameraDistance = cameraDist
            inVisibleView.cameraDistance = cameraDist
            val flipOutAnimatorSet = AnimatorInflater.loadAnimator(context, R.animator.front_animator) as AnimatorSet
            flipOutAnimatorSet.setTarget(inVisibleView)
            val flipInAnimatorSet = AnimatorInflater.loadAnimator(context, R.animator.back_animator) as AnimatorSet
            flipInAnimatorSet.setTarget(visibleView)
            flipOutAnimatorSet.start()
            flipInAnimatorSet.start()
            flipInAnimatorSet.doOnEnd {}
            flipOutAnimatorSet.doOnEnd {
                (activity as BaseActivity).hideLoadingTransperant()
                inVisibleView.hide()
            }
        } catch (e: Exception) {
            Log.d("CARD--Error-", e.message.toString())
        }
    }

    override fun getLayoutId(): Int = R.layout.payment_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): PaymentViewModel = homeViewModel
}