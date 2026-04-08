package anezza.aulia.sitahu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProdukAdapter(
    private val items: List<Produk>,
    private val onEdit: (Produk) -> Unit,
    private val onDelete: (Produk) -> Unit
) : RecyclerView.Adapter<ProdukAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNamaProduk)
        val tvInfo: TextView = view.findViewById(R.id.tvInfoProduk)
        val btnEdit: Button = view.findViewById(R.id.btnEditProduk)
        val btnHapus: Button = view.findViewById(R.id.btnHapusProduk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produk, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvNama.text = item.nama
        holder.tvInfo.text = "${item.satuan} • Stok ${item.stokSaatIni} • Minimum ${item.stokMinimum} • ${FormatHelper.rupiah(item.hargaJual)}"

        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnHapus.setOnClickListener { onDelete(item) }
    }
}
