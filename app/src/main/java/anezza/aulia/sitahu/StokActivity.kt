package anezza.aulia.sitahu

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class StokActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var listProduk: List<Produk>

    // Item 1
    private lateinit var tvNama1: TextView
    private lateinit var tvStok1: TextView
    private lateinit var tvStatus1: TextView
    private lateinit var btnDetail1: CardView

    // Item 2
    private lateinit var tvNama2: TextView
    private lateinit var tvStok2: TextView
    private lateinit var tvStatus2: TextView
    private lateinit var btnDetail2: CardView

    private lateinit var etCari: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stok)

        db = DatabaseHelper(this)

        // bind view
        tvNama1 = findViewById(R.id.tvNamaProduk1)
        tvStok1 = findViewById(R.id.tvStokProduk1)
        tvStatus1 = findViewById(R.id.tvStatusProduk1)
        btnDetail1 = findViewById(R.id.btnDetailProduk1)

        tvNama2 = findViewById(R.id.tvNamaProduk2)
        tvStok2 = findViewById(R.id.tvStokProduk2)
        tvStatus2 = findViewById(R.id.tvStatusProduk2)
        btnDetail2 = findViewById(R.id.btnDetailProduk2)

        etCari = findViewById(R.id.etCariProduk)

        loadData()
    }

    private fun loadData() {
        listProduk = db.getAllProduk()

        if (listProduk.isNotEmpty()) {

            // ITEM 1
            val p1 = listProduk.getOrNull(0)
            p1?.let {
                tvNama1.text = it.nama
                tvStok1.text = "Stok ${it.stok} pcs • Minimum ${it.minStok}"

                val status = getStatus(it)
                tvStatus1.text = status
                setStatusColor(tvStatus1, status)

                btnDetail1.setOnClickListener {
                    openDetail(it.id)
                }
            }

            // ITEM 2
            val p2 = listProduk.getOrNull(1)
            p2?.let {
                tvNama2.text = it.nama
                tvStok2.text = "Stok ${it.stok} pcs • Minimum ${it.minStok}"

                val status = getStatus(it)
                tvStatus2.text = status
                setStatusColor(tvStatus2, status)

                btnDetail2.setOnClickListener {
                    openDetail(it.id)
                }
            }
        }
    }

    private fun getStatus(produk: Produk): String {
        return when {
            produk.stok == 0 -> "Habis"
            produk.stok <= produk.minStok -> "Menipis"
            else -> "Aman"
        }
    }

    private fun setStatusColor(tv: TextView, status: String) {
        when (status) {
            "Aman" -> tv.setBackgroundColor(0xFFD8E3CB.toInt())
            "Menipis" -> tv.setBackgroundColor(0xFFFFE0B2.toInt())
            "Habis" -> tv.setBackgroundColor(0xFFFFCDD2.toInt())
        }
    }

    private fun openDetail(id: Int) {
        val intent = Intent(this, DetailStokActivity::class.java)
        intent.putExtra("id", id)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}