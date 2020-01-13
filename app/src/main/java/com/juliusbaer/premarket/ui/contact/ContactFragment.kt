package com.juliusbaer.premarket.ui.contact

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.core.net.toUri
import com.juliusbaer.premarket.BuildConfig
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.fragments.extentions.initToolbar
import kotlinx.android.synthetic.main.dialog_message.*
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.header_app.*


class ContactFragment : BaseNFragment(R.layout.fragment_contact) {
    companion object {
        const val EMAIL_COMPANY = "derivatives@juliusbaer.com"
        const val SITE_COMPANY = "https://derivatives.juliusbaer.com"
        const val STRUCTURE_NUMBER = "+41 (0)58 888 81 81"
        const val PREMARKET_NUMBER = "+41 (0)58 888 8680"

        const val TOLL_FREE_NNUMBER = "+800 0800 45 45"
        const val STANDARD_RATES_NUMBER = "+41 (0)58 888 45 45"
    }

    private val callToPremarket = object : ClickableSpan() {
        override fun onClick(view: View) {
            confirmCall(PREMARKET_NUMBER)
        }
    }

    private val callToStructure = object : ClickableSpan() {
        override fun onClick(view: View) {
            confirmCall(STRUCTURE_NUMBER)
        }
    }

    private val callToTollFree = object : ClickableSpan() {
        override fun onClick(view: View) {
            confirmCall(TOLL_FREE_NNUMBER)
        }
    }

    private val callToStandardRates = object : ClickableSpan() {
        override fun onClick(view: View) {
            confirmCall(STANDARD_RATES_NUMBER)
        }
    }

    private fun confirmCall(number: String) {
        val dialog = Dialog(requireContext())
        with(dialog) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_message)

            message.text = number
            no.setText(R.string.cancel)
            no.setOnClickListener { dismiss() }
            yes.setText(R.string.call)
            yes.setOnClickListener {
                dismiss()
                callPhone(number)
            }
            show()
        }
    }

    private val privacyPolicyClick = object : ClickableSpan() {
        override fun onClick(view: View) {
            val intent = Intent(Intent.ACTION_SENDTO, "mailto:".toUri())
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(EMAIL_COMPANY))
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            }
        }
    }

    private val openURLSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            startActivity(Intent(Intent.ACTION_VIEW, SITE_COMPANY.toUri()))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbar()
        setSpannableText()
        txtStructureProduct5.text = "Version: ${BuildConfig.VERSION_NAME}"
    }

    private fun setSpannableText() {
        makeLinks(txtStructureProduct, arrayOf(STRUCTURE_NUMBER, EMAIL_COMPANY, SITE_COMPANY), arrayOf(callToStructure, privacyPolicyClick, openURLSpan))
        makeLinks(txtStructureProduct4, arrayOf(TOLL_FREE_NNUMBER, STANDARD_RATES_NUMBER), arrayOf(callToTollFree, callToStandardRates))
        makeLinks(txtPremarket, arrayOf(PREMARKET_NUMBER), arrayOf(callToPremarket))
    }

    private fun setToolbar() {
        initToolbar()
        screenTitle.setText(R.string.contact)
    }

    private fun makeLinks(textView: TextView, links: Array<String>, clickableSpans: Array<ClickableSpan>) {
        val spannableString = SpannableString(textView.text)
        links.forEach { i ->
            val clickableSpan = clickableSpans[links.indexOf(i)]
            val startIndexOfLink = textView.text.toString().indexOf(i)
            spannableString.setSpan(clickableSpan, startIndexOfLink, startIndexOfLink + i.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        with(textView) {
            movementMethod = LinkMovementMethod.getInstance()
            setText(spannableString, TextView.BufferType.SPANNABLE)
        }
    }

}
