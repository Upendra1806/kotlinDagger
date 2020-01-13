package com.juliusbaer.premarket.ui.disclaimer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.ui.base.NavigationChild
import com.juliusbaer.premarket.ui.base.NavigationHost
import com.juliusbaer.premarket.ui.fragments.extentions.initToolbar
import kotlinx.android.synthetic.main.fragment_disclaimer.*
import kotlinx.android.synthetic.main.header_app.*

class DisclaimerFragment : Fragment(), NavigationChild {
    override val titleResId: Int
        get() = R.string.disclaimer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_disclaimer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolbar()

        screenTitle.setText(R.string.disclaimer)

        txtMarketData.setOnClickListener {
            (activity as? NavigationHost)?.openFragment(DisclaimerDetailFragment.newInstance(DisclaimerDetailFragment.MARKET_DATA), null, addToBackStack = true)
        }
        txtTermsOfUse.setOnClickListener {
            (activity as? NavigationHost)?.openFragment(DisclaimerDetailFragment.newInstance(DisclaimerDetailFragment.TERMS_OF_USE), null, addToBackStack = true)
        }
    }
}
