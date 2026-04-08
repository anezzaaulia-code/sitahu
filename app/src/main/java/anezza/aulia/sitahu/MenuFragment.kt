package anezza.aulia.sitahu

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import anezza.aulia.sitahu.database.DatabaseHelper
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.util.Locale

class MenuFragment : Fragment(R.layout.fragment_menu) {

    private lateinit var dbHelper: DatabaseHelper

    private lateinit var cardKelolaProduk: MaterialCardView
    private lateinit var cardKelolaPengeluaran: MaterialCardView
    private lateinit var cardRiwayatUmum: MaterialCardView

    private lateinit var tvProdukHint: TextView
    private lateinit var tvPengeluaranHint: TextView
    private lateinit var tvRiwayatUmumHint: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())

        cardKelolaProduk = view.findViewById(R.id.cardKelolaProduk)
        cardKelolaPengeluaran = view.findViewById(R.id.cardKelolaPengeluaran)
        cardRiwayatUmum = view.findViewById(R.id.cardRiwayatUmum)

        tvProdukHint = view.findViewById(R.id.tvProdukHint)
        tvPengeluaranHint = view.findViewById(R.id.tvPengeluaranHint)
        tvRiwayatUmumHint = view.findViewById(R.id.tvRiwayatUmumHint)

        loadData()

        cardKelolaProduk.setOnClickListener {
            (activity as? MainHost)?.openProdukList()
        }

        cardKelolaPengeluaran.setOnClickListener {
            (activity as? MainHost)?.openPengeluaranList()
        }

        cardRiwayatUmum.setOnClickListener {
            openPage(view, RiwayatUmumFragment())
        }
    }

    override fun onResume() {
        super.onResume()
        if (::dbHelper.isInitialized) {
            loadData()
        }
    }

    private fun loadData() {
        val totalProduk = dbHelper.getJumlahProduk()
        val totalPengeluaran = dbHelper.getTotalPengeluaranNominal()
        val jumlahRiwayat = dbHelper.getRiwayatUmum().size

        val rupiah = NumberFormat.getNumberInstance(Locale("id", "ID"))

        tvProdukHint.text = "$totalProduk produk terdaftar"
        tvPengeluaranHint.text = "Total pengeluaran Rp${rupiah.format(totalPengeluaran)}"
        tvRiwayatUmumHint.text = "$jumlahRiwayat aktivitas tercatat"
    }

    private fun openPage(view: View, fragment: Fragment) {
        val containerId = (view.parent as ViewGroup).id
        parentFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }
}
