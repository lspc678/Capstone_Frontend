package com.toyproject.ecosave

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.toyproject.ecosave.databinding.ActivityCameraBinding

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result?.data != null) {
                val bundle = result.data!!.extras
                val photo = bundle?.get("data") as Bitmap
                binding.image.setImageBitmap(photo)
            }
        }

        binding.btnTakePicture.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            activityResultLauncher.launch(cameraIntent)
        }
    }

//    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
//        val (height: Int, width: Int) = options.run { outHeight to outWidth }
//        var inSampleSize = 1
//
//        if (height > reqHeight || width > reqWidth) {
//
//            val halfHeight: Int = height / 2
//            val halfWidth: Int = width / 2
//
//            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
//                inSampleSize *= 2
//            }
//        }
//
//        return inSampleSize
//    }
}