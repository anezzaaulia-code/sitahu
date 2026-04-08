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

class ProdukActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var rvProduk: RecyclerView
    private lateinit var tvEmpty: TextView

    private val listProduk = mutableListOf<Produk>()
    private lateinit var adapter: ProdukAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produk)

        db = DatabaseHelper(this)
        rvProduk = findViewById(R.id.rvProduk)
        tvEmpty = findViewById(R.id.tvEmptyProduk)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnTambahProduk).setOnClickListener {
            startActivity(Intent(this, FormProdukActivity::class.java))
        }

        adapter = ProdukAdapter(
            items = listProduk,
            onEdit = { produk ->
                startActivity(Intent(this, FormProdukActivity::class.java).putExtra("produk_id", produk.id))
            },
            onDelete = { produk ->
                confirmDelete(produk)
            }
        )
        rvProduk.layoutManager = LinearLayoutManager(this)
        rvProduk.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        listProduk.clear()
        listProduk.addAll(db.getAllProduk())
        adapter.notifyDataSetChanged()
        val isEmpty = listProduk.isEmpty()
        tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvProduk.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun confirmDelete(produk: Produk) {
        AlertDialog.Builder(this)
            .setTitle("Hapus produk")
            .setMessage("Hapus ${produk.nama}? Produk yang sudah punya mutasi stok tidak bisa dihapus.")
            .setPositiveButton("Hapus") { _, _ ->
                val deleted = db.deleteProduk(produk.id)
                if (deleted) {
                    Toast.makeText(this, "Produk dihapus", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(this, "Produk tidak bisa dihapus karena sudah punya transaksi", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
