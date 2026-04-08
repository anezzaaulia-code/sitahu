package anezza.aulia.sitahu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class StockAdapter(
    private val items: List<Produk>,
    private val onClick: (Produk) -> Unit
) : RecyclerView.Adapter<StockAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNama)
        val tvDetail: TextView = view.findViewById(R.id.tvDetail)
        val tvMeta: TextView = view.findViewById(R.id.tvMeta)
        val btnAction: TextView = view.findViewById(R.id.btnAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stock, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val (status, badgeRes, textColorRes) = when {
            item.stokSaatIni <= 0 -> Triple("Habis", R.drawable.bg_badge_orange, R.color.orange_text)
            item.stokSaatIni <= item.stokMinimum -> Triple("Menipis", R.drawable.bg_badge_gold, R.color.gold_text)
            else -> Triple("Aman", R.drawable.bg_badge_green, R.color.green_text)
        }

        holder.tvNama.text = item.nama
        holder.tvDetail.text = "Stok ${item.stokSaatIni} ${item.satuan} • Minimum ${item.stokMinimum}"
        holder.tvMeta.text = status
        holder.tvMeta.background = ContextCompat.getDrawable(holder.itemView.context, badgeRes)
        holder.tvMeta.setTextColor(ContextCompat.getColor(holder.itemView.context, textColorRes))

        holder.itemView.setOnClickListener { onClick(item) }
        holder.btnAction.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
