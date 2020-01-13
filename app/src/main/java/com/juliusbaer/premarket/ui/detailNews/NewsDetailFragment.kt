package com.juliusbaer.premarket.ui.detailNews


import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.NetworkStateManager
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.NewsModel
import com.juliusbaer.premarket.stat.StatisticsManager
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.HasOfflinePlaceHolder
import com.juliusbaer.premarket.ui.fragments.extentions.formatDate
import com.juliusbaer.premarket.ui.fragments.extentions.initToolbar
import kotlinx.android.synthetic.main.fragment_news_detail.*
import java.util.*
import javax.inject.Inject


class NewsDetailFragment : BaseNFragment(R.layout.fragment_news_detail), HasOfflinePlaceHolder {
    @Inject
    lateinit var statisticsManager: StatisticsManager

    private val viewModel by viewModels<NewsDetailViewModel> { viewModelFactory }

    private var startTime = 0L

    companion object {
        private const val ARG_COLLECTION_ID = "collectionId"
        private const val ARG_PUSH_ID = "isPush"
        private const val STATE_START_TIME = "start_time"

        fun newInstance(collectionId: Int, isPush: Boolean = false) = NewsDetailFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_COLLECTION_ID, collectionId)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolbar()

        startTime = savedInstanceState?.getLong(STATE_START_TIME) ?: 0

        viewModel.itemLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> populateNewsDetailUI(it.data)
                is Resource.Failure -> {
                    it.data?.let(::populateNewsDetailUI)
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
    }

    private fun populateNewsDetailUI(data: NewsModel) {
        if (startTime == 0L) {
            startTime = System.currentTimeMillis()
        }
        txtNoNews.isVisible = false

        val pubCal = Calendar.getInstance()
        pubCal.timeInMillis = data.publishDate * 1000

        txtDate.text = data.publishDate.formatDate(if (pubCal.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR)) "d MMM, yyyy" else "d MMM")
        screenTitle.text = data.headLine
        txtBody.text = data.newsText
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(STATE_START_TIME, startTime)
    }

    override fun onResume() {
        super.onResume()

        doRequests(NetworkStateManager.isNetworkAvailable(requireContext()))
    }

    private fun doRequests(isOnline: Boolean) {
        if (isOnline || isFirstStart) {
            progressDialog.show()
            viewModel.loadNewsDetail(collectionId)
        }
    }

    override fun onOffline() {
        if (screenTitle.text.isEmpty() && txtBody.text.isEmpty()) {
            super.onOffline()
        }
    }

    override fun onOnline() {
        doRequests(true)
    }

    override fun onDestroyView() {
        if (startTime > 0) {
            statisticsManager.onNewsEnd(collectionId, startTime, if (isPush) 1 else 0)
        }
        super.onDestroyView()
    }
}
