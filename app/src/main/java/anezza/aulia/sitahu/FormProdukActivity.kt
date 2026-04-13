package anezza.aulia.sitahu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class FormProdukActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, ProductFormFragment.newInstance(intent.getIntExtra("produk_id", 0)))
                .commitNow()
        }
    }
}
