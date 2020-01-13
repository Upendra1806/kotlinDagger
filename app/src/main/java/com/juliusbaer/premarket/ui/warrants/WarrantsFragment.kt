package com.juliusbaer.premarket.ui.warrants

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.ui.base.HasOfflinePlaceHolder
import com.juliusbaer.premarket.ui.base.NavigationChild
import com.juliusbaer.premarket.ui.company.warrants.CompanyWarrantsFragment
import com.juliusbaer.premarket.ui.fragments.extentions.initToolbar
import kotlinx.android.synthetic.main.header_app.*
import kotlinx.android.synthetic.main.include_company_warrants.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.include_warrants_filter.*

class WarrantsFragment : CompanyWarrantsFragment(R.layout.fragment_warrants), HasOfflinePlaceHolder, NavigationChild {
    override val titleResId: Int
        get() = R.string.warrants

    companion object {
        const val TAG = "warrants"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolbar()
        toolbar.inflateMenu(R.menu.call)
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.call) {
                callToTrader()
                true
            } else {
                false
            }
        }
        screenTitle.setText(R.string.warrants)
    }

    override fun doRequests(isOnline: Boolean) {
        if (isOnline) {
            filterLayout.isVisible = true
        }
        super.doRequests(isOnline)
    }

    override fun onOffline() {
        if (recyclerView.adapter?.itemCount == 0) {
            filterLayout.isVisible = false
            super.onOffline()
        }
    }
}