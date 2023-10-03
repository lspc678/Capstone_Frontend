package com.toyproject.ecosave

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.toyproject.ecosave.databinding.ActivityAddDeviceBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class AddDeviceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddDeviceBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var photoFile: File? = null
    val CAPTURE_IMAGE_REQUEST = 1
    private var mCurrentPhotoPath: String? = null

    @SuppressLint("QueryPermissionsNeeded")
    private fun captureImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile()
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    val photoURI = FileProvider.getUriForFile(
                        this,
                        "com.toyproject.ecosave.fileprovider",
                        photoFile!!
                    )
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST)
                }
            } catch (ex: Exception) {
                // Error occurred while creating the File
                displayMessage(baseContext, ex.message.toString())
            }
        } else {
            displayMessage(baseContext, "Null")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val bitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)
            binding.deviceImage.setImageBitmap(bitmap)
        } else {
            displayMessage(baseContext, "Request cancelled or something went wrong.")
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile() : File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName, // prefix
            ".jpg", // suffix
            storageDir // directory
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    private fun displayMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    @Throws(IOException::class)
    private fun rotateImageIfRequired(img: Bitmap, selectedImage: Uri): Bitmap? {
        val ei = ExifInterface(selectedImage.path!!)
        return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90.0F)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180.0F)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270.0F)
            else -> img
        }
    }

    private fun rotateImage(img: Bitmap, degree: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "기기 추가"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 앱바에 back 버튼 활성화

        val items = resources.getStringArray(R.array.category_list)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

        binding.spinner.adapter = spinnerAdapter
        binding.spinner.prompt = "카테고리 선택"
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("SetTextI18n")
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        binding.textEnergyConsumption.text = "월간 소비전력량"
                        binding.textEnergyConsumptionUnit.text = "kWh/월"
                        binding.textCO2EmissionUnit.text = "g/시간"
                    }
                    1 -> {
                        binding.textEnergyConsumption.text = "월간 소비전력량"
                        binding.textEnergyConsumptionUnit.text = "kWh/월"
                        binding.textCO2EmissionUnit.text = "g/시간"
                    }
                    2 -> {
                        binding.textEnergyConsumption.text = "월간 소비전력량"
                        binding.textEnergyConsumptionUnit.text = "kWh/월"
                        binding.textCO2EmissionUnit.text = "g/시간"
                    }
                    3 -> {
                        binding.textEnergyConsumption.text = "1Kg당 소비전력량"
                        binding.textEnergyConsumptionUnit.text = "Wh/kg"
                        binding.textCO2EmissionUnit.text = "g/회"
                    }
                    else -> {
                        binding.textEnergyConsumption.text = "월간 소비전력량"
                        binding.textEnergyConsumptionUnit.text = "kWh/월"
                        binding.textCO2EmissionUnit.text = "g/시간"
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

//        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result?.data != null) {
//                val bundle = result.data!!.extras
//                val bitmap = bundle?.get("data") as Bitmap
//                binding.deviceImage.setImageBitmap(bitmap)
//            }
//        }

        binding.btnTakePicture.setOnClickListener {
            captureImage()
            // val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // activityResultLauncher.launch(cameraIntent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish() // back 버튼을 누르면 이전 화면으로 돌아감
        }
        return true
    }
}