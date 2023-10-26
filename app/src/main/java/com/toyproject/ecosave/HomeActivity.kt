package com.toyproject.ecosave

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.toyproject.ecosave.databinding.ActivityHomeBinding

import com.toyproject.ecosave.models.RelativeElectricPowerConsumeGradeData

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var drawerLayout: DrawerLayout

    private var recyclerView: RecyclerView? = null
    private var recyclerViewRegisteredDeviceListAdapter: RecyclerViewRegisteredDeviceListAdapter? = null
    private var list = mutableListOf<RelativeElectricPowerConsumeGradeData>()

    private fun prepareListData() {
        var data = RelativeElectricPowerConsumeGradeData(0, 1, 10, 35.9F, 0)
        list.add(data)

        data = RelativeElectricPowerConsumeGradeData(1, 2, 15, 131.3F, 0)
        list.add(data)

        data = RelativeElectricPowerConsumeGradeData(5, 3, 28, 83.0F, 1)
        list.add(data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 앱바에 back 버튼 활성화

        list = ArrayList()
        recyclerView = binding.recyclerView

        recyclerViewRegisteredDeviceListAdapter = RecyclerViewRegisteredDeviceListAdapter(this, list)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.adapter = recyclerViewRegisteredDeviceListAdapter
        prepareListData()

        drawerLayout = binding.drawerLayout
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.textMyResident.text = "인천광역시 미추홀구 아암대로 15 인하한양아이클래스 xxxx호"

        binding.btnAddDevice.setOnClickListener {
            val intent = Intent(this, AddDeviceActivity::class.java)
            startActivity(intent)
        }
    }
}