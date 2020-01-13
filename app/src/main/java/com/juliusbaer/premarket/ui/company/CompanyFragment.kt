package com.juliusbaer.premarket.ui.company

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.NetworkStateManager
import com.juliusbaer.premarket.models.ProductType
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.UnderlyingModel
import com.juliusbaer.premarket.stat.StatisticsManager
import com.juliusbaer.premarket.ui.alerts.UnderlyingAlertsActivity
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.HasOfflinePlaceHolder
import com.juliusbaer.premarket.ui.company.news.CompanyNewsFragment
import com.juliusbaer.premarket.ui.company.performance.PerformanceFragment
import com.juliusbaer.premarket.ui.company.warrants.CompanyWarrantsFragment
import com.juliusbaer.premarket.ui.fragments.extentions.initToolbar
import com.juliusbaer.premarket.ui.login.LoginActivity
import kotlinx.android.synthetic.main.fragment_company.*
import kotlinx.android.synthetic.main.header_app.*
import kotlinx.android.synthetic.main.include_toolbar.*
import javax.inject.Inject

class CompanyFragment : BaseNFragment(R.layout.fragment_company), HasOfflinePlaceHolder {
    private val viewModel by viewModels<CompanyVM> { viewModelFactory }

    @Inject
    lateinit var statisticsManager: StatisticsManager

    companion object {
        private const val ARG_COLLECTION_ID = "collectionId"
        private const val ARG_PUSH_ID = "isPush"

        fun newInstance(collectionId: Int? = 0, isPush: Boolean = false) = CompanyFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_COLLECTION_ID, collectionId ?: 0)
                putBoolean(ARG_PUSH_ID, isPush)
            }
        }
    }

    private val collectionId by lazy {
        requireArguments().getInt(ARG_COLLECTION_ID)
    }

    private val isPush by lazy {
        arguments?.getBoolean(ARG_PUSH_ID) ?: false
    }

    private var isImgStarTrue = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolbar()

        toolbar.inflateMenu(R.menu.company)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.call -> {
                    callToTrader()
                    true
                }
                R.id.set_alert -> {
                    if (viewModel.isConfirmed()) {
                        if (NetworkStateManager.isNetworkAvailable(requireContext())) {
                            startActivity(UnderlyingAlertsActivity.newIntent(requireContext(), collectionId, ProductType.UNDERLYING))
                        }
                    } else {
                        context?.startActivity(Intent(context, LoginActivity::class.java))
                    }
                    true
                }
                R.id.add_to_watchlist -> {
                    if (NetworkStateManager.isNetworkAvailable(requireContext())) setAlertState()

                    true
                }
                else -> false
            }
        }
        tabBarCompany.setupWithViewPager(setupViewPager())
        tabBarCompany.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 2) {
                    if (!viewModel.isConfirmed()) {
                        context?.startActivity(Intent(context, LoginActivity::class.java))
                    }
                }
            }

        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.clearTopics()
        viewModel.itemLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> populateUnderlyingUI(it.data)
                is Resource.Failure -> {
                    it.data?.let { data -> populateUnderlyingUI(data) }
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
        viewModel.watchListDelLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            isImgStarTrue = false

            toolbar.menu.findItem(R.id.add_to_watchlist).setIcon(R.drawable.ic_watchlist)
        })
        viewModel.watchListAddLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> {
                    if (!isImgStarTrue) {
                        toolbar.menu.findItem(R.id.add_to_watchlist).setIcon(R.drawable.ic_watchlist_active)
                    }
                    isImgStarTrue = true
                }
                is Resource.Failure -> if (!it.hasBeenHandled) parseError(it.e()!!, true)
            }
        })
        statisticsManager.onProductStart(collectionId, 0, if (isPush) 1 else 0)
    }

    private fun populateUnderlyingUI(data: UnderlyingModel) {
        screenTitle.text = data.name
        isImgStarTrue = data.isInWatchList!!
        if (isImgStarTrue) {
            toolbar.menu.findItem(R.id.add_to_watchlist).setIcon(R.drawable.ic_watchlist_active)
        }
    }

    override fun onResume() {
        super.onResume()
        doRequests(NetworkStateManager.isNetworkAvailable(requireContext()))
    }

    private fun doRequests(isOnline: Boolean) {
        if (isOnline || isFirstStart) {
            progressDialog.show()
            viewModel.loadUnderlyingItemById(collectionId)
        }
    }

    override fun onOnline() {
        doRequests(true)
    }

    override fun onOffline() {
        if (screenTitle.text.isEmpty()) {
            super.onOffline()
        }
    }

    private fun setupViewPager(): ViewPager {
        val titles = arrayOf(R.string.performance, R.string.warrants, R.string.news)
        viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return when (position) {
                    0 -> PerformanceFragment.newInstance(collectionId)
                    1 -> CompanyWarrantsFragment.newInstance(collectionId)
                    2 -> CompanyNewsFragment.newInstance(collectionId)
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

    private fun setAlertState() {
        if (!isImgStarTrue) {
            if (viewModel.isTokenValid()) {
                statisticsManager.onProductStart(collectionId, 1, if (isPush) 1 else 0)
                progressDialog.show()
                viewModel.addWatchListItem(collectionId)
            } else {
                Toast.makeText(context, "Is not authorized", Toast.LENGTH_SHORT).show()
            }
        } else {
            statisticsManager.onProductStart(collectionId, 2, if (isPush) 1 else 0)
            progressDialog.show()
            viewModel.deleteWatchListItemResponse(collectionId)
        }
    }
}


