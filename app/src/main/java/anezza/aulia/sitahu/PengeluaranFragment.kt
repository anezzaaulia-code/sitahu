package anezza.aulia.sitahu

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import anezza.aulia.sitahu.database.DatabaseHelper

class PengeluaranFragment : BaseScreenFragment(R.layout.pengeluaran) {

    private var db: DatabaseHelper? = null
    private var rvPengeluaran: RecyclerView? = null
    private var tvEmpty: TextView? = null
    private var tvTotalPengeluaran: TextView? = null

    private val items = mutableListOf<Pengeluaran>()
    private var adapter: PengeluaranAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseHelper(requireContext())
        rvPengeluaran = view.findViewById(R.id.rvPengeluaran)
        tvEmpty = view.findViewById(R.id.tvEmptyPengeluaran)
        tvTotalPengeluaran = view.findViewById(R.id.tvTotalPengeluaran)

        setupBack(view)
        view.findViewById<View>(R.id.btnTambahPengeluaran).setOnClickListener {
            host().openPengeluaranForm()
        }

        adapter = PengeluaranAdapter(
            items = items,
            onEdit = { item -> host().openPengeluaranForm(item.id) },
            onDelete = { item -> confirmDelete(item) }
        )
        rvPengeluaran?.layoutManager = LinearLayoutManager(requireContext())
        rvPengeluaran?.adapter = adapter

        parentFragmentManager.setFragmentResultListener(RESULT_KEY, viewLifecycleOwner) { _, _ ->
            refreshContent()
        }
    }

    override fun refreshContent() {
        loadData()
    }

    private fun loadData() {
        val helper = db ?: return
        items.clear()
        items.addAll(helper.getAllPengeluaran())
        adapter?.notifyDataSetChanged()

        val isEmpty = items.isEmpty()
        tvEmpty?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvPengeluaran?.visibility = if (isEmpty) View.GONE else View.VISIBLE
        tvTotalPengeluaran?.text = FormatHelper.rupiah(items.sumOf { it.nominal })
    }

    private fun confirmDelete(item: Pengeluaran) {
        val helper = db ?: return
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus pengeluaran")
            .setMessage("Hapus data ${item.namaPengeluaran} dari daftar aktif?")
            .setPositiveButton("Hapus") { _, _ ->
                val deleted = helper.deletePengeluaran(item.id)
                if (deleted) {
                    Toast.makeText(requireContext(), "Pengeluaran disembunyikan dari daftar aktif", Toast.LENGTH_SHORT).show()
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
