package anezza.aulia.sitahu

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import anezza.aulia.sitahu.database.DatabaseHelper

class FormProdukActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    private lateinit var tvTitle: TextView
    private lateinit var etNama: EditText
    private lateinit var etSatuan: EditText
    private lateinit var etStok: EditText
    private lateinit var etMin: EditText
    private lateinit var etHarga: EditText
    private lateinit var etHasilPerMasak: EditText
    private lateinit var btnSimpan: Button

    private var editingId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.form_produk)

        db = DatabaseHelper(this)
        editingId = intent.getIntExtra("produk_id", 0)

        tvTitle = findViewById(R.id.tvTitleProduk)
        etNama = findViewById(R.id.etNama)
        etSatuan = findViewById(R.id.etSatuan)
        etStok = findViewById(R.id.etStok)
        etMin = findViewById(R.id.etMin)
        etHarga = findViewById(R.id.etHarga)
        etHasilPerMasak = findViewById(R.id.etHasilPerMasak)
        btnSimpan = findViewById(R.id.btnSimpan)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        btnSimpan.setOnClickListener { saveProduk() }

        if (editingId > 0) {
            tvTitle.text = "Ubah Produk"
            loadData(editingId)
        }
    }

    private fun loadData(produkId: Int) {
        val produk = db.getProdukById(produkId) ?: return
        etNama.setText(produk.nama)
        etSatuan.setText(produk.satuan)
        etStok.setText(produk.stokSaatIni.toString())
        etMin.setText(produk.stokMinimum.toString())
        etHarga.setText(produk.hargaJual.toString())
        etHasilPerMasak.setText(db.getParameterAktif(produkId).toString())
    }

    private fun saveProduk() {
        val nama = etNama.text.toString().trim()
        val satuan = etSatuan.text.toString().trim()
        val stok = etStok.text.toString().trim().toIntOrNull()
        val stokMinimum = etMin.text.toString().trim().toIntOrNull()
        val hargaJual = etHarga.text.toString().trim().toIntOrNull()
        val hasilPerMasak = etHasilPerMasak.text.toString().trim().toIntOrNull()

        if (nama.isEmpty() || satuan.isEmpty()) {
            Toast.makeText(this, "Nama dan satuan wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (stok == null || stok < 0 || stokMinimum == null || stokMinimum < 0) {
            Toast.makeText(this, "Stok dan stok minimum harus valid", Toast.LENGTH_SHORT).show()
            return
        }
        if (hargaJual == null || hargaJual < 0) {
            Toast.makeText(this, "Harga jual harus valid", Toast.LENGTH_SHORT).show()
            return
        }
        if (hasilPerMasak == null || hasilPerMasak <= 0) {
            Toast.makeText(this, "Hasil per masak harus lebih dari 0", Toast.LENGTH_SHORT).show()
            return
        }

        val produk = Produk(
            id = editingId,
            nama = nama,
            satuan = satuan,
            stokSaatIni = stok,
            stokMinimum = stokMinimum,
            hargaJual = hargaJual
        )

        val success = if (editingId > 0) {
            db.updateProduk(produk, hasilPerMasak)
        } else {
            db.insertProduk(produk, hasilPerMasak) != -1L
        }

        if (success) {
            Toast.makeText(this, "Produk berhasil disimpan", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Gagal menyimpan produk", Toast.LENGTH_SHORT).show()
        }
    }
}
