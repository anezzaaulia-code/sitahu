package anezza.aulia.sitahu

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import anezza.aulia.sitahu.database.DatabaseHelper

class ProduksiFragment : BaseScreenFragment(R.layout.activity_produksi) {

    private var db: DatabaseHelper? = null
    private var layoutRecentProduksi: LinearLayout? = null

    private var tvProduksiHariIni: TextView? = null
    private var tvRingkas1: TextView? = null
    private var tvRingkas2: TextView? = null
    private var tvRingkas3: TextView? = null
    private var tvRingkas4: TextView? = null

    private var btnTambahProduksi: Button? = null
    private var btnRiwayatProduksi: Button? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())
        hideBackButton(view)

        layoutRecentProduksi = view.findViewById(R.id.layoutRecentProduksi)

        tvProduksiHariIni = view.findViewById(R.id.tvProduksiHariIni)
        tvRingkas1 = view.findViewById(R.id.tvProduksiRingkas1)
        tvRingkas2 = view.findViewById(R.id.tvProduksiRingkas2)
        tvRingkas3 = view.findViewById(R.id.tvProduksiRingkas3)
        tvRingkas4 = view.findViewById(R.id.tvProduksiRingkas4)

        btnTambahProduksi = view.findViewById(R.id.btnShortcutProduksi)
        btnRiwayatProduksi = view.findViewById(R.id.btnShortcutRiwayatProduksi)

        btnTambahProduksi?.setOnClickListener { host().openProduksiForm() }
        btnRiwayatProduksi?.setOnClickListener { host().openProduksiHistory() }

        view.findViewById<View>(R.id.panelFormProduksi)?.visibility = View.GONE
        view.findViewById<View>(R.id.panelRiwayatProduksi)?.visibility = View.GONE

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
        renderRecentProduksi()
    }

    private fun renderSummary() {
        val helper = db ?: return
        val readable = helper.readableDatabase
        val todayKey = FormatHelper.todayStorageDate()

        val produksiHariIni = intQuery(
            readable,
            "SELECT COALESCE(SUM(jumlah_hasil),0) FROM produksi WHERE SUBSTR(tanggal_produksi,1,10)=?",
            arrayOf(todayKey)
        )
        val batchHariIni = intQuery(
            readable,
            "SELECT COUNT(*) FROM produksi WHERE SUBSTR(tanggal_produksi,1,10)=?",
            arrayOf(todayKey)
        )
        val parameterAktif = intQuery(
            readable,
            "SELECT COUNT(*) FROM parameter_produksi WHERE aktif=1",
            null
        )
        val riwayatTotal = intQuery(
            readable,
            "SELECT COUNT(*) FROM produksi",
            null
        )

        tvProduksiHariIni?.text = "$produksiHariIni pcs"
        tvRingkas1?.text = "Batch hari ini: $batchHariIni"
        tvRingkas2?.text = "Total hasil hari ini: $produksiHariIni pcs"
        tvRingkas3?.text = "Parameter aktif: $parameterAktif"
        tvRingkas4?.text = "Riwayat total: $riwayatTotal"
    }

    private fun renderRecentProduksi() {
        val helper = db ?: return
        val target = layoutRecentProduksi ?: return
        target.removeAllViews()

        val cursor = helper.readableDatabase.rawQuery(
            """
            SELECT pr.id, pr.tanggal_produksi, pr.jumlah_hasil, pr.jumlah_masak, prod.nama
            FROM produksi pr
            INNER JOIN produk prod ON prod.id = pr.produk_id
            ORDER BY pr.tanggal_produksi DESC, pr.id DESC
            LIMIT 8
            """.trimIndent(),
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("id"))
                val tanggal = it.getString(it.getColumnIndexOrThrow("tanggal_produksi")) ?: ""
                val jumlahHasil = it.getInt(it.getColumnIndexOrThrow("jumlah_hasil"))
                val jumlahMasak = it.getInt(it.getColumnIndexOrThrow("jumlah_masak"))
                val namaProduk = it.getString(it.getColumnIndexOrThrow("nama"))

                target.addView(
                    dashboardItemView(
                        avatar = "P",
                        title = buildDisplayCode("PROD", id),
                        subtitle = "${FormatHelper.toDisplayDateTime(tanggal)} • $namaProduk • $jumlahHasil pcs",
                        chip = "Produksi",
                        value = "$jumlahMasak masak"
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
            .inflate(R.layout.item_dashboard_event, layoutRecentProduksi, false)

        itemView.findViewById<TextView>(R.id.tvAvatar).apply {
            text = avatar
            setBackgroundResource(R.drawable.bg_badge_green)
            setTextColor(requireContext().getColor(R.color.green_text))
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
        const val RESULT_KEY = "produksi_changed"
    }
}
