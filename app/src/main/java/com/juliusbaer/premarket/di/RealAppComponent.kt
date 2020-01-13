package com.juliusbaer.premarket.di

import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.jobs.SendStatisticsJob

interface RealAppComponent {
    fun inject(sendStatisticsJob: SendStatisticsJob)
    fun inject(iUserStorage: IUserStorage)
}
