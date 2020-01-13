package com.juliusbaer.premarket.ui.alerts

import com.juliusbaer.premarket.models.DetailWarrModel
import com.juliusbaer.premarket.models.serverModels.FxModel
import com.juliusbaer.premarket.models.serverModels.IndexModel
import com.juliusbaer.premarket.models.serverModels.UnderlyingModel

sealed class AlertItem(val title: String, val lastTraded: Double?) {
    class Underlying(val model: UnderlyingModel) : AlertItem(model.title, model.lastTraded)
    class Index(val model: IndexModel) : AlertItem(model.marketsTitle
            ?: model.title, model.lastTraded)

    class Warrant(val model: DetailWarrModel) : AlertItem(model.title, model.lastTraded)
    class Fx(val model: FxModel) : AlertItem(model.title, model.lastTraded)
}