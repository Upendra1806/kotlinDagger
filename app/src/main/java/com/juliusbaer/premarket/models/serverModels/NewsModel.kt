package com.juliusbaer.premarket.models.serverModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.Index

open class NewsModel : RealmObject() {
    @Expose
    @Index
    @SerializedName("Id")
    var id: Int = 0

    @Expose
    @SerializedName("Headline")
    var headLine: String = ""

    @Expose
    @SerializedName("PublishDate")
    var publishDate: Long = 0

    @Expose
    @SerializedName("NewsText")
    var newsText: String = ""

    @Expose
    @SerializedName("WasRead")
    var isRead: Boolean? = false

    @Index
    var collectionId: Int? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NewsModel

        if (id != other.id) return false
        if (headLine != other.headLine) return false
        if (publishDate != other.publishDate) return false
        if (newsText != other.newsText) return false
        if (isRead != other.isRead) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }
}