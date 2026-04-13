package anezza.aulia.sitahu

import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import anezza.aulia.sitahu.database.DatabaseHelper

class StokFragment : BaseScreenFragment(R.layout.activity_stok) {

    private var db: DatabaseHelper? = null
    private var lvStok: ListView? = null
    private var tvEmpty: TextView? = null
    private var tvRingkasan: TextView? = null
    private val listProduk = mutableListOf<Produk>()
    private var adapter: StockAdapter? = null
    private var selectedContextPosition: Int = AdapterView.INVALID_POSITION

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())
        hideEmbeddedBottomNav(view)
        hideBackButton(view)

        lvStok = view.findViewById(R.id.lvStok)
        tvEmpty = view.findViewById(R.id.tvEmptyStok)
        tvRingkasan = view.findViewById(R.id.tvRingkasanStok)

        adapter = StockAdapter(listProduk)
        lvStok?.adapter = adapter
        lvStok?.let { listView ->
            registerForContextMenu(listView)
            listView.onItemClickListener = null
            listView.setOnItemLongClickListener { _, _, position, _ ->
                selectedContextPosition = position
                runCatching {
                    requireActivity().openContextMenu(listView)
                }.isSuccess
            }
        }
    }

    override fun refreshContent() {
        val helper = db ?: return
        listProduk.clear()
        listProduk.addAll(helper.getAllProduk(includeInactive = true))
        adapter?.notifyDataSetChanged()
        val isEmpty = listProduk.isEmpty()
        tvEmpty?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        lvStok?.visibility = if (isEmpty) View.GONE else View.VISIBLE
        tvRingkasan?.text = if (isEmpty) {
            "Belum ada produk tersimpan."
        } else {
            "Total stok tersimpan ${helper.getTotalStok()} unit dari ${listProduk.size} produk."
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v.id != R.id.lvStok) return

        val position = (menuInfo as? AdapterView.AdapterContextMenuInfo)?.position
            ?: selectedContextPosition
        val produk = listProduk.getOrNull(position)

        menu.setHeaderTitle(produk?.nama ?: "Aksi stok")
        menu.add(0, MENU_DETAIL_STOK, 0, "Detail stok")
        menu.add(0, MENU_BUKA_PRODUK, 1, "Buka halaman produk")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val position = (item.menuInfo as? AdapterView.AdapterContextMenuInfo)?.position
            ?: selectedContextPosition
        val produk = listProduk.getOrNull(position) ?: return super.onContextItemSelected(item)

        val handled = when (item.itemId) {
            MENU_DETAIL_STOK -> {
                host().openStokDetail(produk.id)
                true
            }
            MENU_BUKA_PRODUK -> {
                host().openProductForm(produk.id)
                true
            }
            else -> false
        }

        if (handled) {
            selectedContextPosition = AdapterView.INVALID_POSITION
            return true
        }
        return super.onContextItemSelected(item)
    }

    companion object {
        private const val MENU_DETAIL_STOK = 1001
        private const val MENU_BUKA_PRODUK = 1002
    }
}
