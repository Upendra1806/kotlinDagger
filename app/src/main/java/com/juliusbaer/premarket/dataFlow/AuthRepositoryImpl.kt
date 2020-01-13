package com.juliusbaer.premarket.dataFlow

import com.google.firebase.iid.FirebaseInstanceId
import com.juliusbaer.premarket.dataFlow.network.AuthApi
import com.juliusbaer.premarket.models.serverModels.RegistrationModel
import com.juliusbaer.premarket.core.utils.CoroutinesDispatchers.IO
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val api: AuthApi, private val storage: IUserStorage): AuthRepository {
    override suspend fun registration(email: String?): RegistrationModel {
        return withContext(IO) {
            val userId = if (storage.getUserID() == "") {
                val userId: String = UUID.randomUUID().toString()
                storage.setUserId(userId)
                userId
            } else {
                storage.getUserID()
            }
            val model = api.registration(email, userId, "identity", FirebaseInstanceId.getInstance().token)

            if (model.resetEmail == true) {
                storage.saveEmail(null)
            } else if (!email.isNullOrBlank()) {
                storage.saveEmail(email)
            }
            storage.saveToken(model.accessToken)
            storage.savePublicKey(model.publicKey)
            storage.saveTokenRole(model.role)

            if (model.role == "Subscriber") {
                storage.setIsConfirmed(true)
            } else {
                storage.setIsConfirmed(false)
            }
            model
        }
    }
}