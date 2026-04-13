package anezza.aulia.sitahu

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import anezza.aulia.sitahu.database.DatabaseHelper

class ProdukFragment : BaseScreenFragment(R.layout.activity_produk) {

    private var db: DatabaseHelper? = null
    private var lvProduk: ListView? = null
    private var tvEmpty: TextView? = null
    private var actvCari: AutoCompleteTextView? = null

    private val allProduk = mutableListOf<Produk>()
    private val filteredProduk = mutableListOf<Produk>()
    private var adapter: ProdukAdapter? = null
    private var suggestionAdapter: ArrayAdapter<String>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())
        setupBack(view)

        lvProduk = view.findViewById(R.id.lvProduk)
        tvEmpty = view.findViewById(R.id.tvEmptyProduk)
        actvCari = view.findViewById(R.id.actvCariProduk)

        adapter = ProdukAdapter(
            items = filteredProduk,
            onEdit = { produk -> host().openProductForm(produk.id) },
            onDelete = { produk -> confirmDelete(produk) }
        )
        lvProduk?.adapter = adapter

        suggestionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf<String>())
        actvCari?.setAdapter(suggestionAdapter)
        actvCari?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

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
        allProduk.clear()
        allProduk.addAll(helper.getAllProduk(includeInactive = true))
        updateSuggestions()
        applyFilter(actvCari?.text?.toString().orEmpty())
    }

    private fun updateSuggestions() {
        val data = allProduk.map { it.nama }.distinct().sorted()
        suggestionAdapter?.clear()
        suggestionAdapter?.addAll(data)
        suggestionAdapter?.notifyDataSetChanged()
    }

    private fun applyFilter(query: String) {
        val keyword = query.trim()
        filteredProduk.clear()
        filteredProduk.addAll(
            if (keyword.isEmpty()) {
                allProduk
            } else {
                allProduk.filter { it.nama.contains(keyword, ignoreCase = true) }
            }
        )
        adapter?.notifyDataSetChanged()

        val isEmpty = filteredProduk.isEmpty()
        tvEmpty?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        lvProduk?.visibility = if (isEmpty) View.GONE else View.VISIBLE
        if (isEmpty && keyword.isNotEmpty()) {
            tvEmpty?.text = "Produk dengan kata kunci '$keyword' tidak ditemukan."
        } else {
            tvEmpty?.text = "Belum ada produk tersimpan."
        }
    }

    private fun confirmDelete(produk: Produk) {
        val helper = db ?: return
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus produk")
            .setMessage("Hapus ${produk.nama} dari daftar? Riwayat transaksi tetap disimpan.")
            .setPositiveButton("Hapus") { _, _ ->
                val deleted = helper.deleteProduk(produk.id)
                if (deleted) {
                    Toast.makeText(requireContext(), "Produk dihapus dari daftar", Toast.LENGTH_SHORT).show()
                    refreshContent()
                } else {
                    Toast.makeText(requireContext(), "Produk gagal dihapus", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    companion object {
        const val RESULT_KEY = "produk_changed"
    }
}
