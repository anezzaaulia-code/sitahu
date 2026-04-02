package anezza.aulia.sitahu

data class Produk(
    val id: Int = 0,
    val name: String,
    val category: String,
    val stock: Int,
    val minStock: Int
)