package anezza.aulia.sitahu

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import anezza.aulia.sitahu.database.DatabaseHelper

class FormPengeluaranActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    private lateinit var tvTitle: TextView
    private lateinit var etTanggal: EditText
    private lateinit var etNamaPengeluaran: EditText
    private lateinit var etNominal: EditText
    private lateinit var etCatatan: EditText
    private lateinit var btnSimpan: Button

    private var editingId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.form_pengeluaran)

        db = DatabaseHelper(this)
        editingId = intent.getIntExtra("pengeluaran_id", 0)

        tvTitle = findViewById(R.id.tvTitlePengeluaran)
        etTanggal = findViewById(R.id.etTanggal)
        etNamaPengeluaran = findViewById(R.id.etKategoriPengeluaran)
        etNominal = findViewById(R.id.etNominal)
        etCatatan = findViewById(R.id.etCatatan)
        btnSimpan = findViewById(R.id.btnSimpan)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        btnSimpan.setOnClickListener { savePengeluaran() }

        if (editingId > 0) {
            tvTitle.text = "Ubah Pengeluaran"
            loadData(editingId)
        } else {
            etTanggal.setText(FormatHelper.today())
        }
    }

    private fun loadData(id: Int) {
        val data = db.getPengeluaranById(id) ?: return
        etTanggal.setText(data.tanggalPengeluaran)
        etNamaPengeluaran.setText(data.namaPengeluaran)
        etNominal.setText(data.nominal.toString())
        etCatatan.setText(data.catatan)
    }

    private fun savePengeluaran() {
        val tanggal = etTanggal.text.toString().trim()
        val namaPengeluaran = etNamaPengeluaran.text.toString().trim()
        val nominal = etNominal.text.toString().trim().toIntOrNull()
        val catatan = etCatatan.text.toString().trim()

        if (tanggal.isEmpty() || namaPengeluaran.isEmpty()) {
            Toast.makeText(this, "Tanggal dan nama pengeluaran wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (nominal == null || nominal <= 0) {
            Toast.makeText(this, "Nominal harus lebih dari 0", Toast.LENGTH_SHORT).show()
            return
        }

        val item = Pengeluaran(
            id = editingId,
            tanggalPengeluaran = tanggal,
            namaPengeluaran = namaPengeluaran,
            nominal = nominal,
            catatan = catatan
        )

        val success = if (editingId > 0) {
            db.updatePengeluaran(item)
        } else {
            db.insertPengeluaran(item) != -1L
        }

        if (success) {
            Toast.makeText(this, "Pengeluaran berhasil disimpan", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Gagal menyimpan pengeluaran", Toast.LENGTH_SHORT).show()
        }
    }
}