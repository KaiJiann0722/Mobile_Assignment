package com.example.demo.ui

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.demo.R
import com.example.demo.databinding.FragmentAddFriendQRBinding
import com.example.demo.databinding.FragmentNewFriendBinding
import com.example.demo.util.toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.io.IOException

class AddFriendQRFragment : Fragment() {
    private lateinit var binding: FragmentAddFriendQRBinding
    private val nav by lazy { findNavController() }
    private val userId by lazy { arguments?.getString("userId") ?: "" }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddFriendQRBinding.inflate(inflater, container, false)

        generateQR(userId)

        binding.btnDownload.setOnClickListener {
            saveImage(binding.imgQR.drawable.toBitmap())
        }
        binding.btnScanQR.setOnClickListener {
            scanQR()
        }

        binding.btnUpload.setOnClickListener{
            selectQRCodeFromFile()
        }

        // Inflate the layout for this fragment
        return binding.root
    }


    private fun scanQR() {
        // TODO(3): Launch embedded activity to scan QR
        val options = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setPrompt("Scan QR Code\n")
            .setBeepEnabled(true)
        getResult.launch(options)
    }

    // TODO(4): Handle scan QR result
    private val getResult = registerForActivityResult(ScanContract()) {
        if (it.contents == null) {
            //binding.txtContent.text = ""
        }
        else {
            val target = it.contents
            if(target == userId){
                toast("You cannot add yourself as a friend.")
            }else{
            nav.navigate(R.id.addFriendDetailsFragment, bundleOf("userId" to target))
            //detail(it.contents)
            }
        }
    }

    private fun generateQR(content: String) {
        // TODO(2): Generate QR and display it in an ImageView
        val bitmap = BarcodeEncoder().encodeBitmap(content, BarcodeFormat.QR_CODE, 400, 400)
        binding.imgQR.setImageBitmap(bitmap)
    }

    private fun saveImage(bitmap: Bitmap) {
        val filename = "QR_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }

        val uri: Uri? = context?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri == null) {
            toast("Failed to create new MediaStore record.")
            return
        }

        try {
            context?.contentResolver?.openOutputStream(uri)?.use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                    toast("Failed to save bitmap.")
                }else{
                    toast("Image saved to Pictures.")
                }
            }
        } catch (e: IOException) {
            toast("Failed to write bitmap: ${e.message}")
        }
    }

    private val selectQRCodeLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val width = bitmap.width
                    val height = bitmap.height
                    val pixels = IntArray(width * height)
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                    val source = RGBLuminanceSource(width, height, pixels)
                    val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                    val reader = MultiFormatReader()
                    val result = reader.decode(binaryBitmap)
                    val content = result.text
                    if(content == userId){
                        toast("You cannot add yourself as a friend.")
                    }else{
                        nav.navigate(R.id.addFriendDetailsFragment, bundleOf("userId" to content))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    toast("QR code is invalid or expired.")
                }
            }
        }

    private fun selectQRCodeFromFile() {
        selectQRCodeLauncher.launch("image/*")
    }
}