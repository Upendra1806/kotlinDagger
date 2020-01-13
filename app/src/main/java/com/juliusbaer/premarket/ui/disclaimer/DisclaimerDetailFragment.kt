package com.juliusbaer.premarket.ui.disclaimer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.ui.fragments.extentions.initToolbar
import kotlinx.android.synthetic.main.fragment_disclaimer_detail.*
import kotlinx.android.synthetic.main.header_app.*

class DisclaimerDetailFragment : Fragment() {
    private val disclaimerType by lazy { requireArguments().getInt(ARG_DISCLAIMER_TYPE) }

    companion object {
        const val TERMS_OF_USE = 1
        const val MARKET_DATA = 2
        private const val ARG_DISCLAIMER_TYPE = "ARG_DISCLAIMER_TYPE"

        fun newInstance(disclaimerType: Int): DisclaimerDetailFragment {
            val fragment = DisclaimerDetailFragment()
            val args = Bundle()
            args.putInt(ARG_DISCLAIMER_TYPE, disclaimerType)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_disclaimer_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initToolbar()

        when (disclaimerType) {
            TERMS_OF_USE -> {
                disclaimerView.loadUrl("file:///android_asset/index2.html")
                screenTitle.setText(R.string.terms_of_use)
            }
            MARKET_DATA -> {
                disclaimerView.loadUrl("file:///android_asset/index1.html")
                screenTitle.setText(R.string.market_data)
            }
        }
    }
}