package com.toyproject.ecosave

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import com.toyproject.ecosave.api.APIClientForServer
import com.toyproject.ecosave.api.APIInterface

import com.toyproject.ecosave.databinding.ActivityEmailAuthenticationBinding
import com.toyproject.ecosave.api.responsemodels.EmailAuthenticationResponse
import com.toyproject.ecosave.widget.simpleDialog

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmailAuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmailAuthenticationBinding

    private val editTextVerificationNumber = mutableListOf<EditText>()
    private lateinit var editTextVerificationNumber0: EditText
    private lateinit var editTextVerificationNumber1: EditText
    private lateinit var editTextVerificationNumber2: EditText
    private lateinit var editTextVerificationNumber3: EditText
    private lateinit var editTextVerificationNumber4: EditText
    private lateinit var editTextVerificationNumber5: EditText
    private lateinit var btnFinishEmailAuthentication: AppCompatButton

    private var selectedEditTextPosition = 0

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            if (s.isNotEmpty()) {
                when (selectedEditTextPosition) {
                    0 -> showKeyboard(editTextVerificationNumber1)
                    1 -> showKeyboard(editTextVerificationNumber2)
                    2 -> showKeyboard(editTextVerificationNumber3)
                    3 -> showKeyboard(editTextVerificationNumber4)
                    4 -> showKeyboard(editTextVerificationNumber5)
                    else -> {

                    }
                }

                if (selectedEditTextPosition <= 4) {
                    selectedEditTextPosition += 1
                }
            }
        }
    }

    private fun showKeyboard(editTextVerificationNumber: EditText) {
        editTextVerificationNumber.requestFocus()
        val inputMethodManager = baseContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editTextVerificationNumber, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            when (selectedEditTextPosition) {
                5 -> showKeyboard(editTextVerificationNumber4)
                4 -> showKeyboard(editTextVerificationNumber3)
                3 -> showKeyboard(editTextVerificationNumber2)
                2 -> showKeyboard(editTextVerificationNumber1)
                1 -> showKeyboard(editTextVerificationNumber0)
            }

            if (selectedEditTextPosition >= 1) {
                selectedEditTextPosition -= 1
            }

            return true
        } else {
            return super.onKeyUp(keyCode, event)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        val email = intent.getStringExtra("email")
        binding.textEmail.text = email

        editTextVerificationNumber0 = binding.editTextVerificationNumber0
        editTextVerificationNumber1 = binding.editTextVerificationNumber1
        editTextVerificationNumber2 = binding.editTextVerificationNumber2
        editTextVerificationNumber3 = binding.editTextVerificationNumber3
        editTextVerificationNumber4 = binding.editTextVerificationNumber4
        editTextVerificationNumber5 = binding.editTextVerificationNumber5

        editTextVerificationNumber.add(editTextVerificationNumber0)
        editTextVerificationNumber.add(editTextVerificationNumber1)
        editTextVerificationNumber.add(editTextVerificationNumber2)
        editTextVerificationNumber.add(editTextVerificationNumber3)
        editTextVerificationNumber.add(editTextVerificationNumber4)
        editTextVerificationNumber.add(editTextVerificationNumber5)

        btnFinishEmailAuthentication = binding.btnFinishEmailAuthentication

        for (editText in editTextVerificationNumber) {
            editText.addTextChangedListener(textWatcher)
        }

        // 맨 왼쪽에 있는 입력칸에 대하여 키보드 창이 열리도록 설정
        showKeyboard(editTextVerificationNumber0)

        // 이메일 인증 완료 버튼 클릭 시
        binding.btnFinishEmailAuthentication.setOnClickListener {
            var code = ""

            // 입력한 인증번호를 code에 저장
            for (editText in editTextVerificationNumber) {
                code += editText.text.toString()
            }

            if (code.length == 6) {
                if (email != null) {
                    val apiInterface = APIClientForServer.getClient().create(APIInterface::class.java)
                    val call = apiInterface.sendEmailAuthenticationRequest(email, code)
                    call.enqueue(
                        object : Callback<EmailAuthenticationResponse> {
                            override fun onResponse(
                                call: Call<EmailAuthenticationResponse>,
                                response: Response<EmailAuthenticationResponse>
                            ) {
                                if (response.isSuccessful) {
                                    // response code가 200 ~ 299일 때
                                    val result = response.body()
                                    if ((result != null) &&
                                        (result.success == true)) {
                                        simpleDialog(
                                            this@EmailAuthenticationActivity,
                                            "이메일 인증",
                                            "이메일 인증에 성공하였습니다."
                                        )
                                    } else {
                                        simpleDialog(
                                            this@EmailAuthenticationActivity,
                                            "이메일 인증",
                                            "이메일 인증에 실패하였습니다. 다시 시도해주세요."
                                        )
                                    }
                                } else {
                                    // response code가 200 ~ 299가 아닐 때
                                    simpleDialog(
                                        this@EmailAuthenticationActivity,
                                        "통신 오류",
                                        "서버와의 통신이 원활하지 않습니다. 잠시 후 다시 시도해 주세요."
                                    )
                                }
                            }

                            override fun onFailure(
                                call: Call<EmailAuthenticationResponse>,
                                t: Throwable
                            ) {
                                simpleDialog(
                                    this@EmailAuthenticationActivity,
                                    "통신 오류",
                                    "서버와의 통신이 원활하지 않습니다. 잠시 후 다시 시도해 주세요."
                                )
                                Log.d("이메일 인증", "결과: 실패")
                                if (t.message != null) {
                                    Log.d("이메일 인증", t.message!!)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}