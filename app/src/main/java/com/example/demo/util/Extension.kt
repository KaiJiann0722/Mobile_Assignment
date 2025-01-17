package com.example.demo.util


import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import com.example.demo.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Blob
import java.io.ByteArrayOutputStream
import java.util.Locale

// ----------------------------------------------------------------------------
// Fragment Extensions
// ----------------------------------------------------------------------------

// Usage: Show a toast from fragment
fun Fragment.toast(text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

// Usage: Show a snackbar from fragment
fun Fragment.snackbar(text: String) {
    Snackbar.make(view!!, text, Snackbar.LENGTH_SHORT).show()
}

// Usage: Show an error dialog from fragment
fun Fragment.errorDialog(text: String) {
    AlertDialog.Builder(context)
        .setIcon(R.drawable.ic_error)
        .setTitle("Error")
        .setMessage(text)
        .setPositiveButton("Dismiss", null)
        .show()
}

fun Fragment.successDialog(text: String) {
    AlertDialog.Builder(context)
        .setIcon(R.drawable.ic_done)
        .setTitle("Success")
        .setMessage(text)
        .setPositiveButton("OK", null)
        .show()
}

fun Fragment.showConfirmationDialog(title: String, message: String, confirmAction: () -> Unit) {
    AlertDialog.Builder(requireContext())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("Yes") { dialog, _ ->
            confirmAction()
            dialog.dismiss()
        }
        .setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        .create()
        .show()
}



// Usage: Show an information dialog from fragment
fun Fragment.infoDialog(text: String) {
    AlertDialog.Builder(context)
        .setIcon(R.drawable.ic_info)
        .setTitle("Information")
        .setMessage(text)
        .setPositiveButton("Dismiss", null)
        .show()
}

// ----------------------------------------------------------------------------
// Bitmap Extensions
// ----------------------------------------------------------------------------

// Usage: Crop and resize bitmap (upscale)
fun Bitmap.crop(width: Int, height: Int): Bitmap {
    // Source width, height and ratio
    val sw = this.width
    val sh = this.height
    val sratio = 1.0 * sw / sh

    // Target offset (x, y), width, height and ratio
    val x: Int
    val y: Int
    val w: Int
    val h: Int
    val ratio = 1.0 * width / height

    if (ratio >= sratio) {
        // Retain width, calculate height
        w = sw
        h = (sw / ratio).toInt()
        x = 0
        y = (sh - h) / 2
    }
    else {
        // Retain height, calculate width
        w = (sh * ratio).toInt()
        h = sh
        x = (sw - w) / 2
        y = 0
    }

    return Bitmap
        .createBitmap(this, x, y, w, h) // Crop
        .scale(width, height) // Resize
}

// Usage: Convert from Bitmap to Firebase Blob
@RequiresApi(Build.VERSION_CODES.R)
fun Bitmap.toBlob(): Blob {
    ByteArrayOutputStream().use {
        compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, it)
        return Blob.fromBytes(it.toByteArray())
    }
}

// ----------------------------------------------------------------------------
// Firebase Blob Extensions
// ----------------------------------------------------------------------------

// Usage: Convert from Blob to Bitmap
fun Blob.toBitmap(): Bitmap? {
    val bytes = toBytes()
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

// ----------------------------------------------------------------------------
// ImageView Extensions
// ----------------------------------------------------------------------------

// Usage: Crop to Firebase Blob
fun ImageView.cropToBlob(width: Int, height: Int): Blob {
    return drawable?.toBitmapOrNull()?.crop(width, height)?.toBlob() ?: Blob.fromBytes(ByteArray(0))
}

// Usage: Load Firebase Blob
fun ImageView.setImageBlob(blob: Blob) {
    setImageBitmap(blob.toBitmap())
}

fun ImageView.toBlob(): Blob {
    return drawable?.toBitmapOrNull()?.toBlob() ?: Blob.fromBytes(ByteArray(0))
}

fun formatTimestamp(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}