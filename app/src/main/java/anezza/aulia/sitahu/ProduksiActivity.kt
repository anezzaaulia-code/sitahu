package anezza.aulia.sitahu

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import anezza.aulia.sitahu.database.DatabaseHelper

class ProduksiActivity : AppCompatActivity() {

    lateinit var db: DatabaseHelper

    lateinit var etTanggal: EditText
    lateinit var spProduk: Spinner
    lateinit var etJumlah: EditText
    lateinit var etCatatan: EditText
    lateinit var btnSimpan: Button

    lateinit var listProduk: List<Produk>
    var selectedProdukId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produksi)

        db = DatabaseHelper(this)

        initView()
        loadProduk()
        setupAction()
    }

    // =========================
    // INIT VIEW
    // =========================
    private fun initView() {
        etTanggal = findViewById(R.id.etTanggal)
        spProduk = findViewById(R.id.spProduk)
        etJumlah = findViewById(R.id.etJumlahMasak)
        etCatatan = findViewById(R.id.etCatatan)
        btnSimpan = findViewById(R.id.btnSimpan)
    }

    // =========================
    // LOAD DATA PRODUK KE SPINNER
    // =========================
    private fun loadProduk() {
        listProduk = db.getAllProduk()

        val namaList = listProduk.map { it.nama }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            namaList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spProduk.adapter = adapter

        spProduk.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val produk = listProduk[position]
                selectedProdukId = produk.id

                // sementara tampilkan parameter via toast
                val hasil = db.getParameterAktif(selectedProdukId)
                Toast.makeText(
                    this@ProduksiActivity,
                    "$hasil pcs / masak",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // =========================
    // BUTTON ACTION
    // =========================
    private fun setupAction() {
        btnSimpan.setOnClickListener {
            simpanProduksi()
        }
    }

    // =========================
    // SIMPAN PRODUKSI
    // =========================
    private fun simpanProduksi() {

        val jumlahText = etJumlah.text.toString()

        if (jumlahText.isEmpty()) {
            Toast.makeText(this, "Jumlah masak harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedProdukId == 0) {
            Toast.makeText(this, "Pilih produk dulu", Toast.LENGTH_SHORT).show()
            return
        }

        val jumlahMasak = jumlahText.toInt()

        val hasilPerMasak = db.getParameterAktif(selectedProdukId)
        val jumlahHasil = jumlahMasak * hasilPerMasak

        val sukses = db.insertProduksi(
            selectedProdukId,
            jumlahMasak,
            etCatatan.text.toString()
        )

        if (sukses) {
            Toast.makeText(
                this,
                "Produksi berhasil ($jumlahHasil pcs)",
                Toast.LENGTH_LONG
            ).show()

            clearForm()
        } else {
            Toast.makeText(this, "Gagal menyimpan", Toast.LENGTH_SHORT).show()
        }
    }

    // =========================
    // CLEAR FORM
    // =========================
    private fun clearForm() {
        etJumlah.setText("")
        etCatatan.setText("")
    }
}