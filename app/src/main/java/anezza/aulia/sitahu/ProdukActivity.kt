package anezza.aulia.sitahu

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import anezza.aulia.sitahu.database.DatabaseHelper
import anezza.aulia.sitahu.Produk

class ProdukActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var listView: ListView
    private lateinit var btnTambah: Button

    private var listProduk = mutableListOf<Produk>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produk)

        db = DatabaseHelper(this)

        listView = findViewById(R.id.listProduk)
        btnTambah = findViewById(R.id.btnTambah)

        loadData()

        // ➕ tambah produk
        btnTambah.setOnClickListener {
            startActivity(Intent(this, FormProdukActivity::class.java))
        }

        // 👉 klik item (bisa nanti ke detail/edit)
        listView.setOnItemClickListener { _, _, position, _ ->
            val produk = listProduk[position]

            Toast.makeText(
                this,
                "Produk: ${produk.nama}",
                Toast.LENGTH_SHORT
            ).show()
        }

        // 🔥 long click = delete
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val produk = listProduk[position]

            db.deleteProduk(produk.id)
            Toast.makeText(this, "Produk dihapus", Toast.LENGTH_SHORT).show()

            loadData()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        listProduk.clear()
        listProduk.addAll(db.getAllProduk())

        val display = listProduk.map {
            "${it.nama}\nStok: ${it.stokSaatIni} ${it.satuan} • Min: ${it.stokMinimum}\nHarga: Rp ${it.hargaJual}"
        }

        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            display
        )

        listView.adapter = adapter
    }
}