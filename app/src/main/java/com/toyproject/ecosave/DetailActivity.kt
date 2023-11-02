package com.toyproject.ecosave

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View

import com.toyproject.ecosave.databinding.ActivityDetailBinding
import com.toyproject.ecosave.models.DeviceTypeList
import com.toyproject.ecosave.utilities.getCO2EmissionUnit
import com.toyproject.ecosave.utilities.getPowerOfConsumeUnit
import com.toyproject.ecosave.utilities.getTranslatedDeviceType

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val deviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("deviceType", DeviceTypeList::class.java)
        } else {
            intent.getSerializableExtra("deviceType") as DeviceTypeList
        }

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        when (deviceType) {
            deviceType -> {
                supportActionBar?.title = "상세 페이지 (${getTranslatedDeviceType(deviceType)})"
                binding.textPowerOfConsumeType.text = getPowerOfConsumeUnit(deviceType)["description"]
                binding.textPowerOfConsumeUnit.text = getPowerOfConsumeUnit(deviceType)["symbol"]
                binding.textCO2EmissionUnit.text = getCO2EmissionUnit(deviceType)
            }
            else -> {
                supportActionBar?.title = "상세 페이지"
            }
        }

        // CO2 배출량이 표기되어 있지 않은 제품(예: 보일러)의 경우 CO2 배출량에 관한 UI가 보이지 않도록 설정
        if (binding.textCO2EmissionUnit.text == "") {
            binding.relativeLayoutForCO2.visibility = View.GONE
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 앱바에 back 버튼 활성화
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish() // back 버튼을 누르면 이전 화면으로 돌아감
        }
        return true
    }
}