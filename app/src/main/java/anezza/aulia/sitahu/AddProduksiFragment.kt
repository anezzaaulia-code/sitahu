package anezza.aulia.sitahu

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import anezza.aulia.sitahu.database.DatabaseHelper

class AddProduksiFragment : BaseScreenFragment(R.layout.fragment_tambah_produksi) {

    private var db: DatabaseHelper? = null
    private var etTanggal: EditText? = null
    private var spProduk: Spinner? = null
    private var etJumlahMasak: EditText? = null
    private var etCatatan: EditText? = null
    private var tvHasilInfo: TextView? = null
    private var btnSimpan: Button? = null

    private var listProduk: List<Produk> = emptyList()
    private var selectedProduk: Produk? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())
        setupBack(view)

        etTanggal = view.findViewById(R.id.etTanggal)
        spProduk = view.findViewById(R.id.spProduk)
        etJumlahMasak = view.findViewById(R.id.etJumlahMasak)
        etCatatan = view.findViewById(R.id.etCatatan)
        tvHasilInfo = view.findViewById(R.id.tvHasilInfo)
        btnSimpan = view.findViewById(R.id.btnSimpan)

        etTanggal?.let { DateTimePickerHelper.bind(this, it, FormatHelper.nowDisplay()) }
        btnSimpan?.setOnClickListener { simpanProduksi() }
    }

    override fun refreshContent() {
        if (etTanggal?.text.isNullOrBlank()) {
            etTanggal?.setText(FormatHelper.nowDisplay())
        }
        loadProduk()
    }

    private fun loadProduk() {
        val helper = db ?: return
        val spinner = spProduk ?: return

        listProduk = helper.getAllProduk()
        if (listProduk.isEmpty()) {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                listOf("Belum ada produk")
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            selectedProduk = null
            btnSimpan?.isEnabled = false
            tvHasilInfo?.text = "Tambahkan produk di menu pengaturan dulu."
            return
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listProduk.map { it.nama }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        btnSimpan?.isEnabled = true

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedProduk = listProduk[position]
                updateHasilInfo()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedProduk = null
                tvHasilInfo?.text = "Pilih produk untuk melihat hasil produksi."
            }
        }

        selectedProduk = listProduk.firstOrNull()
        updateHasilInfo()
    }

    private fun updateHasilInfo() {
        val helper = db ?: return
        val produk = selectedProduk ?: return
        val hasil = helper.getParameterAktif(produk.id)
        tvHasilInfo?.text = "1 kali masak menghasilkan $hasil ${produk.satuan}."
    }

    private fun simpanProduksi() {
        val helper = db ?: return
        val produk = selectedProduk
        val jumlahMasak = etJumlahMasak?.text?.toString()?.trim()?.toIntOrNull()
        val tanggalInput = etTanggal?.text?.toString()?.trim().orEmpty()
        val catatan = etCatatan?.text?.toString()?.trim().orEmpty()

        if (produk == null) {
            Toast.makeText(requireContext(), "Pilih produk dulu", Toast.LENGTH_SHORT).show()
            return
        }
        if (tanggalInput.isEmpty()) {
            Toast.makeText(requireContext(), "Tanggal dan jam produksi wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (jumlahMasak == null || jumlahMasak <= 0) {
            Toast.makeText(requireContext(), "Jumlah masak harus lebih dari 0", Toast.LENGTH_SHORT).show()
            return
        }

        val saved = helper.insertProduksi(produk.id, jumlahMasak, FormatHelper.normalizeDateTime(tanggalInput), catatan)
        if (saved) {
            val hasil = jumlahMasak * helper.getParameterAktif(produk.id)
            Toast.makeText(
                requireContext(),
                "Produksi tersimpan: $hasil ${produk.satuan}",
                Toast.LENGTH_SHORT
            ).show()
            parentFragmentManager.setFragmentResult(ProduksiFragment.RESULT_KEY, Bundle())
            parentFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "Gagal menyimpan produksi", Toast.LENGTH_SHORT).show()
        }
    }
}
