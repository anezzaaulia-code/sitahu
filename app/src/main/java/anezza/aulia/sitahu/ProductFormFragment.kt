package anezza.aulia.sitahu

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import anezza.aulia.sitahu.database.DatabaseHelper

class ProductFormFragment : BaseScreenFragment(R.layout.form_produk) {

    private var db: DatabaseHelper? = null
    private var tvTitle: TextView? = null
    private var etNama: EditText? = null
    private var etSatuan: EditText? = null
    private var etStok: EditText? = null
    private var etMin: EditText? = null
    private var etHarga: EditText? = null
    private var etHasilPerMasak: EditText? = null
    private var rbAktif: RadioButton? = null
    private var rbNonaktif: RadioButton? = null
    private var btnSimpan: Button? = null
    private var editingId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editingId = arguments?.getInt(ARG_PRODUK_ID) ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())
        setupBack(view)

        tvTitle = view.findViewById(R.id.tvTitleProduk)
        etNama = view.findViewById(R.id.etNama)
        etSatuan = view.findViewById(R.id.etSatuan)
        etStok = view.findViewById(R.id.etStok)
        etMin = view.findViewById(R.id.etMin)
        etHarga = view.findViewById(R.id.etHarga)
        etHasilPerMasak = view.findViewById(R.id.etHasilPerMasak)
        rbAktif = view.findViewById(R.id.rbProdukAktif)
        rbNonaktif = view.findViewById(R.id.rbProdukNonaktif)
        btnSimpan = view.findViewById(R.id.btnSimpan)
        btnSimpan?.setOnClickListener { saveProduk() }
        if (editingId > 0) {
            tvTitle?.text = "Ubah Produk"
        } else {
            rbAktif?.isChecked = true
        }
    }

    override fun refreshContent() {
        if (editingId > 0) loadData(editingId)
    }

    private fun loadData(produkId: Int) {
        val helper = db ?: return
        val produk = helper.getProdukById(produkId) ?: return
        etNama?.setText(produk.nama)
        etSatuan?.setText(produk.satuan)
        etStok?.setText(produk.stokSaatIni.toString())
        etMin?.setText(produk.stokMinimum.toString())
        etHarga?.setText(produk.hargaJual.toString())
        etHasilPerMasak?.setText(helper.getParameterAktif(produkId).toString())
        rbAktif?.isChecked = produk.aktif
        rbNonaktif?.isChecked = !produk.aktif
    }

    private fun saveProduk() {
        val helper = db ?: return
        val nama = etNama?.text?.toString()?.trim().orEmpty()
        val satuan = etSatuan?.text?.toString()?.trim().orEmpty()
        val stok = etStok?.text?.toString()?.trim()?.toIntOrNull()
        val stokMinimum = etMin?.text?.toString()?.trim()?.toIntOrNull()
        val hargaJual = etHarga?.text?.toString()?.trim()?.toIntOrNull()
        val hasilPerMasak = etHasilPerMasak?.text?.toString()?.trim()?.toIntOrNull()
        val aktif = rbAktif?.isChecked == true

        if (nama.isEmpty() || satuan.isEmpty()) {
            Toast.makeText(requireContext(), "Nama dan satuan wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (stok == null || stokMinimum == null || hargaJual == null || hasilPerMasak == null) {
            Toast.makeText(requireContext(), "Semua angka wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (stok < 0 || stokMinimum < 0 || hargaJual < 0 || hasilPerMasak <= 0) {
            Toast.makeText(requireContext(), "Nilai angka tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val produk = Produk(
            id = editingId,
            nama = nama,
            satuan = satuan,
            stokSaatIni = stok,
            stokMinimum = stokMinimum,
            hargaJual = hargaJual,
            aktif = aktif
        )

        val success = if (editingId > 0) {
            helper.updateProduk(produk, hasilPerMasak)
        } else {
            helper.insertProduk(produk, hasilPerMasak) != -1L
        }

        if (success) {
            Toast.makeText(requireContext(), "Produk berhasil disimpan", Toast.LENGTH_SHORT).show()
            parentFragmentManager.setFragmentResult(ProdukFragment.RESULT_KEY, Bundle())
            parentFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "Gagal menyimpan produk", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val ARG_PRODUK_ID = "produk_id"

        fun newInstance(produkId: Int?): ProductFormFragment {
            return ProductFormFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PRODUK_ID, produkId ?: 0)
                }
            }
        }
    }
}
