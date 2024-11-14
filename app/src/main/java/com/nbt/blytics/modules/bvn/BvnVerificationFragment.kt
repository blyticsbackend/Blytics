package com.nbt.blytics.modules.bvn

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.nbt.blytics.activity.main.MainActivity
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.BvnVerificationFragmentBinding
import com.nbt.blytics.modules.bvn.models.BvnRegisterResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.SharePreferences
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class BvnVerificationFragment :
    BaseFragment<BvnVerificationFragmentBinding, BvnVerificationViewModel>() {

    private val TAG = "BaseFragment==="
    private lateinit var binding: BvnVerificationFragmentBinding
    private val bvnVerificationViewModel: BvnVerificationViewModel by viewModels()
    private var isDoc1Selected = false
    private var document1: File? = null
    private var document2: File? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()
        observer()


        binding.btnDoc1.setOnClickListener {
            isDoc1Selected = true
            checkPermissions()
        }
        binding.btnDoc2.setOnClickListener {
            isDoc1Selected = false
            checkPermissions()
        }

        binding.btnContinue.setOnClickListener {
            if(validation()) {
                showLoading()
                val userId = pref().getStringValue(SharePreferences.USER_ID, "")
                val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
                if (userId.isNotBlank()) {
                    bvnVerificationViewModel.registerBVN(
                        userId,
                        binding.edtBvn.text.toString().trim(),
                        userToken,
                        document1!!,
                        document2!!
                    )
                }

            }
        }
        binding.btnSkip.setOnClickListener {
            getToMainActivity()
        }
    }

    private fun validation(): Boolean {
        if(binding.edtBvn.text.toString().isBlank()){
            showToast("Enter BVN.")
            return false
        }

        if (document1 == null) {
            showToast("Select ID proof")
            return false
        }
        if (document2 == null) {
            showToast("Select address proof")
            return false
        }
        return true

    }

    private fun observer() {
        bvnVerificationViewModel.observerResponse.observe(viewLifecycleOwner, {
            when (it) {

                is BvnRegisterResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        showToast(it.message)
                        getToMainActivity()
                    } else {
                        showToast(it.message)
                    }
                    bvnVerificationViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    bvnVerificationViewModel.observerResponse.value = null
                }
            }
        })
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d(TAG, "${it.key} = ${it.value}")
            }
            if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true && permissions[Manifest.permission.CAMERA] == true) {
                Log.d(TAG, "Permission granted")

                fetchFile()
            } else {
                Log.d(TAG, "Permission not granted")
            }
        }

    private fun checkPermissions() {
        if (requireContext().let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Request Permissions")
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        } else {
            if (requireContext().let {
                    ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.CAMERA
                    )
                } != PackageManager.PERMISSION_GRANTED) {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                )
            } else {

                fetchFile()
                Log.d(TAG, "Permission Already Granted")
            }
        }
    }

    private fun fetchFile() {

        startActivityForResult(
            Intent.createChooser(
                getFileChooserIntentForImageAndPdf(),
                "Select File"
            ), Constants.GALLERY
        )

    }

    private fun getFileChooserIntentForImageAndPdf(): Intent {
        val mimeTypes = arrayOf("image/*", "application/pdf")
        return Intent(Intent.ACTION_GET_CONTENT)
            .setType("image/*|application/pdf")
            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.GALLERY) {
            if (data != null) {
                try {
                    if (isDoc1Selected) {
                        data.data?.let { documentUri ->
                            document1 = getFile(requireContext(), documentUri)
                            binding.btnDoc1.setBackgroundResource(R.color.app_green_light)
                            Log.d("File_Path", documentUri.path.toString())

                        }
                    } else {
                        data.data?.let { documentUri ->
                            document2 = getFile(requireContext(), documentUri)
                            binding.btnDoc2.setBackgroundResource(R.color.app_green_light)
                        }

                    }

                } catch (e: IOException) {
                    e.printStackTrace()

                }
            }
        }
    }

    @Throws(IOException::class)
    fun getFile(context: Context, uri: Uri): File {
        val destinationFilename =
            File(context.filesDir.path + File.separatorChar + queryName(context, uri))
        try {
            context.contentResolver.openInputStream(uri).use { ins ->
                createFileFromStream(
                    ins!!,
                    destinationFilename
                )
            }
        } catch (e: Exception) {
            Log.e("Save File", e.message!!)
            e.printStackTrace()
        }
        return destinationFilename
    }

    private fun queryName(context: Context, uri: Uri): String {
        val returnCursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name: String = returnCursor.getString(nameIndex)
        returnCursor.close()
        Log.d("File_Name=======", name)
        return name
    }

    private fun createFileFromStream(ins: InputStream, destination: File?) {
        try {
            FileOutputStream(destination).use { os ->
                val buffer = ByteArray(4096)
                var length: Int
                while (ins.read(buffer).also { length = it } > 0) {
                    os.write(buffer, 0, length)
                }
                os.flush()
            }
        } catch (ex: java.lang.Exception) {
            Log.e("Save File", ex.message!!)
            ex.printStackTrace()
        }
    }


    private fun getToMainActivity() {
        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finishAffinity()
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
                binding.btnDoc1.setBackgroundResource(R.color.gray_dark)
                binding.btnDoc2.setBackgroundResource(R.color.gray_dark)
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_btn)

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_white)
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnDoc1.setBackgroundResource(R.color.gray_dark)
                binding.btnDoc2.setBackgroundResource(R.color.gray_dark)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_white)
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnDoc1.setBackgroundResource(R.color.gray_dark)
                binding.btnDoc2.setBackgroundResource(R.color.gray_dark)
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.bvn_verification_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): BvnVerificationViewModel = bvnVerificationViewModel
}