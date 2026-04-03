package anezza.aulia.sitahu

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class FormProdukActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.form_produk)

        db = DatabaseHelper(this)

        val etNama = findViewById<EditText>(R.id.etNama)
        val etKategori = findViewById<EditText>(R.id.etKategori)
        val etStok = findViewById<EditText>(R.id.etStok)
        val etMin = findViewById<EditText>(R.id.etMin)

        val btnSimpan = findViewById<Button>(R.id.btnSimpan)

        btnSimpan.setOnClickListener {

            val produk = Produk(
                nama = etNama.text.toString(),
                kategori = etKategori.text.toString(),
                stok = etStok.text.toString().toIntOrNull() ?: 0,
                minStok = etMin.text.toString().toIntOrNull() ?: 0
            )

            val result = db.insertProduk(produk)

            if (result != -1L) {
                Toast.makeText(this, "Berhasil", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}