package anezza.aulia.sitahu

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import anezza.aulia.sitahu.database.DatabaseHelper

class StokActivity : AppCompatActivity() {

    lateinit var db: DatabaseHelper

    lateinit var tvNama1: TextView
    lateinit var tvStok1: TextView
    lateinit var tvStatus1: TextView

    lateinit var tvNama2: TextView
    lateinit var tvStok2: TextView
    lateinit var tvStatus2: TextView

    lateinit var btnDetail1: LinearLayout
    lateinit var btnDetail2: LinearLayout

    lateinit var listProduk: List<Produk>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stok)

        db = DatabaseHelper(this)

        initView()
        loadData()
        setupClick()
    }

    private fun initView() {
        tvNama1 = findViewById(R.id.tvNamaProduk1)
        tvStok1 = findViewById(R.id.tvStokProduk1)
        tvStatus1 = findViewById(R.id.tvStatusProduk1)

        tvNama2 = findViewById(R.id.tvNamaProduk2)
        tvStok2 = findViewById(R.id.tvStokProduk2)
        tvStatus2 = findViewById(R.id.tvStatusProduk2)

        btnDetail1 = findViewById(R.id.itemTahuPutih)
        btnDetail2 = findViewById(R.id.itemTahuKuning)
    }

    private fun loadData() {
        listProduk = db.getAllProduk()

        if (listProduk.size >= 2) {
            val p1 = listProduk[0]
            val p2 = listProduk[1]

            setProdukUI(p1, tvNama1, tvStok1, tvStatus1)
            setProdukUI(p2, tvNama2, tvStok2, tvStatus2)
        }
    }

    private fun setProdukUI(
        produk: Produk,
        tvNama: TextView,
        tvStok: TextView,
        tvStatus: TextView
    ) {
        tvNama.text = produk.nama
        tvStok.text = "Stok ${produk.stokSaatIni} pcs • Minimum ${produk.stokMinimum}"

        val status = when {
            produk.stokSaatIni <= 0 -> "Habis"
            produk.stokSaatIni <= produk.stokMinimum -> "Menipis"
            else -> "Aman"
        }

        tvStatus.text = status
    }

    private fun setupClick() {
        btnDetail1.setOnClickListener {
            openDetail(listProduk[0].id)
        }

        btnDetail2.setOnClickListener {
            openDetail(listProduk[1].id)
        }
    }

    private fun openDetail(produkId: Int) {
        val intent = Intent(this, DetailStokActivity::class.java)
        intent.putExtra("produk_id", produkId)
        startActivity(intent)
    }
}