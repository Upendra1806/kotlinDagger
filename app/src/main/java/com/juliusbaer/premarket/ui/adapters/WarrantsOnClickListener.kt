package com.juliusbaer.premarket.ui.adapters

import com.juliusbaer.premarket.models.serverModels.WarrantModel

interface WarrantsOnClickListener {
    fun onClick(warrant: WarrantModel)
}