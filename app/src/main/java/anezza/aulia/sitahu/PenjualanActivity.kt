package anezza.aulia.sitahu

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import anezza.aulia.sitahu.database.DatabaseHelper
import java.text.NumberFormat
import java.util.*

class PenjualanActivity : AppCompatActivity() {

    lateinit var db: DatabaseHelper

    lateinit var spProduk: Spinner
    lateinit var etJumlah: EditText
    lateinit var tvHarga: TextView
    lateinit var tvTotal: TextView
    lateinit var tvJumlahItem: TextView

    lateinit var btnTambah: Button
    lateinit var btnSimpan: Button

    lateinit var rv: RecyclerView
    lateinit var layoutEmpty: LinearLayout

    lateinit var listProduk: List<Produk>
    var selectedProduk: Produk? = null

    val listItem = mutableListOf<ItemRekap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_penjualan)

        db = DatabaseHelper(this)

        initView()
        loadProduk()
        setupRecycler()
        setupAction()
    }

    private fun initView() {
        spProduk = findViewById(R.id.spProduk)
        etJumlah = findViewById(R.id.etJumlah)

        tvHarga = findViewById(R.id.tvHargaKanal)
        tvTotal = findViewById(R.id.tvTotalRekap)
        tvJumlahItem = findViewById(R.id.tvJumlahItem)

        btnTambah = findViewById(R.id.btnTambahItemRekap)
        btnSimpan = findViewById(R.id.btnSimpanRekap)

        rv = findViewById(R.id.rvItemRekap)
        layoutEmpty = findViewById(R.id.layoutEmptyItem)
    }

    private fun setupRecycler() {
        rv.layoutManager = LinearLayoutManager(this)

        rv.adapter = ItemRekapAdapter(listItem) { position ->
            showEditDialog(position)
        }
    }

    private fun loadProduk() {
        listProduk = db.getAllProduk()

        val namaList = listProduk.map { it.nama }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            namaList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spProduk.adapter = adapter

        spProduk.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedProduk = listProduk[position]

                val harga = selectedProduk?.hargaJual ?: 0
                tvHarga.text = formatRupiah(harga)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupAction() {
        btnTambah.setOnClickListener { tambahItem() }
        btnSimpan.setOnClickListener { simpanRekap() }
    }

    // =========================
    // TAMBAH ITEM
    // =========================
    private fun tambahItem() {

        val jumlahText = etJumlah.text.toString()
        val produk = selectedProduk ?: return

        if (jumlahText.isEmpty()) {
            Toast.makeText(this, "Isi jumlah dulu", Toast.LENGTH_SHORT).show()
            return
        }

        val jumlah = jumlahText.toInt()

        // 🔥 VALIDASI STOK
        if (jumlah > produk.stokSaatIni) {
            Toast.makeText(this, "Stok tidak cukup!", Toast.LENGTH_SHORT).show()
            return
        }

        val subtotal = jumlah * produk.hargaJual

        listItem.add(
            ItemRekap(produk.id, produk.nama, jumlah, subtotal)
        )

        updateUI()

        etJumlah.setText("")
    }

    // =========================
    // UPDATE UI
    // =========================
    private fun updateUI() {
        rv.adapter?.notifyDataSetChanged()

        if (listItem.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rv.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rv.visibility = View.VISIBLE
        }

        val total = listItem.sumOf { it.subtotal }

        tvTotal.text = formatRupiah(total)
        tvJumlahItem.text = "${listItem.size} item"
    }

    // =========================
    // EDIT / HAPUS ITEM
    // =========================
    private fun showEditDialog(position: Int) {

        val item = listItem[position]

        val input = EditText(this)
        input.setText(item.jumlah.toString())

        AlertDialog.Builder(this)
            .setTitle("Edit ${item.namaProduk}")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->

                val newJumlah = input.text.toString().toIntOrNull() ?: return@setPositiveButton

                val harga = getHarga(item.produkId)
                val newSubtotal = newJumlah * harga

                listItem[position] = item.copy(
                    jumlah = newJumlah,
                    subtotal = newSubtotal
                )

                updateUI()
            }
            .setNegativeButton("Hapus") { _, _ ->
                listItem.removeAt(position)
                updateUI()
            }
            .setNeutralButton("Batal", null)
            .show()
    }

    private fun getHarga(produkId: Int): Int {
        val produk = listProduk.find { it.id == produkId }
        return produk?.hargaJual ?: 0
    }

    // =========================
    // SIMPAN REKAP
    // =========================
    private fun simpanRekap() {

        if (listItem.isEmpty()) {
            Toast.makeText(this, "Belum ada item", Toast.LENGTH_SHORT).show()
            return
        }

        val tripleList = listItem.map {
            Triple(it.produkId, it.jumlah, it.subtotal)
        }

        val total = listItem.sumOf { it.subtotal }

        val sukses = db.insertPenjualan(tripleList, total)

        if (sukses) {
            Toast.makeText(this, "Berhasil disimpan", Toast.LENGTH_LONG).show()

            listItem.clear()
            updateUI()
        } else {
            Toast.makeText(this, "Gagal", Toast.LENGTH_SHORT).show()
        }
    }

    // =========================
    // FORMAT RUPIAH
    // =========================
    private fun formatRupiah(number: Int): String {
        val localeID = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        return format.format(number)
    }
}