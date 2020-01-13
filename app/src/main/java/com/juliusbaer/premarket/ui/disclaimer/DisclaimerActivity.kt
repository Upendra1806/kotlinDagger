package com.juliusbaer.premarket.ui.disclaimer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.ui.fragments.extentions.createDefaultConfiguration
import com.juliusbaer.premarket.ui.promotion.PromotionActivity
import com.juliusbaer.premarket.core.viewmodel.ViewModelFactory
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_disclaimer.*
import kotlinx.android.synthetic.main.header_app.*
import kotlinx.android.synthetic.main.include_toolbar.*
import javax.inject.Inject

class DisclaimerActivity : AppCompatActivity(R.layout.activity_disclaimer) {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by viewModels<DisclaimerViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        toolbar.setNavigationIcon(R.drawable.icon_caret_left_white)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        imgDecline.loadUrl("file:///android_asset/index2.html")
        screenTitle.text = getString(R.string.disclaimer)
        btnOk.setOnClickListener {
            viewModel.setFirstTimeValue()

            startActivity(Intent(this, PromotionActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.createDefaultConfiguration())
    }
}