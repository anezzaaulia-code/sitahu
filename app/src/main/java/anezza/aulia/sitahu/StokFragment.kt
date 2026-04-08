package anezza.aulia.sitahu

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import anezza.aulia.sitahu.database.DatabaseHelper

class StokFragment : BaseScreenFragment(R.layout.activity_stok) {

    private var db: DatabaseHelper? = null
    private var rvStok: RecyclerView? = null
    private var tvEmpty: TextView? = null
    private var tvRingkasan: TextView? = null
    private val listProduk = mutableListOf<Produk>()
    private var adapter: StockAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())
        hideEmbeddedBottomNav(view)
        hideBackButton(view)

        rvStok = view.findViewById(R.id.rvStok)
        tvEmpty = view.findViewById(R.id.tvEmptyStok)
        tvRingkasan = view.findViewById(R.id.tvRingkasanStok)

        adapter = StockAdapter(listProduk) { produk -> host().openStokDetail(produk.id) }
        rvStok?.layoutManager = LinearLayoutManager(requireContext())
        rvStok?.adapter = adapter
    }

    override fun refreshContent() {
        val helper = db ?: return
        listProduk.clear()
        listProduk.addAll(helper.getAllProduk())
        adapter?.notifyDataSetChanged()
        val isEmpty = listProduk.isEmpty()
        tvEmpty?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvStok?.visibility = if (isEmpty) View.GONE else View.VISIBLE
        tvRingkasan?.text = if (isEmpty) {
            "Belum ada produk tersimpan."
        } else {
            "Total stok tersimpan ${helper.getTotalStok()} unit dari ${listProduk.size} produk."
        }
    }
}
