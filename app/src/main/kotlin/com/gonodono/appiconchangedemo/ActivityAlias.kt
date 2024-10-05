package com.gonodono.appiconchangedemo

import android.content.pm.ActivityInfo

internal class ActivityAlias(private val activityInfo: ActivityInfo) {

    val title: String = activityInfo.metaData.getString(META_DATA_TITLE)!!

    val icon: Int = activityInfo.icon

    val name: String = activityInfo.name

    val simpleName: String = name.substringAfterLast('.')

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        return (other as? ActivityAlias)?.activityInfo == activityInfo
    }

    override fun hashCode(): Int = activityInfo.hashCode()

    companion object {
        const val META_DATA_TITLE = "${BuildConfig.APPLICATION_ID}.ALIAS_TITLE"
    }
}