package com.juliusbaer.premarket.ui.promotion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.core.viewmodel.ViewModelFactory
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.PromotionInfoModel
import com.juliusbaer.premarket.ui.fragments.extentions.createDefaultConfiguration
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_promotion.*
import javax.inject.Inject

class PromotionActivity : AppCompatActivity(R.layout.activity_promotion) {
    companion object {
        var promotionModel: PromotionInfoModel? = null
    }

    private val viewModel by viewModels<PromotionViewModel> { viewModelFactory }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        viewModel.promotionLiveData.observe(this, Observer {
            if (it is Resource.Success) {
                it.data?.let { promotionInfo -> showPromotion(promotionInfo) } ?: run { finish() }
            }
        })

        imgClose.setOnClickListener {
            finish()
        }
        promotionModel?.let {
            showPromotion(it)
            promotionModel = null
        } ?: run {
            viewModel.getPromotion()
        }
    }

    private fun showPromotion(promotionInfo: PromotionInfoModel) {
        val title = promotionInfo.title

        if (!title.isNullOrEmpty()) {
            txtHeader.text = promotionInfo.title
            txtHeader.isVisible = true
        } else {
            txtHeader.isVisible = false
        }
        val fontStyleHtml = """<style>
                                @font-face {
                                    font-family: 'verlag_light';
                                    src: url('fonts/verlag_light.otf');
                                }
                                body {
                                    font-family: 'verlag_light';
                                    font-size: 10.4pt;
                                }</style>"""
        val description = promotionInfo.formattedDescription ?: fontStyleHtml
        promotionInfo.description?.replace("\n", "<br>") ?: ""
        txtBody.loadDataWithBaseURL("file:///android_asset/", description, "text/html; charset=utf-8", "UTF-8", "")

        val buttonText = promotionInfo.buttonText
        if (promotionInfo.showButton == true && !buttonText.isNullOrEmpty()) {
            txtMoreInfo.text = buttonText
            txtMoreInfo.isVisible = true

            val buttonLink = promotionInfo.link
            txtMoreInfo.setOnClickListener {
                if (!buttonLink.isNullOrEmpty()) {
                    val url = if (buttonLink.startsWith("http", ignoreCase = true)) {
                        buttonLink
                    } else {
                        "http://$buttonLink"
                    }
                    startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                } else {
                    finish()
                }
            }
        } else {
            txtMoreInfo.isVisible = false
        }
        if (!promotionInfo.imageUrl.isNullOrEmpty()) {
            imgHeader.isVisible = true
            Glide.with(this)
                    .load(promotionInfo.imageUrl)
                    .apply(RequestOptions.skipMemoryCacheOf(true))
                    .into(imgHeader)
        } else {
            imgHeader.isVisible = false
        }
    }

    override fun onBackPressed() {
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.createDefaultConfiguration())
    }
}
