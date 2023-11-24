package com.toyproject.ecosave

import android.annotation.SuppressLint
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle

import com.toyproject.ecosave.databinding.ActivityLoginBinding
import com.toyproject.ecosave.api.requestmodels.LoginRequest
import com.toyproject.ecosave.api.responsemodels.LoginResponse

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import com.toyproject.ecosave.api.APIClientForServerByPassSSLCertificate
import com.toyproject.ecosave.api.APIInterface
import com.toyproject.ecosave.widget.createDialog
import com.toyproject.ecosave.widget.defaultNegativeDialogInterfaceOnClickListener
import com.toyproject.ecosave.widget.simpleDialog

// 테스트용 (정식 버전에서는 삭제 예정)
import android.util.Log
import com.toyproject.ecosave.test.TestMainActivity
import com.toyproject.ecosave.test.api.TestAPIClient
import com.toyproject.ecosave.test.api.TestAPIInterface
import com.toyproject.ecosave.test.api.requestmodel.TestLoginRequest
import com.toyproject.ecosave.test.api.responsemodel.TestLoginResponse

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 토큰 정보 가져오기
        val token = App.prefs.token
        if (token != null) {
            // 토큰 정보가 남아있으므로 이메일, 비밀번호 입력 과정 없이 로그인 진행
            Log.d("로그인", token)

            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.textRegister.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // 로그인 버튼 클릭시
        binding.btnLogin.setOnClickListener {
            val userData = LoginRequest(
                binding.textInputEmail.editText?.text.toString(),
                binding.textInputPassword.editText?.text.toString()
            )

            val apiInterface = APIClientForServerByPassSSLCertificate
                .getClient()
                .create(APIInterface::class.java)
            val call = apiInterface.login(userData)

            call.enqueue(
                object : Callback<LoginResponse> {
                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (response.isSuccessful) {
                            // status code가 200 ~ 299일 때
                            val result = response.body()
                            if (result != null) {
                                if (result.success == true) {
                                    Log.d("로그인", "$result")

                                    // 토큰 정보를 Shared Preferences에 저장
                                    App.prefs.token = result.token

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
                        } else {
                            // status code가 200 ~ 299가 아닐 때
                            val errorResult = response.errorBody()
                            val result = response.body()

                            if (errorResult != null) {
                                simpleDialog(
                                    this@LoginActivity,
                                    "로그인 실패",
                                    "이메일 또는 비밀번호가 맞지 않습니다."
                                )

                                Log.d("로그인", "결과: 실패")
                                Log.d("로그인", errorResult.string())
                                Log.d("로그인", result.toString())
                            }
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        simpleDialog(
                            this@LoginActivity,
                            "로그인 실패",
                            "서버와의 통신이 원활하지 않습니다. 잠시 후 다시 시도해주세요."
                        )
                        Log.d("로그인", "결과: 실패")
                        Log.d("로그인", t.message.toString())
                    }
                }
            )
        }

        // 게스트로 로그인 (테스트용 버튼이며 정식 버전에서는 삭제 예정)
        // 토큰 저장 기능은 없음
        binding.btnLoginForGuest.setOnClickListener {
            val userData = TestLoginRequest(
                binding.textInputEmail.editText?.text.toString(),
                binding.textInputPassword.editText?.text.toString()
            )

            // 테스팅용 API를 날림
            val apiInterface = TestAPIClient.getClient().create(TestAPIInterface::class.java)
            val call = apiInterface.login(userData)
            call.enqueue(
                object : Callback<TestLoginResponse> {
                    @SuppressLint("SetTextI18n")
                    override fun onResponse(
                        call: Call<TestLoginResponse>,
                        response: Response<TestLoginResponse>
                    ) {
                        if (response.isSuccessful) {
                            // status code가 200 ~ 299일 때
                            val result = response.body()
                            Log.d("로그인", "결과: 성공 (statusCode: ${response.code()})")
                            Log.d("로그인", result.toString())

                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // status code가 200 ~ 299가 아닐 때

                            val positiveButtonOnClickListener = DialogInterface.OnClickListener { _, _ ->
                                binding.editTextForEmail.setText("eve.holt@reqres.in")
                                binding.editTextForPassword.setText("cityslicka")
                            }

                            createDialog(
                                this@LoginActivity,
                                "로그인 실패",
                                "이메일 또는 비밀번호가 맞지 않습니다.\n" +
                                        "자동으로 테스트용 이메일, 비밀번호를 입력하시겠습니까?\n\n" +
                                        "테스트용 이메일: eve.holt@reqres.in\n테스트용 비밀번호: cityslicka",
                                positiveButtonOnClickListener,
                                defaultNegativeDialogInterfaceOnClickListener
                            )

                            Log.d("로그인", "결과: 실패 (statusCode: ${response.code()})")
                            Log.d("로그인", response.message())
                        }
                    }

                    override fun onFailure(call: Call<TestLoginResponse>, t: Throwable) {
                        simpleDialog(
                            this@LoginActivity,
                            "로그인 실패",
                            "서버와의 통신이 원활하지 않습니다. 잠시 후 다시 시도해주세요."
                        )
                        Log.d("로그인", "결과: 실패")
                        if (t.message != null) {
                            Log.d("로그인", t.message!!)
                        }
                    }
                }
            )
        }

        // 개발자 도구 (테스트용 버튼이며 정식 버전에서는 삭제 예정)
        binding.btnDevelopersTool.setOnClickListener {
            val intent = Intent(this, TestMainActivity::class.java)
            startActivity(intent)
        }
    }
}