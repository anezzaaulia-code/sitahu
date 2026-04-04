package anezza.aulia.sitahu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemRekapAdapter(
    private val list: MutableList<ItemRekap>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<ItemRekapAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNamaProduk)
        val tvJumlah: TextView = view.findViewById(R.id.tvJumlah)
        val tvSubtotal: TextView = view.findViewById(R.id.tvSubtotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rekap, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvNama.text = item.namaProduk
        holder.tvJumlah.text = "${item.jumlah} pcs"
        holder.tvSubtotal.text = "Rp ${item.subtotal}"

        holder.itemView.setOnClickListener {
            onClick(position)
        }
    }
}