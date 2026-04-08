package anezza.aulia.sitahu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PengeluaranAdapter(
    private val items: List<Pengeluaran>,
    private val onEdit: (Pengeluaran) -> Unit,
    private val onDelete: (Pengeluaran) -> Unit
) : RecyclerView.Adapter<PengeluaranAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNamaPengeluaran: TextView = view.findViewById(R.id.tvNamaPengeluaran)
        val tvInfo: TextView = view.findViewById(R.id.tvInfoPengeluaran)
        val btnEdit: Button = view.findViewById(R.id.btnEditPengeluaran)
        val btnHapus: Button = view.findViewById(R.id.btnHapusPengeluaran)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pengeluaran, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvNamaPengeluaran.text = item.namaPengeluaran
        holder.tvInfo.text = "${FormatHelper.toDisplayDateTime(item.tanggalPengeluaran)} • ${FormatHelper.rupiah(item.nominal)}\n${item.catatan.ifBlank { "Tanpa catatan" }}"

        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnHapus.setOnClickListener { onDelete(item) }
    }
}
