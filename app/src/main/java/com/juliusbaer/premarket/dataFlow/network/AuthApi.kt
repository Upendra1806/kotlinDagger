package com.juliusbaer.premarket.dataFlow.network

import com.juliusbaer.premarket.models.serverModels.RegistrationModel
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApi {
    @FormUrlEncoded
    @POST("token")
    suspend fun registration(@Field("email") email: String?, @Field("client_id") installationId: String, @Field("grant_type") grant_type: String, @Field("device_token") device_token: String?): RegistrationModel
}