package anezza.aulia.sitahu

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import anezza.aulia.sitahu.database.DatabaseHelper

class PengeluaranFormFragment : BaseScreenFragment(R.layout.form_pengeluaran) {

    private var db: DatabaseHelper? = null
    private var tvTitle: TextView? = null
    private var etTanggal: EditText? = null
    private var etKategori: EditText? = null
    private var etNominal: EditText? = null
    private var etCatatan: EditText? = null
    private var btnSimpan: Button? = null
    private var editingId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editingId = arguments?.getInt(ARG_PENGELUARAN_ID) ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())
        setupBack(view)

        tvTitle = view.findViewById(R.id.tvTitlePengeluaran)
        etTanggal = view.findViewById(R.id.etTanggal)
        etKategori = view.findViewById(R.id.etKategoriPengeluaran)
        etNominal = view.findViewById(R.id.etNominal)
        etCatatan = view.findViewById(R.id.etCatatan)
        btnSimpan = view.findViewById(R.id.btnSimpan)

        etTanggal?.let { DateTimePickerHelper.bind(this, it, FormatHelper.nowDisplay()) }
        btnSimpan?.setOnClickListener { savePengeluaran() }

        if (editingId > 0) {
            tvTitle?.text = "Ubah Pengeluaran"
        }
    }

    override fun refreshContent() {
        if (editingId > 0) {
            loadData(editingId)
        } else if (etTanggal?.text.isNullOrBlank()) {
            etTanggal?.setText(FormatHelper.nowDisplay())
        }
    }

    private fun loadData(id: Int) {
        val data = db?.getPengeluaranById(id) ?: return
        etTanggal?.setText(FormatHelper.toDisplayDateTime(data.tanggalPengeluaran))
        etKategori?.setText(data.namaPengeluaran)
        etNominal?.setText(data.nominal.toString())
        etCatatan?.setText(data.catatan)
    }

    private fun savePengeluaran() {
        val helper = db ?: return
        val tanggalInput = etTanggal?.text?.toString()?.trim().orEmpty()
        val namaPengeluaran = etKategori?.text?.toString()?.trim().orEmpty()
        val nominal = etNominal?.text?.toString()?.trim()?.toIntOrNull()
        val catatan = etCatatan?.text?.toString()?.trim().orEmpty()

        if (tanggalInput.isEmpty() || namaPengeluaran.isEmpty()) {
            Toast.makeText(requireContext(), "Tanggal, jam, dan nama pengeluaran wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (nominal == null || nominal <= 0) {
            Toast.makeText(requireContext(), "Nominal harus lebih dari 0", Toast.LENGTH_SHORT).show()
            return
        }

        val item = Pengeluaran(
            id = editingId,
            tanggalPengeluaran = FormatHelper.normalizeDateTime(tanggalInput),
            namaPengeluaran = namaPengeluaran,
            nominal = nominal,
            catatan = catatan
        )

        val success = if (editingId > 0) helper.updatePengeluaran(item) else helper.insertPengeluaran(item) != -1L
        if (success) {
            Toast.makeText(requireContext(), "Pengeluaran berhasil disimpan", Toast.LENGTH_SHORT).show()
            parentFragmentManager.setFragmentResult(PengeluaranFragment.RESULT_KEY, Bundle())
            parentFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "Gagal menyimpan pengeluaran", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val ARG_PENGELUARAN_ID = "pengeluaran_id"

        fun newInstance(pengeluaranId: Int?): PengeluaranFormFragment {
            return PengeluaranFormFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PENGELUARAN_ID, pengeluaranId ?: 0)
                }
            }
        }
    }
}
