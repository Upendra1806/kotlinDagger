package com.juliusbaer.premarket.ui.watchlist


import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.NetworkStateManager
import com.juliusbaer.premarket.models.ProductType
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.FxType
import com.juliusbaer.premarket.ui.alerts.UnderlyingAlertsActivity
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.HasOfflinePlaceHolder
import com.juliusbaer.premarket.ui.base.NavigationChild
import com.juliusbaer.premarket.ui.base.NavigationHost
import com.juliusbaer.premarket.ui.company.CompanyFragment
import com.juliusbaer.premarket.ui.detailWarrant.WarrantDetailFragment
import com.juliusbaer.premarket.ui.fragments.extentions.initToolbar
import com.juliusbaer.premarket.ui.indexDetail.IndexDetailFragment
import com.juliusbaer.premarket.ui.markets.fx.FxDetailFragment
import com.juliusbaer.premarket.utils.UiUtils
import com.juliusbaer.premarket.utils.UiUtils.hideKeyboard
import kotlinx.android.synthetic.main.fragment_watchlist.*
import kotlinx.android.synthetic.main.header_app.*
import kotlinx.android.synthetic.main.include_toolbar.*
import me.leolin.shortcutbadger.ShortcutBadger

class WatchlistFragment : BaseNFragment(R.layout.fragment_watchlist), HasOfflinePlaceHolder, NavigationChild {
    companion object {
        const val TAG = "Watchlist"
        private const val STATE_LIST = "listState"
    }

    private var isOpened: Boolean = false

    override val titleResId: Int
        get() = R.string.watchlist

    private val viewModel by viewModels<WatchlistVM> { viewModelFactory }

    private lateinit var adapter: WatchlistAdapter
    private var groups = listOf<List<WatchListItem>>()

