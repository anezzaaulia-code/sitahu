package anezza.aulia.sitahu

data class MutasiStok(
    val id: Int,
    val produkId: Int,
    val jenisReferensi: String,
    val referensiId: Int,
    val tanggal: String,
    val arah: String,
    val jumlah: Int,
    val catatan: String
)