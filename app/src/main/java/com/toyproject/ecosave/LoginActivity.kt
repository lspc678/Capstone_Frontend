package com.toyproject.ecosave

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

import com.toyproject.ecosave.databinding.ActivityLoginBinding
import com.toyproject.ecosave.apis.LoginAPI
import com.toyproject.ecosave.models.LoginRequestBody
import com.toyproject.ecosave.models.LoginResponseBody

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// 테스트용 (정식 버전에서는 삭제 예정)
import android.util.Log
import com.toyproject.ecosave.test.TestMainActivity
import com.toyproject.ecosave.test.testapis.TestAPI
import com.toyproject.ecosave.test.testmodels.TestRequestPost
import com.toyproject.ecosave.test.testmodels.TestResponsePost
import com.toyproject.ecosave.widget.simpleDialog

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textRegister.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // 로그인 버튼 클릭시
        binding.btnLogin.setOnClickListener {
            val api = LoginAPI.create()

            val userData = LoginRequestBody(
                binding.textInputEmail.editText?.text.toString(),
                binding.textInputPassword.editText?.text.toString()
            )

            api.postLogin(userData).enqueue(object : Callback<LoginResponseBody> {
                override fun onResponse(
                    call: Call<LoginResponseBody>,
                    response: Response<LoginResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        if (result?.success == "true") {
                            Log.d("로그인", "$result")

                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            simpleDialog(
                                this@LoginActivity,
                                "로그인 실패",
                                "이메일 또는 비밀번호가 맞지 않습니다."
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<LoginResponseBody>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                    Log.d("로그인", t.message.toString())
                }
            })
        }

        // 게스트로 로그인 (테스트용 버튼이며 정식 버전에서는 삭제 예정)
        binding.btnLoginForGuest.setOnClickListener {
            // 테스팅용 API를 날림
            val api = TestAPI.create()

            api.postUser(TestRequestPost("pawan", "programmer")).enqueue(object : Callback<TestResponsePost> {
                override fun onResponse(
                    call: Call<TestResponsePost>,
                    response: Response<TestResponsePost>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        Log.d("테스트 로그인 성공", "$result")

                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

                override fun onFailure(call: Call<TestResponsePost>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "테스트 로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                    Log.d("테스트 로그인 실패", t.message.toString())
                }
            })
        }

        // 개발자 도구 (테스트용 버튼이며 정식 버전에서는 삭제 예정)
        binding.btnDevelopersTool.setOnClickListener {
            val intent = Intent(this, TestMainActivity::class.java)
            startActivity(intent)
        }
    }
}