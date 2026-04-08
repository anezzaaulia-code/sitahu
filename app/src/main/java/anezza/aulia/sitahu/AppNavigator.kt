package anezza.aulia.sitahu

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

object AppNavigator {

    enum class Tab(val menuId: Int, val target: Class<out Activity>) {
        BERANDA(R.id.nav_beranda, MainActivity::class.java),
        PRODUKSI(R.id.nav_produksi, ProduksiActivity::class.java),
        PENJUALAN(R.id.nav_penjualan, PenjualanActivity::class.java),
        STOK(R.id.nav_stok, StokActivity::class.java),
        MENU(R.id.nav_menu, MenuActivity::class.java);

        companion object {
            fun fromMenuId(menuId: Int): Tab? = entries.firstOrNull { it.menuId == menuId }
        }
    }

    fun setupBottomNav(activity: Activity, currentTab: Tab) {
        val bottomNavigation = activity.findViewById<BottomNavigationView?>(R.id.bottomNavigation) ?: return
        bottomNavigation.selectedItemId = currentTab.menuId
        bottomNavigation.setOnItemSelectedListener { item ->
            val targetTab = Tab.fromMenuId(item.itemId) ?: return@setOnItemSelectedListener false
            if (targetTab == currentTab) {
                true
            } else {
                navigateTo(activity, targetTab.target)
                true
            }
        }
    }

    fun navigateTo(activity: Activity, target: Class<out Activity>, finishCurrent: Boolean = true) {
        if (activity.javaClass == target) return
        val intent = Intent(activity, target).apply {
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        activity.startActivity(intent)
        activity.overridePendingTransition(0, 0)
        if (finishCurrent) {
            activity.finish()
        }
    }
}
