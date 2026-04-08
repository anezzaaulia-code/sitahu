package anezza.aulia.sitahu

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import anezza.aulia.sitahu.database.DatabaseHelper

class StokActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var rvStok: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvRingkasan: TextView

    private val listProduk = mutableListOf<Produk>()
    private lateinit var adapter: StockAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stok)

        db = DatabaseHelper(this)
        rvStok = findViewById(R.id.rvStok)
        tvEmpty = findViewById(R.id.tvEmptyStok)
        tvRingkasan = findViewById(R.id.tvRingkasanStok)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        adapter = StockAdapter(listProduk) { produk ->
            startActivity(Intent(this, DetailStokActivity::class.java).putExtra("produk_id", produk.id))
        }
        rvStok.layoutManager = LinearLayoutManager(this)
        rvStok.adapter = adapter

        AppNavigator.setupBottomNav(this, AppNavigator.Tab.STOK)
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
        rvStok.visibility = if (isEmpty) View.GONE else View.VISIBLE
        tvRingkasan.text = if (isEmpty) {
            "Belum ada produk tersimpan."
        } else {
            "Total stok tersimpan ${db.getTotalStok()} unit dari ${listProduk.size} produk."
        }
    }
}
