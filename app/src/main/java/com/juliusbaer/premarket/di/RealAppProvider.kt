package com.juliusbaer.premarket.di

interface RealAppProvider: RealAppComponentProvider {
    val isInBackground: Boolean
    val wasInBackground: Boolean
}