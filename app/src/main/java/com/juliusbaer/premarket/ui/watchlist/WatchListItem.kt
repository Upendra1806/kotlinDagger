package com.juliusbaer.premarket.ui.watchlist

import com.juliusbaer.premarket.models.serverModels.FxModel
import com.juliusbaer.premarket.models.serverModels.IndexModel
import com.juliusbaer.premarket.models.serverModels.UnderlyingModel
import com.juliusbaer.premarket.models.serverModels.WarrantModel

sealed class WatchListItem(
    val id: Int,
    val title: String,
    val valor: String?,
    val ticker: String?,
    val isin: String?,
    val notificationReceived: Boolean?) {

    class Underlying(val model: UnderlyingModel) : WatchListItem(model.id, model.title, model.valor, model.ticker, model.isin, model.notificationReceived)
    class Index(val model: IndexModel) : WatchListItem(model.id, model.marketsTitle?: model.title, model.valor, model.ticker, model.isin, model.notificationReceived)

    class Warrant(val model: WarrantModel) : WatchListItem(model.id, model.title, model.valor, model.ticker, model.isin, model.notificationReceived)
    class Fx(val model: FxModel) : WatchListItem(model.id, model.title, model.valor, model.ticker, null, model.notificationReceived)
}
