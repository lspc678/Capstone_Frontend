package com.toyproject.ecosave

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged

import com.toyproject.ecosave.api.APIClientForServerByPassSSLCertificate
import com.toyproject.ecosave.api.APIInterface
import com.toyproject.ecosave.api.requestmodels.SignUpRequest
import com.toyproject.ecosave.api.responsemodels.CheckDuplicateNicknameResponse
import com.toyproject.ecosave.api.responsemodels.SignUpResponse
import com.toyproject.ecosave.api.responsemodels.SignUpSendMailResponse
import com.toyproject.ecosave.databinding.ActivitySignUpBinding
import com.toyproject.ecosave.utilities.GPSLocation
import com.toyproject.ecosave.widget.createDialog
import com.toyproject.ecosave.widget.defaultNegativeDialogInterfaceOnClickListener
import com.toyproject.ecosave.widget.simpleDialog

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    private var passEmailAuthentication = false
    private var finalEmail = ""
    private var checkDuplicateNickname = false
    private var finalNickname = ""

    companion object {
        const val EMAIL_AUTHENTICATION_REQUEST = 500
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EMAIL_AUTHENTICATION_REQUEST) {
            if (data != null) {
                passEmailAuthentication = data.getBooleanExtra("emailAuthentication", false)
                finalEmail = binding.editTextForEmail.text.toString()

                // 이메일은 더 이상 수정할 수 없도록 함
                val editTextForEmail = binding.editTextForEmail
                editTextForEmail.isEnabled = false

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인증번호 발송 버튼 클릭 시
        binding.btnSendAuthentication.setOnClickListener {
            val intent = Intent(this, EmailAuthenticationActivity::class.java)
            val email = binding.editTextForEmail.text.toString()

            // 유효한 이메일 주소인지 확인
            if (email.isNotEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                // 유효한 이메일

                val apiInterface = APIClientForServerByPassSSLCertificate
                    .getClient()
                    .create(APIInterface::class.java)
                val call = apiInterface.sendMail(email)

                call.enqueue(
                    object : Callback<SignUpSendMailResponse> {
                        override fun onResponse(
                            call: Call<SignUpSendMailResponse>,
                            response: Response<SignUpSendMailResponse>
                        ) {
                            if (response.isSuccessful) {
                                // { "success":true, "message":"Send Certification Number"}
                                val result = response.body()

                                if (result != null) {
                                    if (result.success) {
                                        Log.d("이메일 인증", "결과: 성공")
                                        intent.putExtra("email", email)

                                        startActivityForResult(intent, EMAIL_AUTHENTICATION_REQUEST)
                                    } else {
                                        simpleDialog(
                                            this@SignUpActivity,
                                            "이메일 인증",
                                            "이미 존재하는 이메일입니다. 다른 이메일 주소를 사용해 주세요."
                                        )
                                        Log.d("이메일 인증", "결과: 실패")
                                        Log.d("이메일 인증", result.toString())
                                    }
                                }
                            } else {
                                val errorResult = response.errorBody()
                                val result = response.body()

                                if (errorResult != null) {
                                    // { "success":false, "message":"Too Large input" }

                                    Log.d("이메일 인증", "결과: 실패")
                                    Log.d("이메일 인증", errorResult.string())
                                    Log.d("이메일 인증", result.toString())

                                    if (result != null) {
                                        if (result.message == "Too Large input") {
                                            simpleDialog(
                                                this@SignUpActivity,
                                                "이메일 인증",
                                                "이메일 길이가 너무 깁니다."
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call<SignUpSendMailResponse>, t: Throwable) {
                            simpleDialog(
                                this@SignUpActivity,
                                "이메일 인증",
                                "서버와의 통신이 원활하지 않습니다. 잠시 후 다시 시도해주세요."
                            )
                            Log.d("이메일 인증", "결과: 실패")
                            Log.d("이메일 인증", t.message.toString())
                        }
                    }
                )
            } else if (email.isEmpty()) {
                simpleDialog(this, "이메일 인증", "이메일 주소를 입력해 주세요.")
            } else {
                simpleDialog(this, "이메일 인증", "유효한 이메일 주소가 아닙니다.")
            }
        }

        // 닉네임의 최소 길이는 4자, 최대 길이는 31자로 제한 
        // 최대 길이의 경우 editText에서 자체적으로 제한함
        binding.editTextForNickname.doOnTextChanged { text, _, _, _ ->
            if (text!!.length <= 3) {
                binding.textInputForNickname.error = "닉네임의 최소 길이는 4자입니다."
            } else {
                binding.textInputForNickname.error = null
            }
        }

        // 중복 확인 버튼 클릭시
        // 닉네임의 최소 길이는 4자, 최대 길이는 31자로 제한
        // 최대 길이의 경우 editText에서 자체적으로 제한함
        binding.btnCheckDuplicateNickname.setOnClickListener {
            if ((!checkDuplicateNickname)
                && (binding.editTextForNickname.text.toString().length >= 4)
                && (binding.editTextForNickname.text.toString().length <= 31)) {
                // 닉네임 중복 확인이 진행되지 않았을 경우
                val nickname = binding.editTextForNickname.text.toString()

                if (nickname.isNotEmpty()) {
                    val apiInterface = APIClientForServerByPassSSLCertificate
                        .getClient()
                        .create(APIInterface::class.java)
                    val call = apiInterface.checkDuplicateNickname(nickname)
                    call.enqueue(
                        object : Callback<CheckDuplicateNicknameResponse> {
                            override fun onResponse(
                                call: Call<CheckDuplicateNicknameResponse>,
                                response: Response<CheckDuplicateNicknameResponse>
                            ) {
                                if (response.isSuccessful) {
                                    val result = response.body()

                                    if (result != null) {
                                        if (result.success == true) {
                                            // 닉네임 사용 가능

                                            // 닉네임 사용 확인 시 더 이상 닉네임을 수정할 수 없도록 함
                                            val positiveButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
                                                val editTextForNickname = binding.editTextForNickname
                                                editTextForNickname.isEnabled = false
                                                finalNickname = nickname
                                            }

                                            createDialog(
                                                this@SignUpActivity,
                                                "닉네임 중복 확인",
                                                "해당 닉네임은 사용할 수 있습니다. 사용하시겠습니까?",
                                                positiveButtonOnClickListener,
                                                defaultNegativeDialogInterfaceOnClickListener
                                            )

                                            Log.d("닉네임 중복 확인", "결과: 성공")
                                            // 닉네임 중복 확인 완료
                                            checkDuplicateNickname = true
                                        } else {
                                            Log.d("닉네임 중복 확인", "결과: 실패 (success: false)")
                                            Log.d("닉네임 중복 확인", result.toString())
                                        }
                                    }
                                } else {
                                    val errorResult = response.errorBody()
                                    val result = response.body()

                                    if (errorResult != null) {
                                        // { "success":false, "message":"{nickname}is Too short input" }
                                        Log.d("닉네임 중복 확인", "결과: 실패 (response.isSuccessful 통과하지 못함)")
                                        Log.d("닉네임 중복 확인", result.toString())
                                        Log.d("닉네임 중복 확인", errorResult.string())

                                        try {
                                            simpleDialog(
                                                this@SignUpActivity,
                                                "닉네임 중복 확인",
                                                "닉네임은 최소 4자이상 입력해야 합니다."
                                            )
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            Log.d("닉네임 중복 확인", e.toString())
                                        }
                                    }
                                }
                            }

                            override fun onFailure(
                                call: Call<CheckDuplicateNicknameResponse>,
                                t: Throwable
                            ) {
                                simpleDialog(
                                    this@SignUpActivity,
                                    "닉네임 중복 확인",
                                    "서버와의 통신이 원활하지 않습니다. 잠시 후 다시 시도해주세요."
                                )
                                Log.d("닉네임 중복 확인", "결과: 실패")
                                Log.d("닉네임 중복 확인", t.message.toString())
                            }
                        }
                    )
                } else {
                    simpleDialog(this, "닉네임 중복 확인", "닉네임을 입력해 주세요.")
                }
            }
        }

        // 비밀번호 일치 확인
        binding.editTextForPasswordConfirm.doOnTextChanged { _, _, _, _ ->
            val password = binding.editTextForPassword.text.toString()
            val passwordConfirm = binding.editTextForPasswordConfirm.text.toString()

            if (password != passwordConfirm) {
                binding.textInputForPasswordConfirm.error = "비밀번호가 일치하지 않습니다."
            } else {
                binding.textInputForPasswordConfirm.error = null
            }
        }

        // 회원가입 완료 버튼 클릭 시
        binding.btnFinishSignUp.setOnClickListener {
            if (!passEmailAuthentication) {
                simpleDialog(
                    this@SignUpActivity,
                    "회원가입",
                    "이메일 인증이 진행되지 않았습니다."
                )
                return@setOnClickListener
            }

            if (!checkDuplicateNickname) {
                simpleDialog(
                    this@SignUpActivity,
                    "회원가입",
                    "닉네임 중복 확인이 되지 않았습니다."
                )
                return@setOnClickListener
            }

            val password = binding.editTextForPassword.text.toString()
            val passwordConfirm = binding.editTextForPasswordConfirm.text.toString()

            if (password != passwordConfirm) {
                simpleDialog(
                    this@SignUpActivity,
                    "회원가입",
                    "비밀번호가 일치하지 않습니다."
                )
                return@setOnClickListener
            }

            val positiveButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
                val gpsLocation = GPSLocation(this@SignUpActivity)
                gpsLocation.getMyLocation(this)

                if ((GPSLocation.currentLatitude != 0.0)
                    && (GPSLocation.currentLongitude != 0.0)) {
                    Log.d("회원가입", finalEmail)
                    Log.d("회원가입", password)
                    Log.d("회원가입", passwordConfirm)
                    Log.d("회원가입", finalNickname)
                    Log.d("회원가입", App.prefs.getStringValue("code", ""))
                    Log.d("회원가입", GPSLocation.currentLongitude.toString())
                    Log.d("회원가입", GPSLocation.currentLatitude.toString())

                    val signUpData = SignUpRequest(
                        finalEmail,
                        password,
                        passwordConfirm,
                        finalNickname,
                        App.prefs.getStringValue("code", ""),
                        HomeActivity.currentLongitude,
                        HomeActivity.currentLatitude
                    )

                    val apiInterface = APIClientForServerByPassSSLCertificate
                        .getClient()
                        .create(APIInterface::class.java)
                    val call = apiInterface.signUp(signUpData)

                    call.enqueue(
                        object : Callback<SignUpResponse> {
                            override fun onResponse(
                                call: Call<SignUpResponse>,
                                response: Response<SignUpResponse>
                            ) {
                                if (response.isSuccessful) {
                                    val result = response.body()

                                    if (result != null) {
                                        if (result.success == true) {
                                            // 회원가입 성공

                                            simpleDialog(
                                                this@SignUpActivity,
                                                "회원가입",
                                                "회원가입이 완료되었습니다. 로그인 화면에서 로그인을 진행해주세요.",
                                            )

                                            Log.d("회원가입", "결과: 성공")

                                            // 로그인 화면으로 이동
                                            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            simpleDialog(
                                                this@SignUpActivity,
                                                "회원가입",
                                                "회원가입에 실패하였습니다. 잠시 후 다시 시도해주세요."
                                            )
                                            Log.d("회원가입", "결과: 실패 (result.success = false)")
                                            Log.d("회원가입", result.toString())
                                        }
                                    }
                                } else {
                                    val errorResult = response.errorBody()
                                    val result = response.body()

                                    if (errorResult != null) {
                                        simpleDialog(
                                            this@SignUpActivity,
                                            "회원가입",
                                            "회원가입에 실패하였습니다. 잠시 후 다시 시도해주세요."
                                        )

                                        Log.d("회원가입", "결과: 실패 (response.isSuccessful 통과 실패)")
                                        Log.d("회원가입", errorResult.string())
                                        Log.d("회원가입", result.toString())
                                    }
                                }
                            }

                            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                                simpleDialog(
                                    this@SignUpActivity,
                                    "회원가입",
                                    "서버와의 통신이 원활하지 않습니다. 잠시 후 다시 시도해주세요."
                                )
                                Log.d("회원가입", "결과: 실패(onFailure)")
                                Log.d("회원가입", t.message.toString())
                            }
                        }
                    )
                } else {
                    simpleDialog(
                        this@SignUpActivity,
                        "내 거주지 변경",
                        "현재 위치를 가져오지 못했습니다. 잠시 후 다시 시도해 주세요."
                    )
                }
            }

            createDialog(
                this@SignUpActivity,
                "회원가입",
                "회원가입을 하기 위해 현재 위치를 파악합니다",
                positiveButtonOnClickListener,
                defaultNegativeDialogInterfaceOnClickListener
            )
        }
    }
}