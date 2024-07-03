package com.forestspi.ritluck.ui.main

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.forestspi.ritluck.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*

class DateTimePickerBottomSheetFragment(private val onDateTimeSelected: (String) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var npDate: NumberPicker
    private lateinit var npHour: NumberPicker
    private lateinit var npMinute: NumberPicker

    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_date_time_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        npDate = view.findViewById(R.id.npDate)
        npHour = view.findViewById(R.id.npHour)
        npMinute = view.findViewById(R.id.npMinute)

        setupDatePicker()
        setupTimePicker()

        view.findViewById<TextView>(R.id.cancelButton).setOnClickListener {
            dismiss()
        }

        view.findViewById<TextView>(R.id.doneButton).setOnClickListener {
            val selectedDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, npDate.value)
            }
            calendar.set(
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH),
                npHour.value,
                npMinute.value
            )
            val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(calendar.time)
            onDateTimeSelected(formattedDate)
            dismiss()
        }
    }

    private fun setupDatePicker() {
        val days = arrayOfNulls<String>(30)
        val dateFormat = SimpleDateFormat("EEE MMM dd", Locale.US)
        for (i in 0 until 30) {
            val date = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, i)
            }.time
            days[i] = dateFormat.format(date)
        }
        npDate.minValue = 0
        npDate.maxValue = days.size - 1
        npDate.displayedValues = days
        npDate.wrapSelectorWheel = true
        setNumberPickerTextColor(npDate, ContextCompat.getColor(requireContext(), android.R.color.black))
        hideNumberPickerDivider(npDate)
    }

    private fun setupTimePicker() {
        npHour.minValue = 0
        npHour.maxValue = 23
        npHour.wrapSelectorWheel = true
        setNumberPickerTextColor(npHour, ContextCompat.getColor(requireContext(), android.R.color.black))
        hideNumberPickerDivider(npHour)

        npMinute.minValue = 0
        npMinute.maxValue = 59
        npMinute.wrapSelectorWheel = true
        setNumberPickerTextColor(npMinute, ContextCompat.getColor(requireContext(), android.R.color.black))
        hideNumberPickerDivider(npMinute)
    }

    @SuppressLint("SoonBlockedPrivateApi")
    private fun setNumberPickerTextColor(numberPicker: NumberPicker, color: Int) {
        try {
            val count = numberPicker.childCount
            for (i in 0 until count) {
                val child = numberPicker.getChildAt(i)
                if (child is EditText) {
                    child.setTextColor(color)
                }
            }
            numberPicker.invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SoonBlockedPrivateApi")
    private fun hideNumberPickerDivider(numberPicker: NumberPicker) {
        try {
            val fields = NumberPicker::class.java.declaredFields
            for (field in fields) {
                if (field.name == "mSelectionDivider") {
                    field.isAccessible = true
                    field.set(numberPicker, null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
