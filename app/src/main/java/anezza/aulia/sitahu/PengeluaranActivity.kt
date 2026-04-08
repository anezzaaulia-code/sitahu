package anezza.aulia.sitahu

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import anezza.aulia.sitahu.database.DatabaseHelper

class PengeluaranActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var rvPengeluaran: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvTotalPengeluaran: TextView

    private val items = mutableListOf<Pengeluaran>()
    private lateinit var adapter: PengeluaranAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pengeluaran)

        db = DatabaseHelper(this)
        rvPengeluaran = findViewById(R.id.rvPengeluaran)
        tvEmpty = findViewById(R.id.tvEmptyPengeluaran)
        tvTotalPengeluaran = findViewById(R.id.tvTotalPengeluaran)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnTambahPengeluaran).setOnClickListener {
            startActivity(Intent(this, FormPengeluaranActivity::class.java))
        }

        adapter = PengeluaranAdapter(
            items = items,
            onEdit = { item ->
                startActivity(
                    Intent(this, FormPengeluaranActivity::class.java)
                        .putExtra("pengeluaran_id", item.id)
                )
            },
            onDelete = { item -> confirmDelete(item) }
        )

        rvPengeluaran.layoutManager = LinearLayoutManager(this)
        rvPengeluaran.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        items.clear()
        items.addAll(db.getAllPengeluaran())
        adapter.notifyDataSetChanged()

        val isEmpty = items.isEmpty()
        tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvPengeluaran.visibility = if (isEmpty) View.GONE else View.VISIBLE
        tvTotalPengeluaran.text = FormatHelper.rupiah(items.sumOf { it.nominal })
    }

    private fun confirmDelete(item: Pengeluaran) {
        AlertDialog.Builder(this)
            .setTitle("Hapus pengeluaran")
            .setMessage("Hapus data ${item.namaPengeluaran}?")
            .setPositiveButton("Hapus") { _, _ ->
                val deleted = db.deletePengeluaran(item.id)
                if (deleted) {
                    Toast.makeText(this, "Pengeluaran dihapus", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(this, "Gagal menghapus pengeluaran", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}