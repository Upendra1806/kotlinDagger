package com.juliusbaer.premarket.ui.mostActive

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.NetworkStateManager
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.WarrantModel
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.HasOfflinePlaceHolder
import com.juliusbaer.premarket.ui.base.NavigationChild
import com.juliusbaer.premarket.ui.base.NavigationHost
import com.juliusbaer.premarket.ui.detailWarrant.WarrantDetailFragment
import com.juliusbaer.premarket.ui.fragments.extentions.initToolbar
import kotlinx.android.synthetic.main.header_app.*
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlinx.android.synthetic.main.most_active_fragment.*

class MostActiveFragment : BaseNFragment(R.layout.most_active_fragment), HasOfflinePlaceHolder, NavigationChild {
    override val titleResId: Int
        get() = R.string.most_active

    companion object {
        fun newInstance() = MostActiveFragment()
    }

    private val viewModel by viewModels<MostActiveViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        screenTitle.setText(R.string.most_active)
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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.itemsLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> setData(it.data)
                is Resource.Failure -> {
                    it.data?.let { data -> setData(data) }
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
    }

    private fun setData(items: List<WarrantModel>) {
        val adapter = (recyclerMostActive.adapter as? MostActiveAdapter)
                ?: MostActiveAdapter(object : MostActiveAdapter.OnItemClick {
                    override fun onClick(item: WarrantModel) {
                        val warrant = WarrantModel().apply {
                            strikeType = item.strikeType
                            ticker = item.ticker
                            priceBid = item.priceBid
                            priceAsk = item.priceAsk
                            priceChangePct = item.priceChangePct
                            id = item.id
                            isTop = item.isTop
                            valor = item.valor
                            title = item.title
                            strikeLevel = item.strikeLevel
                            exerciseDate = item.exerciseDate
                        }
                        (activity as? NavigationHost)?.openFragment(WarrantDetailFragment.newInstance(
                                item.id,
                                false,
                                warrant), "DetailFragment", addToBackStack = true)
                    }
                }).apply {
                    recyclerMostActive.adapter = this
                }
        adapter.submitList(items)

        val hasItems = items.isNotEmpty()
        recyclerMostActive.isVisible = hasItems
        txtEmpty.isVisible = !hasItems
    }

    override fun onResume() {
        super.onResume()

        doRequests(NetworkStateManager.isNetworkAvailable(requireContext()))
    }

    override fun onOffline() {
        if (recyclerMostActive.adapter?.itemCount == 0) {
            super.onOffline()
        }
    }

    private fun doRequests(isOnline: Boolean) {
        if (isOnline || isFirstStart) {
            progressDialog.show()
            viewModel.loadMostActive()
        }
    }

    override fun onOnline() {
        doRequests(true)
    }
}
