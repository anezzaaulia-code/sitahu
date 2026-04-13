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

class PengeluaranFragment : BaseScreenFragment(R.layout.pengeluaran) {

    private var db: DatabaseHelper? = null
    private var lvPengeluaran: ListView? = null
    private var tvEmpty: TextView? = null
    private var tvTotalPengeluaran: TextView? = null
    private var actvCari: AutoCompleteTextView? = null

    private val allItems = mutableListOf<Pengeluaran>()
    private val filteredItems = mutableListOf<Pengeluaran>()
    private var adapter: PengeluaranAdapter? = null
    private var suggestionAdapter: ArrayAdapter<String>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseHelper(requireContext())
        lvPengeluaran = view.findViewById(R.id.lvPengeluaran)
        tvEmpty = view.findViewById(R.id.tvEmptyPengeluaran)
        tvTotalPengeluaran = view.findViewById(R.id.tvTotalPengeluaran)
        actvCari = view.findViewById(R.id.actvCariPengeluaran)

        setupBack(view)
        view.findViewById<View>(R.id.btnTambahPengeluaran).setOnClickListener {
            host().openPengeluaranForm()
        }

        adapter = PengeluaranAdapter(
            items = filteredItems,
            onEdit = { item -> host().openPengeluaranForm(item.id) },
            onDelete = { item -> confirmDelete(item) }
        )
        lvPengeluaran?.adapter = adapter

        suggestionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf<String>())
        actvCari?.setAdapter(suggestionAdapter)
        actvCari?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        parentFragmentManager.setFragmentResultListener(RESULT_KEY, viewLifecycleOwner) { _, _ ->
            refreshContent()
        }
    }

    override fun refreshContent() {
        loadData()
    }

    private fun loadData() {
        val helper = db ?: return
        allItems.clear()
        allItems.addAll(helper.getAllPengeluaran())
        tvTotalPengeluaran?.text = FormatHelper.rupiah(allItems.sumOf { it.nominal })
        updateSuggestions()
        applyFilter(actvCari?.text?.toString().orEmpty())
    }

    private fun updateSuggestions() {
        val data = allItems.map { it.namaPengeluaran }.distinct().sorted()
        suggestionAdapter?.clear()
        suggestionAdapter?.addAll(data)
        suggestionAdapter?.notifyDataSetChanged()
    }

    private fun applyFilter(query: String) {
        val keyword = query.trim()
        filteredItems.clear()
        filteredItems.addAll(
            if (keyword.isEmpty()) {
                allItems
            } else {
                allItems.filter { it.namaPengeluaran.contains(keyword, ignoreCase = true) || it.catatan.contains(keyword, ignoreCase = true) }
            }
        )
        adapter?.notifyDataSetChanged()

        val isEmpty = filteredItems.isEmpty()
        tvEmpty?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        lvPengeluaran?.visibility = if (isEmpty) View.GONE else View.VISIBLE
        if (isEmpty && keyword.isNotEmpty()) {
            tvEmpty?.text = "Pengeluaran dengan kata kunci '$keyword' tidak ditemukan."
        } else {
            tvEmpty?.text = "Belum ada pengeluaran tersimpan."
        }
    }

    private fun confirmDelete(item: Pengeluaran) {
        val helper = db ?: return
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus pengeluaran")
            .setMessage("Hapus data ${item.namaPengeluaran} dari daftar aktif?")
            .setPositiveButton("Hapus") { _, _ ->
                val deleted = helper.deletePengeluaran(item.id)
                if (deleted) {
                    Toast.makeText(requireContext(), "Pengeluaran dihapus", Toast.LENGTH_SHORT).show()
                    refreshContent()
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus pengeluaran", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    companion object {
        const val RESULT_KEY = "pengeluaran_changed"
    }
}
