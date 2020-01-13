package com.juliusbaer.premarket.ui.underluings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.serverModels.UnderlyingType
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.NavigationChild
import com.juliusbaer.premarket.ui.fragments.extentions.initToolbar
import kotlinx.android.synthetic.main.child_header_app.*
import kotlinx.android.synthetic.main.fragment_underlyings.*
import kotlinx.android.synthetic.main.include_toolbar.*

class UnderlyingsFragment : BaseNFragment(R.layout.fragment_underlyings), NavigationChild {
    companion object {
        const val TAG = "Underlyings"
    }

    override val titleResId: Int
        get() = R.string.equities

    private val viewModel by viewModels<UnderlyingsViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        screenTitle.setText(R.string.equities)

        val titles = arrayOf(R.string.jb_smi, R.string.jb_mid_cap)
        viewPager.adapter = object: FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return when (position) {
                    0 -> UnderlyingListFragment.newInstance(UnderlyingType.SMI)
                    1 -> UnderlyingListFragment.newInstance(UnderlyingType.MIDCAP)
                    else -> throw IllegalStateException("invalid view pager position $position")
                }
            }

            override fun getCount(): Int {
                return 2
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return getString(titles[position])
            }
        }
        tabLayout.setupWithViewPager(viewPager)

        initToolbar()

        toolbar.inflateMenu(R.menu.underlyings)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.call -> {
                    callToTrader()
                    true
                }
                R.id.candlestick -> {
                    if (viewModel.boxesLiveData.value!!) {
                        viewModel.setBoxes(false)
                        item.setIcon(R.drawable.ic_boxes)
                    } else {
                        viewModel.setBoxes(true)
                        item.setIcon(R.drawable.ic_candlestick_white)
                    }
                    true
                }
                R.id.sort -> {
                    val isTop = !(viewModel.topLiveData.value?:true)
                    item.setIcon(if (isTop) R.drawable.ic_sortalphabetic else R.drawable.ic_sortperformans)

                    viewModel.setTop(isTop)
                    true
                }
                else -> false
            }
        }
    }
}


