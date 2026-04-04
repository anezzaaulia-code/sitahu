package anezza.aulia.sitahu

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import anezza.aulia.sitahu.database.DatabaseHelper

class DetailStokActivity : AppCompatActivity() {

    lateinit var db: DatabaseHelper

    lateinit var tvNama: TextView
    lateinit var tvStok: TextView
    lateinit var tvMinimum: TextView
    lateinit var tvStatus: TextView

    lateinit var containerLog: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_stok)

        db = DatabaseHelper(this)

        val produkId = intent.getIntExtra("produk_id", 0)

        initView()
        loadData(produkId)
    }

    private fun initView() {
        tvNama = findViewById(R.id.tvNamaProduk)
        tvStok = findViewById(R.id.tvStokSaatIni)
        tvMinimum = findViewById(R.id.tvStokMinimum)
        tvStatus = findViewById(R.id.tvStatusStok)

        containerLog = findViewById(R.id.containerLog)
    }

    private fun loadData(produkId: Int) {

        val produk = db.getProdukById(produkId) ?: return

        tvNama.text = produk.nama
        tvStok.text = "${produk.stokSaatIni} pcs"
        tvMinimum.text = "${produk.stokMinimum} pcs"

        val status = when {
            produk.stokSaatIni <= 0 -> "Habis"
            produk.stokSaatIni <= produk.stokMinimum -> "Menipis"
            else -> "Aman"
        }

        tvStatus.text = status

        loadMutasi(produkId)
    }

    private fun loadMutasi(produkId: Int) {

        val list = db.getMutasiByProduk(produkId)

        containerLog.removeAllViews()

        for (item in list) {

            val tv = TextView(this)
            val arah = if (item.arah == "MASUK") "+" else "-"

            tv.text = "${item.catatan} • ${arah}${item.jumlah} pcs"
            tv.textSize = 14f

            containerLog.addView(tv)
        }
    }
}