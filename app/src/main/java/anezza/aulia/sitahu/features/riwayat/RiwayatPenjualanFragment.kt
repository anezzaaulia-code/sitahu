package anezza.aulia.sitahu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import anezza.aulia.sitahu.database.DatabaseHelper

class RiwayatPenjualanFragment : BaseScreenFragment(R.layout.fragment_riwayat_penjualan) {

    private var db: DatabaseHelper? = null
    private var layoutRiwayat: LinearLayout? = null
    private var tvEmpty: TextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())
        setupBack(view)
        layoutRiwayat = view.findViewById(R.id.layoutRiwayatPenjualan)
        tvEmpty = view.findViewById(R.id.tvRiwayatPenjualanEmpty)

        parentFragmentManager.setFragmentResultListener(PenjualanFragment.RESULT_KEY, viewLifecycleOwner) { _, _ ->
            refreshContent()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshContent()
    }

    override fun refreshContent() {
        renderRiwayatPenjualan()
    }

    private fun renderRiwayatPenjualan() {
        val helper = db ?: return
        val target = layoutRiwayat ?: return
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
            LIMIT 50
            """.trimIndent(),
            null
        )

        var count = 0
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
                count += 1
            }
        }

        tvEmpty?.text = if (count == 0) "Belum ada riwayat penjualan." else ""
    }

    private fun dashboardItemView(
        avatar: String,
        title: String,
        subtitle: String,
        chip: String,
        value: String
    ): View {
        val itemView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_dashboard_event, layoutRiwayat, false)

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

    private fun buildDisplayCode(prefix: String, id: Int): String {
        return "$prefix-${String.format("%04d", id)}"
    }
}
