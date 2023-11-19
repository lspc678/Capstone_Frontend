package com.toyproject.ecosave

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns

import com.toyproject.ecosave.api.APIClientForServerByPassSSLCertificate
import com.toyproject.ecosave.api.APIInterface
import com.toyproject.ecosave.api.responsemodels.SignUpSendMailResponse
import com.toyproject.ecosave.databinding.ActivitySignUpBinding
import com.toyproject.ecosave.widget.createDialog
import com.toyproject.ecosave.widget.simpleDialog

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSendAuthentication.setOnClickListener {
            val intent = Intent(this, EmailAuthenticationActivity::class.java)
            val email = binding.editTextForEmail.text.toString()

            // 유효한 이메일 주소인지 확인
            if (email.isNotEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                // 유효한 이메일

                val positiveButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
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
                                            startActivity(intent)
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
                }

                val negativeButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
                    intent.putExtra("email", email)
                    startActivity(intent)
                }

                createDialog(
                    this@SignUpActivity,
                    "이메일 인증(테스트)",
                    "이메일 사용 가능 여부를 판단하시겠습니까?",
                    positiveButtonOnClickListener,
                    negativeButtonOnClickListener
                )
            } else if (email.isEmpty()) {
                simpleDialog(this, "이메일 인증", "이메일 주소를 입력해 주세요.")
            } else {
                simpleDialog(this, "이메일 인증", "유효한 이메일 주소가 아닙니다.")
            }
        }
    }
}