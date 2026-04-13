package anezza.aulia.sitahu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

class StockAdapter(
    private val items: List<Produk>
) : BaseAdapter() {

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Produk = items[position]

    override fun getItemId(position: Int): Long = items[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock, parent, false)

        val item = getItem(position)
        val tvNama = view.findViewById<TextView>(R.id.tvNama)
        val tvDetail = view.findViewById<TextView>(R.id.tvDetail)
        val tvMeta = view.findViewById<TextView>(R.id.tvMeta)
        val btnAction = view.findViewById<TextView>(R.id.btnAction)

        val (status, badgeRes, textColorRes) = when {
            item.stokSaatIni <= 0 -> Triple("Habis", R.drawable.bg_badge_orange, R.color.orange_text)
            item.stokSaatIni <= item.stokMinimum -> Triple("Menipis", R.drawable.bg_badge_gold, R.color.gold_text)
            else -> Triple("Aman", R.drawable.bg_badge_green, R.color.green_text)
        }

        tvNama.text = item.nama
        tvDetail.text = buildString {
            append("Stok ${item.stokSaatIni} ${item.satuan} • Minimum ${item.stokMinimum}")
            if (!item.aktif) append(" • Nonaktif")
        }
        tvMeta.text = status
        tvMeta.background = ContextCompat.getDrawable(view.context, badgeRes)
        tvMeta.setTextColor(ContextCompat.getColor(view.context, textColorRes))

        // Biarkan ListView yang menangani tap biasa dan long press agar ContextMenu bisa muncul stabil.
        view.isClickable = false
        view.isLongClickable = false
        btnAction.isClickable = false
        btnAction.isLongClickable = false

        return view
    }
}
