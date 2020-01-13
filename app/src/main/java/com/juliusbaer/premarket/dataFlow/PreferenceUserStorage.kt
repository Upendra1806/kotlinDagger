package com.juliusbaer.premarket.dataFlow

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.core.content.edit
import com.juliusbaer.premarket.BuildConfig
import com.juliusbaer.premarket.BuildConfig.TOKEN
import com.juliusbaer.premarket.helpers.HardwareIdProvider
import com.juliusbaer.premarket.helpers.chart.ChartInterval
import com.juliusbaer.premarket.helpers.encryptor.SecurityController
import javax.inject.Inject
import kotlin.experimental.xor

class PreferenceUserStorage @Inject constructor(var context: Context, val securityController: SecurityController) : IUserStorage {
    private val preference: SharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    init {
        if (preference.getInt(VERSION_CODE, 0) < BuildConfig.VERSION_CODE) {
            preference.edit {
                putInt(VERSION_CODE, BuildConfig.VERSION_CODE)
                remove(PROMOTION_LAST_MODIFIED_DATE)
            }
        }
    }

    override var promotionLastModifiedDate: Long
        get() = preference.getLong(PROMOTION_LAST_MODIFIED_DATE, 0)
        set(value) {
            preference.edit { putLong(PROMOTION_LAST_MODIFIED_DATE, value) }
        }

    override var noInternetAlertShown: Boolean
        get() = preference.getBoolean(NO_INTERNET_ALERT_SHOWN, false)
        set(value) {
            preference.edit {  putBoolean(NO_INTERNET_ALERT_SHOWN, value) }
        }

    override var badgeCount: Int
        get() = preference.getInt(BADGE_COUNT, 0)
        set(value) {
            preference.edit { putInt(BADGE_COUNT, value) }
        }


    override var interval: ChartInterval
        get() = ChartInterval.values().firstOrNull { it.v == preference.getString(INTERVAL, null) }
                ?: ChartInterval.INTRADAY
        set(value) {
            preference.edit { putString(INTERVAL, value.v) }
        }
    override var fxInterval: ChartInterval
        get() = ChartInterval.values().firstOrNull { it.v == preference.getString(FX_INTERVAL, null) }
                ?: ChartInterval.INTRADAY
        set(value) {
            preference.edit { putString(FX_INTERVAL, value.v) }
        }

    override fun savePublicKey(publicKey: String) {
        preference.edit { putString(encrypt(PUBLIC_KEY), encrypt(publicKey)) }
    }

    override fun getPublicKey(): String = decrypt(preference.getString(encrypt(PUBLIC_KEY), encrypt("")))

    override fun getUserID(): String {
        val userId = preference.getString(USER_ID, "")
        //before version 1.1.16 userId was encrypted
        return if (userId != null && !HardwareIdProvider.isValidUUID(userId)) {
            val decrypted = securityController.getDecrypted(userId)
            if (decrypted.isNotEmpty()) {
                setUserId(decrypted)
            }
            decrypted
        } else {
            userId
        }
    }

    override fun setUserId(deviceId: String) {
        preference.edit { putString(USER_ID, deviceId) }
    }

    override fun setAlphabetic(toBoolean: Boolean) {
        preference.edit { putBoolean("Alphabetic", toBoolean) }
    }

    override fun setTop(toBoolean: Boolean) {
        preference.edit { putBoolean("Top", toBoolean) }
    }

    override fun setBoxes(toBoolean: Boolean) {
        preference.edit { putBoolean("Boxes", toBoolean) }
    }

    override fun setCandles(toBoolean: Boolean) {
        preference.edit { putBoolean("Candles", toBoolean) }
    }

    override fun getAlphabetic(): Boolean = preference.getBoolean("Alphabetic", true)

    override fun getTop(): Boolean = preference.getBoolean("Top", false)

    override fun getBoxes(): Boolean = preference.getBoolean("Boxes", true)

    override fun clearFilter() {
        preference.edit {
            remove("topOnly")
            remove("underlyingId")
            remove("contractOption")
            remove("maturityStartDate")
            remove("maturityEndDate")
            remove("strikePriceMin")
            remove("strikePriceMax")
            remove("paginationCount")
            remove("paginationStart")
            remove("categories")
            remove("tradedVolume")
        }
    }

    override fun isConfirmed(): Boolean = preference.getBoolean(IS_CONFIRMED, false)

    override fun setIsConfirmed(confirmed: Boolean) {
        preference.edit { putBoolean(IS_CONFIRMED, confirmed) }
    }

    override fun getTokenRole(): String = preference.getString(TOKEN_ROLE, "")!!

    override fun saveTokenRole(role: String) {
        preference.edit { putString(TOKEN_ROLE, role) }
    }

    override fun saveEmail(email: String?) {
        preference.edit { putString(EMAIL_ADRESS, email) }
    }

    override fun getEmail(): String? = preference.getString(EMAIL_ADRESS, null)

    override fun savePhoneNumber(number: String) {
        preference.edit { putString(PHONE_NUMBER, number) }
    }

    override fun getPhoneNumber(): String? = preference.getString(PHONE_NUMBER, null)

    override fun getToken(): String = decrypt(preference.getString(encrypt(TOKEN), encrypt("")))

    override fun saveToken(token: String) {
        preference.edit { putString(encrypt(TOKEN), encrypt(token)) }
    }

    override fun getFirstTimeLoading(): Boolean = preference.getBoolean(FIRST_LOADING_KEY, true)

    override fun setFirstTimeLoading(firstTime: Boolean) {
        preference.edit { putBoolean(FIRST_LOADING_KEY, firstTime) }
    }

    companion object {
        private const val NAME = "jpa_preference_name"
        private const val INTERVAL = "INTERVAL"
        private const val FX_INTERVAL = "FX_INTERVAL"
        private const val IS_CONFIRMED = "is_confirmed"
        private const val FIRST_LOADING_KEY = "jvi_first_time_loading"
        private const val PHONE_NUMBER = "phone"
        private const val EMAIL_ADRESS = "email"
        private const val BADGE_COUNT = "badge_count"
        private const val TOKEN_ROLE = "token_role"
        private const val USER_ID = "USER_ID"
        private const val PUBLIC_KEY = "PUBLIC_KEY"
        private const val VERSION_CODE = "version_code"
        private const val PROMOTION_LAST_MODIFIED_DATE = "promotionLastModifiedDate"
        private const val NO_INTERNET_ALERT_SHOWN = "noInternetAlertShown"
    }

    fun encrypt(input: String?): String {
        // This is base64 encoding, which is not an encryption
        return Base64.encodeToString(input?.toByteArray(), Base64.DEFAULT)
    }

    fun decrypt(input: String): String {
        return String(Base64.decode(input, Base64.DEFAULT))
    }

    fun encodeXOR(pText: String, pKey: String): ByteArray {
        val txt = pText.toByteArray()
        val key = pKey.toByteArray()
        val res = ByteArray(pText.length)

        for (i in txt.indices) {
            res[i] = (txt[i] xor key[i % key.size])
        }

        return res
    }

    fun decodeXOR(pText: ByteArray, pKey: String): String {
        val res = ByteArray(pText.size)
        val key = pKey.toByteArray()

        for (i in pText.indices) {
            res[i] = pText[i] xor key[i % key.size]
        }

        return String(res)
    }
}