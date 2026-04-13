package anezza.aulia.sitahu

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import anezza.aulia.sitahu.database.DatabaseHelper
import anezza.aulia.sitahu.model.RiwayatUmumItem

class RiwayatUmumFragment : Fragment(R.layout.fragment_riwayat_umum) {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var lvRiwayat: ListView
    private lateinit var tvRingkasan: TextView
    private lateinit var cbPenjualan: CheckBox
    private lateinit var cbProduksi: CheckBox
    private lateinit var cbPengeluaran: CheckBox

    private val allItems = mutableListOf<RiwayatUmumItem>()
    private val filteredItems = mutableListOf<RiwayatUmumItem>()
    private lateinit var adapter: RiwayatUmumAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        lvRiwayat = view.findViewById(R.id.lvRiwayatUmum)
        tvRingkasan = view.findViewById(R.id.tvRingkasanRiwayat)
        cbPenjualan = view.findViewById(R.id.cbRiwayatPenjualan)
        cbProduksi = view.findViewById(R.id.cbRiwayatProduksi)
        cbPengeluaran = view.findViewById(R.id.cbRiwayatPengeluaran)

        adapter = RiwayatUmumAdapter(filteredItems)
        lvRiwayat.adapter = adapter

        val listener = View.OnClickListener { applyFilter() }
        cbPenjualan.setOnClickListener(listener)
        cbProduksi.setOnClickListener(listener)
        cbPengeluaran.setOnClickListener(listener)

        loadData()

        view.findViewById<View>(R.id.btnBackRiwayatUmum).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::dbHelper.isInitialized) {
            loadData()
        }
    }

    private fun loadData() {
        allItems.clear()
        allItems.addAll(dbHelper.getRiwayatUmum())
        applyFilter()
    }

    private fun applyFilter() {
        filteredItems.clear()
        filteredItems.addAll(
            allItems.filter { item ->
                when (item.tipe) {
                    "PENJUALAN" -> cbPenjualan.isChecked
                    "PRODUKSI" -> cbProduksi.isChecked
                    "PENGELUARAN" -> cbPengeluaran.isChecked
                    else -> true
                }
            }
        )
        adapter.notifyDataSetChanged()
        tvRingkasan.text = "${filteredItems.size} aktivitas tercatat"
    }
}
