package com.toyproject.ecosave

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.toyproject.ecosave.databinding.ActivityMainBinding

// 앱 전역에서 SharedPreferences를 사용하기 위해 싱글톤 패턴으로 SharedPreferences 클래스를 생성
class App : Application() {
    companion object {
        lateinit var prefs : PreferenceUtil

        // 화면의 가로 길이를 구하는 함수
        fun getWidth(context: Context) : Int {
            val metrics = context.resources.displayMetrics
            return metrics.widthPixels
        }
    }

    override fun onCreate() {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate()
    }
}

class PreferenceUtil(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("prefs_name", Context.MODE_PRIVATE)

    // 토큰 관리
    var token:String?
        get() = prefs.getString("token", null)
        set(value) {
            prefs.edit().putString("token", value).apply()
        }

    // SharedPreferences에 값을 저장할 때
    // key, value 형태로 저장
    fun setValue(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun setStringValue(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    // SharedPreferences에서 값을 가져올 때
    // defaultValue: key에 해당하는 값이 없을 경우 default로 지정되는 값
    fun getValue(key: String, defaultValue: Int) : String {
        return prefs.getInt(key, defaultValue).toString()
    }

    fun getStringValue(key: String, defaultValue: String) : String {
        return prefs.getString(key, defaultValue).toString()
    }

    // SharedPreferences에 저장된 key값 삭제
    fun removeKey(key: String) {
        prefs.edit().remove(key).apply()
    }

    // SharedPreferences에 저장된 모든 값 삭제
    fun clearValue() {
        prefs.edit().clear().apply()
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}