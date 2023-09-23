package com.toyproject.ecosave

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.toyproject.ecosave.databinding.ActivityAddDeviceBinding

class AddDeviceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddDeviceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "기기 추가"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 앱바에 back 버튼 활성화

        val items = resources.getStringArray(R.array.category_list)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

        binding.spinner.adapter = spinnerAdapter
        binding.spinner.prompt = "카테고리 선택"
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("SetTextI18n")
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        binding.textEnergyConsumption.text = "월간 소비전력량"
                        binding.textEnergyConsumptionUnit.text = "kWh/월"
                        binding.textCO2EmissionUnit.text = "g/시간"
                    }
                    1 -> {
                        binding.textEnergyConsumption.text = "월간 소비전력량"
                        binding.textEnergyConsumptionUnit.text = "kWh/월"
                        binding.textCO2EmissionUnit.text = "g/시간"
                    }
                    2 -> {
                        binding.textEnergyConsumption.text = "월간 소비전력량"
                        binding.textEnergyConsumptionUnit.text = "kWh/월"
                        binding.textCO2EmissionUnit.text = "g/시간"
                    }
                    3 -> {
                        binding.textEnergyConsumption.text = "1Kg당 소비전력량"
                        binding.textEnergyConsumptionUnit.text = "Wh/kg"
                        binding.textCO2EmissionUnit.text = "g/회"
                    }
                    else -> {
                        binding.textEnergyConsumption.text = "월간 소비전력량"
                        binding.textEnergyConsumptionUnit.text = "kWh/월"
                        binding.textCO2EmissionUnit.text = "g/시간"
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnTakePicture.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish() // back 버튼을 누르면 이전 화면으로 돌아감
        }
        return true
    }
}