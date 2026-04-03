package anezza.aulia.sitahu

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class ProdukActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produk)

        db = DatabaseHelper(this)
        container = findViewById(R.id.containerProduk)

        loadData()

        findViewById<LinearLayout>(R.id.btnTambahProduk).setOnClickListener {
            startActivity(Intent(this, FormProdukActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        container.removeAllViews()

        val list = db.getAllProducts()

        for (item in list) {
            val view = layoutInflater.inflate(R.layout.item_produk, null)

            val tvNama = view.findViewById<android.widget.TextView>(R.id.tvNama)
            val tvStok = view.findViewById<android.widget.TextView>(R.id.tvStok)

            tvNama.text = item.name
            tvStok.text = "Stok: ${item.stock}"

            view.setOnLongClickListener {
                db.deleteProduct(item.id)
                loadData()
                true
            }

            container.addView(view)
        }
    }
}