package com.nbt.blytics.modules.galleryBottomSheet

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nbt.blytics.databinding.FragmentGalleryBinding
import java.util.concurrent.ExecutionException

class GalleryFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentGalleryBinding
    private lateinit var galleryAdapter: GalleryAdapter
    private var imageList: List<Uri> = emptyList()
    private val REQUEST_PERMISSION_CODE = 101
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var preview: Preview
    private lateinit var previewView: PreviewView
    private var selectedImageUri: Uri? = null
    var imageSelectionListener: ImageSelectionListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentGalleryBinding.inflate(inflater, container, false)

        galleryAdapter = GalleryAdapter(requireContext(), imageList) { selectedUri ->
            selectedImageUri = selectedUri
            imageSelectionListener?.onImageSelected(selectedUri) // Pass the selected URI to the main fragment
            dismiss()
        }

        binding.galleryRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.galleryRecyclerView.adapter = galleryAdapter

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnCamera.setOnClickListener {
            clickImage()
        }

        previewView = binding.cameraPreviewView

        checkPermissions()
        startCamera()
        return binding.root
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQUEST_PERMISSION_CODE)
            } else {
                loadGalleryImages()
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION_CODE)
            } else {

                loadGalleryImages()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadGalleryImages()
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadGalleryImages() {
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
        val cursor = requireActivity().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            MediaStore.Images.Media.DATE_ADDED + " DESC"
        )

        cursor?.let {
            val imageUris = mutableListOf<Uri>()
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                val uri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageUris.add(uri)
            }
            it.close()

            if (imageUris.isNotEmpty()) {
                imageList = imageUris
                galleryAdapter.updateData(imageList)
            } else {
                Toast.makeText(requireContext(), "No images found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (exc: ExecutionException) {
                Log.e("GalleryFragment", "Camera initialization failed", exc)
            } catch (exc: InterruptedException) {
                Log.e("GalleryFragment", "Camera initialization interrupted", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun clickImage() {
        Toast.makeText(requireContext(), "Camera button clicked!", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        try {
            cameraProvider.unbindAll()
        } catch (e: Exception) {
            Log.e("GalleryFragment", "Error unbinding camera", e)
        }
    }

    interface ImageSelectionListener {
        fun onImageSelected(uri: Uri)
    }

    companion object {
        fun newInstance() = GalleryFragment()
    }
}