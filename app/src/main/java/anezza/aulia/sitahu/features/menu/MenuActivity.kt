package anezza.aulia.sitahu

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import anezza.aulia.sitahu.database.DatabaseHelper

class MenuActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var tvProdukHint: TextView
    private lateinit var tvPengeluaranHint: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_admin)

        db = DatabaseHelper(this)
        tvProdukHint = findViewById(R.id.tvProdukHint)
        tvPengeluaranHint = findViewById(R.id.tvPengeluaranHint)

        findViewById<MaterialCardView>(R.id.cardKelolaProduk).setOnClickListener {
            startActivity(Intent(this, ProdukActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardKelolaPengeluaran).setOnClickListener {
            startActivity(Intent(this, PengeluaranActivity::class.java))
        }

        AppNavigator.setupBottomNav(this, AppNavigator.Tab.MENU)
    }

    override fun onResume() {
        super.onResume()
        tvProdukHint.text = "${db.getTotalProduk()} produk terdaftar"
        tvPengeluaranHint.text = "Total pengeluaran ${FormatHelper.rupiah(db.getTotalPengeluaran())}"
    }
}
