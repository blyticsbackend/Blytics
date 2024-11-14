package com.nbt.blytics.modules.apps

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.viewModels
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.AppsFragmentBinding
import com.nbt.blytics.utils.SharePreferences
import com.nbt.blytics.utils.THEME
import com.nbt.blytics.utils.setImage
import com.nbt.blytics.utils.switchToThemeMode

class AppsFragment : BaseFragment<AppsFragmentBinding, AppsViewModel>(),
    CompoundButton.OnCheckedChangeListener {
    private val appsViewModel: AppsViewModel by viewModels()
    private lateinit var binding: AppsFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        hideLoading()
        binding.apply {
            imgProfile.setImage(pref().getStringValue(SharePreferences.USER_PROFILE_IMAGE, ""))
            txtUserName.text = "${pref().getStringValue(SharePreferences.USER_FIRST_NAME, "")} ${pref().getStringValue(SharePreferences.USER_LAST_NAME, "")}"
            btnSwSystemLock.isChecked = pref().getBooleanValue(SharePreferences.SYSTEM_LOCK_IS_ACTIVE)
            btnSwPassword.isChecked = pref().getBooleanValue(SharePreferences.PASSWORD_LOCK_IS_ACTIVE)
            btnSwSms.isChecked = pref().getBooleanValue(SharePreferences.SMS_NOTIFICATION)
            btnSwWhatsapp.isChecked = pref().getBooleanValue(SharePreferences.WHATSAPP_NOTIFICATION)
          //  btnSwLight.isChecked = pref().getBooleanValue(SharePreferences.SYSTEM_MODE_DAY)
          //  btnSwDarkMode.isChecked = pref().getBooleanValue(SharePreferences.SYSTEM_MODE_NIGHT)
            //fillValueText()

            val isDarkModeActive = pref().getBooleanValue(SharePreferences.SYSTEM_MODE_NIGHT)
            btnSwLightDarkMode.isChecked = isDarkModeActive

            btnSwSystemLock.setOnCheckedChangeListener(this@AppsFragment)
            btnSwPassword.setOnCheckedChangeListener(this@AppsFragment)
            btnSwSms.setOnCheckedChangeListener(this@AppsFragment)
            btnSwWhatsapp.setOnCheckedChangeListener(this@AppsFragment)
            btnSwLightDarkMode.setOnCheckedChangeListener(this@AppsFragment)
            //btnSwLight.setOnCheckedChangeListener(this@AppsFragment)
            //btnSwDarkMode.setOnCheckedChangeListener(this@AppsFragment)
            /*  btnSwSystemLock.setOnClickListener {
                btnSwPassword.isChecked = false
              }
              btnSwPassword.setOnClickListener {
                  btnSwSystemLock.isChecked = false
                  pref().setBooleanValue(pref().PASSWORD_LOCK_IS_ACTIVE, true)
                  pref().setBooleanValue(pref().SYSTEM_LOCK_IS_ACTIVE, false)
              }*/
        }
        happyThemeChanges()
    }
    private fun toggleTheme(isDarkMode: Boolean) {
        binding.apply {
            if (isDarkMode) {
                pref().setBooleanValue(SharePreferences.SYSTEM_MODE_NIGHT, true)
                pref().setBooleanValue(SharePreferences.SYSTEM_MODE_DAY, false)
                (requireActivity() as BconfigActivity).switchToThemeMode(THEME.MODE_NIGHT)
            } else {
                pref().setBooleanValue(SharePreferences.SYSTEM_MODE_NIGHT, false)
                pref().setBooleanValue(SharePreferences.SYSTEM_MODE_DAY, true)
                (requireActivity() as BconfigActivity).switchToThemeMode(THEME.MODE_DEFAULT)
            }
            pref().setBooleanValue(SharePreferences.IS_MODE_SELECTED, true)
            pref().setIntValue(SharePreferences.SELECTED_MODE, if (isDarkMode) 1 else 0)
        }
    }
    override fun getLayoutId(): Int = R.layout.apps_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): AppsViewModel = appsViewModel

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        binding.apply {
            when (buttonView?.id) {
                R.id.btnSwLightDarkMode -> {
                    toggleTheme(isChecked)
                }

                R.id.btn_sw_system_lock -> {
                    if (btnSwSystemLock.isChecked) {
                        btnSwPassword.isChecked = false
                        pref().setBooleanValue(pref().SYSTEM_LOCK_IS_ACTIVE, true)
                    } else {
                        pref().setBooleanValue(pref().SYSTEM_LOCK_IS_ACTIVE, false)
                    }
                }

                R.id.btn_sw_password -> {
                    if (btnSwPassword.isChecked) {
                        btnSwSystemLock.isChecked = false
                        pref().setBooleanValue(pref().PASSWORD_LOCK_IS_ACTIVE, true)
                    } else {
                        pref().setBooleanValue(pref().PASSWORD_LOCK_IS_ACTIVE, false)
                    }
                }

                R.id.btn_sw_sms -> {
                    if (btnSwSms.isChecked) {
                        pref().setBooleanValue(pref().SMS_NOTIFICATION, true)
                    } else {
                        pref().setBooleanValue(pref().SMS_NOTIFICATION, false)
                    }
                }

                R.id.btn_sw_whatsapp -> {
                    if (btnSwWhatsapp.isChecked) {
                        pref().setBooleanValue(pref().WHATSAPP_NOTIFICATION, true)
                    } else {
                        pref().setBooleanValue(pref().WHATSAPP_NOTIFICATION, false)
                    }
                }/*  R.id.btn_sw_email -> {
                      if(btnSwEmail.isChecked){
                          pref().setBooleanValue(pref().EMAIL_NOTIFICATION,true)
                      }else {
                          pref().setBooleanValue(pref().EMAIL_NOTIFICATION, false)
                      }
                  }*/
              /*  R.id.btn_sw_light -> {
                    if (btnSwLight.isPressed) {
                        if (btnSwLight.isChecked) {
                         //   btnSwDarkMode.isChecked = false
                            pref().setBooleanValue(pref().SYSTEM_MODE_DAY, true)
                            (requireActivity() as BconfigActivity).switchToThemeMode(THEME.MODE_DEFAULT)
                            pref().setBooleanValue(SharePreferences.IS_MODE_SELECTED, true)
                            pref().setIntValue(SharePreferences.SELECTED_MODE, 0)
                        } else {
                          //  btnSwDarkMode.isChecked = true
                            pref().setBooleanValue(pref().SYSTEM_MODE_DAY, false)
                            pref().setBooleanValue(pref().SYSTEM_MODE_NIGHT, true)
                            (requireActivity() as BconfigActivity).switchToThemeMode(THEME.MODE_NIGHT)
                            pref().setBooleanValue(SharePreferences.IS_MODE_SELECTED, true)
                            pref().setIntValue(SharePreferences.SELECTED_MODE, 1)
                        }
                    }
                }*/

              /*  R.id.btn_sw_dark_mode -> {
                    if (btnSwDarkMode.isPressed) {
                        if (btnSwDarkMode.isChecked) {
                            btnSwLight.isChecked = false
                            pref().setBooleanValue(pref().SYSTEM_MODE_NIGHT, true)
                            pref().setBooleanValue(pref().SYSTEM_MODE_DAY, false)
                            (requireActivity() as BconfigActivity).switchToThemeMode(THEME.MODE_NIGHT)
                            pref().setBooleanValue(SharePreferences.IS_MODE_SELECTED, true)
                            pref().setIntValue(SharePreferences.SELECTED_MODE, 1)
                        } else {
                            btnSwLight.isChecked = true
                            pref().setBooleanValue(pref().SYSTEM_MODE_NIGHT, false)
                            pref().setBooleanValue(pref().SYSTEM_MODE_DAY, true)
                            (requireActivity() as BconfigActivity).switchToThemeMode(THEME.MODE_DEFAULT)
                            pref().setBooleanValue(SharePreferences.IS_MODE_SELECTED, true)
                            pref().setIntValue(SharePreferences.SELECTED_MODE, 0)
                        }
                    } else if (btnSwLight.isPressed) {
                        if (btnSwLight.isChecked) {
                            btnSwDarkMode.isChecked = false
                            pref().setBooleanValue(pref().SYSTEM_MODE_NIGHT, false)
                            pref().setBooleanValue(pref().SYSTEM_MODE_DAY, true)
                            (requireActivity() as BconfigActivity).switchToThemeMode(THEME.MODE_DEFAULT)
                            pref().setBooleanValue(SharePreferences.IS_MODE_SELECTED, true)
                            pref().setIntValue(SharePreferences.SELECTED_MODE, 0)

                        } else {
                            btnSwDarkMode.isChecked = true
                            pref().setBooleanValue(pref().SYSTEM_MODE_NIGHT, true)
                            pref().setBooleanValue(pref().SYSTEM_MODE_DAY, false)
                            (requireActivity() as BconfigActivity).switchToThemeMode(THEME.MODE_NIGHT)
                            pref().setBooleanValue(SharePreferences.IS_MODE_SELECTED, true)
                            pref().setIntValue(SharePreferences.SELECTED_MODE, 1)
                        }
                    }
                }*/
            }
        }
        pref().setBooleanValue(SharePreferences.SET_DEFAULT_APPS, true)
        // setSystemMode()
    }

    fun setSystemMode() {
        binding.apply {
          /*  if (btnSwLight.isChecked) {
                (requireActivity() as BconfigActivity).switchToThemeMode(THEME.MODE_DEFAULT)
                pref().setBooleanValue(SharePreferences.IS_MODE_SELECTED, true)
                pref().setIntValue(SharePreferences.SELECTED_MODE, 0)
            }*/
          /*  if (btnSwDarkMode.isChecked) {
                (requireActivity() as BconfigActivity).switchToThemeMode(THEME.MODE_NIGHT)
                pref().setBooleanValue(SharePreferences.IS_MODE_SELECTED, true)
                pref().setIntValue(SharePreferences.SELECTED_MODE, 1)
            }*/
        }
    }

    private fun happyThemeChanges() {
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_dark)
                /*     binding.txtTitle1.setBackgroundResource(R.color.b_bg_color_dark)
                       binding.txtTitle2.setBackgroundResource(R.color.b_bg_color_dark)
                       binding.txtTitle3.setBackgroundResource(R.color.b_bg_color_dark)*/
                binding.card1.setCardBackgroundColor(resources.getColor(R.color.black))
                binding.card2.setCardBackgroundColor(resources.getColor(R.color.black))
                binding.card3.setCardBackgroundColor(resources.getColor(R.color.black))
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)/*  binding.txtTitle1.setBackgroundResource(R.color.b_title_bg)
                  binding.txtTitle2.setBackgroundResource(R.color.b_title_bg)
                  binding.txtTitle3.setBackgroundResource(R.color.b_title_bg)*/
                binding.card1.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.card2.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.card3.setCardBackgroundColor(resources.getColor(R.color.white))
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)/* binding.txtTitle1.setBackgroundResource(R.color.b_title_bg)
                 binding.txtTitle2.setBackgroundResource(R.color.b_title_bg)
                 binding.txtTitle3.setBackgroundResource(R.color.b_title_bg)*/
                binding.card1.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.card2.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.card3.setCardBackgroundColor(resources.getColor(R.color.white))
            }
        }
    }
}