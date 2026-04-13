package anezza.aulia.sitahu

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import anezza.aulia.sitahu.database.DatabaseHelper

class HomeFragment : BaseScreenFragment(R.layout.beranda) {

    private var db: DatabaseHelper? = null
    private var layoutLowStock: LinearLayout? = null
    private var layoutRecentTransactions: LinearLayout? = null
    private var tvLowStockEmpty: TextView? = null
    private var tvSummaryTitle: TextView? = null
    private var tvSummaryTotal: TextView? = null
    private var tvSummary1: TextView? = null
    private var tvSummary2: TextView? = null
    private var tvSummary3: TextView? = null
    private var tvSummary4: TextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())

        layoutLowStock = view.findViewById(R.id.layoutLowStock)
        layoutRecentTransactions = view.findViewById(R.id.layoutRecentTransactions)
        tvLowStockEmpty = view.findViewById(R.id.tvLowStockEmpty)
        tvSummaryTitle = view.findViewById(R.id.tvHomeSummaryTitle)
        tvSummaryTotal = view.findViewById(R.id.tvHomeSummaryTotal)
        tvSummary1 = view.findViewById(R.id.tvHomeSummary1)
        tvSummary2 = view.findViewById(R.id.tvHomeSummary2)
        tvSummary3 = view.findViewById(R.id.tvHomeSummary3)
        tvSummary4 = view.findViewById(R.id.tvHomeSummary4)

        view.findViewById<View>(R.id.cardProduksi).setOnClickListener {
            host().switchToTab(RootTab.PRODUKSI)
        }
        view.findViewById<View>(R.id.cardPenjualan).setOnClickListener {
            host().switchToTab(RootTab.PENJUALAN)
        }
        view.findViewById<View>(R.id.cardStok).setOnClickListener {
            host().switchToTab(RootTab.STOK)
        }
        view.findViewById<View>(R.id.cardMenu).setOnClickListener {
            host().openPengeluaranList()
        }
    }

    override fun refreshContent() {
        val helper = db ?: return
        renderSummary(helper)
        renderLowStock(helper)
        renderRecentTransactions(helper)
    }

    private fun renderSummary(helper: DatabaseHelper) {
        val readable = helper.readableDatabase
        val todayKey = FormatHelper.todayStorageDate()
        val totalPenjualan = intQuery(
            readable,
            "SELECT COALESCE(SUM(total_penjualan),0) FROM penjualan WHERE SUBSTR(tanggal_penjualan,1,10)=?",
            arrayOf(todayKey)
        )
        val totalProduksi = intQuery(
            readable,
            "SELECT COALESCE(SUM(jumlah_hasil),0) FROM produksi WHERE SUBSTR(tanggal_produksi,1,10)=?",
            arrayOf(todayKey)
        )
                val totalStok = helper.getTotalStok()
        val totalPengeluaran = intQuery(
            readable,
            "SELECT COALESCE(SUM(nominal),0) FROM pengeluaran WHERE SUBSTR(tanggal_pengeluaran,1,10)=? AND is_deleted=0",
            arrayOf(todayKey)
        )

        val totalLaba = totalPenjualan - totalPengeluaran

        tvSummaryTitle?.text = "Laba hari ini"
        tvSummaryTotal?.text = FormatHelper.rupiah(totalLaba)
        tvSummary1?.text = "Pemasukan hari ini: ${FormatHelper.rupiah(totalPenjualan)}"
        tvSummary2?.text = "Produksi hari ini: $totalProduksi pcs"
        tvSummary3?.text = "Total stok: $totalStok pcs"
        tvSummary4?.text = "Pengeluaran hari ini: ${FormatHelper.rupiah(totalPengeluaran)}"
    }

    private fun renderLowStock(helper: DatabaseHelper) {
        val container = layoutLowStock ?: return
        container.removeAllViews()
        val lowStock = helper.getActiveProduk()
            .filter { it.stokSaatIni <= it.stokMinimum }
            .sortedBy { it.stokSaatIni }
            .take(4)

        if (lowStock.isEmpty()) {
            tvLowStockEmpty?.text = "Belum ada produk yang menipis."
            return
        }

        tvLowStockEmpty?.text = ""
        lowStock.forEach { produk ->
            container.addView(
                dashboardItemView(
                    avatar = produk.nama.take(1).uppercase(),
                    title = produk.nama,
                    subtitle = "Sisa ${produk.stokSaatIni} ${produk.satuan} • Minimum ${produk.stokMinimum}",
                    chip = if (produk.stokSaatIni <= 0) "Habis" else "Menipis",
                    value = "Stok",
                    avatarBackground = if (produk.stokSaatIni <= 0) R.drawable.bg_badge_orange else R.drawable.bg_badge_gold,
                    avatarTextColor = if (produk.stokSaatIni <= 0) R.color.orange_text else R.color.gold_text
                ) {
                    host().switchToTab(RootTab.STOK)
                }
            )
        }
    }

    private fun renderRecentTransactions(helper: DatabaseHelper) {
        val container = layoutRecentTransactions ?: return
        container.removeAllViews()
        val dbReadable = helper.readableDatabase
        val cursor = dbReadable.rawQuery(
            """
            SELECT 'PENJUALAN' AS tipe,
                   p.id AS row_id,
                   p.tanggal_penjualan AS waktu,
                   p.total_penjualan AS nilai,
                   (SELECT GROUP_CONCAT(nama_produk_snapshot || ' x' || jumlah, ', ')
                    FROM item_penjualan i
                    WHERE i.penjualan_id = p.id) AS detail
            FROM penjualan p
            UNION ALL
            SELECT 'PRODUKSI' AS tipe,
                   pr.id AS row_id,
                   pr.tanggal_produksi AS waktu,
                   pr.jumlah_hasil AS nilai,
                   prod.nama || ' • ' || pr.jumlah_masak || ' masak' AS detail
            FROM produksi pr
            INNER JOIN produk prod ON prod.id = pr.produk_id
            ORDER BY waktu DESC, row_id DESC
            LIMIT 4
            """.trimIndent(),
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                val tipe = it.getString(it.getColumnIndexOrThrow("tipe"))
                val rowId = it.getInt(it.getColumnIndexOrThrow("row_id"))
                val waktu = it.getString(it.getColumnIndexOrThrow("waktu")) ?: ""
                val nilai = it.getInt(it.getColumnIndexOrThrow("nilai"))
                val detail = it.getString(it.getColumnIndexOrThrow("detail")) ?: ""
                val isPenjualan = tipe == "PENJUALAN"
                container.addView(
                    dashboardItemView(
                        avatar = if (isPenjualan) "T" else "P",
                        title = buildDisplayCode(if (isPenjualan) "TRX" else "PROD", rowId),
                        subtitle = "${FormatHelper.toDisplayDateTime(waktu)} • $detail",
                        chip = if (isPenjualan) "Pemasukan" else "Produksi",
                        value = if (isPenjualan) FormatHelper.rupiah(nilai) else "$nilai pcs",
                        avatarBackground = if (isPenjualan) R.drawable.bg_badge_gold else R.drawable.bg_badge_green,
                        avatarTextColor = if (isPenjualan) R.color.gold_text else R.color.green_text
                    ) {
                        host().switchToTab(if (isPenjualan) RootTab.PENJUALAN else RootTab.PRODUKSI)
                    }
                )
            }
        }
    }

    private fun dashboardItemView(
        avatar: String,
        title: String,
        subtitle: String,
        chip: String,
        value: String,
        avatarBackground: Int,
        avatarTextColor: Int,
        onClick: (() -> Unit)? = null
    ): View {
        val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_dashboard_event, layoutRecentTransactions, false)
        itemView.findViewById<TextView>(R.id.tvAvatar).apply {
            text = avatar
            setBackgroundResource(avatarBackground)
            setTextColor(requireContext().getColor(avatarTextColor))
        }
        itemView.findViewById<TextView>(R.id.tvTitle).text = title
        itemView.findViewById<TextView>(R.id.tvSubtitle).text = subtitle
        itemView.findViewById<TextView>(R.id.tvChip).text = chip
        itemView.findViewById<TextView>(R.id.tvValue).text = value
        itemView.setOnClickListener { onClick?.invoke() }
        return itemView
    }

    private fun intQuery(db: SQLiteDatabase, sql: String, args: Array<String>?): Int {
        val cursor = db.rawQuery(sql, args)
        return cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    private fun buildDisplayCode(prefix: String, id: Int): String = "$prefix-${String.format("%04d", id)}"
}
