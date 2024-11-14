package com.nbt.blytics.modules.sqverify

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.SecurityQuestionVerityFragmentBinding
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.sqverify.adapter.SqVerifyAdapter
import com.nbt.blytics.modules.sqverify.models.SQVerifyResponse
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.show

class SecurityQuestionVerityFragment :
    BaseFragment<SecurityQuestionVerityFragmentBinding, SecurityQuestionVerityViewModel>() {
    private var userID: String? = ""
    private var userWalletUUID: String? = ""
    private var isComingFor: String? = ""
    private var phoneNumber: String? = ""
    private val sqVerityViewModel: SecurityQuestionVerityViewModel by viewModels()
    private lateinit var binding: SecurityQuestionVerityFragmentBinding
    private val questionList = mutableListOf<SQVerifyResponse.Data.QuesAn>()
    private lateinit var sqVerifyAdapter: SqVerifyAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()
        observer()
        setAdapter()
        userID = requireArguments().getString(Constants.USER_ID)
        userWalletUUID = requireArguments().getString(Constants.USER_WALLET_UUID)
        isComingFor = requireArguments().getString(Constants.COMING_FOR)
        phoneNumber = requireArguments().getString(Constants.PHONE_NUMBER)
        binding.btnVerify.hide()
        userID?.let {uID ->
            if (uID.isNotBlank()) {
                showLoading()
                sqVerityViewModel.getQuestion(uID)
            }

        }

        binding.btnVerify.setOnClickListener {

            for (i in questionList.indices) {
                if(questionList[i].userEnterAns.isNotBlank()){
                if (questionList[i].ans.equals(questionList[i].userEnterAns, true).not()) {
                    showToast("Incorrect answer ${i+1}")
                    return@setOnClickListener
                }
                }else{
                    showToast("Answer all question.")
                    return@setOnClickListener
                }
            }

            findNavController().navigate(R.id.action_securityQuestionVerityFragment2_to_changeTipFragment,
                bundleOf(
                    Constants.USER_ID to userID,
                    Constants.USER_WALLET_UUID to userWalletUUID,
                    Constants.COMING_FOR to isComingFor,
                    Constants.PHONE_NUMBER to phoneNumber,
                ))


        }

    }

    private fun setAdapter() {
        sqVerifyAdapter = SqVerifyAdapter(requireContext(), questionList) {

        }
        binding.rvQuestion.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.rvQuestion.adapter = sqVerifyAdapter
    }

    private fun observer() {
        sqVerityViewModel.observerResponse.observe(viewLifecycleOwner, {
            when (it) {
                is SQVerifyResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.let { sqData ->
                            questionList.clear()
                            questionList.addAll(sqData.quesAns)
                            sqVerifyAdapter.notifyDataSetChanged()
                            if(sqData.quesAns.isNotEmpty()){
                            binding.btnVerify.show()
                            }
                        }
                    } else {
                        showToast(it.message)
                    }
                    sqVerityViewModel.observerResponse.value =null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    sqVerityViewModel.observerResponse.value =null

                }
                is Throwable ->{
                    hideLoading()
                    sqVerityViewModel.observerResponse.value = null
                }
            }
        })
    }


    private fun happyThemeChanges() {
        if (BaseActivity.isCustomMode) {
            binding.apply {
                topLayout.setBackgroundResource(R.color.yellow_500)
            }
        }
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_black)
                binding.btnVerify.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_white)
                binding.btnVerify.setBackgroundResource(R.drawable.bg_gradient_btn)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_white)
                binding.btnVerify.setBackgroundResource(R.drawable.bg_gradient_btn)
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.security_question_verity_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): SecurityQuestionVerityViewModel = sqVerityViewModel

}