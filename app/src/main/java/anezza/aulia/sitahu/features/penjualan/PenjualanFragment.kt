package anezza.aulia.sitahu

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import anezza.aulia.sitahu.database.DatabaseHelper

class PenjualanFragment : BaseScreenFragment(R.layout.activity_penjualan) {

    private var db: DatabaseHelper? = null
    private var layoutRecentPenjualan: LinearLayout? = null

    private var tvTanggal: TextView? = null
    private var tvTotal: TextView? = null
    private var tvRingkas1: TextView? = null
    private var tvRingkas2: TextView? = null
    private var tvRingkas3: TextView? = null
    private var tvRingkas4: TextView? = null

    private var btnTambahPenjualan: Button? = null
    private var btnRiwayatPenjualan: Button? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())
        hideBackButton(view)

        layoutRecentPenjualan = view.findViewById(R.id.layoutRecentPenjualan)

        tvTanggal = view.findViewById(R.id.tvTanggalRekap)
        tvTotal = view.findViewById(R.id.tvTotalRekap)
        tvRingkas1 = view.findViewById(R.id.tvPenjualanRingkas1)
        tvRingkas2 = view.findViewById(R.id.tvPenjualanRingkas2)
        tvRingkas3 = view.findViewById(R.id.tvPenjualanRingkas3)
        tvRingkas4 = view.findViewById(R.id.tvJumlahItem)

        btnTambahPenjualan = view.findViewById(R.id.btnShortcutInputPenjualan)
        btnRiwayatPenjualan = view.findViewById(R.id.btnShortcutRiwayatPenjualan)

        btnTambahPenjualan?.setOnClickListener { host().openPenjualanForm() }
        btnRiwayatPenjualan?.setOnClickListener { host().openPenjualanHistory() }

        view.findViewById<View>(R.id.panelFormPenjualan)?.visibility = View.GONE
        view.findViewById<View>(R.id.panelRiwayatPenjualan)?.visibility = View.GONE

        parentFragmentManager.setFragmentResultListener(RESULT_KEY, viewLifecycleOwner) { _, _ ->
            refreshContent()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshContent()
    }

    override fun refreshContent() {
        renderSummary()
        renderRecentPenjualan()
    }

    private fun renderSummary() {
        val helper = db ?: return
        val readable = helper.readableDatabase
        val todayKey = FormatHelper.todayStorageDate()

        val totalHariIni = intQuery(
            readable,
            "SELECT COALESCE(SUM(total_penjualan),0) FROM penjualan WHERE SUBSTR(tanggal_penjualan,1,10)=?",
            arrayOf(todayKey)
        )
        val totalItem = intQuery(
            readable,
            """
            SELECT COALESCE(SUM(i.jumlah),0)
            FROM item_penjualan i
            INNER JOIN penjualan p ON p.id = i.penjualan_id
            WHERE SUBSTR(p.tanggal_penjualan,1,10)=?
            """.trimIndent(),
            arrayOf(todayKey)
        )
        val totalTransaksi = intQuery(
            readable,
            "SELECT COUNT(*) FROM penjualan WHERE SUBSTR(tanggal_penjualan,1,10)=?",
            arrayOf(todayKey)
        )
        val totalJenis = intQuery(
            readable,
            """
            SELECT COUNT(DISTINCT i.produk_id)
            FROM item_penjualan i
            INNER JOIN penjualan p ON p.id = i.penjualan_id
            WHERE SUBSTR(p.tanggal_penjualan,1,10)=?
            """.trimIndent(),
            arrayOf(todayKey)
        )
        val pengeluaranHariIni = intQuery(
            readable,
            "SELECT COALESCE(SUM(nominal),0) FROM pengeluaran WHERE SUBSTR(tanggal_pengeluaran,1,10)=? AND is_deleted=0",
            arrayOf(todayKey)
        )

        tvTanggal?.text = "Penjualan hari ini"
        tvTotal?.text = FormatHelper.rupiah(totalHariIni)
        tvRingkas1?.text = "Total item: $totalItem pcs"
        tvRingkas2?.text = "Transaksi: $totalTransaksi"
        tvRingkas3?.text = "Produk terjual: $totalJenis jenis"
        tvRingkas4?.text = "Pengeluaran hari ini: ${FormatHelper.rupiah(pengeluaranHariIni)}"
    }

    private fun renderRecentPenjualan() {
        val helper = db ?: return
        val target = layoutRecentPenjualan ?: return
        target.removeAllViews()
        val cursor = helper.readableDatabase.rawQuery(
            """
            SELECT p.id,
                   p.tanggal_penjualan,
                   p.total_penjualan,
                   (SELECT GROUP_CONCAT(nama_produk_snapshot || ' x' || jumlah, ', ')
                    FROM item_penjualan i
                    WHERE i.penjualan_id = p.id) AS detail
            FROM penjualan p
            ORDER BY p.tanggal_penjualan DESC, p.id DESC
            LIMIT 8
            """.trimIndent(),
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("id"))
                val tanggal = it.getString(it.getColumnIndexOrThrow("tanggal_penjualan")) ?: ""
                val total = it.getInt(it.getColumnIndexOrThrow("total_penjualan"))
                val detail = it.getString(it.getColumnIndexOrThrow("detail")) ?: ""
                target.addView(
                    dashboardItemView(
                        avatar = "T",
                        title = buildDisplayCode("TRX", id),
                        subtitle = "${FormatHelper.toDisplayDateTime(tanggal)} • $detail",
                        chip = "Tunai",
                        value = FormatHelper.rupiah(total)
                    )
                )
            }
        }
    }

    private fun dashboardItemView(
        avatar: String,
        title: String,
        subtitle: String,
        chip: String,
        value: String
    ): View {
        val itemView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_dashboard_event, layoutRecentPenjualan, false)

        itemView.findViewById<TextView>(R.id.tvAvatar).apply {
            text = avatar
            setBackgroundResource(R.drawable.bg_badge_gold)
            setTextColor(requireContext().getColor(R.color.gold_text))
        }
        itemView.findViewById<TextView>(R.id.tvTitle).text = title
        itemView.findViewById<TextView>(R.id.tvSubtitle).text = subtitle
        itemView.findViewById<TextView>(R.id.tvChip).text = chip
        itemView.findViewById<TextView>(R.id.tvValue).text = value

        return itemView
    }

    private fun intQuery(db: SQLiteDatabase, sql: String, args: Array<String>?): Int {
        val cursor = db.rawQuery(sql, args)
        return cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    private fun buildDisplayCode(prefix: String, id: Int): String {
        return "$prefix-${String.format("%04d", id)}"
    }

    companion object {
        const val RESULT_KEY = "penjualan_changed"
    }
}
