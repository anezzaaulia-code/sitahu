package anezza.aulia.sitahu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), MainHost {

    private lateinit var bottomNavigation: BottomNavigationView
    private var currentTab: RootTab = RootTab.BERANDA
    private var ignoreNavigationCallback = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottomNavigation)
        setupWindowInsets()

        currentTab = savedInstanceState?.getString(KEY_CURRENT_TAB)?.let {
            runCatching { RootTab.valueOf(it) }.getOrDefault(RootTab.BERANDA)
        } ?: RootTab.BERANDA

        bottomNavigation.setOnItemSelectedListener { item ->
            if (ignoreNavigationCallback) return@setOnItemSelectedListener true
            val tab = RootTab.fromMenuId(item.itemId) ?: return@setOnItemSelectedListener false
            switchToTab(tab)
            true
        }

        if (savedInstanceState == null) {
            switchTabInternal(currentTab)
            handleIntentNavigation(intent)
        } else {
            if (bottomNavigation.selectedItemId != currentTab.menuId) {
                ignoreNavigationCallback = true
                bottomNavigation.selectedItemId = currentTab.menuId
                ignoreNavigationCallback = false
            }
            showOnlyCurrentRoot()
        }
    }

    private fun setupWindowInsets() {
        val fragmentContainer = findViewById<android.view.View>(R.id.fragmentContainer)
        val bottomCard = findViewById<android.view.View>(R.id.bottomNavCard)

        val fragmentPaddingLeft = fragmentContainer.paddingLeft
        val fragmentPaddingTop = fragmentContainer.paddingTop
        val fragmentPaddingRight = fragmentContainer.paddingRight
        val fragmentPaddingBottom = fragmentContainer.paddingBottom

        val bottomStart = bottomCard.paddingLeft
        val bottomTop = bottomCard.paddingTop
        val bottomEnd = bottomCard.paddingRight
        val bottomBottom = bottomCard.paddingBottom

        val params =
            bottomCard.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        val baseBottomMargin = params.bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRoot)) { _, insets ->
            val statusInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            fragmentContainer.setPadding(
                fragmentPaddingLeft,
                fragmentPaddingTop + statusInsets.top,
                fragmentPaddingRight,
                fragmentPaddingBottom
            )

            bottomCard.setPadding(bottomStart, bottomTop, bottomEnd, bottomBottom)

            (bottomCard.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams).apply {
                bottomMargin = baseBottomMargin + navInsets.bottom
            }.also { bottomCard.layoutParams = it }

            insets
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntentNavigation(intent)
    }

    private fun handleIntentNavigation(intent: Intent?) {
        when (intent?.getStringExtra(EXTRA_OPEN_TARGET)) {
            TARGET_PRODUK -> {
                switchToTab(RootTab.MENU)
                openProdukList()
            }
            TARGET_PENGELUARAN -> {
                switchToTab(RootTab.MENU)
                openPengeluaranList()
            }
            TARGET_STOK -> switchToTab(RootTab.STOK)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_CURRENT_TAB, currentTab.name)
        super.onSaveInstanceState(outState)
    }

    override fun switchToTab(tab: RootTab) {
        switchTabInternal(tab)
    }

    private fun switchTabInternal(tab: RootTab) {
        clearNestedBackStack()
        currentTab = tab

        val transaction = supportFragmentManager.beginTransaction().setReorderingAllowed(true)

        RootTab.entries.forEach { rootTab ->
            val existing = supportFragmentManager.findFragmentByTag(rootTab.tag)
            if (existing != null) {
                if (rootTab == tab) {
                    transaction.show(existing)
                } else {
                    transaction.hide(existing)
                }
            }
        }

        val target = supportFragmentManager.findFragmentByTag(tab.tag)
        if (target == null) {
            transaction.add(R.id.fragmentContainer, createRootFragment(tab), tab.tag)
        }

        transaction.commitNow()

        if (bottomNavigation.selectedItemId != tab.menuId) {
            ignoreNavigationCallback = true
            bottomNavigation.selectedItemId = tab.menuId
            ignoreNavigationCallback = false
        }

        (supportFragmentManager.findFragmentByTag(tab.tag) as? RefreshableScreen)?.refreshContent()
    }

    private fun showOnlyCurrentRoot() {
        val transaction = supportFragmentManager.beginTransaction().setReorderingAllowed(true)

        RootTab.entries.forEach { tab ->
            val fragment = supportFragmentManager.findFragmentByTag(tab.tag) ?: return@forEach
            if (tab == currentTab) {
                transaction.show(fragment)
            } else {
                transaction.hide(fragment)
            }
        }

        transaction.commitNowAllowingStateLoss()

        if (bottomNavigation.selectedItemId != currentTab.menuId) {
            ignoreNavigationCallback = true
            bottomNavigation.selectedItemId = currentTab.menuId
            ignoreNavigationCallback = false
        }
    }

    private fun createRootFragment(tab: RootTab): Fragment = when (tab) {
        RootTab.BERANDA -> HomeFragment()
        RootTab.PRODUKSI -> ProduksiFragment()
        RootTab.PENJUALAN -> PenjualanFragment()
        RootTab.STOK -> StokFragment()
        RootTab.MENU -> MenuFragment()
    }

    private fun clearNestedBackStack() {
        while (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        }
    }

    private fun openNestedFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(
                R.anim.fragment_open_enter,
                R.anim.fragment_open_exit,
                R.anim.fragment_close_enter,
                R.anim.fragment_close_exit
            )
            .add(R.id.fragmentContainer, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    override fun openProdukList() {
        openNestedFragment(ProdukFragment(), "produk_list")
    }

    override fun openProductForm(produkId: Int?) {
        openNestedFragment(
            ProductFormFragment.newInstance(produkId),
            "produk_form_${produkId ?: "new"}"
        )
    }

    override fun openPengeluaranList() {
        openNestedFragment(PengeluaranFragment(), "pengeluaran_list")
    }

    override fun openPengeluaranForm(pengeluaranId: Int?) {
        openNestedFragment(
            PengeluaranFormFragment.newInstance(pengeluaranId),
            "pengeluaran_form_${pengeluaranId ?: "new"}"
        )
    }

    override fun openStokDetail(produkId: Int) {
        openNestedFragment(DetailStokFragment.newInstance(produkId), "detail_stok_$produkId")
    }

    override fun openProduksiForm() {
        openNestedFragment(AddProduksiFragment(), "produksi_form")
    }

    override fun openProduksiHistory() {
        openNestedFragment(RiwayatProduksiFragment(), "produksi_history")
    }

    override fun openPenjualanForm() {
        openNestedFragment(AddPenjualanFragment(), "penjualan_form")
    }

    override fun openPenjualanHistory() {
        openNestedFragment(RiwayatPenjualanFragment(), "penjualan_history")
    }

    override fun goBack() {
        onBackPressedDispatcher.onBackPressed()
    }

    companion object {
        private const val KEY_CURRENT_TAB = "current_tab"
        const val EXTRA_OPEN_TARGET = "open_target"
        const val TARGET_PRODUK = "target_produk"
        const val TARGET_PENGELUARAN = "target_pengeluaran"
        const val TARGET_STOK = "target_stok"
    }
}