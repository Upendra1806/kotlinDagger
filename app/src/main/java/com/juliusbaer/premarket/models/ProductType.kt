package com.juliusbaer.premarket.models

enum class ProductType(val v: Int) {
    NEWS(0),
    INDEX(1),
    UNDERLYING(2),
    WARRANT(3),
    CURRENCY(4),
    METAL(5)
}