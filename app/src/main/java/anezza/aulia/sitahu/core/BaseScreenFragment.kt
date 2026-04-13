package anezza.aulia.sitahu

import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseScreenFragment(@LayoutRes layoutId: Int) : Fragment(layoutId), RefreshableScreen {

    protected fun host(): MainHost = requireActivity() as MainHost

    protected fun hideEmbeddedBottomNav(root: View) {
        root.findViewById<BottomNavigationView?>(R.id.bottomNavigation)?.visibility = View.GONE
    }

    protected fun hideBackButton(root: View) {
        root.findViewById<View?>(R.id.btnBack)?.visibility = View.GONE
    }

    protected fun setupBack(root: View) {
        root.findViewById<View?>(R.id.btnBack)?.setOnClickListener { host().goBack() }
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden) refreshContent()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && view != null) refreshContent()
    }
}
