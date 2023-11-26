package com.toyproject.ecosave

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.toyproject.ecosave.api.APIClientForServer
import com.toyproject.ecosave.api.APIInterface
import com.toyproject.ecosave.api.requestmodels.AppliancePostRequest
import com.toyproject.ecosave.api.responsemodels.DefaultResponse
import com.toyproject.ecosave.databinding.ActivityAddDeviceBinding
import com.toyproject.ecosave.models.DeviceTypeList
import com.toyproject.ecosave.utilities.getCO2EmissionUnit
import com.toyproject.ecosave.utilities.getPowerOfConsumeUnit
import com.toyproject.ecosave.utilities.getTranslatedDeviceType
import com.toyproject.ecosave.widget.ProgressDialog
import com.toyproject.ecosave.widget.createDialog
import com.toyproject.ecosave.widget.defaultNegativeDialogInterfaceOnClickListener
import com.toyproject.ecosave.widget.simpleDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AddDeviceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddDeviceBinding
    private var photoFile: File? = null
    private var mCurrentPhotoPath: String? = null
    private lateinit var finalBitmap: Bitmap
    private lateinit var photoURI: Uri

    private var energyConsumption = 0.0F
    private var amountOfCO2 = 0.0F

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1000
        private const val CAPTURE_IMAGE_REQUEST = 1
        private const val GET_ENERGY_CONSUMPTION_AND_CO2 = 50
    }

    private fun reset() {
        binding.editEnergyConsumption.text = "0"
        binding.editCO2Emission.text = "0"
        energyConsumption = 0.0F
        amountOfCO2 = 0.0F
    }

    // 기기등록
    private fun callAppliancePost(deviceType: DeviceTypeList) {
        // progress bar 불러오기
        val progressDialog = ProgressDialog.getProgressDialog(this, "처리 중 입니다")
        progressDialog.show()

        val type = getTranslatedDeviceType(deviceType)
        val apiInterface = APIClientForServer
            .getClient()
            .create(APIInterface::class.java)

        val call: Call<DefaultResponse>?

        when (deviceType) {
            DeviceTypeList.REFRIGERATOR -> {
                call = apiInterface.applianceRefrigeratorPost(
                    AppliancePostRequest(
                        energyConsumption.toDouble(),
                        amountOfCO2.toDouble(),
                        type
                    )
                )
            }
            DeviceTypeList.AIR_CONDITIONER -> {
                call = apiInterface.applianceAirConditionerPost(
                    AppliancePostRequest(
                        energyConsumption.toDouble(),
                        amountOfCO2.toDouble(),
                        type
                    )
                )
            }
            DeviceTypeList.TV -> {
                call = apiInterface.applianceTelevisionPost(
                    AppliancePostRequest(
                        energyConsumption.toDouble(),
                        amountOfCO2.toDouble(),
                        type
                    )
                )
            }
            DeviceTypeList.WASHING_MACHINE -> {
                call = apiInterface.applianceWashingMachinePost(
                    AppliancePostRequest(
                        energyConsumption.toDouble(),
                        amountOfCO2.toDouble(),
                        type
                    )
                )
            }
            DeviceTypeList.MICROWAVE_OVEN -> {
                call = apiInterface.applianceMicrowavePost(
                    AppliancePostRequest(
                        energyConsumption.toDouble(),
                        amountOfCO2.toDouble(),
                        type
                    )
                )
            }
            DeviceTypeList.BOILER -> {
                call = apiInterface.applianceBoilerPost(
                    AppliancePostRequest(
                        energyConsumption.toDouble(),
                        amountOfCO2.toDouble(),
                        type
                    )
                )
            }
            else -> {
                call = null
            }
        }

        call?.enqueue(
            object : Callback<DefaultResponse> {
                override fun onResponse(
                    call: Call<DefaultResponse>,
                    response: Response<DefaultResponse>
                ) {
                    // progress bar 종료
                    progressDialog.dismiss()

                    if (response.isSuccessful) {
                        // status code가 200 ~ 299일 때
                        val result = response.body()

                        if (result != null) {
                            if ((result.success) && (result.message == "전송성공")) {
                                Log.d("기기등록 ($type)", "결과: 성공")
                                Log.d("기기등록 ($type)", result.toString())

                                simpleDialog(
                                    this@AddDeviceActivity,
                                    "기기 추가",
                                    "기기가 등록되었습니다."
                                )

                                reset()
                            } else {
                                simpleDialog(
                                    this@AddDeviceActivity,
                                    "기기 추가",
                                    "기기 등록에 실패했습니다. 다시 시도해주세요."
                                )

                                Log.d("기기등록 ($type)", "결과: 실패")
                                Log.d("기기등록 ($type)", result.toString())
                            }
                        }
                    } else {
                        // status code가 200 ~ 299가 아닐 때
                        val errorResult = response.errorBody()
                        val result = response.body()

                        if (errorResult != null) {
                            Log.d("기기등록 ($type)", "결과: 실패 (response.isSuccessful 통과하지 못함)")
                            Log.d("기기등록 ($type)", "statusCode: ${response.code()}")
                            Log.d("기기등록 ($type)", errorResult.string())
                            Log.d("기기등록 ($type)", result.toString())
                        }
                    }
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    // progress bar 종료
                    progressDialog.dismiss()

                    simpleDialog(
                        this@AddDeviceActivity,
                        "기기 추가",
                        "서버와의 통신이 원활하지 않습니다. 잠시 후 다시 시도해주세요."
                    )

                    Log.d("기기등록 ($type)", "결과: 실패 (onFailure)")
                    Log.d("기기등록 ($type)", t.message.toString())
                }
            }
        )
    }

