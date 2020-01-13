package com.juliusbaer.premarket.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.helpers.chart.ChartInterval
import com.juliusbaer.premarket.models.serverModels.AlertsModel
import com.juliusbaer.premarket.models.serverModels.UserInfoModel
import kotlinx.android.synthetic.main.notification_layout.view.*
import kotlinx.android.synthetic.main.settings_header.view.*


class SettingsAdapter(private var chartInterval: ChartInterval,
                      private var chartIntervalFx: ChartInterval,
                      private val listener: OnSettingsClickListener) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {
    companion object {
        private const val TYPE_FIXED_OPTIONS = 1

        const val PAYLOAD_NOTIFICATIONS = 1
    }

    var userInfoModel: UserInfoModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return HeaderHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.settings_header, parent, false))
    }

    override fun getItemCount(): Int = if (userInfoModel != null) 1 else 0

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return TYPE_FIXED_OPTIONS
        }
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            when (holder) {
                is HeaderHolder -> {
                    when (payloads[0]) {
                        PAYLOAD_NOTIFICATIONS -> {
                            return holder.bindNotificationSettings()
                        }
                    }
                }
            }
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is HeaderHolder) {
            userInfoModel?.let { holder.bind(it, chartInterval, chartIntervalFx) }
        }
    }

    private inner class HeaderHolder(itemView: View) : ViewHolder(itemView) {
        private val phoneNumber = itemView.phoneNumber
        private val btnSave = itemView.btnSave
        private val switchNotification = itemView.switchAlert2

        private val switchNews = itemView.switchNews2

        private val chartInterval = itemView.chartInterval
        private val chartIntervalFx = itemView.chartIntervalFx

        private val textView34 = itemView.textView34
        private val switchAlert2 = itemView.switchAlert2
        private val img4 = itemView.img4
        private val txtNews2 = itemView.txtNews2
        private val switchNews2 = itemView.switchNews2
        private val notificationsDisabled = itemView.notificationsDisabled

        init {
            notificationsDisabled.setOnClickListener {
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, itemView.context.packageName)
                    }
                } else {
                    Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
                        putExtra("app_package", itemView.context.packageName)
                        putExtra("app_uid", itemView.context.applicationInfo.uid)
                    }
                }
                itemView.context.startActivity(intent)
            }
            chartInterval.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.switchIntraday -> listener.saveInterval(ChartInterval.INTRADAY)
                    R.id.switchThreemonth -> listener.saveInterval(ChartInterval.THREE_MONTH)
                    R.id.switchSixMonths -> listener.saveInterval(ChartInterval.SIX_MONTH)
                    R.id.switchOneYear -> listener.saveInterval(ChartInterval.YEAR)
                }
            }
            chartIntervalFx.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.switchIntradayFx -> listener.saveFxInterval(ChartInterval.INTRADAY)
                    R.id.switchOneMonthFx -> listener.saveFxInterval(ChartInterval.ONE_MONTH)
                    R.id.switchThreeMonthsFx -> listener.saveFxInterval(ChartInterval.THREE_MONTH)
                    R.id.switchSixMonthsFx -> listener.saveFxInterval(ChartInterval.SIX_MONTH)
                }
            }
        }

        fun bind(userInfoModel: UserInfoModel, interval: ChartInterval, fxInterval: ChartInterval) {
            when (interval) {
                ChartInterval.INTRADAY -> chartInterval.check(R.id.switchIntraday)
                ChartInterval.THREE_MONTH -> chartInterval.check(R.id.switchThreemonth)
                ChartInterval.SIX_MONTH -> chartInterval.check(R.id.switchSixMonths)
                ChartInterval.YEAR -> chartInterval.check(R.id.switchOneYear)
            }
            when (fxInterval) {
                ChartInterval.INTRADAY -> chartIntervalFx.check(R.id.switchIntradayFx)
                ChartInterval.ONE_MONTH -> chartIntervalFx.check(R.id.switchOneMonthFx)
                ChartInterval.THREE_MONTH -> chartIntervalFx.check(R.id.switchThreeMonthsFx)
                ChartInterval.SIX_MONTH -> chartIntervalFx.check(R.id.switchSixMonthsFx)
            }
            bindNotificationSettings()

            if (userInfoModel.isNotificationForAlertsEnable!!) {
                switchNotification.isChecked = true
            }
            if (userInfoModel.isNotificationForNewsEnable!!) {
                switchNews.isChecked = true
            }

            phoneNumber.setText(userInfoModel.traderPhoneNumber, TextView.BufferType.EDITABLE)
            phoneNumber.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    listener.onSavePhone(phoneNumber.text.toString())
                    false
                } else
                    false
            }
            btnSave.setOnClickListener {
                listener.onSavePhone(phoneNumber.text.toString())
                val imm = phoneNumber.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(btnSave.windowToken, 0)
            }
            switchNotification.setOnCheckedChangeListener { _, isChecked ->
                listener.onNotificationsChecked(isChecked)

            }
            switchNews.setOnCheckedChangeListener { _, isChecked ->
                listener.onNewsChecked(isChecked)
            }
        }

        fun bindNotificationSettings() {
            val isNotificationsEnabled = NotificationManagerCompat.from(itemView.context).areNotificationsEnabled()
            textView34.isVisible = isNotificationsEnabled
            switchAlert2.isVisible = isNotificationsEnabled
            img4.isVisible = isNotificationsEnabled
            txtNews2.isVisible = isNotificationsEnabled
            switchNews2.isVisible = isNotificationsEnabled
            notificationsDisabled.isVisible = !isNotificationsEnabled
        }
    }

    open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        open fun bind(newsModel: AlertsModel) {
        }
    }

    interface OnSettingsClickListener {
        fun onSavePhone(number: String)
        fun onNewsChecked(isChecked: Boolean)
        fun onNotificationsChecked(isChecked: Boolean)
        fun saveInterval(interval: ChartInterval)
        fun saveFxInterval(interval: ChartInterval)
    }
}