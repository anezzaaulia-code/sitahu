package anezza.aulia.sitahu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.PopupMenu
import android.widget.TextView

class PengeluaranAdapter(
    private val items: List<Pengeluaran>,
    private val onEdit: (Pengeluaran) -> Unit = {},
    private val onDelete: (Pengeluaran) -> Unit = {}
) : BaseAdapter() {

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Pengeluaran = items[position]

    override fun getItemId(position: Int): Long = items[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pengeluaran, parent, false)

        val item = getItem(position)
        val tvNama = view.findViewById<TextView>(R.id.tvNamaPengeluaran)
        val tvInfo = view.findViewById<TextView>(R.id.tvInfoPengeluaran)
        val btnMore = view.findViewById<TextView>(R.id.btnMorePengeluaran)

        tvNama.text = item.namaPengeluaran
        tvInfo.text = "${FormatHelper.toDisplayDateTime(item.tanggalPengeluaran)} • ${FormatHelper.rupiah(item.nominal)}\n${item.catatan.ifBlank { "Tanpa catatan" }}"

        view.setOnClickListener(null)
        view.isClickable = false
        view.isLongClickable = false

        btnMore.setOnClickListener { anchor ->
            PopupMenu(anchor.context, anchor).apply {
                menuInflater.inflate(R.menu.menu_pengeluaran_actions, menu)
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
