package com.toyproject.ecosave

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.toyproject.ecosave.databinding.ActivityLivePreviewBinding

class LivePreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLivePreviewBinding

    private fun startCamera() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLivePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}