package com.gonodono.appiconchangedemo

import android.content.pm.ActivityInfo

internal data class ActivityAlias(private val activityInfo: ActivityInfo) {

    val isEnabledInManifest: Boolean get() = activityInfo.enabled

    val icon: Int get() = activityInfo.icon

    val name: String get() = activityInfo.name

    val simpleName: String get() = name.substringAfterLast('.')

    val title: String get() = activityInfo.metaData.getString(META_DATA_TITLE)!!

    companion object {

        const val META_DATA_TITLE = "${BuildConfig.APPLICATION_ID}.ALIAS_TITLE"
    }
}