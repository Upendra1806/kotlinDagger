package com.juliusbaer.premarket.helpers.encryptor


import android.util.Base64
import com.juliusbaer.premarket.helpers.encryptor.SecurityController.Companion.CIPHER_TYPE
import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.CipherInputStream


class DeCryptor {
    fun decryptString(keyStore: KeyStore, alias: String, cipherText: String): String {
        try {
            val privateKeyEntry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
            val privateKey = privateKeyEntry.privateKey

            val output = Cipher.getInstance(CIPHER_TYPE)
            output.init(Cipher.DECRYPT_MODE, privateKey)

            val cipherInputStream = CipherInputStream(
                    ByteArrayInputStream(Base64.decode(cipherText, Base64.DEFAULT)), output)
            val values = ArrayList<Byte>()
            var nextByte = cipherInputStream.read()

            while (nextByte != -1) {

                values.add(nextByte.toByte())
                nextByte = cipherInputStream.read()
            }

            val bytes = ByteArray(values.size)
            for (i in bytes.indices) {
                bytes[i] = values[i]
            }

            return String(bytes, 0, bytes.size, Charset.forName("UTF-8"))

        } catch (e: Exception) {
            return cipherText
        }

    }

}