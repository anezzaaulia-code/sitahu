package anezza.aulia.sitahu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import anezza.aulia.sitahu.database.DatabaseHelper

class RiwayatProduksiFragment : BaseScreenFragment(R.layout.fragment_riwayat_produksi) {

    private var db: DatabaseHelper? = null
    private var layoutRiwayat: LinearLayout? = null
    private var tvEmpty: TextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())
        setupBack(view)
        layoutRiwayat = view.findViewById(R.id.layoutRiwayatProduksi)
        tvEmpty = view.findViewById(R.id.tvRiwayatProduksiEmpty)

        parentFragmentManager.setFragmentResultListener(ProduksiFragment.RESULT_KEY, viewLifecycleOwner) { _, _ ->
            refreshContent()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshContent()
    }

    override fun refreshContent() {
        renderRiwayatProduksi()
    }

    private fun renderRiwayatProduksi() {
        val helper = db ?: return
        val target = layoutRiwayat ?: return
        target.removeAllViews()

        val cursor = helper.readableDatabase.rawQuery(
            """
            SELECT pr.id, pr.tanggal_produksi, pr.jumlah_hasil, pr.jumlah_masak, prod.nama
            FROM produksi pr
            INNER JOIN produk prod ON prod.id = pr.produk_id
            ORDER BY pr.tanggal_produksi DESC, pr.id DESC
            LIMIT 50
            """.trimIndent(),
            null
        )

        var count = 0
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
                count += 1
            }
        }

        tvEmpty?.text = if (count == 0) "Belum ada riwayat produksi." else ""
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
            setBackgroundResource(R.drawable.bg_badge_green)
            setTextColor(requireContext().getColor(R.color.green_text))
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
