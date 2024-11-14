package com.nbt.blytics.testing.signup

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.RegFragmentBinding
import com.nbt.blytics.utils.hide
import com.nbt.blytics.widget.CircleRecyclerView
import com.nbt.blytics.widget.CircularHorizontalMode
import com.nbt.blytics.widget.ItemViewMode
import java.util.*

import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.nbt.blytics.modules.profile.MyBounceInterpolator
import com.nbt.blytics.modules.profile.SliderAdapter
import com.nbt.blytics.modules.profile.SliderLayoutManager
import com.nbt.blytics.utils.show
import com.nbt.blytics.widget.CircularProgressBar


class RegFragment : BaseFragment<RegFragmentBinding, RegViewModel>() {

    private var mCircleRecyclerView: CircleRecyclerView? = null
    private lateinit var mItemViewMode: ItemViewMode
    private lateinit var mLayoutManager: LinearLayoutManager
    private var mImgList = mutableListOf<Int>()
    private lateinit var viewFlipper:ViewFlipper
    private var mIsNotLoop = false
    private lateinit var tvSelectedItem: TextView
    private lateinit var viewModel: RegViewModel
    private var temp =-1

 private lateinit var binding:RegFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        tvSelectedItem = view.findViewById(R.id.tv_selected_item)
        mCircleRecyclerView = view.findViewById(R.id.circle_rv)
        viewFlipper = view.findViewById(R.id.view_flipper)

        view.findViewById<CircularProgressBar>(R.id.profile_progress_bar).progress = 30f
    /*    val data = (1..7).toList().map { it.toString() } as ArrayList<String>*/


        mItemViewMode = CircularHorizontalMode()
        val padding: Int = ScreenUtils.getScreenWidth(requireContext()) / 2 - ScreenUtils.dpToPx(
            requireContext(),
            40
        )
        mCircleRecyclerView!!.setPadding(padding, 0, padding, 0)
        mCircleRecyclerView!!.layoutManager = SliderLayoutManager(requireContext()).apply {
            callback = object : SliderLayoutManager.OnItemSelectedListener {
                override fun onItemSelected(layoutPosition: Int) {

                        showHideLayout(layoutPosition)
                    Log.d("Tag====", layoutPosition.toString())
                }
            }
        }
        mCircleRecyclerView!!.setViewMode(mItemViewMode)
        mCircleRecyclerView!!.setNeedCenterForce(true)
        mCircleRecyclerView!!.setNeedLoop(false)

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(mCircleRecyclerView!!)

        mCircleRecyclerView!!.adapter = SliderAdapter(requireContext()).apply {
            setData(data)
            callback = object : SliderAdapter.Callback {
                override fun onItemClicked(view: View) {
                    mCircleRecyclerView!!.smoothScrollToPosition(
                        mCircleRecyclerView!!.getChildLayoutPosition(
                            view
                        )
                    )
                }
            }
        }


        binding.btnNameEdit.setOnClickListener {
            binding.txtName.hide()
            binding.layoutName.show()
            binding.layoutLastName.show()
            binding.btnNameSave.show()
            binding.btnNameEdit.hide()

        }

        binding.btnEmailEdit.setOnClickListener {
            binding.txtPhone.hide()
            binding.layoutEmail.show()
            binding.btnEmailSave.show()
            binding.btnEmailEdit.hide()
        }


        binding.btnPhoneEdit.setOnClickListener {
            binding.txtPhone.hide()
            binding.layoutPhone.show()
            binding.btnPhoneSave.show()
            binding.btnPhoneEdit.hide()
        }


        binding.btnAddressEdit.setOnClickListener {
            binding.txtAddress.hide()
            binding.layoutAddress.show()
            binding.btnAddressSave.show()
            binding.btnAddressEdit.hide()
        }

        binding.btnCountryEdit.setOnClickListener {
            binding.txtCountry.hide()
            binding.layoutCountry.show()
            binding.btnCountrySave.show()
            binding.btnCountryEdit.hide()
        }


        binding.btnStateEdit.setOnClickListener {
            binding.txtState.hide()
            binding.layoutState.show()
            binding.btnStateSave.show()
            binding.btnStateEdit.hide()
        }


        binding.btnPincodeEdit.setOnClickListener {
            binding.txtPincode.hide()
            binding.layoutPincode.show()
            binding.btnPincodeSave.show()
            binding.btnPincodeEdit.hide()
        }

