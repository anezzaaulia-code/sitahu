package anezza.aulia.sitahu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import anezza.aulia.sitahu.database.DatabaseHelper

class DetailStokActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var tvNamaProduk: TextView
    private lateinit var tvStokSaatIni: TextView
    private lateinit var tvStokMinimum: TextView
    private lateinit var tvStatusStok: TextView
    private lateinit var containerLog: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_stok)

        db = DatabaseHelper(this)
        tvNamaProduk = findViewById(R.id.tvNamaProduk)
        tvStokSaatIni = findViewById(R.id.tvStokSaatIni)
        tvStokMinimum = findViewById(R.id.tvStokMinimum)
        tvStatusStok = findViewById(R.id.tvStatusStok)
        containerLog = findViewById(R.id.containerLog)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        val produkId = intent.getIntExtra("produk_id", 0)
        if (produkId <= 0) {
            Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        loadData(produkId)
    }

    private fun loadData(produkId: Int) {
        val produk = db.getProdukById(produkId)
        if (produk == null) {
            Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvNamaProduk.text = produk.nama
        tvStokSaatIni.text = "${produk.stokSaatIni} ${produk.satuan}"
        tvStokMinimum.text = "${produk.stokMinimum} ${produk.satuan}"

        val (statusLabel, statusBg, statusColor) = when {
            produk.stokSaatIni <= 0 -> Triple("Habis", R.drawable.bg_badge_orange, R.color.orange_text)
            produk.stokSaatIni <= produk.stokMinimum -> Triple("Menipis", R.drawable.bg_badge_gold, R.color.gold_text)
            else -> Triple("Aman", R.drawable.bg_badge_green, R.color.green_text)
        }
        tvStatusStok.text = statusLabel
        tvStatusStok.background = ContextCompat.getDrawable(this, statusBg)
        tvStatusStok.setTextColor(ContextCompat.getColor(this, statusColor))

        containerLog.removeAllViews()
        val inflater = LayoutInflater.from(this)
        val items = db.getMutasiByProduk(produkId)
        if (items.isEmpty()) {
            val view = inflater.inflate(R.layout.item_mutasi, containerLog, false)
            view.findViewById<TextView>(R.id.tvArah).text = "Belum ada riwayat"
            view.findViewById<TextView>(R.id.tvArah).background = ContextCompat.getDrawable(this, R.drawable.bg_badge_blue)
            view.findViewById<TextView>(R.id.tvArah).setTextColor(ContextCompat.getColor(this, R.color.blue_text))
            view.findViewById<TextView>(R.id.tvTanggal).text = ""
            view.findViewById<TextView>(R.id.tvCatatan).text = "Mutasi stok akan muncul di sini"
            view.findViewById<TextView>(R.id.tvJumlah).text = "-"
            containerLog.addView(view)
            return
        }

        items.forEach { item ->
            val view = inflater.inflate(R.layout.item_mutasi, containerLog, false)
            val prefix = if (item.arah == "MASUK") "+" else "-"
            val arahView = view.findViewById<TextView>(R.id.tvArah)
            val (badgeRes, textColorRes) = if (item.arah == "MASUK") {
                R.drawable.bg_badge_green to R.color.green_text
            } else {
                R.drawable.bg_badge_orange to R.color.orange_text
            }
            arahView.text = item.arah
            arahView.background = ContextCompat.getDrawable(this, badgeRes)
            arahView.setTextColor(ContextCompat.getColor(this, textColorRes))
            view.findViewById<TextView>(R.id.tvTanggal).text = item.tanggal
            view.findViewById<TextView>(R.id.tvCatatan).text = item.catatan.ifBlank { item.jenisReferensi }
            view.findViewById<TextView>(R.id.tvJumlah).text = "$prefix${item.jumlah} ${produk.satuan}"
            containerLog.addView(view)
        }
    }
}
