package com.toyproject.ecosave

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.toyproject.ecosave.databinding.ActivityMainBinding

// 앱 전역에서 SharedPreferences를 사용하기 위해 싱글톤 패턴으로 SharedPreferences 클래스를 생성
class App : Application() {
    companion object {
        lateinit var prefs : PreferenceUtil
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

class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var gestureDetector: GestureDetector

    private var x1 = 0.0f
    private var x2 = 0.0f

    private val MIN_DISTANCE = 150

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gestureDetector = GestureDetector(this, this)

        // 텍스트에 에니메이션 효과 추가
        val animation: Animation
        animation = AlphaAnimation(0.2f, 0.8f)
        animation.setDuration(1000)
        animation.setStartOffset(20)
        animation.setRepeatMode(Animation.REVERSE)
        animation.setRepeatCount(Animation.INFINITE)

        binding.textSwipeToLeft.startAnimation(animation)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            gestureDetector.onTouchEvent(event)
        }

        when (event?.action) {
            // swipe start
            0 -> {
                x1 = event.x
            }

            // swipe end
            1 -> {
                x2 = event.x

                val valueX = x2 - x1

                // 화면 왼쪽으로 슬라이드하면 로그인 화면으로 넘어가도록 구현
                if (kotlin.math.abs(valueX) > MIN_DISTANCE) {
                    if (x1 > x2) {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out)
                        finish()
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {}

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {}

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }
}