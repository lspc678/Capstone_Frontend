package com.toyproject.ecosave

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.toyproject.ecosave.databinding.ActivityLoginBinding
import com.toyproject.ecosave.models.LoginRequestBody
import com.toyproject.ecosave.services.LoginService
import com.toyproject.ecosave.testscreen.TestMainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userData = LoginRequestBody(
            binding.textInputEmail.editText?.text.toString(),
            binding.textInputPassword.editText?.text.toString()
        )

        binding.btnLogin.setOnClickListener {
            val loginService = LoginService(userData)
            loginService.work()
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 테스트용 버튼이며 정식 버전에서는 삭제 예정
        binding.btnLoginForGuest.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 테스트용 버튼이며 정식 버전에서는 삭제 예정
        binding.btnDevelopersTool.setOnClickListener {
            val intent = Intent(this, TestMainActivity::class.java)
            startActivity(intent)
        }
    }
}