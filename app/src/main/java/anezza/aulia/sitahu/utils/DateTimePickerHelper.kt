package anezza.aulia.sitahu

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.EditText
import androidx.fragment.app.Fragment
import java.util.Calendar

object DateTimePickerHelper {

    fun bind(fragment: Fragment, editText: EditText, initialValue: String? = null) {
        if (editText.text.isNullOrBlank()) {
            editText.setText(initialValue ?: FormatHelper.nowDisplay())
        }

        editText.keyListener = null
        editText.isFocusable = false
        editText.isClickable = true
        editText.isCursorVisible = false

        editText.setOnClickListener {
            showPicker(fragment, editText)
        }
    }

    private fun showPicker(fragment: Fragment, editText: EditText) {
        val calendar = FormatHelper.parseToCalendar(editText.text?.toString().orEmpty())

        DatePickerDialog(
            fragment.requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    fragment.requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        editText.setText(FormatHelper.formatDisplay(calendar.time))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
