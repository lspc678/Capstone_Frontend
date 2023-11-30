package com.toyproject.ecosave.widget

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment

import com.toyproject.ecosave.App
import com.toyproject.ecosave.SelectedAverageUsageTimePerDayInterface
import com.toyproject.ecosave.databinding.DialogSelectAverageUsageTimePerDayBinding

class SelectAverageUsageTimePerDayDialog(
    private val selectedHours: Int,
    private val selectedMinutes: Int
) : DialogFragment() {
    private var _binding: DialogSelectAverageUsageTimePerDayBinding? = null
    private val binding get() = _binding!!

    private val hourArr = ArrayList<String>()
    private val minuteArr: ArrayList<String> = arrayListOf("0", "30")

    private lateinit var mCallback: SelectedAverageUsageTimePerDayInterface

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSelectAverageUsageTimePerDayBinding.inflate(inflater, container, false)
        val view = binding.root

        try {
            // 시간 배열 생성
            createHourArr()

            // 시간 피커 설정
            setHourPicker(selectedHours)

            // 분 피커 설정
            if (selectedHours == 24) {
                setMinutePicker(selectedMinutes, 1)
            } else {
                setMinutePicker(selectedMinutes, 2)
            }

            binding.btnConfirm.setOnClickListener {
                val hours = binding.numberPickerForHour.value

                when (binding.numberPickerForMinute.value) {
                    0 -> {
                        mCallback.onSelectedHour(hours, 0)
                    }
                    1 -> {
                        mCallback.onSelectedHour(hours, 30)
                    }
                }

                dismiss()
            }
            return view
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("시뮬레이션 (하루 평균 사용 시간 선택)", e.toString())
            return view
        }
    }

    private fun createHourArr() {
        for (i in 0 until 25) {
            hourArr.add(i.toString())
        }
    }

    private fun setHourPicker(selectedHours: Int) {
        binding.numberPickerForHour.let {
            it.minValue = 0
            it.maxValue = 24
            it.value = selectedHours
            it.displayedValues = hourArr.toTypedArray()
            it.wrapSelectorWheel = false
            it.setOnValueChangedListener { _, _, newVal ->
                if (newVal == 24) {
                    // 시간을 24로 선택했을 경우 0분만 선택할 수 있음
                    setMinutePicker(selectedMinutes, 1)
                } else {
                    // 그 이외의 경우에는 0분 또는 30분을 선택할 수 있음
                    setMinutePicker(selectedMinutes, 2)
                }
            }
        }
    }

    private fun setMinutePicker(selectedMinutes: Int, size: Int) {
        binding.numberPickerForMinute.let {
            if (size == 1) {
                // 0분만 선택가능
                it.minValue = 0
                it.maxValue = 0
                it.value = selectedMinutes
                it.displayedValues = minuteArr.toTypedArray()
                it.wrapSelectorWheel = false
            } else {
                // 0분 또는 30분 선택가능
                it.minValue = 0
                it.maxValue = 1
                it.value = selectedMinutes
                it.displayedValues = minuteArr.toTypedArray()
                it.wrapSelectorWheel = false
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallback = activity as SelectedAverageUsageTimePerDayInterface
    }

    override fun onResume() {
        super.onResume()
        try {
            // 화면의 가로 길이를 구함
            val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
            val deviceWidth = App.getWidth(requireContext())
            
            // dialog의 가로 길이를 화면 가로 길이의 90%로 설정
            params?.width = (deviceWidth * 0.9).toInt()
            dialog?.window?.attributes = params as WindowManager.LayoutParams
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("시뮬레이션 (하루 평균 사용 시간 선택)", e.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}