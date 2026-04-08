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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import anezza.aulia.sitahu.database.DatabaseHelper

class PenjualanActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    private lateinit var tvTanggal: TextView
    private lateinit var spProduk: Spinner
    private lateinit var etJumlah: EditText
    private lateinit var etCatatan: EditText
    private lateinit var tvHarga: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvJumlahItem: TextView
    private lateinit var btnTambah: Button
    private lateinit var btnSimpan: Button
    private lateinit var rvItem: RecyclerView
    private lateinit var emptyState: LinearLayout

    private val listItem = mutableListOf<ItemRekap>()
    private var listProduk: List<Produk> = emptyList()
    private var selectedProduk: Produk? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_penjualan)

        db = DatabaseHelper(this)

        tvTanggal = findViewById(R.id.tvTanggalRekap)
        spProduk = findViewById(R.id.spProduk)
        etJumlah = findViewById(R.id.etJumlah)
        etCatatan = findViewById(R.id.etCatatanPenjualan)
        tvHarga = findViewById(R.id.tvHargaKanal)
        tvTotal = findViewById(R.id.tvTotalRekap)
        tvJumlahItem = findViewById(R.id.tvJumlahItem)
        btnTambah = findViewById(R.id.btnTambahItemRekap)
        btnSimpan = findViewById(R.id.btnSimpanRekap)
        rvItem = findViewById(R.id.rvItemRekap)
        emptyState = findViewById(R.id.layoutEmptyItem)

        tvTanggal.text = FormatHelper.today()
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        rvItem.layoutManager = LinearLayoutManager(this)
        rvItem.adapter = ItemRekapAdapter(listItem) { position -> showEditDialog(position) }

        btnTambah.setOnClickListener { tambahItem() }
        btnSimpan.setOnClickListener { simpanRekap() }

        AppNavigator.setupBottomNav(this, AppNavigator.Tab.PENJUALAN)
    }

    override fun onResume() {
        super.onResume()
        loadProduk()
        updateUi()
    }

    private fun loadProduk() {
        listProduk = db.getAllProduk()
        if (listProduk.isEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Belum ada produk"))
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spProduk.adapter = adapter
            selectedProduk = null
            btnTambah.isEnabled = false
            btnSimpan.isEnabled = false
            tvHarga.text = FormatHelper.rupiah(0)
            return
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listProduk.map { it.nama })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spProduk.adapter = adapter
        btnTambah.isEnabled = true
        btnSimpan.isEnabled = true
        spProduk.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedProduk = listProduk[position]
                tvHarga.text = FormatHelper.rupiah(selectedProduk?.hargaJual ?: 0)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedProduk = null
                tvHarga.text = FormatHelper.rupiah(0)
            }
        }
        selectedProduk = listProduk.firstOrNull()
        tvHarga.text = FormatHelper.rupiah(selectedProduk?.hargaJual ?: 0)
    }

    private fun tambahItem() {
        val produk = selectedProduk
        val jumlah = etJumlah.text.toString().trim().toIntOrNull()

        if (produk == null) {
            Toast.makeText(this, "Pilih produk dulu", Toast.LENGTH_SHORT).show()
            return
        }
        if (jumlah == null || jumlah <= 0) {
            Toast.makeText(this, "Jumlah harus lebih dari 0", Toast.LENGTH_SHORT).show()
            return
        }
        val totalProdukDiRekap = getTotalJumlahUntukProduk(produk.id)
        if (totalProdukDiRekap + jumlah > produk.stokSaatIni) {
            Toast.makeText(this, "Stok ${produk.nama} tidak cukup", Toast.LENGTH_SHORT).show()
            return
        }

        listItem.add(
            ItemRekap(
                produkId = produk.id,
                namaProduk = produk.nama,
                jumlah = jumlah,
                subtotal = jumlah * produk.hargaJual
            )
        )
        etJumlah.setText("")
        updateUi()
    }

    private fun getTotalJumlahUntukProduk(produkId: Int, excludedPosition: Int? = null): Int {
        return listItem.mapIndexedNotNull { index, item ->
            when {
                item.produkId != produkId -> null
                excludedPosition != null && excludedPosition == index -> null
                else -> item.jumlah
            }
        }.sum()
    }

    private fun showEditDialog(position: Int) {
        val item = listItem[position]
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(item.jumlah.toString())
        }

        AlertDialog.Builder(this)
            .setTitle("Ubah jumlah ${item.namaProduk}")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val newJumlah = input.text.toString().trim().toIntOrNull()
                if (newJumlah == null || newJumlah <= 0) {
                    Toast.makeText(this, "Jumlah tidak valid", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val stokTersedia = listProduk.firstOrNull { it.id == item.produkId }?.stokSaatIni ?: 0
                val totalLain = getTotalJumlahUntukProduk(item.produkId, position)
                if (newJumlah + totalLain > stokTersedia) {
                    Toast.makeText(this, "Stok tidak cukup", Toast.LENGTH_SHORT).show()
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
        if (listItem.isEmpty()) {
            Toast.makeText(this, "Tambahkan item dulu", Toast.LENGTH_SHORT).show()
            return
        }
        val tanggal = tvTanggal.text.toString().trim()
        val catatan = etCatatan.text.toString().trim()
        val total = listItem.sumOf { it.subtotal }
        val payload = listItem.map { Triple(it.produkId, it.jumlah, it.subtotal) }

        val saved = db.insertPenjualan(payload, total, tanggal, if (catatan.isBlank()) "Penjualan harian" else catatan)
        if (saved) {
            Toast.makeText(this, "Penjualan berhasil disimpan", Toast.LENGTH_SHORT).show()
            listItem.clear()
            etCatatan.setText("")
            updateUi()
            loadProduk()
        } else {
            Toast.makeText(this, "Gagal menyimpan penjualan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUi() {
        rvItem.adapter?.notifyDataSetChanged()
        val isEmpty = listItem.isEmpty()
        emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        rvItem.visibility = if (isEmpty) View.GONE else View.VISIBLE
        tvJumlahItem.text = "${listItem.size} item"
        tvTotal.text = FormatHelper.rupiah(listItem.sumOf { it.subtotal })
    }
}