    private var listState: Parcelable? = null

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
        editTextSearch.viewTreeObserver.addOnGlobalLayoutListener(keyboardLayoutListener)
        editTextSearch.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                cancel.isVisible = true
            }
        }
        editTextSearch.setOnClickListener {
            if (it.hasFocus() && (it as EditText).text.isNullOrEmpty()) {
                cancel.isVisible = true
            }
        }
        editTextSearch.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    hideKeyboard(editTextSearch)
                    true
                }
                else -> false
            }
        }
        editTextSearch.addTextChangedListener(editTextWatcher)

        screenTitle.setText(R.string.watchlist)

        clear.setOnClickListener {
            editTextSearch.setText("")
        }
        cancel.setOnClickListener {
            cancel.isVisible = false

            hideKeyboard(editTextSearch)
            editTextSearch.setText("")
            editTextSearch.clearFocus()
        }
        listState = savedInstanceState?.getParcelable(STATE_LIST)
        adapter = WatchlistAdapter(object : WatchlistAdapter.OnExpandableClickListener {
            override fun onDelete(id: Int) {
                DeleteWatchlistDialog.newInstance(id).show(childFragmentManager, "deleteWatchlist")
            }

            override fun onSetAlert(item: WatchListItem) {
                val productType = when (item) {
                    is WatchListItem.Underlying -> ProductType.UNDERLYING
                    is WatchListItem.Index -> ProductType.INDEX
                    is WatchListItem.Warrant -> ProductType.WARRANT
                    is WatchListItem.Fx -> if (item.model.type == FxType.METAL.ordinal) ProductType.METAL else ProductType.CURRENCY
                }
                startActivity(UnderlyingAlertsActivity.newIntent(requireContext(), item.id, productType, (item as? WatchListItem.Fx)?.model?.precision))
            }

            override fun onItemClick(item: WatchListItem) {
                when (item) {
                    is WatchListItem.Underlying -> (activity as? NavigationHost)?.openFragment(CompanyFragment.newInstance(item.id, false), null, true, R.style.FragStyle)
                    is WatchListItem.Index -> (activity as? NavigationHost)?.openFragment(IndexDetailFragment.newInstance(item.id, false, item.model), null, true, R.style.FragStyle)
                    is WatchListItem.Warrant -> (activity as? NavigationHost)?.openFragment(WarrantDetailFragment.newInstance(item.id), null, addToBackStack = true)
                    is WatchListItem.Fx -> (activity as? NavigationHost)?.openFragment(FxDetailFragment.newInstance(item.id, FxType.values()[item.model.type!!], false, item.model), null, addToBackStack = true)
                }
            }
        })
        recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        editTextSearch.viewTreeObserver.removeOnGlobalLayoutListener(keyboardLayoutListener)
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.watchListItemDel.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> {}
                is Resource.Failure -> if (!it.hasBeenHandled) parseError(it.e()!!, true)
            }
        })
        viewModel.itemLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()

            when (it) {
                is Resource.Success -> {
                    if (it.data.any { it.isNotEmpty() }) {
                        groups = it.data
                        adapter.submitList(groups)

                        listState?.let {
                            (recyclerView.layoutManager as LinearLayoutManager).onRestoreInstanceState(listState)
                            listState = null
                        }
                        viewModel.subscribeToRtUpdates(it.data.flatten().map { it.id })
                    } else {
                        groups = emptyList()
                        viewModel.unsubscribeFromRtUpdates()
                    }
                    checkDataUI()
                }
                is Resource.Failure -> {
                    txtEmpty.isVisible = true
                    progressDialog.dismiss()
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
        viewModel.productModelLiveData.observe(viewLifecycleOwner, Observer { productModel ->
            val model = adapter.updateItem(productModel)
            if (model != null) {
                when (model) {
                    is WatchListItem.Underlying -> viewModel.updateUnderlying(productModel)
                    is WatchListItem.Warrant -> viewModel.updateWarrant(productModel)
                    is WatchListItem.Index -> viewModel.updateIndex(productModel)
                    is WatchListItem.Fx -> viewModel.updateFxItem(productModel)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

        ShortcutBadger.applyCount(context, 0)
        storage.badgeCount = 0

        doRequests(NetworkStateManager.isNetworkAvailable(requireContext()))
    }

    private fun doRequests(isOnline: Boolean) {
        if (isOnline) viewModel.resubscribeToRtUpdates()

        if (isOnline || isFirstStart) {
            progressDialog.show()
            viewModel.getWatchListAnswer()
        }
    }

    override fun onOnline() {
        doRequests(true)
    }

    override fun onOffline() {
        if (groups.isEmpty()) {
            super.onOffline()
        }
    }

    private fun checkDataUI() {
        val hasGroups = groups.isNotEmpty()
        if (!hasGroups) {
            txtEmpty.setText(R.string.your_watch_list_is_empty)
        }
        txtEmpty.isVisible = !hasGroups
        recyclerView.isVisible = hasGroups
    }

    private val keyboardLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        if (UiUtils.keyboardShown(editTextSearch.rootView)) {
            isOpened = true
        } else if (isOpened) {
            isOpened = false
            if (editTextSearch.text.isEmpty()) cancel.isVisible = false
        }
    }

    private val editTextWatcher = object : TextWatcher {
        private var originalText: CharSequence? = null

        override fun afterTextChanged(s: Editable) {
            val searchQuery = s.toString()
            if (searchQuery.trim() == "") {
                clear.isVisible = false

                originalText = ""
                adapter.submitList(groups)

                checkDataUI()
            } else {
                val tmp = groups.map {
                    it.filter { el ->
                        el.title.contains(s, true)
                                || el.valor?.contains(searchQuery, true) == true
                                || el.ticker?.contains(searchQuery, true) == true
                                || el.isin?.contains(searchQuery, true) == true
                    }
                }
                val hasResults = tmp.any { it.isNotEmpty() }
                if (!hasResults) {
                    setSearchTextInternal(originalText)
                    return
                }
                originalText = searchQuery
                clear.isVisible = true

                if (hasResults) {
                    recyclerView.isVisible = true
                    txtEmpty.isVisible = false
                } else {
                    recyclerView.isVisible = false
                    txtEmpty.isVisible = true
                    txtEmpty.setText(R.string.no_results)
                }
                adapter.submitList(tmp)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }

    private fun setSearchTextInternal(originalText: CharSequence?) {
        editTextSearch.removeTextChangedListener(editTextWatcher)
        editTextSearch.setText(originalText)
        editTextSearch.setSelection(originalText?.length ?: 0)
        editTextSearch.addTextChangedListener(editTextWatcher)
    }

    override fun onPause() {
        viewModel.unsubscribeFromRtUpdates()
        super.onPause()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        recyclerView?.let {
            outState.putParcelable(STATE_LIST, (it.layoutManager as LinearLayoutManager).onSaveInstanceState())
        }
    }

    private fun deleteWatchlist(id: Int) {
        progressDialog.show()
        viewModel.deleteWatchListItem(id)
    }

    class DeleteWatchlistDialog : DialogFragment() {
        companion object {
            private const val ARG_WATCHLIST_ID = "watchlist_id"

            fun newInstance(id: Int): DeleteWatchlistDialog {
                return DeleteWatchlistDialog().apply {
                    arguments = bundleOf(ARG_WATCHLIST_ID to id)
                }
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(requireContext())
                    .setMessage(R.string.are_you_sure_delete_from_watchlist)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        (parentFragment as? WatchlistFragment)?.deleteWatchlist(requireArguments().getInt(ARG_WATCHLIST_ID))
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
        }
    }
}
