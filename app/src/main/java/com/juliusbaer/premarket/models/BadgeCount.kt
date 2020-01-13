package com.juliusbaer.premarket.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class BadgeCount : RealmObject() {
    @PrimaryKey
    var id: Int = 0
    var count: Int? = 0
}