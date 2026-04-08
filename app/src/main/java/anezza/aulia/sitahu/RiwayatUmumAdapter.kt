package anezza.aulia.sitahu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import anezza.aulia.sitahu.model.RiwayatUmumItem

class RiwayatUmumAdapter(
    private val items: List<RiwayatUmumItem>
) : RecyclerView.Adapter<RiwayatUmumAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcon: TextView = view.findViewById(R.id.tvIconRiwayat)
        val tvId: TextView = view.findViewById(R.id.tvIdRiwayat)
        val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitleRiwayat)
        val tvBadge: TextView = view.findViewById(R.id.tvBadgeRiwayat)
        val tvNilai: TextView = view.findViewById(R.id.tvNilaiRiwayat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat_umum, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

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

        holder.tvIcon.text = icon
        holder.tvId.text = item.judul
        holder.tvSubtitle.text = "${item.id} • ${FormatHelper.toDisplayDateTime(item.tanggal)}\n${item.subtitle}"
        holder.tvBadge.text = badge
        holder.tvNilai.text = item.nilai
    }
}
