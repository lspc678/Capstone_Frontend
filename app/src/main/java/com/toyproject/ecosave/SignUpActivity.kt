package com.toyproject.ecosave

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty
import com.toyproject.ecosave.apis.SignUpAPI
import com.toyproject.ecosave.databinding.ActivitySignUpBinding
import com.toyproject.ecosave.models.SignUpSendMailResponse
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
                intent.putExtra("email", email)
                startActivity(intent)
            } else if (email.isEmpty()) {
                simpleDialog(this, "이메일 인증", "이메일 주소를 입력해 주세요.")
            } else {
                simpleDialog(this, "이메일 인증", "유효한 이메일 주소가 아닙니다.")
            }

            /*
            val api = SignUpAPI.create()

            // Log.d("이메일 인증", binding.textInputEmail.text.toString())

            api.signUp(binding.editTextForEmail.text.toString()).enqueue(
                object : Callback<SignUpSendMailResponse> {
                    override fun onResponse(
                        call: Call<SignUpSendMailResponse>,
                        response: Response<SignUpSendMailResponse>
                    ) {
                        Log.d("이메일 인증", response.body().toString())
                        if (response.isSuccessful) {
                            // { "success":true, "message":"Send Certification Number"}
                            val result = response.body()
                            // Log.d("이메일 인증", result.toString())

                            if (result != null) {
                                if (result.success) {
                                    simpleDialog(
                                        this@SignUpActivity,
                                        "이메일 인증",
                                        "해당 이메일은 사용할 수 있습니다."
                                    )
                                } else {
                                    simpleDialog(
                                        this@SignUpActivity,
                                        "이메일 인증",
                                        "이미 존재하는 이메일입니다. 다른 이메일 주소를ㄱ 사용해 주세요."
                                    )
                                    Log.d("이메일 인증", "이미 존재하는 이메일입니다.")
                                }
                            }
                        } else {
                            val result = response.errorBody()
                            val result2 = response.body()
                            if (result != null) {
                                // { "success":false, "message":"Too Large input" }
                                // Log.d("이메일 인증 실패", result.string())

                                if (result2 != null) {
                                    if (result2.message == "Too Large input") {
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
                        Log.d("이메일 인증 실패", t.message.toString())
                    }
                }
            )
            */
        }
    }
}