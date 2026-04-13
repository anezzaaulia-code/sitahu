package anezza.aulia.sitahu

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import anezza.aulia.sitahu.database.DatabaseHelper

class FormPengeluaranFragment : Fragment(R.layout.fragment_form_pengeluaran) {

    private lateinit var dbHelper: DatabaseHelper
    private var pengeluaranId: Int = 0

    companion object {
        private const val ARG_ID = "arg_id"

        fun newInstance(id: Int): FormPengeluaranFragment {
            val fragment = FormPengeluaranFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_ID, id)
            }
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        pengeluaranId = arguments?.getInt(ARG_ID, 0) ?: 0

        val etTanggal = view.findViewById<EditText>(R.id.etTanggalPengeluaran)
        val etNamaPengeluaran = view.findViewById<EditText>(R.id.etKategoriPengeluaran)
        val etNominal = view.findViewById<EditText>(R.id.etNominalPengeluaran)
        val etCatatan = view.findViewById<EditText>(R.id.etCatatanPengeluaran)
        val btnSimpan = view.findViewById<Button>(R.id.btnSimpanPengeluaran)

        if (pengeluaranId != 0) {
            val item = dbHelper.getPengeluaranById(pengeluaranId)
            if (item != null) {
                etTanggal.setText(item.tanggalPengeluaran)
                etNamaPengeluaran.setText(item.namaPengeluaran)
                etNominal.setText(item.nominal.toString())
                etCatatan.setText(item.catatan)
            }
        }

        btnSimpan.setOnClickListener {
            val item = Pengeluaran(
                id = pengeluaranId,
                tanggalPengeluaran = etTanggal.text.toString().trim(),
                namaPengeluaran = etNamaPengeluaran.text.toString().trim(),
                nominal = etNominal.text.toString().toIntOrNull() ?: 0,
                catatan = etCatatan.text.toString().trim()
            )

            val success = if (pengeluaranId == 0) {
                dbHelper.insertPengeluaran(item) != -1L
            } else {
                dbHelper.updatePengeluaran(item)
            }

            if (success) {
                parentFragmentManager.popBackStack()
            }
        }
    }
}