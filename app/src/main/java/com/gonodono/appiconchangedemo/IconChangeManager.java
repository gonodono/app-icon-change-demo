package com.gonodono.appiconchangedemo;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;
import static android.content.pm.PackageManager.GET_ACTIVITIES;
import static android.content.pm.PackageManager.GET_DISABLED_COMPONENTS;
import static android.content.pm.PackageManager.GET_META_DATA;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

class IconChangeManager {

    private static final String
            META_DATA_KEY_ALIAS_TITLE = BuildConfig.APPLICATION_ID + ".ALIAS_TITLE";
    private static final String
            EXTRA_REFRESHING = BuildConfig.APPLICATION_ID + ".EXTRA_REFRESHING";

    private final Activity activity;
    private final PackageManager packageManager;
    private final List<Alias> iconAliases;
    private final Alias initialAlias;
    private final Alias cloneInitialAlias;
    private final Alias currentAlias;
    private final boolean isIconChangeActivated;

    private boolean isRefreshing;

    IconChangeManager(Activity activity) {
        this.activity = activity;
        this.packageManager = activity.getPackageManager();
        this.iconAliases = new ArrayList<>();

        Alias initial = null;
        Alias cloneInitial = null;
        Alias current = null;
        boolean isActivated;

        try {
            final PackageInfo packageInfo = getPackageInfo();
            for (ActivityInfo activityInfo : packageInfo.activities) {
                if (isIconChangeActivityAlias(activityInfo)) {
                    final Alias alias =
                            new Alias(activityInfo.name,
                                    activityInfo.metaData.getString(META_DATA_KEY_ALIAS_TITLE),
                                    activityInfo.icon);

                    if (isAliasComponentEnabled(activityInfo)) {
                        if (current == null) {
                            current = alias;
                        } else {
                            throw new IllegalStateException("Multiple aliases currently enabled");
                        }
                    }

                    if (activityInfo.enabled) {
                        if (initial == null) {
                            initial = alias;
                        } else {
                            throw new IllegalStateException(
                                    "Multiple initial aliases enabled in manifest");
                        }
                    } else {
                        iconAliases.add(alias);
                    }
                }
            }

            if (current == null) {
                throw new IllegalStateException("No alias currently enabled");
            }

            if (initial == null) {
                throw new IllegalStateException("No initial alias enabled in manifest");
            } else {
                isActivated = packageManager
                        .getComponentEnabledSetting(new ComponentName(activity, initial.className))
                        == COMPONENT_ENABLED_STATE_DISABLED;
            }

            for (Alias alias : iconAliases) {
                if (!alias.equals(initial) && alias.iconResId == initial.iconResId) {
                    if (cloneInitial == null) {
                        cloneInitial = alias;
                    } else {
                        throw new IllegalStateException("Multiple clone initial aliases found");
                    }
                }
            }

            if (cloneInitial == null) {
                throw new IllegalStateException("No clone initial alias found");
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        initialAlias = initial;
        cloneInitialAlias = cloneInitial;
        currentAlias = current;
        isIconChangeActivated = isActivated;
    }

    List<Alias> getAliases() {
        return iconAliases;
    }

    Alias getCurrentAlias() {
        return currentAlias;
    }

    boolean isIconChangeActivated() {
        return isIconChangeActivated;
    }

    void activateIconChange() {
        if (!isIconChangeActivated) {
            setAliasComponentEnabled(cloneInitialAlias, true);
            setAliasComponentEnabled(initialAlias, false);
            activity.finish();
        }
    }

    boolean deactivateIconChange() {
        if (isIconChangeActivated) {
            try {
                final PackageInfo packageInfo = getPackageInfo();
                for (ActivityInfo activityInfo : packageInfo.activities) {
                    if (isIconChangeActivityAlias(activityInfo)) {
                        packageManager.setComponentEnabledSetting(
                                createComponentName(activityInfo.name),
                                COMPONENT_ENABLED_STATE_DEFAULT,
                                DONT_KILL_APP);
                    }
                }
                refresh();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    void setCurrentAlias(Alias alias) {
        if (!currentAlias.equals(alias)) {
            setAliasComponentEnabled(alias, true);
            setAliasComponentEnabled(currentAlias, false);
            refresh();
        }
    }

    void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(EXTRA_REFRESHING, isRefreshing);
    }

    boolean isIconChangeRefresh(Bundle savedInstanceState) {
        return savedInstanceState != null && savedInstanceState.getBoolean(EXTRA_REFRESHING);
    }

    private PackageInfo getPackageInfo() throws PackageManager.NameNotFoundException {
        return packageManager.getPackageInfo(activity.getPackageName(),
                GET_ACTIVITIES | GET_DISABLED_COMPONENTS | GET_META_DATA);
    }

    private boolean isIconChangeActivityAlias(ActivityInfo activityInfo) {
        return activityInfo.targetActivity != null && activityInfo.metaData != null &&
                activityInfo.metaData.containsKey(META_DATA_KEY_ALIAS_TITLE);
    }

    private boolean isAliasComponentEnabled(ActivityInfo aliasInfo) {
        final int state =
                packageManager.getComponentEnabledSetting(createComponentName(aliasInfo.name));
        if (state == COMPONENT_ENABLED_STATE_DEFAULT) {
            return aliasInfo.enabled;
        }
        return state == COMPONENT_ENABLED_STATE_ENABLED;
    }

    private void setAliasComponentEnabled(Alias alias, boolean enabled) {
        final int newState =
                enabled ? COMPONENT_ENABLED_STATE_ENABLED :
                        alias.equals(initialAlias) ? COMPONENT_ENABLED_STATE_DISABLED :
                                COMPONENT_ENABLED_STATE_DEFAULT;

        packageManager.setComponentEnabledSetting(createComponentName(alias.className),
                newState, DONT_KILL_APP);
    }

    private ComponentName createComponentName(String className) {
        return new ComponentName(activity.getPackageName(), className);
    }

    private void refresh() {
        isRefreshing = true;
        activity.recreate();
    }
}