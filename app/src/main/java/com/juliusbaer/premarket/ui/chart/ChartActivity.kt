package com.juliusbaer.premarket.ui.chart

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.helpers.chart.ChartInterval
import com.juliusbaer.premarket.ui.fragments.extentions.addFragmentSafely
import com.juliusbaer.premarket.ui.fragments.extentions.createDefaultConfiguration
import com.juliusbaer.premarket.utils.Constants
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class ChartActivity : AppCompatActivity(R.layout.activity_menu), HasAndroidInjector {
    companion object {
        private const val EXTRA_ITEM_ID = "ITEM_ID"
        private const val EXTRA_TITLE = "TITLE"
        private const val EXTRA_PRECISION = "PRECISION"
        private const val EXTRA_PERIODS = "PERIODS"
        private const val EXTRA_INTERVAL = "INTERVAL"

        fun newIntent(ctx: Context,
                      itemId: Int,
                      title: String,
                      periods: ArrayList<ChartInterval>,
                      interval: ChartInterval,
                      precision: Int = Constants.PRECISION): Intent {
            return Intent(ctx, ChartActivity::class.java)
                    .putExtra(EXTRA_ITEM_ID, itemId)
                    .putExtra(EXTRA_TITLE, title)
                    .putExtra(EXTRA_PERIODS, periods)
                    .putExtra(EXTRA_INTERVAL, interval)
                    .putExtra(EXTRA_PRECISION, precision)
        }
    }

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        val fragment = LandingChartFragment.newInstance(
                intent.getIntExtra(EXTRA_ITEM_ID, 0),
                intent.getStringExtra(EXTRA_TITLE),
                intent.getSerializableExtra(EXTRA_PERIODS) as ArrayList<ChartInterval>,
                intent.getSerializableExtra(EXTRA_INTERVAL) as ChartInterval,
                intent.getIntExtra(EXTRA_PRECISION, Constants.PRECISION))

        addFragmentSafely(
                fragment,
                "chart",
                addToBackStack = true,
                containerViewId = R.id.activity_chart)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.createDefaultConfiguration())
    }
}
