package anezza.aulia.sitahu

data class Pengeluaran(
    val id: Int = 0,
    val tanggalPengeluaran: String,
    val namaPengeluaran: String,
    val nominal: Int,
    val catatan: String = ""
)
