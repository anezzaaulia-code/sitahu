package anezza.aulia.sitahu

import android.widget.LinearLayout
import android.view.LayoutInflater
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DetailStokActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var produk: Produk? = null

    private lateinit var tvNama: TextView
    private lateinit var tvJenis: TextView
    private lateinit var tvStok: TextView
    private lateinit var tvMin: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvTag: TextView

    private lateinit var btnEdit: Button
    private lateinit var btnAdjust: Button
    private lateinit var btnBack: CardView
    private lateinit var containerLog: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_stok)

        db = DatabaseHelper(this)

        // bind view
        tvNama = findViewById(R.id.tvNamaProduk)
        tvJenis = findViewById(R.id.tvJenisProduk)
        tvStok = findViewById(R.id.tvStokSaatIni)
        tvMin = findViewById(R.id.tvStokMinimum)
        tvStatus = findViewById(R.id.tvStatusStok)
        tvTag = findViewById(R.id.tvTagProduk)

        btnEdit = findViewById(R.id.btnEditProduk)
        btnAdjust = findViewById(R.id.btnAdjustment)
        btnBack = findViewById(R.id.btn_back)

        // 🔥 TAMBAHAN (ambil container log)
        containerLog = findViewById(R.id.containerLog)

        val id = intent.getIntExtra("id", -1)

        loadData(id)

        // tombol back
        btnBack.setOnClickListener {
            finish()
        }

        // edit produk
        btnEdit.setOnClickListener {
            produk?.let {
                val intent = Intent(this, FormProdukActivity::class.java)
                intent.putExtra("id", it.id)
                startActivity(intent)
            }
        }

        // adjustment stok → ke form pengeluaran
        btnAdjust.setOnClickListener {
            produk?.let {
                val intent = Intent(this, FormPengeluaranActivity::class.java)
                intent.putExtra("id", it.id)
                startActivity(intent)
            }
        }
    }

    private fun loadData(id: Int) {
        produk = db.getProdukById(id)

        produk?.let {

            // isi data utama
            tvNama.text = it.nama
            tvJenis.text = it.kategori.uppercase()

            tvStok.text = "${it.stok} pcs"
            tvMin.text = "${it.minStok} pcs"

            // status
            val status = getStatus(it)
            tvStatus.text = status
            setStatusColor(status)

            // tag (opsional)
            tvTag.text = if (it.kategori.lowercase().contains("tahu")) "KASIR" else "PRODUK"

            // 🔥 TAMBAHAN (tampilkan riwayat)
            tampilLog(it.id)
        }
    }

    private fun tampilLog(produkId: Int) {
        val logs = db.getLogByProdukId(produkId)

        containerLog.removeAllViews()

        for (log in logs) {
            val view = LayoutInflater.from(this)
                .inflate(android.R.layout.simple_list_item_2, containerLog, false)

            val text1 = view.findViewById<TextView>(android.R.id.text1)
            val text2 = view.findViewById<TextView>(android.R.id.text2)

            val tipe = if (log.first == "keluar") "Pengeluaran" else "Masuk"
            val jumlah = if (log.first == "keluar") "-${log.second}" else "+${log.second}"

            text1.text = "$tipe • ${log.third}"
            text2.text = "$jumlah pcs"

            containerLog.addView(view)
        }
    }

    private fun getStatus(p: Produk): String {
        return when {
            p.stok == 0 -> "Habis"
            p.stok <= p.minStok -> "Menipis"
            else -> "Aman"
        }
    }

    private fun setStatusColor(status: String) {
        when (status) {
            "Aman" -> tvStatus.setBackgroundColor(0xFFD8E3CB.toInt())
            "Menipis" -> tvStatus.setBackgroundColor(0xFFFFE0B2.toInt())
            "Habis" -> tvStatus.setBackgroundColor(0xFFFFCDD2.toInt())
        }
    }
}