        binding.btnDobEdit.setOnClickListener {
            binding.txtDob.hide()
            binding.layoutDob.show()
            binding.btnDobSave.show()
            binding.btnDobEdit.hide()
        }
    }
    fun showHideLayout(index:Int){
        Log.d("Test==", index.toString())
        val myAnim: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        val animationDuration: Double = 0.5 * 1000
        myAnim.duration = animationDuration.toLong()

        val interpolator = MyBounceInterpolator(0.4, 0.2)
        myAnim.interpolator = interpolator
        allViewHide()
        when(index){
            0-> {

                binding.cardName.show()
                binding.txtName.show()
                binding.layoutName.hide()
                binding.btnNameEdit.show()
                binding.layoutLastName.hide()
                binding.btnNameSave.hide()

                if(temp>1){
                    temp =-1
                }else{
                    temp++
                    binding.cardName.startAnimation(myAnim)
                }
            }
            1 ->{
                binding.cardEmail.show()
                binding.txtEmail.show()
                binding.btnEmailEdit.show()
                binding.layoutEmail.hide()
                binding.btnEmailSave.hide()

                if(temp>1){
                    temp =-1

                }else{
                    temp++
                    binding.cardEmail.startAnimation(myAnim)
                }
            }
            2->{
                binding.cardPhone.show()
                binding.txtPhone.show()
                binding.btnPhoneEdit.show()
                binding.layoutPhone.hide()
                binding.btnPhoneSave.hide()
                if(temp>1){
                    temp =-1
                }else{
                    temp++
                    binding.cardPhone.startAnimation(myAnim)
                }
            }
            3->{
                binding.cardAddress.show()
                binding.txtAddress.show()
                binding.btnAddressEdit.show()
                binding.layoutAddress.hide()
                binding.btnAddressSave.hide()
                if(temp>1){
                    temp =-1
                }else{
                    temp++
                    binding.cardPhone.startAnimation(myAnim)
                }
            }

            4->{
                binding.cardCountry.show()
                binding.txtCountry.show()
                binding.btnCountryEdit.show()
                binding.layoutCountry.hide()
                binding.btnCountrySave.hide()
                if(temp>1){
                    temp =-1
                }else{
                    temp++
                    binding.cardCountry.startAnimation(myAnim)
                }
            }

            5->{
                binding.cardState.show()
                binding.txtState.show()
                binding.btnStateEdit.show()
                binding.layoutState.hide()
                binding.btnStateSave.hide()
                if(temp>1){
                    temp =-1
                }else{
                    temp++
                    binding.cardState.startAnimation(myAnim)
                }
            }
            6->{
                binding.cardPincode.show()
                binding.txtPincode.show()
                binding.btnPincodeEdit.show()
                binding.layoutPincode.hide()
                binding.btnPincodeSave.hide()
                if(temp>1){
                    temp =-1
                }else{
                    temp++
                    binding.cardPincode.startAnimation(myAnim)
                }
            }

            7->{
                binding.cardDob.show()
                binding.txtDob.show()
                binding.btnDobEdit.show()
                binding.layoutDob.hide()
                binding.btnDobSave.hide()
                if(temp>1){
                    temp =-1
                }else{
                    temp++
                    binding.cardDob.startAnimation(myAnim)
                }
            }

            else ->{
                temp =-1
            }
        }
    }

    private fun allViewHide(){
        binding.apply {
            val listCardView = mutableListOf<View>(cardName, cardEmail, cardPhone, cardAddress, cardCountry, cardState, cardPincode,cardDob)
            for(i in listCardView.indices){
               listCardView[i].hide()
            }
        }
    }


    class ScreenUtils {
        companion object {

            fun getScreenWidth(context: Context): Int {
                val windowManager =
                    context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val dm = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(dm)
                return dm.widthPixels
            }

            fun dpToPx(context: Context, value: Int): Int {
                return TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    value.toFloat(),
                    context.resources.displayMetrics
                ).toInt()
            }
        }
    }

    private fun backView() {
        if (viewFlipper.displayedChild > 0) {
            viewFlipper.setInAnimation(requireContext(), android.R.anim.slide_in_left)
            viewFlipper.setOutAnimation(requireContext(), android.R.anim.slide_out_right)
            viewFlipper.showPrevious()
        }
    }
    private fun nextView() {
        viewFlipper.setInAnimation(requireContext(), android.R.anim.slide_in_left)
        viewFlipper.setOutAnimation(requireContext(), android.R.anim.slide_out_right)
        viewFlipper.showNext()

    }

    private val regViewModel :RegViewModel by viewModels()
    override fun getLayoutId(): Int =R.layout.reg_fragment
    override fun getBindingVariable(): Int =0
    override fun getViewModel(): RegViewModel = regViewModel
}

