package anezza.aulia.sitahu

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import anezza.aulia.sitahu.database.DatabaseHelper

class FormProdukFragment : Fragment(R.layout.fragment_form_produk) {

    private lateinit var dbHelper: DatabaseHelper
    private var produkId: Int = 0

    companion object {
        private const val ARG_ID = "arg_id"

        fun newInstance(id: Int): FormProdukFragment {
            val fragment = FormProdukFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_ID, id)
            }
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        produkId = arguments?.getInt(ARG_ID, 0) ?: 0

        val etNama = view.findViewById<EditText>(R.id.etNamaProduk)
        val etSatuan = view.findViewById<EditText>(R.id.etSatuanProduk)
        val etStok = view.findViewById<EditText>(R.id.etStokProduk)
        val etMinimum = view.findViewById<EditText>(R.id.etMinimumProduk)
        val etHarga = view.findViewById<EditText>(R.id.etHargaProduk)
        val etHasilPerMasak = view.findViewById<EditText>(R.id.etHasilPerMasak)
        val btnSimpan = view.findViewById<Button>(R.id.btnSimpanProduk)

        if (produkId != 0) {
            val produk = dbHelper.getProdukById(produkId)
            if (produk != null) {
                etNama.setText(produk.nama)
                etSatuan.setText(produk.satuan)
                etStok.setText(produk.stokSaatIni.toString())
                etMinimum.setText(produk.stokMinimum.toString())
                etHarga.setText(produk.hargaJual.toString())
                etHasilPerMasak.setText(dbHelper.getParameterAktif(produkId).toString())
            }
        }

        btnSimpan.setOnClickListener {
            val produk = Produk(
                id = produkId,
                nama = etNama.text.toString().trim(),
                satuan = etSatuan.text.toString().trim(),
                stokSaatIni = etStok.text.toString().toIntOrNull() ?: 0,
                stokMinimum = etMinimum.text.toString().toIntOrNull() ?: 0,
                hargaJual = etHarga.text.toString().toIntOrNull() ?: 0,
                dibuatPada = "",
                diubahPada = ""
            )

            val hasilPerMasak = etHasilPerMasak.text.toString().toIntOrNull() ?: 1

            val success = if (produkId == 0) {
                dbHelper.insertProduk(produk, hasilPerMasak) != -1L
            } else {
                dbHelper.updateProduk(produk, hasilPerMasak)
            }

            if (success) {
                parentFragmentManager.popBackStack()
            }
        }
    }
}