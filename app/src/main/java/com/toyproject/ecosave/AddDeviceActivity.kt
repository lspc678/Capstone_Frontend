package com.toyproject.ecosave

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast

import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider

import com.toyproject.ecosave.databinding.ActivityAddDeviceBinding

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class AddDeviceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddDeviceBinding
    private var photoFile: File? = null
    private var mCurrentPhotoPath: String? = null

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1000
        private const val CAPTURE_IMAGE_REQUEST = 1
    }

    private fun displayMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun getPermissionForCamera() {
        val cameraPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        )

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            // 카메라 권한이 없는 경우 권한 요청
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION
                )
            } else {
                // 카메라 권한 거부 및 '다시 묻지 않음'인 경우
                val alertDialogBuilderBtn = AlertDialog.Builder(this)
                alertDialogBuilderBtn.setTitle("카메라 권한 요청")
                alertDialogBuilderBtn.setMessage("카메라 권한이 거부되었습니다. 설정(앱 정보)에서 카메라 권한을 허용해 주세요.")
                alertDialogBuilderBtn.setPositiveButton("확인") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                alertDialogBuilderBtn.setNegativeButton("취소") { _, _ -> }

                val alertDialogBox = alertDialogBuilderBtn.create()
                alertDialogBox.show()
            }
        } else {
            // 카메라 권한이 있음
            captureImage()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage()
            }
        }
    }

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
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST)
                }
            } catch (ex: Exception) {
                // Error occurred while creating the File
                Log.d("사진 촬영", ex.message.toString())
                displayMessage(baseContext, "알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
            }
        } else {
            displayMessage(baseContext, "Null")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            var exif: ExifInterface? = null

            try {
                exif = ExifInterface(photoFile!!.absolutePath)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }

            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            val bitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)
            val cameraRotated = rotateBitmap(bitmap, orientation)
            binding.deviceImage.setImageBitmap(cameraRotated)
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile() : File? {
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

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int?): Bitmap? {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> return bitmap
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1F, 1F)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180F)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180F)
                matrix.postScale(-1F, 1F)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90F)
                matrix.postScale(-1F, 1F)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90F)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90F)
                matrix.postScale(-1F, 1F)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90F)
            else -> return bitmap
        }

        return try {
            val bmRotated: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()
            bmRotated
        } catch (ex: OutOfMemoryError) {
            ex.printStackTrace()
            null
        }
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

        // 사진 촬영 버튼
        binding.btnTakePicture.setOnClickListener {
            getPermissionForCamera()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish() // back 버튼을 누르면 이전 화면으로 돌아감
        }
        return true
    }
}