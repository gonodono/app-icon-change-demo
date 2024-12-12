package com.gonodono.appiconchangedemo

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
import android.content.pm.PackageManager.DONT_KILL_APP
import android.content.pm.PackageManager.GET_ACTIVITIES
import android.content.pm.PackageManager.GET_META_DATA
import android.content.pm.PackageManager.MATCH_DISABLED_COMPONENTS
import android.os.Bundle

internal class IconChangeManager(private val activity: Activity) {

    private val packageManager: PackageManager = activity.packageManager

    private val currentInit: ActivityAlias
    private val initialAlias: ActivityAlias
    private val cloneInitialAlias: ActivityAlias

    var aliases: List<ActivityAlias>
        private set

    init {
        val myAliases = activity.getManifestInfo().activities!!
            .filter { info -> info.isIconChangeAlias }
            .map { info -> ActivityAlias(info) }

        val componentEnabled = myAliases.filter { it.isComponentEnabled }
        currentInit = when (componentEnabled.size) {
            1 -> componentEnabled[0]
            0 -> error("No alias component currently enabled")
            else -> error("Multiple alias components currently enabled")
        }

        val (enabled, disabled) = myAliases.partition { it.isEnabledInManifest }
        initialAlias = when (enabled.size) {
            1 -> enabled[0]
            0 -> error("No initial alias enabled in manifest")
            else -> error("Multiple aliases enabled in manifest")
        }

        val sameIcon = disabled.filter { it.icon == initialAlias.icon }
        cloneInitialAlias = when (sameIcon.size) {
            1 -> sameIcon[0]
            0 -> error("No clone initial alias found")
            else -> error("Multiple clone initial aliases found")
        }

        aliases = disabled
    }

    var currentAlias: ActivityAlias = currentInit
        set(alias) {
            if (field == alias) return
            field.isComponentEnabled = false
            alias.isComponentEnabled = true
            field = alias
            isChangingIcon = true
            activity.recreate()
        }

    private var isChangingIcon = false

    fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EXTRA_IS_CHANGING_ICON, isChangingIcon)
    }

    var isIconChangeActivated: Boolean = !initialAlias.isComponentEnabled
        set(activated) {
            if (field == activated) return
            field = activated
            if (activated) {
                initialAlias.isComponentEnabled = false
                cloneInitialAlias.isComponentEnabled = true

                // Originally, the demo just finished the Activity and required
                // the user to relaunch. While updating, I found that a restart
                // works too, at least with this simple setup. If you have any
                // issues, you might need to change back to a manual relaunch.
                activity.restart()
            } else {
                for (activityInfo in activity.getManifestInfo().activities!!) {
                    if (!activityInfo.isIconChangeAlias) continue

                    packageManager.setComponentEnabledSetting(
                        activityInfo.name.toComponentName(),
                        COMPONENT_ENABLED_STATE_DEFAULT,
                        DONT_KILL_APP
                    )
                }

                // Activity#recreate() seems to suffice here as well,
                // but the demo does a restart to keep things symmetric.
                activity.restart()
            }
        }

    enum class StartMode { Normal, ActivationChange, IconChange }

    fun determineStartMode(
        intent: Intent,
        savedInstanceState: Bundle?
    ): StartMode = when {
        // Order matters.
        savedInstanceState?.getBoolean(EXTRA_IS_CHANGING_ICON) ?: false -> {
            StartMode.IconChange
        }
        intent.getBooleanExtra(EXTRA_IS_CHANGING_ACTIVATION, false) -> {
            StartMode.ActivationChange
        }
        else -> StartMode.Normal
    }

    private var ActivityAlias.isComponentEnabled: Boolean
        get() {
            val componentName = name.toComponentName()
            val state = packageManager.getComponentEnabledSetting(componentName)
            return if (state == COMPONENT_ENABLED_STATE_DEFAULT) {
                isEnabledInManifest
            } else {
                state == COMPONENT_ENABLED_STATE_ENABLED
            }
        }
        set(enabled) {
            val newState = when {
                enabled -> COMPONENT_ENABLED_STATE_ENABLED
                this == initialAlias -> COMPONENT_ENABLED_STATE_DISABLED
                else -> COMPONENT_ENABLED_STATE_DEFAULT
            }
            packageManager.setComponentEnabledSetting(
                name.toComponentName(),
                newState,
                DONT_KILL_APP
            )
        }

    private fun String.toComponentName() = ComponentName(activity, this)
}

private const val EXTRA_IS_CHANGING_ACTIVATION: String =
    BuildConfig.APPLICATION_ID + ".extra.IS_CHANGING_ACTIVATION"

private const val EXTRA_IS_CHANGING_ICON: String =
    BuildConfig.APPLICATION_ID + ".extra.IS_CHANGING_ICON"

private fun Activity.getManifestInfo(): PackageInfo =
    packageManager.getPackageInfo(
        packageName,
        GET_ACTIVITIES or GET_META_DATA or MATCH_DISABLED_COMPONENTS
    )

private fun Activity.restart() {
    finish() // <- Must come first, else it cancels the startActivity().
    val intent = packageManager.getLaunchIntentForPackage(packageName)!!
    startActivity(intent.putExtra(EXTRA_IS_CHANGING_ACTIVATION, true))
}

private val ActivityInfo.isIconChangeAlias: Boolean
    get() = metaData?.containsKey(ActivityAlias.META_DATA_TITLE) == true