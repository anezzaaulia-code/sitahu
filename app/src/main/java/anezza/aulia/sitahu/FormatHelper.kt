package anezza.aulia.sitahu

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FormatHelper {
    private val localeId = Locale("id", "ID")

    private fun storageDateTimeFormat() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private fun storageDateFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private fun displayDateTimeFormat() = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private fun displayDateFormat() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private fun legacyDateTimeFormat() = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun rupiah(value: Int): String {
        return NumberFormat.getCurrencyInstance(localeId).format(value)
    }

    fun nowStorage(): String = storageDateTimeFormat().format(Date())

    fun nowDisplay(): String = displayDateTimeFormat().format(Date())

    fun todayStorageDate(): String = storageDateFormat().format(Date())

    fun today(): String = nowDisplay()

    fun formatStorage(date: Date): String = storageDateTimeFormat().format(date)

    fun formatDisplay(date: Date): String = displayDateTimeFormat().format(date)

    fun normalizeDateTime(raw: String): String {
        val parsed = parseFlexibleDate(raw) ?: Date()
        return formatStorage(parsed)
    }

    fun toDisplayDateTime(raw: String): String {
        val parsed = parseFlexibleDate(raw) ?: return raw
        return formatDisplay(parsed)
    }

    fun parseToCalendar(raw: String): Calendar {
        val calendar = Calendar.getInstance()
        parseFlexibleDate(raw)?.let { calendar.time = it }
        return calendar
    }

    fun normalizeLegacyTimestamp(raw: String): String = normalizeDateTime(raw)

    private fun parseFlexibleDate(raw: String): Date? {
        val clean = raw.trim()
        if (clean.isEmpty()) return null

        if (clean.all { it.isDigit() }) {
            return clean.toLongOrNull()?.let { Date(it) }
        }

        val patterns = listOf(
            storageDateTimeFormat(),
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()),
            legacyDateTimeFormat(),
            displayDateFormat(),
            storageDateFormat()
        )

        patterns.forEach { format ->
            runCatching { return format.parse(clean) }.getOrNull()
        }
        return null
    }
}
