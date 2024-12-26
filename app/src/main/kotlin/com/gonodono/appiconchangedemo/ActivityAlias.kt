package com.gonodono.appiconchangedemo

import android.content.pm.ActivityInfo

internal data class ActivityAlias(private val activityInfo: ActivityInfo) {

    val isEnabledInManifest: Boolean = activityInfo.enabled

    val icon: Int = activityInfo.icon

    val name: String = activityInfo.name

    val simpleName: String = name.substringAfterLast('.')

    val title: String = activityInfo.metaData.getString(META_DATA_TITLE)!!

    companion object {

        const val META_DATA_TITLE = "${BuildConfig.APPLICATION_ID}.ALIAS_TITLE"
    }
}