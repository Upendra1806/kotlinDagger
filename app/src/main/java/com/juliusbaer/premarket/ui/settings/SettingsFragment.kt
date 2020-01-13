package com.juliusbaer.premarket.ui.settings


import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.helpers.chart.ChartInterval
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.HasOfflinePlaceHolder
import com.juliusbaer.premarket.ui.fragments.extentions.initToolbar
import com.juliusbaer.premarket.ui.fragments.extentions.toast
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.header_app.*
import timber.log.Timber


class SettingsFragment : BaseNFragment(R.layout.fragment_settings), HasOfflinePlaceHolder {
    companion object {
        private const val STATE_LIST = "listState"
    }

    private val viewModel by viewModels<SettingsVM> { viewModelFactory }
    private lateinit var adapter: SettingsAdapter

    private var isSwitchAlertChecked: Boolean = false
    private var isSwitchNewsChecked: Boolean = false
    private var phoneNumber: String? = null

    private var listState: Parcelable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolbar()
        screenTitle.setText(R.string.settings)

        listState = savedInstanceState?.getParcelable(STATE_LIST)
        adapter = SettingsAdapter(
                storage.interval,
                storage.fxInterval,
                object : SettingsAdapter.OnSettingsClickListener {
                    override fun saveFxInterval(interval: ChartInterval) {
                        viewModel.saveFxInterval(interval)
                    }

                    override fun saveInterval(interval: ChartInterval) {
                        viewModel.saveInterval(interval)
                    }
                    override fun onSavePhone(number: String) {
                        phoneNumber = number
                        putUserInfo(SettingsVM.SettingsOption.PHONE)
                    }

                    override fun onNewsChecked(isChecked: Boolean) {
                        isSwitchNewsChecked = isChecked
                        putUserInfo(SettingsVM.SettingsOption.NEWS)
                    }

                    override fun onNotificationsChecked(isChecked: Boolean) {
                        isSwitchAlertChecked = isChecked
                        putUserInfo(SettingsVM.SettingsOption.NOTIFICATIONS)
                    }
                })
        recycleView.adapter = adapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        phoneNumber = storage.getPhoneNumber()
        viewModel.userInfoUpdateLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> if (it.data == SettingsVM.SettingsOption.PHONE) {
                    requireContext().toast(R.string.successfully_saved)
                }
                is Resource.Failure -> if (!it.hasBeenHandled) Timber.e(it.e()!!)
            }
        })
        viewModel.userInfoLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> {
                    if ((it.data).isNotificationForAlertsEnable != null) {
                        isSwitchAlertChecked = it.data.isNotificationForAlertsEnable!!
                    }
                    if ((it.data).isNotificationForNewsEnable != null) {
                        isSwitchNewsChecked = it.data.isNotificationForNewsEnable!!
                    }
                    phoneNumber = it.data.traderPhoneNumber
                    adapter.userInfoModel = it.data
                    adapter.notifyDataSetChanged()

                    listState?.let {
                        (recycleView.layoutManager as LinearLayoutManager).onRestoreInstanceState(listState)
                        listState = null
                    }
                }
                is Resource.Failure -> if (!it.hasBeenHandled) parseError(it.e()!!)
            }
        })
        doRequests()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        recycleView?.let {
            outState.putParcelable(STATE_LIST, (it.layoutManager as LinearLayoutManager).onSaveInstanceState())
        }
    }

    override fun onOnline() {
        if (adapter.userInfoModel == null) {
            doRequests()
        }
    }

    private fun doRequests() {
        progressDialog.show()
        viewModel.getUserInfo()
    }

    override fun onStart() {
        super.onStart()

        adapter.notifyItemChanged(0, SettingsAdapter.PAYLOAD_NOTIFICATIONS)
    }

    private fun putUserInfo(type: SettingsVM.SettingsOption) {
        progressDialog.show()
        listState = (recycleView.layoutManager as LinearLayoutManager).onSaveInstanceState()
        viewModel.putUserInfoRequest(
                type,
                isSwitchAlertChecked,
                isSwitchNewsChecked,
                1,
                1,
                phoneNumber ?: "")
    }
}


