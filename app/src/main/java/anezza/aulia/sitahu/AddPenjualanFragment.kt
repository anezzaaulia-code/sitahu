package anezza.aulia.sitahu

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import anezza.aulia.sitahu.database.DatabaseHelper

class AddPenjualanFragment : BaseScreenFragment(R.layout.fragment_tambah_penjualan) {

    private var db: DatabaseHelper? = null
    private var etTanggal: EditText? = null
    private var spProduk: Spinner? = null
    private var etJumlah: EditText? = null
    private var etCatatan: EditText? = null
    private var tvHarga: TextView? = null
    private var btnTambah: Button? = null
    private var btnSimpan: Button? = null
    private var rvItem: RecyclerView? = null
    private var emptyState: LinearLayout? = null
    private var tvTotalSemuaItem: TextView? = null

    private val listItem = mutableListOf<ItemRekap>()
    private var listProduk: List<Produk> = emptyList()
    private var selectedProduk: Produk? = null
    private var adapter: ItemRekapAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())
        setupBack(view)

        etTanggal = view.findViewById(R.id.etTanggalPenjualan)
        spProduk = view.findViewById(R.id.spProduk)
        etJumlah = view.findViewById(R.id.etJumlah)
        etCatatan = view.findViewById(R.id.etCatatanPenjualan)
        tvHarga = view.findViewById(R.id.tvHargaKanal)
        btnTambah = view.findViewById(R.id.btnTambahItemRekap)
        btnSimpan = view.findViewById(R.id.btnSimpanRekap)
        rvItem = view.findViewById(R.id.rvItemRekap)
        emptyState = view.findViewById(R.id.layoutEmptyItem)
        tvTotalSemuaItem = view.findViewById(R.id.tvTotalSemuaItemPenjualan)

        etTanggal?.let { DateTimePickerHelper.bind(this, it, FormatHelper.nowDisplay()) }

        adapter = ItemRekapAdapter(listItem) { position -> showEditDialog(position) }
        rvItem?.layoutManager = LinearLayoutManager(requireContext())
        rvItem?.adapter = adapter

        btnTambah?.setOnClickListener { tambahItem() }
        btnSimpan?.setOnClickListener { simpanRekap() }
    }

    override fun refreshContent() {
        if (etTanggal?.text.isNullOrBlank()) {
            etTanggal?.setText(FormatHelper.nowDisplay())
        }
        loadProduk()
        updateUi()
    }

    private fun loadProduk() {
        val helper = db ?: return
        val spinner = spProduk ?: return

        listProduk = helper.getActiveProduk()
        if (listProduk.isEmpty()) {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                listOf("Belum ada produk")
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            selectedProduk = null
            btnTambah?.isEnabled = false
            btnSimpan?.isEnabled = false
            tvHarga?.text = FormatHelper.rupiah(0)
            return
        }

        val adapterSpinner = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listProduk.map { it.nama }
        )
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterSpinner
        btnTambah?.isEnabled = true
        btnSimpan?.isEnabled = true

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedProduk = listProduk[position]
                tvHarga?.text = FormatHelper.rupiah(selectedProduk?.hargaJual ?: 0)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedProduk = null
                tvHarga?.text = FormatHelper.rupiah(0)
            }
        }

        selectedProduk = listProduk.firstOrNull()
        tvHarga?.text = FormatHelper.rupiah(selectedProduk?.hargaJual ?: 0)
    }

    private fun tambahItem() {
        val produk = selectedProduk
        val jumlah = etJumlah?.text?.toString()?.trim()?.toIntOrNull()

        if (produk == null) {
            Toast.makeText(requireContext(), "Pilih produk dulu", Toast.LENGTH_SHORT).show()
            return
        }
        if (jumlah == null || jumlah <= 0) {
            Toast.makeText(requireContext(), "Jumlah harus lebih dari 0", Toast.LENGTH_SHORT).show()
            return
        }

        val totalProdukDiRekap = getTotalJumlahUntukProduk(produk.id)
        if (totalProdukDiRekap + jumlah > produk.stokSaatIni) {
            Toast.makeText(requireContext(), "Stok ${produk.nama} tidak cukup", Toast.LENGTH_SHORT).show()
            return
        }

        listItem.add(ItemRekap(produk.id, produk.nama, jumlah, jumlah * produk.hargaJual))
        etJumlah?.setText("")
        updateUi()
    }

    private fun getTotalJumlahUntukProduk(produkId: Int, excludedPosition: Int? = null): Int {
        return listItem.mapIndexedNotNull { index, item ->
            if (excludedPosition != null && index == excludedPosition) {
                null
            } else if (item.produkId == produkId) {
                item.jumlah
            } else {
                null
            }
        }.sum()
    }

    private fun showEditDialog(position: Int) {
        val item = listItem[position]
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(item.jumlah.toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Ubah jumlah ${item.namaProduk}")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val newJumlah = input.text.toString().trim().toIntOrNull()
                if (newJumlah == null || newJumlah <= 0) {
                    Toast.makeText(requireContext(), "Jumlah tidak valid", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val stokTersedia = listProduk.firstOrNull { it.id == item.produkId }?.stokSaatIni ?: 0
                val totalLain = getTotalJumlahUntukProduk(item.produkId, position)
                if (newJumlah + totalLain > stokTersedia) {
                    Toast.makeText(requireContext(), "Stok tidak cukup", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val harga = listProduk.firstOrNull { it.id == item.produkId }?.hargaJual ?: 0
                listItem[position] = item.copy(jumlah = newJumlah, subtotal = newJumlah * harga)
                updateUi()
            }
            .setNegativeButton("Hapus") { _, _ ->
                listItem.removeAt(position)
                updateUi()
            }
            .setNeutralButton("Batal", null)
            .show()
    }

    private fun simpanRekap() {
        val helper = db ?: return

        if (listItem.isEmpty()) {
            Toast.makeText(requireContext(), "Tambahkan item dulu", Toast.LENGTH_SHORT).show()
            return
        }

        val tanggalInput = etTanggal?.text?.toString()?.trim().orEmpty()
        if (tanggalInput.isEmpty()) {
            Toast.makeText(requireContext(), "Tanggal dan jam penjualan wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val catatan = etCatatan?.text?.toString()?.trim().orEmpty()
        val total = listItem.sumOf { it.subtotal }
        val payload = listItem.map { Triple(it.produkId, it.jumlah, it.subtotal) }

        val saved = helper.insertPenjualan(
            payload,
            total,
            FormatHelper.normalizeDateTime(tanggalInput),
            if (catatan.isBlank()) "Penjualan harian" else catatan
        )

        if (saved) {
            Toast.makeText(requireContext(), "Penjualan berhasil disimpan", Toast.LENGTH_SHORT).show()
            parentFragmentManager.setFragmentResult(PenjualanFragment.RESULT_KEY, Bundle())
            parentFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "Gagal menyimpan penjualan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUi() {
        adapter?.notifyDataSetChanged()
        val isEmpty = listItem.isEmpty()
        val totalSemuaItem = listItem.sumOf { it.subtotal }

        emptyState?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvItem?.visibility = if (isEmpty) View.GONE else View.VISIBLE
        tvTotalSemuaItem?.text = "Total semua item: ${FormatHelper.rupiah(totalSemuaItem)}"
        tvTotalSemuaItem?.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
