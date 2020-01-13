package com.juliusbaer.premarket.ui.markets

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.serverModels.FxType
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.NavigationChild
import com.juliusbaer.premarket.ui.fragments.extentions.initToolbar
import com.juliusbaer.premarket.ui.markets.fx.FxFragment
import com.juliusbaer.premarket.ui.markets.indices.MarketsIndicesFragment
import kotlinx.android.synthetic.main.fragment_markets.*
import kotlinx.android.synthetic.main.header_app.*
import kotlinx.android.synthetic.main.include_toolbar.*

class MarketsFragment: BaseNFragment(R.layout.fragment_markets), NavigationChild {
    companion object {
        const val TAG = "Markets"
    }

    override val titleResId: Int
        get() = R.string.markets

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolbar()

        screenTitle.setText(R.string.markets)

        toolbar.inflateMenu(R.menu.call)
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.call) {
                callToTrader()
                true
            } else {
                false
            }
        }

        tabLayout.setupWithViewPager(setupViewPager())
    }

    private fun setupViewPager(): ViewPager {
        val titles = arrayOf(R.string.indices, R.string.fx, R.string.precious_metals)
        viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return when (position) {
                    0 -> MarketsIndicesFragment()
                    1 -> FxFragment.newInstance(FxType.CURRENCY)
                    2 -> FxFragment.newInstance(FxType.METAL)
                    else -> throw IllegalStateException("Invalid page pos $position")
                }
            }

            override fun getCount(): Int {
                return 3
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return getString(titles[position])
            }
        }
        return viewPager
    }
}