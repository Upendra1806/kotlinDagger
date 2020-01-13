package com.juliusbaer.premarket.ui.login

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.core.viewmodel.ViewModelFactory
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.ui.fragments.extentions.createDefaultConfiguration
import com.juliusbaer.premarket.ui.main.MainActivity
import com.juliusbaer.premarket.utils.UiUtils
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber
import javax.inject.Inject


class LoginActivity : AppCompatActivity(R.layout.activity_login) {
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var storage: IUserStorage

    private val viewModel by viewModels<LoginViewModel> { viewModelFactory }

    private lateinit var dialog: ProgressDialog
    private var isFirst: Boolean = true
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        dialog = ProgressDialog(this).apply {
            isIndeterminate = true
            setMessage("Please wait a bit…")
        }
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        btnClose.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        etLogin.setOnEditorActionListener { _, _, _ ->
            login()
            true
        }
        btnLogin.setOnClickListener {
            if (!etLogin.text.isNullOrEmpty() and isValidEmail(etLogin.text.toString())) {
                isFirst = false
                txtLogin.setText(R.string.go_to_premarket)
                if (!dialog.isShowing) {
                    dialog.show()
                }
                login()
            } else {
                AlertDialog.Builder(this)
                        .setMessage(R.string.incorrect_email)
                        .setPositiveButton(R.string.ok, null)
                        .show()
            }
        }
        viewModel.loginLiveData.observe(this, Observer {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            when (it) {
                is Resource.Success -> {
                    when (it.data) {
                        "Subscriber" -> finish()
                        "NotConfirmed" -> btnLogin.setOnClickListener {
                            login()
                        }
                    }
                }
                is Resource.Failure -> {
                    val error = it.e()!!

                    Timber.e(error)
                    UiUtils.parseError(error, this, supportFragmentManager, storage, true) {}
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        login()
    }

    private fun login() {
        if (etLogin.text.isBlank()) return

        if (isValidEmail(etLogin.text.toString())) {
            txtLogin.setText(R.string.go_to_premarket)

            if (isFirst) {
                isFirst = false
                showEmailSentDialog()
            } else {
                handler.postDelayed({
                    showEmailSentDialog()
                }, 3000)
            }
            if (!dialog.isShowing) dialog.show()
            viewModel.login(etLogin.text.toString())
        } else {
            AlertDialog.Builder(this)
                    .setMessage(R.string.incorrect_email)
                    .setPositiveButton(R.string.ok, null)
                    .show()
        }
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(etLogin.windowToken, 0)
    }

    private fun showEmailSentDialog() {
        AlertDialog.Builder(this)
                .setMessage(R.string.we_have_sent_you_email)
                .setPositiveButton(R.string.ok) { _, _ ->
                    if (!dialog.isShowing) dialog.show()
                    viewModel.login(etLogin.text.toString())
                }
                .show()
    }

    override fun onPause() {
        if (isFinishing || isChangingConfigurations) {
            handler.removeCallbacksAndMessages(null)
        }
        super.onPause()
    }

    private fun isValidEmail(target: CharSequence): Boolean = android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.createDefaultConfiguration())
    }
}
