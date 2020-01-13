package com.juliusbaer.premarket.ui.alerts

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.ui.base.BaseNActivity

import kotlinx.android.synthetic.main.activity_alert_del.*
import timber.log.Timber

class AlertDelActivity : BaseNActivity(R.layout.activity_alert_del) {
    private val viewModel by viewModels<AlertDeleteViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imgBtnClose.setOnClickListener { finish() }
        imgBtnYes.setOnClickListener {
            progressDialog.show()
            viewModel.alertDelete(intent.getIntExtra("productId", 0))

        }
        imgBtnNo.setOnClickListener { finish() }

        viewModel.alertDel.observe(this, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> {
                    Timber.d("Response: ${it.data}")
                    finish()
                }
                is Resource.Failure -> if (!it.hasBeenHandled) parseError(it.e()!!, true)
            }
        })
    }
}
