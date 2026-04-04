package anezza.aulia.sitahu

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import anezza.aulia.sitahu.database.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

class FormPengeluaranActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    private lateinit var tvTanggal: TextView
    private lateinit var etKategori: EditText
    private lateinit var etNominal: EditText
    private lateinit var etCatatan: EditText
    private lateinit var btnSimpan: Button
    private lateinit var btnKembali: Button
    private lateinit var btnBack: CardView

    private var produkId: Int = -1
    private var stokSekarang: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.form_pengeluaran)

        db = DatabaseHelper(this)

        // bind view
        tvTanggal = findViewById(R.id.tvTanggal)
        etKategori = findViewById(R.id.etKategoriPengeluaran)
        etNominal = findViewById(R.id.etNominal)
        etCatatan = findViewById(R.id.etCatatan)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnKembali = findViewById(R.id.btnKembali)
        btnBack = findViewById(R.id.btn_back)

        // ambil data dari intent
        produkId = intent.getIntExtra("id", -1)

        val produk = db.getProdukById(produkId)
        produk?.let {
            stokSekarang = it.stok
            etKategori.setText(it.nama)
        }

        setTanggalHariIni()

        btnBack.setOnClickListener { finish() }
        btnKembali.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            simpanPengeluaran()
        }
    }

    private fun setTanggalHariIni() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val tanggal = sdf.format(Date())
        tvTanggal.text = tanggal
    }

    private fun simpanPengeluaran() {
        val nominalStr = etNominal.text.toString()

        if (nominalStr.isEmpty()) {
            Toast.makeText(this, "Nominal tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val jumlahKeluar = nominalStr.toInt()

        if (jumlahKeluar <= 0) {
            Toast.makeText(this, "Nominal harus lebih dari 0", Toast.LENGTH_SHORT).show()
            return
        }

        if (jumlahKeluar > stokSekarang) {
            Toast.makeText(this, "Stok tidak cukup!", Toast.LENGTH_SHORT).show()
            return
        }

        val stokBaru = stokSekarang - jumlahKeluar

        db.updateStok(produkId, stokBaru)

        val tanggal = tvTanggal.text.toString()

        db.insertLog(produkId, "keluar", jumlahKeluar, tanggal)

        Toast.makeText(this, "Pengeluaran berhasil", Toast.LENGTH_SHORT).show()

        finish()
    }
}