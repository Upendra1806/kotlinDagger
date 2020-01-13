package com.juliusbaer.premarket.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.ui.alerts.AlertsFragment
import com.juliusbaer.premarket.ui.base.NavigationChild
import com.juliusbaer.premarket.ui.base.NavigationHost
import com.juliusbaer.premarket.ui.contact.ContactFragment
import com.juliusbaer.premarket.ui.disclaimer.DisclaimerFragment
import com.juliusbaer.premarket.ui.mostActive.MostActiveFragment
import com.juliusbaer.premarket.ui.settings.SettingsFragment
import com.juliusbaer.premarket.ui.warrants.WarrantsFragment
import kotlinx.android.synthetic.main.fragment_more.*
import kotlinx.android.synthetic.main.header_app.*

class MoreFragment: Fragment(), NavigationChild {
    companion object {
        const val TAG = "more"
    }

    override val titleResId: Int
        get() = R.string.more

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        screenTitle.setText(R.string.more)
        moreList.setHasFixedSize(true)
        moreList.adapter = MoreAdapter(listOf(
                ItemMenu(R.string.most_active, R.drawable.most_tab_btn),
                ItemMenu(R.string.warrants, R.drawable.warrants_tab_btn),
                ItemMenu(R.string.alerts, R.drawable.alerts_tab_btn),
                ItemMenu(R.string.settings, R.drawable.settings_tab_btn),
                ItemMenu(R.string.contact, R.drawable.contacts_tab_btn),
                ItemMenu(R.string.disclaimer, R.drawable.disclaimer_tab_btn)
        )) { pos ->
            when (pos) {
                0 -> (activity as NavigationHost).openFragment(MostActiveFragment(), null,  true)
                1 -> (activity as NavigationHost).openFragment(WarrantsFragment(), null, true)
                2 -> (activity as NavigationHost).openFragment(AlertsFragment(), null,  true)
                3 -> (activity as NavigationHost).openFragment(SettingsFragment(), null, true)
                4 -> (activity as NavigationHost).openFragment(ContactFragment(), null,  true)
                5 -> (activity as NavigationHost).openFragment(DisclaimerFragment(), null, true)
            }
        }
    }
}