//    private fun getPermissionForCamera() {
//        val cameraPermission = ContextCompat.checkSelfPermission(
//            this,
//            android.Manifest.permission.CAMERA
//        )
//
//        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
//            // 카메라 권한이 없는 경우 권한 요청
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)) {
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(android.Manifest.permission.CAMERA),
//                    REQUEST_CAMERA_PERMISSION
//                )
//            } else {
//                // 카메라 권한 거부 및 '다시 묻지 않음'인 경우
//                val alertDialogBuilderBtn = AlertDialog.Builder(this)
//                alertDialogBuilderBtn.setTitle("카메라 권한 요청")
//                alertDialogBuilderBtn.setMessage("카메라 권한이 거부되었습니다. 설정(앱 정보)에서 카메라 권한을 허용해 주세요.")
//                alertDialogBuilderBtn.setPositiveButton("확인") { _, _ ->
//                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                    val uri = Uri.fromParts("package", packageName, null)
//                    intent.data = uri
//                    startActivity(intent)
//                }
//                alertDialogBuilderBtn.setNegativeButton("취소") { _, _ -> }
//
//                val alertDialogBox = alertDialogBuilderBtn.create()
//                alertDialogBox.show()
//            }
//        } else {
//            // 카메라 권한이 있음
//            captureImage()
//        }
//    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                captureImage()
//            }
//        }
//    }

//    private fun captureImage() {
//        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        if (takePictureIntent.resolveActivity(packageManager) != null) {
//            // Create the File where the photo should go
//            try {
//                photoFile = createImageFile()
//                // Continue only if the File was successfully created
//                if (photoFile != null) {
//                    photoURI = FileProvider.getUriForFile(
//                        this,
//                        "com.toyproject.ecosave.fileprovider",
//                        photoFile!!
//                    )
//                    takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
//                    startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST)
//                }
//            } catch (ex: Exception) {
//                // Error occurred while creating the File
//                Log.d("사진 촬영", ex.message.toString())
//                Toast.makeText(
//                    baseContext,
//                    "알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        } else {
//            Toast.makeText(
//                baseContext,
//                "Null",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GET_ENERGY_CONSUMPTION_AND_CO2) {
            if (data != null) {
                Log.d("기기추가", data.getFloatExtra("energyConsumption", 0.0F).toString())
                Log.d("기기추가", data.getFloatExtra("amountOfCO2", 0.0F).toString())

                energyConsumption = data.getFloatExtra("energyConsumption", 0.0F)
                if (energyConsumption > 0.0F) {
                    binding.editEnergyConsumption.text = energyConsumption.toString()
                }

                amountOfCO2 = data.getFloatExtra("amountOfCO2", 0.0F)
                if (amountOfCO2 > 0.0F) {
                    binding.editCO2Emission.text = amountOfCO2.toString()
                }
            } else {
                Log.d("기기추가", "null")
            }
        }

