package anezza.aulia.sitahu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.PopupMenu
import android.widget.TextView

class ProdukAdapter(
    private val items: List<Produk>,
    private val onEdit: (Produk) -> Unit = {},
    private val onDelete: (Produk) -> Unit = {}
) : BaseAdapter() {

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Produk = items[position]

    override fun getItemId(position: Int): Long = items[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produk, parent, false)

        val item = getItem(position)
        val tvNama = view.findViewById<TextView>(R.id.tvNamaProduk)
        val tvInfo = view.findViewById<TextView>(R.id.tvInfoProduk)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatusProduk)
        val btnMore = view.findViewById<TextView>(R.id.btnMoreProduk)

        tvNama.text = item.nama
        tvInfo.text = "${item.satuan} • Stok ${item.stokSaatIni} • Minimum ${item.stokMinimum} • ${FormatHelper.rupiah(item.hargaJual)}"
        tvStatus.text = if (item.aktif) "Aktif" else "Nonaktif"
        tvStatus.setBackgroundResource(if (item.aktif) R.drawable.bg_badge_green else R.drawable.bg_badge_orange)
        tvStatus.setTextColor(view.context.getColor(if (item.aktif) R.color.green_text else R.color.orange_text))

        view.setOnClickListener(null)
        view.isClickable = false
        view.isFocusable = false

        btnMore.setOnClickListener { anchor ->
            PopupMenu(anchor.context, anchor).apply {
                menuInflater.inflate(R.menu.menu_produk_actions, menu)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit -> onEdit(item)
                        R.id.action_delete -> onDelete(item)
                    }
                    true
                }
            }.show()
        }

        return view
    }
}
