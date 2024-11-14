package com.nbt.blytics.modules.squpdate

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.UpdateSQFragmentBinding
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.squpdate.model.SqResponse
import com.nbt.blytics.modules.squpdate.model.UpdateSqResponse
import com.nbt.blytics.modules.squpdate.model.UpdateSqlRequest
import com.nbt.blytics.modules.userprofile.models.SQ
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.SharePreferences
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class UpdateSQFragment : BaseFragment<UpdateSQFragmentBinding, UpdateSQViewModel>() {


    private val updateSQViewModel: UpdateSQViewModel by viewModels()
    private lateinit var binding: UpdateSQFragmentBinding

    // private lateinit var sqAdapter :UpdateSqAdapter
    private val sqList = mutableListOf<SqResponse.Question>()
    private val filterSqList = mutableListOf<SqResponse.Question>()

    private var firstSpinnerIsSelected: Boolean = false
    private var secondSpinnerIsSelected: Boolean = false
    var listQuestion = mutableListOf<SQ>()
    lateinit var pref: SharePreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()
        observer()
        setAdapter()
        showLoading()
        updateSQViewModel.getQuestion()
        pref = SharePreferences.getInstance(requireContext())
        listQuestion.clear()
        try {
            val type: Type = object : TypeToken<MutableList<SQ>>() {}.type
            val gson = Gson().fromJson<MutableList<SQ>>(
                pref.getStringValue(pref.USER_SECURITY_QUES, ""), type
            )
            listQuestion.addAll(gson)
        }catch (ex:Exception){

        }

        if (listQuestion.isNotEmpty()) {
            binding.hintText1.text = listQuestion[0].hint
            binding.hintText2.text = listQuestion[1].hint


        }
        if (listQuestion.isNotEmpty()) {
            binding.questionViewFlipper.visibility = View.VISIBLE
            binding.viewFlipper.visibility = View.GONE

        } else {
            binding.questionViewFlipper.visibility = View.GONE

        }


        binding.btnNext1.setOnClickListener {
            if (listQuestion[0].ans.equals(binding.edtSecAns1.text.toString())) {
                questionNextView()

            } else {
                showToast("incorrect answer")
            }
        }
        binding.hint.setOnClickListener {
            binding.hintText1.visibility = View.VISIBLE
        }
        binding.hint1.setOnClickListener {
            binding.hintText2.visibility = View.VISIBLE
        }
        binding.btnNext2.setOnClickListener {
            if (listQuestion[1].ans.equals(binding.edtSecAns2.text.toString())) {
                questionNextView()
                binding.questionViewFlipper.visibility = View.GONE
                binding.viewFlipper.visibility = View.VISIBLE


            } else {
                showToast("incorrect Answer")
            }

        }
        binding.btnNext.setOnClickListener {
            if (firstSpinnerIsSelected.not()) {
                showToast("Select Question")
            } else if (binding.edtAns.text.toString().equals("")) {
                showToast("enter answer")
            } else if (binding.edtAnsHint.text.toString().isBlank()) {
                showToast("enter hint")
            } else {
                nextView()

            }

        }
        binding.btnSave.setOnClickListener {
            pref().apply {
                if (validation()) {


                    val selectedSq = mutableListOf<UpdateSqlRequest.QueData>()
                    binding.apply {
                        for (i in sqList.indices) {
                            if (sqList[i].selected) {
                                selectedSq.add(
                                    UpdateSqlRequest.QueData(
                                        quesNo ="1",
                                        ans = edtAns.text.toString(),
                                        hint = edtAnsHint.text.toString(),
                                        ques = sqList[i].id
                                    )
                                )
                                return@apply
                            }
                        }
                    }
                    binding.apply {
                        for (i in filterSqList.indices) {
                            if (filterSqList[i].selected) {
                                selectedSq.add(
                                    UpdateSqlRequest.QueData(
                                        quesNo ="2",
                                        ans = edtAns2.text.toString(),
                                        hint = edtAnsHint2.text.toString(),
                                        ques = filterSqList[i].id
                                    )
                                )
                                return@apply
                            }
                        }
                    }
                    Log.d("Tag", selectedSq.toString())

                    showLoading()
                    updateSQViewModel.postSQ(
                        UpdateSqlRequest(
                            selectedSq,
                            getStringValue(USER_ID, ""),
                            getStringValue(USER_TOKEN, "")
                        )
                    )
                }

            }
        }

        activity?.onBackPressedDispatcher?.addCallback(
            requireActivity(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {


                    if (binding.questionViewFlipper.isVisible) {
                        if (binding.questionViewFlipper.displayedChild == 0) {
                            remove()
                            requireActivity().onBackPressed()
                        } else {
                            questionBackView()
                        }
                    } else {
                        if (binding.viewFlipper.displayedChild == 0) {
                            remove()
                            requireActivity().onBackPressed()
                        } else {
                            backView()
                        }
                    }


                }
            })

    }

    private fun nextView() {
        binding.viewFlipper.setInAnimation(requireContext(), android.R.anim.slide_in_left)
        binding.viewFlipper.setOutAnimation(requireContext(), android.R.anim.slide_out_right)
        binding.viewFlipper.showNext()
        hideSoftKeyBoard()
    }


    private fun backView() {
        if (binding.viewFlipper.displayedChild > 0) {
            binding.viewFlipper.setInAnimation(requireContext(), android.R.anim.slide_in_left)
            binding.viewFlipper.setOutAnimation(requireContext(), android.R.anim.slide_out_right)
            binding.viewFlipper.showPrevious()
            hideSoftKeyBoard()

        }
    }

    private fun questionNextView() {
        binding.questionViewFlipper.setInAnimation(requireContext(), android.R.anim.slide_in_left)
        binding.questionViewFlipper.setOutAnimation(
            requireContext(),
            android.R.anim.slide_out_right
        )
        binding.questionViewFlipper.showNext()
        hideSoftKeyBoard()
    }


    private fun questionBackView() {
        if (binding.questionViewFlipper.displayedChild > 0) {
            binding.questionViewFlipper.setInAnimation(
                requireContext(),
                android.R.anim.slide_in_left
            )
            binding.questionViewFlipper.setOutAnimation(
                requireContext(),
                android.R.anim.slide_out_right
            )
            binding.questionViewFlipper.showPrevious()
            hideSoftKeyBoard()

        }
    }


    fun validation(): Boolean {
        binding.apply {

            if (firstSpinnerIsSelected.not()) {
                showToast("Select Question")
                return false
            }
            if (secondSpinnerIsSelected.not()) {
                showToast("Select Question")
                return false
            }
            if (edtAns.text.toString().isBlank()) {
                showToast("enter answer")
                return false
            }

            if (edtAnsHint.text.toString().isBlank()) {
                showToast("enter hint")
                return false
            }
            if (edtAns2.text.toString().isBlank()) {
                showToast("enter answer")
                return false
            }
            if (edtAnsHint2.text.toString().isBlank()) {
                showToast("enter hint")
                return false
            }
        }
        return true

    }
    /* override fun onActivityCreated(savedInstanceState: Bundle?) {
         super.onActivityCreated(savedInstanceState)
         observer()
         setAdapter()

        showLoading()
         updateSQViewModel.getQuestion()




         binding.btnSave.setOnClickListener {

             // val sqFinalList =  mutableListOf<UpdateSqlRequest.QueData>()
             *//*  for (i in sqList.indices){
                  if(sqList[i].ans.isNotBlank() && sqList[i].hint.isNotBlank()) {
                      val queData = UpdateSqlRequest.QueData(
                          ans = sqList[i].ans,
                          hint = sqList[i].hint,
                          ques = sqList[i].id
                      )
                      sqFinalList.add(queData)
                  }
              }

              updateSQViewModel.postSQ(
                  UpdateSqlRequest(
                      sqFinalList,
                      userId = userId,
                      userToken = userToken
                  )

              )*//*
        }




    }*/

    /*   private fun filter() {
           for (j in sqList.indices) {
               for (i in userSqList.indices) {
                   if (userSqList[i].ques.toInt() == sqList[j].id) {
                       sqList[j].ans = userSqList[i].ans
                       sqList[j].hint = userSqList[i].hint
                   }
               }
           }
       }*/

    private fun observer() {
        updateSQViewModel.observerResponse.observe(viewLifecycleOwner, {
            when (it) {
                is SqResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.question.let { list ->
                            sqList.clear()
                            sqList.addAll(list)
                            if (listQuestion.isNotEmpty()) {


                                if (listQuestion[0].ques.equals(sqList[0].id.toString(),true)) {
                                    binding.secQuestion1.text = sqList[0].question
                                }
                                if (listQuestion[1].ques.equals(sqList[1].id.toString(),true)){
                                    binding.secQuestion2.text = sqList[1].question

                                }
                            }

                        }
                    }
                    updateSQViewModel.observerResponse.value = null
                }
                is UpdateSqResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        showToast(it.message)
                        val intent = Intent()
                        requireActivity().setResult(RESULT_OK, intent)
                        requireActivity().finish()
                    } else {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    updateSQViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    updateSQViewModel.observerResponse.value = null

                }
            }
        })
    }

    private fun happyThemeChanges() {

        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {

                binding.hint.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.orange_dark
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.hint1.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.orange_dark
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )

                binding.btnSave.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnNext.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnNext1.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnNext2.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.hint.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.black),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.hint1.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.black),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.btnNext1.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnNext2.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnSave.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnNext.setBackgroundResource(R.drawable.bg_gradient_btn)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.hint.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.black),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.hint1.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.black),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.btnSave.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnNext.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnNext1.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnNext2.setBackgroundResource(R.drawable.bg_gradient_btn)
            }
        }
    }


    private fun setAdapter() {

        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.list_item, sqList)
        (binding.firstSpinner as? AutoCompleteTextView)?.setAdapter(arrayAdapter)
        (binding.firstSpinner as? AutoCompleteTextView)?.setOnItemClickListener { parent, view, position, id ->
            for (i in sqList.indices) {
                sqList[i].selected = false
            }
            binding.apply {
                sqList[position].selected = true
                sqList[position].ans = edtAns.text.toString()
                sqList[position].hint = edtAnsHint.text.toString()
            }
            for (i in sqList.indices) {
                if (sqList[i].selected.not()) {
                    filterSqList.add(sqList[i])
                } else {
                    Log.d("Selcted--", sqList[position].toString())
                }
            }
            firstSpinnerIsSelected = true

            Log.d("printListSq", sqList[position].ans)
            Log.d("printListSq", sqList[position].hint)
            val arrayAdapter2 = ArrayAdapter(requireContext(), R.layout.list_item, filterSqList)
            (binding.secondSpinner as? AutoCompleteTextView)?.setAdapter(arrayAdapter2)
            (binding.secondSpinner as? AutoCompleteTextView)?.setOnItemClickListener { parent, view, position, id ->
                for (i in filterSqList.indices) {
                    filterSqList[i].selected = false
                }
                binding.apply {
                    filterSqList[position].selected = true
                    filterSqList[position].ans = edtAns2.text.toString()
                    filterSqList[position].hint = edtAnsHint2.text.toString()
                }

                secondSpinnerIsSelected = true
                Log.d("printListSqfilter", filterSqList[position].ans)
                Log.d("printListSqfilter", filterSqList[position].hint)
            }
        }


    }

    override fun getLayoutId(): Int = R.layout.update_s_q_fragment

    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): UpdateSQViewModel = updateSQViewModel

}