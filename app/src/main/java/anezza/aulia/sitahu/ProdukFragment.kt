package anezza.aulia.sitahu

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import anezza.aulia.sitahu.database.DatabaseHelper

class ProdukFragment : BaseScreenFragment(R.layout.activity_produk) {

    private var db: DatabaseHelper? = null
    private var rvProduk: RecyclerView? = null
    private var tvEmpty: TextView? = null

    private val listProduk = mutableListOf<Produk>()
    private var adapter: ProdukAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())
        setupBack(view)

        rvProduk = view.findViewById(R.id.rvProduk)
        tvEmpty = view.findViewById(R.id.tvEmptyProduk)

        adapter = ProdukAdapter(
            items = listProduk,
            onEdit = { produk -> host().openProductForm(produk.id) },
            onDelete = { produk -> confirmDelete(produk) }
        )

        rvProduk?.layoutManager = LinearLayoutManager(requireContext())
        rvProduk?.adapter = adapter

        view.findViewById<View>(R.id.btnTambahProduk).setOnClickListener {
            host().openProductForm()
        }

        parentFragmentManager.setFragmentResultListener(RESULT_KEY, viewLifecycleOwner) { _, _ ->
            refreshContent()
        }
    }

    override fun refreshContent() {
        loadData()
    }

    private fun loadData() {
        val helper = db ?: return
        listProduk.clear()
        listProduk.addAll(helper.getAllProduk())
        adapter?.notifyDataSetChanged()

        val isEmpty = listProduk.isEmpty()
        tvEmpty?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvProduk?.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun confirmDelete(produk: Produk) {
        val helper = db ?: return
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus produk")
            .setMessage("Hapus ${produk.nama} dari daftar aktif? Riwayat transaksi tetap disimpan.")
            .setPositiveButton("Hapus") { _, _ ->
                val deleted = helper.deleteProduk(produk.id)
                if (deleted) {
                    Toast.makeText(requireContext(), "Produk disembunyikan dari daftar aktif", Toast.LENGTH_SHORT).show()
                    refreshContent()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Produk gagal dihapus",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    companion object {
        const val RESULT_KEY = "produk_changed"
    }
}
