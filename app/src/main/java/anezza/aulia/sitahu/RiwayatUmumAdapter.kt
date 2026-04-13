package anezza.aulia.sitahu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import anezza.aulia.sitahu.model.RiwayatUmumItem

class RiwayatUmumAdapter(
    private val items: List<RiwayatUmumItem>
) : BaseAdapter() {

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): RiwayatUmumItem = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat_umum, parent, false)

        val item = getItem(position)
        val tvIcon = view.findViewById<TextView>(R.id.tvIconRiwayat)
        val tvId = view.findViewById<TextView>(R.id.tvIdRiwayat)
        val tvSubtitle = view.findViewById<TextView>(R.id.tvSubtitleRiwayat)
        val tvBadge = view.findViewById<TextView>(R.id.tvBadgeRiwayat)
        val tvNilai = view.findViewById<TextView>(R.id.tvNilaiRiwayat)

        val icon = when (item.tipe) {
            "PENJUALAN" -> "T"
            "PRODUKSI" -> "P"
            "PENGELUARAN" -> "E"
            else -> "R"
        }

        val badge = when (item.tipe) {
            "PENJUALAN" -> "Penjualan"
            "PRODUKSI" -> "Produksi"
            "PENGELUARAN" -> "Pengeluaran"
            else -> "Riwayat"
        }

        tvIcon.text = icon
        tvId.text = item.judul
        tvSubtitle.text = "${item.id} • ${FormatHelper.toDisplayDateTime(item.tanggal)}\n${item.subtitle}"
        tvBadge.text = badge
        tvNilai.text = item.nilai

        return view
    }
}
