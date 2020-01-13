package com.juliusbaer.premarket.dataFlow

import com.juliusbaer.premarket.models.serverModels.RegistrationModel

interface AuthRepository {
    suspend fun registration(email: String?): RegistrationModel
}