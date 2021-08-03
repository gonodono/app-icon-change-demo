package com.gonodono.appiconchangedemo;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;
import static android.content.pm.PackageManager.GET_ACTIVITIES;
import static android.content.pm.PackageManager.GET_DISABLED_COMPONENTS;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String EXTRA_REFRESHING = BuildConfig.APPLICATION_ID + ".EXTRA_REFRESHING";

    private Alias currentAlias;
    private boolean isRefreshing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView output = findViewById(R.id.text_output);
        final Button activateButton = findViewById(R.id.button_activate);
        final Button resetButton = findViewById(R.id.button_reset);
        final RadioGroup groupIconColor = findViewById(R.id.group_icon_color);

        currentAlias = getCurrentAlias();

        if (currentAlias == null) {
            output.setText(R.string.error);
        } else {
            output.setText(getString(R.string.current_alias, currentAlias.simpleName));

            if (isIconChangeActivated()) {
                resetButton.setOnClickListener(v -> resetIconChange());
                groupIconColor.check(getRadioIdForAlias(currentAlias));
                groupIconColor.setOnCheckedChangeListener((rg, id) -> onAliasSelected(id));
                groupIconColor.setVisibility(View.VISIBLE);
            } else {
                activateButton.setOnClickListener(v -> activateIconChange());
                activateButton.setVisibility(View.VISIBLE);
            }

            if (savedInstanceState != null && savedInstanceState.getBoolean(EXTRA_REFRESHING)) {
                showRefreshMessage();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_REFRESHING, isRefreshing);
    }

    private void showRefreshMessage() {
        final String message = isIconChangeActivated() ?
                getString(R.string.message_alias_selected, currentAlias.simpleName) :
                getString(R.string.message_alias_reset);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void activateIconChange() {
        hideContent();
        setAliasEnabled(Alias.CLONE_INITIAL_ALIAS, true);
        setAliasEnabled(Alias.INITIAL_ALIAS, false);
        finish();
    }

    private void resetIconChange() {
        hideContent();
        try {
            final PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),
                    GET_ACTIVITIES | GET_DISABLED_COMPONENTS);
            for (ActivityInfo activityInfo : packageInfo.activities) {
                getPackageManager().setComponentEnabledSetting(createComponentName(activityInfo),
                        COMPONENT_ENABLED_STATE_DEFAULT, DONT_KILL_APP);
            }
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error resetting", Toast.LENGTH_SHORT).show();
        }
    }

    private void onAliasSelected(int checkedId) {
        setCurrentAlias(getAliasForRadioId(checkedId));
    }

    private void setCurrentAlias(Alias alias) {
        setAliasEnabled(alias, true);
        setAliasEnabled(currentAlias, false);
        refresh();
    }

    private void refresh() {
        isRefreshing = true;
        recreate();
    }

    private void setAliasEnabled(Alias alias, boolean enabled) {
        final boolean isInitialAlias = alias == Alias.INITIAL_ALIAS;
        final int newState =
                enabled ? COMPONENT_ENABLED_STATE_ENABLED :
                        isInitialAlias ? COMPONENT_ENABLED_STATE_DISABLED :
                                COMPONENT_ENABLED_STATE_DEFAULT;

        getPackageManager().setComponentEnabledSetting(alias.getComponentName(),
                newState, DONT_KILL_APP);
    }

    private Alias getCurrentAlias() {
        try {
            final PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),
                    GET_ACTIVITIES | GET_DISABLED_COMPONENTS);
            for (ActivityInfo activityInfo : packageInfo.activities) {
                if (!isTargetActivity(activityInfo) && isAliasComponentEnabled(activityInfo)) {
                    return Alias.forClassName(activityInfo.name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isTargetActivity(ActivityInfo activityInfo) {
        return MainActivity.class.getName().equals(activityInfo.name);
    }

    private boolean isAliasComponentEnabled(ActivityInfo aliasInfo) {
        final int state =
                getPackageManager().getComponentEnabledSetting(createComponentName(aliasInfo));
        if (state == COMPONENT_ENABLED_STATE_DEFAULT) {
            return aliasInfo.enabled;
        }
        return state == COMPONENT_ENABLED_STATE_ENABLED;
    }

    private boolean isIconChangeActivated() {
        return getPackageManager()
                .getComponentEnabledSetting(Alias.INITIAL_ALIAS.getComponentName()) ==
                COMPONENT_ENABLED_STATE_DISABLED;
    }

    private ComponentName createComponentName(ActivityInfo info) {
        return new ComponentName(info.packageName, info.name);
    }

    private void hideContent() {
        findViewById(android.R.id.content).setVisibility(View.GONE);
    }

    private int getRadioIdForAlias(Alias alias) {
        if (alias == Alias.RED_ALIAS) {
            return R.id.radio_red;
        } else if (alias == Alias.GREEN_ALIAS) {
            return R.id.radio_green;
        } else if (alias == Alias.BLUE_ALIAS) {
            return R.id.radio_blue;
        }
        return R.id.radio_clone_initial;
    }

    private Alias getAliasForRadioId(int radioId) {
        if (radioId == R.id.radio_red) {
            return Alias.RED_ALIAS;
        } else if (radioId == R.id.radio_green) {
            return Alias.GREEN_ALIAS;
        } else if (radioId == R.id.radio_blue) {
            return Alias.BLUE_ALIAS;
        }
        return Alias.CLONE_INITIAL_ALIAS;
    }
}