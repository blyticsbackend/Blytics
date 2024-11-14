package com.nbt.blytics.testing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.biometric.BiometricPrompt
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.annotations.Until
import com.nbt.blytics.R


/**
 * Created bynbton 07-10-2021
 */
class AuthBottomSheetDialog(val callback:()-> Unit): BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.layout_auth_verificaition, container, false)
        val button = v.findViewById<Button>(R.id.btn_auth)
        val title = v.findViewById<TextView>(R.id.title)
        val subtitle = v.findViewById<TextView>(R.id.subtitle)

        title.text =  "Sign in with your biometric?"
        subtitle.text =  "Use your biometric to authenticate with Blytics."

        button.setOnClickListener {
            callback()
        }
        return v
    }
}