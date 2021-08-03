package com.gonodono.appiconchangedemo;

import android.content.ComponentName;

enum Alias {
    INITIAL_ALIAS("InitialAlias"),
    CLONE_INITIAL_ALIAS("CloneInitialAlias"),
    RED_ALIAS("RedAlias"),
    GREEN_ALIAS("GreenAlias"),
    BLUE_ALIAS("BlueAlias");

    final String simpleName;
    private ComponentName componentName;

    Alias(String simpleName) {
        this.simpleName = simpleName;
    }

    ComponentName getComponentName() {
        if (componentName == null) {
            componentName = new ComponentName(BuildConfig.APPLICATION_ID,
                    BuildConfig.APPLICATION_ID + "." + simpleName);
        }
        return componentName;
    }

    static Alias forClassName(String className) {
        final String simpleName = className.substring(className.lastIndexOf('.') + 1);
        for (Alias alias : values()) {
            if (simpleName.equals(alias.simpleName)) {
                return alias;
            }
        }
        return null;
    }
}