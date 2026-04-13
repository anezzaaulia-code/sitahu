package anezza.aulia.sitahu

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import anezza.aulia.sitahu.database.DatabaseHelper

class ProduksiActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    private lateinit var etTanggal: EditText
    private lateinit var spProduk: Spinner
    private lateinit var etJumlahMasak: EditText
    private lateinit var etCatatan: EditText
    private lateinit var tvHasilInfo: TextView
    private lateinit var btnSimpan: Button

    private var listProduk: List<Produk> = emptyList()
    private var selectedProduk: Produk? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produksi)

        db = DatabaseHelper(this)

        etTanggal = findViewById(R.id.etTanggal)
        spProduk = findViewById(R.id.spProduk)
        etJumlahMasak = findViewById(R.id.etJumlahMasak)
        etCatatan = findViewById(R.id.etCatatan)
        tvHasilInfo = findViewById(R.id.tvHasilInfo)
        btnSimpan = findViewById(R.id.btnSimpan)

        etTanggal.setText(FormatHelper.today())
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        btnSimpan.setOnClickListener { simpanProduksi() }

        AppNavigator.setupBottomNav(this, AppNavigator.Tab.PRODUKSI)
    }

    override fun onResume() {
        super.onResume()
        loadProduk()
    }

    private fun loadProduk() {
        listProduk = db.getActiveProduk()
        if (listProduk.isEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Belum ada produk"))
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spProduk.adapter = adapter
            selectedProduk = null
            btnSimpan.isEnabled = false
            tvHasilInfo.text = "Tambahkan produk di menu pengaturan dulu."
            return
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listProduk.map { it.nama })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spProduk.adapter = adapter
        btnSimpan.isEnabled = true
        spProduk.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedProduk = listProduk[position]
                updateHasilInfo()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedProduk = null
                tvHasilInfo.text = "Pilih produk untuk melihat hasil produksi."
            }
        }
        if (listProduk.isNotEmpty()) {
            selectedProduk = listProduk.first()
            updateHasilInfo()
        }
    }

    private fun updateHasilInfo() {
        val produk = selectedProduk ?: return
        val hasil = db.getParameterAktif(produk.id)
        tvHasilInfo.text = "1 kali masak menghasilkan $hasil ${produk.satuan}."
    }

    private fun simpanProduksi() {
        val produk = selectedProduk
        val jumlahMasak = etJumlahMasak.text.toString().trim().toIntOrNull()
        val tanggal = etTanggal.text.toString().trim()
        val catatan = etCatatan.text.toString().trim()

        if (produk == null) {
            Toast.makeText(this, "Pilih produk dulu", Toast.LENGTH_SHORT).show()
            return
        }
        if (tanggal.isEmpty()) {
            Toast.makeText(this, "Tanggal produksi wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (jumlahMasak == null || jumlahMasak <= 0) {
            Toast.makeText(this, "Jumlah masak harus lebih dari 0", Toast.LENGTH_SHORT).show()
            return
        }

        val saved = db.insertProduksi(produk.id, jumlahMasak, tanggal, catatan)
        if (saved) {
            val hasil = jumlahMasak * db.getParameterAktif(produk.id)
            Toast.makeText(this, "Produksi tersimpan: $hasil ${produk.satuan}", Toast.LENGTH_SHORT).show()
            etJumlahMasak.setText("")
            etCatatan.setText("")
        } else {
            Toast.makeText(this, "Gagal menyimpan produksi", Toast.LENGTH_SHORT).show()
        }
    }
}
