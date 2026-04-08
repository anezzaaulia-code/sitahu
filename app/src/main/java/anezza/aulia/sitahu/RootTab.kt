package anezza.aulia.sitahu

enum class RootTab(val menuId: Int, val tag: String) {
    BERANDA(R.id.nav_beranda, "root_beranda"),
    PRODUKSI(R.id.nav_produksi, "root_produksi"),
    PENJUALAN(R.id.nav_penjualan, "root_penjualan"),
    STOK(R.id.nav_stok, "root_stok"),
    MENU(R.id.nav_menu, "root_menu");

    companion object {
        fun fromMenuId(menuId: Int): RootTab? = entries.firstOrNull { it.menuId == menuId }
    }
}
