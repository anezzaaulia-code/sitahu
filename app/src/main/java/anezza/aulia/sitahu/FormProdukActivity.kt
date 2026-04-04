package anezza.aulia.sitahu

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import anezza.aulia.sitahu.database.DatabaseHelper
import anezza.aulia.sitahu.Produk

class FormProdukActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.form_produk)

        db = DatabaseHelper(this)

        val etNama = findViewById<EditText>(R.id.etNama)
        val etSatuan = findViewById<EditText>(R.id.etSatuan)
        val etStok = findViewById<EditText>(R.id.etStok)
        val etMin = findViewById<EditText>(R.id.etMin)
        val etHarga = findViewById<EditText>(R.id.etHarga)

        val btnSimpan = findViewById<Button>(R.id.btnSimpan)

        btnSimpan.setOnClickListener {

            val produk = Produk(
                nama = etNama.text.toString(),
                satuan = etSatuan.text.toString(),
                stokSaatIni = etStok.text.toString().toIntOrNull() ?: 0,
                stokMinimum = etMin.text.toString().toIntOrNull() ?: 0,
                hargaJual = etHarga.text.toString().toIntOrNull() ?: 0
            )

            val result = db.insertProduk(produk)

            if (result != -1L) {
                Toast.makeText(this, "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}