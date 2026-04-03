package anezza.aulia.sitahu

data class Produk(
    val id: Int = 0,
    val nama: String,
    val kategori: String,
    val stok: Int,
    val minStok: Int
) {
    fun getStatusStok(): String {
    return if (stok > minStok) "Aman" else "Menipis"
    }
}