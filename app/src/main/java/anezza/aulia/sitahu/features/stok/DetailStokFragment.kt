package anezza.aulia.sitahu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import anezza.aulia.sitahu.database.DatabaseHelper

class DetailStokFragment : BaseScreenFragment(R.layout.detail_stok) {

    private var db: DatabaseHelper? = null
    private var tvNamaProduk: TextView? = null
    private var tvStokSaatIni: TextView? = null
    private var tvStokMinimum: TextView? = null
    private var tvStatusStok: TextView? = null
    private var containerLog: LinearLayout? = null
    private var produkId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        produkId = arguments?.getInt(ARG_PRODUK_ID) ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())
        setupBack(view)
        tvNamaProduk = view.findViewById(R.id.tvNamaProduk)
        tvStokSaatIni = view.findViewById(R.id.tvStokSaatIni)
        tvStokMinimum = view.findViewById(R.id.tvStokMinimum)
        tvStatusStok = view.findViewById(R.id.tvStatusStok)
        containerLog = view.findViewById(R.id.containerLog)
    }

    override fun refreshContent() {
        if (produkId <= 0) {
            Toast.makeText(requireContext(), "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
            host().goBack()
            return
        }
        loadData(produkId)
    }

    private fun loadData(produkId: Int) {
        val helper = db ?: return
        val produk = helper.getProdukById(produkId)
        if (produk == null) {
            Toast.makeText(requireContext(), "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
            host().goBack()
            return
        }

        tvNamaProduk?.text = produk.nama
        tvStokSaatIni?.text = "${produk.stokSaatIni} ${produk.satuan}"
        tvStokMinimum?.text = "${produk.stokMinimum} ${produk.satuan}"

        val (statusLabel, statusBg, statusColor) = when {
            produk.stokSaatIni <= 0 -> Triple("Habis", R.drawable.bg_badge_orange, R.color.orange_text)
            produk.stokSaatIni <= produk.stokMinimum -> Triple("Menipis", R.drawable.bg_badge_gold, R.color.gold_text)
            else -> Triple("Aman", R.drawable.bg_badge_green, R.color.green_text)
        }
        tvStatusStok?.text = statusLabel
        tvStatusStok?.background = ContextCompat.getDrawable(requireContext(), statusBg)
        tvStatusStok?.setTextColor(ContextCompat.getColor(requireContext(), statusColor))

        val target = containerLog ?: return
        target.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val items = helper.getMutasiByProduk(produkId)
        if (items.isEmpty()) {
            val itemView = inflater.inflate(R.layout.item_mutasi, target, false)
            itemView.findViewById<TextView>(R.id.tvArah).apply {
                text = "Belum ada riwayat"
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_badge_blue)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_text))
            }
            itemView.findViewById<TextView>(R.id.tvTanggal).text = ""
            itemView.findViewById<TextView>(R.id.tvCatatan).text = "Mutasi stok akan muncul di sini"
            itemView.findViewById<TextView>(R.id.tvJumlah).text = "-"
            target.addView(itemView)
            return
        }

        items.forEach { item ->
            val itemView = inflater.inflate(R.layout.item_mutasi, target, false)
            val prefix = if (item.arah == "MASUK") "+" else "-"
            val arahView = itemView.findViewById<TextView>(R.id.tvArah)
            val (badgeRes, textColorRes) = if (item.arah == "MASUK") {
                R.drawable.bg_badge_green to R.color.green_text
            } else {
                R.drawable.bg_badge_orange to R.color.orange_text
            }
            arahView.text = item.arah
            arahView.background = ContextCompat.getDrawable(requireContext(), badgeRes)
            arahView.setTextColor(ContextCompat.getColor(requireContext(), textColorRes))
            itemView.findViewById<TextView>(R.id.tvTanggal).text = FormatHelper.toDisplayDateTime(item.tanggal)
            itemView.findViewById<TextView>(R.id.tvCatatan).text = item.catatan.ifBlank { item.jenisReferensi }
            itemView.findViewById<TextView>(R.id.tvJumlah).text = "$prefix${item.jumlah} ${produk.satuan}"
            target.addView(itemView)
        }
    }

    companion object {
        private const val ARG_PRODUK_ID = "produk_id"

        fun newInstance(produkId: Int): DetailStokFragment {
            return DetailStokFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PRODUK_ID, produkId)
                }
            }
        }
    }
}
