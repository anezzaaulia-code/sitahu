package anezza.aulia.sitahu

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import anezza.aulia.sitahu.database.DatabaseHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.util.Locale

class MenuFragment : Fragment(R.layout.fragment_menu) {

    private lateinit var dbHelper: DatabaseHelper

    private lateinit var menuToolbar: MaterialToolbar
    private lateinit var cardKelolaProduk: MaterialCardView
    private lateinit var cardKelolaPengeluaran: MaterialCardView
    private lateinit var cardRiwayatUmum: MaterialCardView

    private lateinit var tvProdukHint: TextView
    private lateinit var tvPengeluaranHint: TextView
    private lateinit var tvRiwayatUmumHint: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())

        menuToolbar = view.findViewById(R.id.menuToolbar)
        cardKelolaProduk = view.findViewById(R.id.cardKelolaProduk)
        cardKelolaPengeluaran = view.findViewById(R.id.cardKelolaPengeluaran)
        cardRiwayatUmum = view.findViewById(R.id.cardRiwayatUmum)

        tvProdukHint = view.findViewById(R.id.tvProdukHint)
        tvPengeluaranHint = view.findViewById(R.id.tvPengeluaranHint)
        tvRiwayatUmumHint = view.findViewById(R.id.tvRiwayatUmumHint)

        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(menuToolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            invalidateOptionsMenu()
        }

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_top_logout, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::dbHelper.isInitialized) {
            loadData()
        }
        requireActivity().invalidateOptionsMenu()
    }

    override fun onDestroyView() {
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(null)
            invalidateOptionsMenu()
        }
        super.onDestroyView()
    }

    private fun loadData() {
        val totalProduk = dbHelper.getJumlahProduk()
        val totalPengeluaran = dbHelper.getTotalPengeluaranNominal()
        val jumlahRiwayat = dbHelper.getRiwayatUmum().size

        val rupiah = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))

        tvProdukHint.text = "$totalProduk produk terdaftar"
        tvPengeluaranHint.text = "Total pengeluaran Rp${rupiah.format(totalPengeluaran)}"
        tvRiwayatUmumHint.text = "$jumlahRiwayat aktivitas tercatat"
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Keluar dari aplikasi sekarang?")
            .setPositiveButton("Logout") { _, _ ->
                activity?.finishAffinity()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun openPage(view: View, fragment: Fragment) {
        val containerId = (view.parent as ViewGroup).id
        parentFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }
}
