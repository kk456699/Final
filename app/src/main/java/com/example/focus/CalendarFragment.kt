package com.example.focus

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
    private lateinit var calendar: Calendar
    private lateinit var dateText: TextView
    private lateinit var descriptionEditText: EditText
    private lateinit var addBtn: Button
    private lateinit var listView: ListView
    private lateinit var listAdapter: ArrayAdapter<String>
    private lateinit var dateDescriptions: ArrayList<String>
    private var selectedItemIndex: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        calendar = Calendar.getInstance()
        dateText = view.findViewById(R.id.date_text)
        descriptionEditText = view.findViewById(R.id.description_edittext)
        addBtn = view.findViewById(R.id.add_button)
        listView = view.findViewById(R.id.listview)

        dateDescriptions = ArrayList()
        listAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dateDescriptions)
        listView.adapter = listAdapter

        dateText.setOnClickListener {
            showDatePicker()
        }

        addBtn.setOnClickListener {
            val description = descriptionEditText.text.toString().trim()
            val selectedDate = dateText.text.toString()
            if (description.isEmpty()) {
                showToast("請輸入描述")
            } else if (dateDescriptions.any { it.startsWith(selectedDate) }) {
                showToast("該日期已有紀錄")
            } else {
                if (selectedItemIndex == -1) {
                    addDescription()
                } else {
                    updateDescription()
                }
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            selectedItemIndex = position
            val selectedItem = dateDescriptions[position]
            val selectedDate = getSelectedDate(selectedItem)
            dateText.text = selectedDate
            descriptionEditText.setText(getDescription(selectedItem))
            addBtn.text = "更新"
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            deleteDescription(position)
            true
        }

        return view
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
                val selectedDate = dateFormat.format(calendar.time)
                dateText.text = selectedDate
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun addDescription() {
        val description = descriptionEditText.text.toString()
        val selectedDate = dateText.text.toString()
        val dateDescription = "$selectedDate: $description"

        dateDescriptions.add(dateDescription)
        listAdapter.notifyDataSetChanged()

        descriptionEditText.text.clear()
        showToast("描述已添加")
    }

    private fun updateDescription() {
        val description = descriptionEditText.text.toString()
        val selectedDate = dateText.text.toString()
        val dateDescription = "$selectedDate: $description"

        dateDescriptions[selectedItemIndex] = dateDescription
        listAdapter.notifyDataSetChanged()

        clearFields()
        showToast("描述已更新")
    }

    private fun deleteDescription(position: Int) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("刪除描述")
            .setMessage("確定要刪除該描述嗎？")
            .setPositiveButton("確定") { dialogInterface: DialogInterface, _: Int ->
                dateDescriptions.removeAt(position)
                listAdapter.notifyDataSetChanged()

                // 如果被删除的项是当前选中的项，清除字段
                if (position == selectedItemIndex) {
                    clearFields()
                }

                showToast("描述已刪除")
                dialogInterface.dismiss()
            }
            .setNegativeButton("取消") { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }
            .create()

        alertDialog.show()
    }

    private fun clearFields() {
        selectedItemIndex = -1
        dateText.text = ""
        descriptionEditText.text.clear()
        addBtn.text = "新增"
    }

    private fun getSelectedDate(dateDescription: String): String {
        return dateDescription.substringBefore(":").trim()
    }

    private fun getDescription(dateDescription: String): String {
        return dateDescription.substringAfter(":").trim()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}