//        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
//            var exif: ExifInterface? = null
//
//            try {
//                exif = ExifInterface(photoFile!!.absolutePath)
//            } catch (ex: IOException) {
//                ex.printStackTrace()
//            }
//
//            val orientation = exif?.getAttributeInt(
//                ExifInterface.TAG_ORIENTATION,
//                ExifInterface.ORIENTATION_UNDEFINED
//            )
//
//            val bitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)
//            val cameraRotated = rotateBitmap(bitmap, orientation)
//
//            if (cameraRotated != null) {
//                finalBitmap = cameraRotated
//                binding.deviceImage.setImageURI(photoURI)
//                recognizeText()
//            }
//        }
    }

//    @SuppressLint("SimpleDateFormat")
//    @Throws(IOException::class)
//    private fun createImageFile() : File? {
//        // Create an image file name
//        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        val imageFileName = "JPEG_" + timeStamp + "_"
//        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        val image = File.createTempFile(
//            imageFileName, // prefix
//            ".jpg", // suffix
//            storageDir // directory
//        )
//
//        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = image.absolutePath
//        return image
//    }

//    private fun rotateBitmap(bitmap: Bitmap, orientation: Int?): Bitmap? {
//        val matrix = Matrix()
//        when (orientation) {
//            ExifInterface.ORIENTATION_NORMAL -> return bitmap
//            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1F, 1F)
//            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180F)
//            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
//                matrix.setRotate(180F)
//                matrix.postScale(-1F, 1F)
//            }
//            ExifInterface.ORIENTATION_TRANSPOSE -> {
//                matrix.setRotate(90F)
//                matrix.postScale(-1F, 1F)
//            }
//            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90F)
//            ExifInterface.ORIENTATION_TRANSVERSE -> {
//                matrix.setRotate(-90F)
//                matrix.postScale(-1F, 1F)
//            }
//            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90F)
//            else -> return bitmap
//        }
//
//        return try {
//            val bmRotated: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//            bitmap.recycle()
//            bmRotated
//        } catch (ex: OutOfMemoryError) {
//            ex.printStackTrace()
//            null
//        }
//    }
//
//    private fun recognizeText() {
//        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
//
//        // val image = InputImage.fromBitmap(finalBitmap, 0)
//        try {
//            val image = InputImage.fromFilePath(this, photoURI)
//            Log.d("인식", photoURI.toString())
//            // [START run_detector]
//            val result = recognizer.process(image)
//                .addOnSuccessListener {
//                    processTextBlock(it)
//                }
//                .addOnFailureListener { e ->
//                    e.printStackTrace()
//                }
//            // [END run_detector]
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun processTextBlock(result: Text) {
//        for (block in result.textBlocks) {
//            for (line in block.lines) {
//                Log.d("인식(line)", line.text)
//                for (element in line.elements) {
//
//                }
//            }
//        }
//    }

    @androidx.camera.core.ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "기기 추가"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 앱바에 back 버튼 활성화

        // 사진 촬영 버튼
        binding.btnTakePicture.setOnClickListener {
            val intent = Intent(this, LivePreviewActivity::class.java)
            intent.putExtra("selectedItemPosition", binding.spinner.selectedItemPosition)
            startActivityForResult(intent, GET_ENERGY_CONSUMPTION_AND_CO2)
        }

        val items = resources.getStringArray(R.array.category_list)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

        binding.spinner.adapter = spinnerAdapter
        binding.spinner.prompt = "카테고리 선택"
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("SetTextI18n")
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long) {
                lateinit var powerOfConsumeUnit: Map<String, String>
                lateinit var co2EmissionUnit: String
                when (position) {
                    0 -> { // 냉장고
                        powerOfConsumeUnit = getPowerOfConsumeUnit(DeviceTypeList.REFRIGERATOR)
                        co2EmissionUnit = getCO2EmissionUnit(DeviceTypeList.REFRIGERATOR)
                    }
                    1 -> { // 에어컨
                        powerOfConsumeUnit = getPowerOfConsumeUnit(DeviceTypeList.AIR_CONDITIONER)
                        co2EmissionUnit = getCO2EmissionUnit(DeviceTypeList.AIR_CONDITIONER)
                    }
                    2 -> { // TV
                        powerOfConsumeUnit = getPowerOfConsumeUnit(DeviceTypeList.TV)
                        co2EmissionUnit = getCO2EmissionUnit(DeviceTypeList.TV)
                    }
                    3 -> { // 세탁기
                        powerOfConsumeUnit = getPowerOfConsumeUnit(DeviceTypeList.WASHING_MACHINE)
                        co2EmissionUnit = getCO2EmissionUnit(DeviceTypeList.WASHING_MACHINE)
                    }
                    4 -> { // 전자레인지
                        powerOfConsumeUnit = getPowerOfConsumeUnit(DeviceTypeList.MICROWAVE_OVEN)
                        co2EmissionUnit = getCO2EmissionUnit(DeviceTypeList.MICROWAVE_OVEN)
                    }
                    5 -> { // 보일러
                        powerOfConsumeUnit = getPowerOfConsumeUnit(DeviceTypeList.BOILER)
                        co2EmissionUnit = getCO2EmissionUnit(DeviceTypeList.BOILER)
                    }
                    else -> {
                        powerOfConsumeUnit = getPowerOfConsumeUnit(DeviceTypeList.OTHERS)
                        co2EmissionUnit = getCO2EmissionUnit(DeviceTypeList.OTHERS)
                    }
                }
                binding.textEnergyConsumption.text = powerOfConsumeUnit["description"]
                binding.textEnergyConsumptionUnit.text = powerOfConsumeUnit["symbol"]
                binding.textCO2EmissionUnit.text = co2EmissionUnit

                reset()

                if (co2EmissionUnit == "") {
                    binding.constraintLayoutForCO2.visibility = View.GONE
                } else {
                    binding.constraintLayoutForCO2.visibility = View.VISIBLE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 기기 추가 버튼 클릭 시
        binding.btnAddDevice.setOnClickListener {
            if ((energyConsumption > 0.0F)
                && (amountOfCO2 > 0.0F)) {
                val positiveButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
                    when (binding.spinner.selectedItemPosition) {
                        0 -> { // 냉장고
                            callAppliancePost(DeviceTypeList.REFRIGERATOR)
                        }
                        1 -> { // 에어컨
                            callAppliancePost(DeviceTypeList.AIR_CONDITIONER)
                        }
                        2 -> { // TV
                            callAppliancePost(DeviceTypeList.TV)
                        }
                        3 -> { // 세탁기
                            callAppliancePost(DeviceTypeList.WASHING_MACHINE)
                        }
                        4 -> { // 전자레인지
                            callAppliancePost(DeviceTypeList.MICROWAVE_OVEN)
                        }
                        5 -> { // 보일러
                            callAppliancePost(DeviceTypeList.BOILER)
                        }
                        else -> {}
                    }
                }

                createDialog(
                    this@AddDeviceActivity,
                    "기기 등록",
                    "기기 등록을 하시겠습니까?",
                    positiveButtonOnClickListener,
                    defaultNegativeDialogInterfaceOnClickListener
                )
            } else {
                simpleDialog(
                    this@AddDeviceActivity,
                    "기기 추가",
                    "사진 촬영이 진행되지 않았습니다."
                )
            }

//            if (cameraRotated == null) {
//                simpleDialog(
//                    this,
//                    "기기 추가",
//                    "에너지 소비 등급 라벨을 촬영해 주세요."
//                )
//            } else {
//                if (energyConsumption == 0.0) {
//                    simpleDialog(
//                        this,
//                        "기기 추가",
//                        "해당 기기의 에너지 소비전력을 인식하지 못했습니다. 다시 촬영해 주세요."
//                    )
//                } else {
//                    if ((binding.spinner.selectedItemPosition == 0) ||
//                        (binding.spinner.selectedItemPosition == 1) ||
//                        (binding.spinner.selectedItemPosition == 2) ||
//                        (binding.spinner.selectedItemPosition == 3)) {
//                        if (amountOfCO2 == 0.0) {
//                            simpleDialog(
//                                this,
//                                "기기 추가",
//                                "해당 기기의 CO2 발생량을 인식하지 못했습니다. 다시 촬영해 주세요."
//                            )
//                        } else {
//                            //
//                        }
//                    } else {
//                        //
//                    }
//                }
//            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish() // back 버튼을 누르면 이전 화면으로 돌아감
        }
        return true
    }
}