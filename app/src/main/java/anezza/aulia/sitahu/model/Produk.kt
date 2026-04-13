package anezza.aulia.sitahu

data class Produk(
    val id: Int = 0,
    val nama: String,
    val satuan: String,
    val stokSaatIni: Int,
    val stokMinimum: Int,
    val hargaJual: Int,
    val dibuatPada: String = "",
    val diubahPada: String = "",
    val aktif: Boolean = true
)
