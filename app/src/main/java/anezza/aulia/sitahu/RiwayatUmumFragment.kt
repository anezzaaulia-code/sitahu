package anezza.aulia.sitahu

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import anezza.aulia.sitahu.database.DatabaseHelper

class RiwayatUmumFragment : Fragment(R.layout.fragment_riwayat_umum) {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvRiwayat: RecyclerView
    private lateinit var tvRingkasan: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())

        rvRiwayat = view.findViewById(R.id.rvRiwayatUmum)
        tvRingkasan = view.findViewById(R.id.tvRingkasanRiwayat)

        rvRiwayat.layoutManager = LinearLayoutManager(requireContext())

        loadData()

        view.findViewById<View>(R.id.btnBackRiwayatUmum).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::dbHelper.isInitialized) {
            loadData()
        }
    }

    private fun loadData() {
        val items = dbHelper.getRiwayatUmum()
        rvRiwayat.adapter = RiwayatUmumAdapter(items)
        tvRingkasan.text = "${items.size} aktivitas tercatat"
    }
}