package anezza.aulia.sitahu

interface MainHost {
    fun switchToTab(tab: RootTab)
    fun openProdukList()
    fun openProductForm(produkId: Int? = null)
    fun openPengeluaranList()
    fun openPengeluaranForm(pengeluaranId: Int? = null)
    fun openStokDetail(produkId: Int)
    fun openProduksiForm()
    fun openProduksiHistory()
    fun openPenjualanForm()
    fun openPenjualanHistory()
    fun goBack()
}
