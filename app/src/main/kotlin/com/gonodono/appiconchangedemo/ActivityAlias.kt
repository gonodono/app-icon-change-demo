package com.gonodono.appiconchangedemo

import android.content.pm.ActivityInfo

internal data class ActivityAlias(private val activityInfo: ActivityInfo) {

    val title: String = activityInfo.metaData.getString(META_DATA_TITLE)!!

    val icon: Int = activityInfo.icon

    val name: String = activityInfo.name

    val simpleName: String = name.substringAfterLast('.')

    companion object {
        const val META_DATA_TITLE = "${BuildConfig.APPLICATION_ID}.ALIAS_TITLE"
    }
}