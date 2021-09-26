package com.gonodono.appiconchangedemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    private IconChangeManager iconChangeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iconChangeManager = new IconChangeManager(this);

        final TextView output = findViewById(R.id.text_output);
        final ToggleButton activateButton = findViewById(R.id.button_activate);

        output.setText(getString(R.string.current_alias,
                iconChangeManager.getCurrentAlias().getSimpleName()));

        activateButton.setChecked(iconChangeManager.isIconChangeActivated());
        activateButton.setOnCheckedChangeListener((v, checked) -> switchActivation(checked));

        if (iconChangeManager.isIconChangeActivated()) {
            final GridView gridView = findViewById(R.id.grid_view);
            gridView.setAdapter(new AliasAdapter(this, iconChangeManager));
            gridView.setOnItemClickListener((av, v, p, id) -> onAliasSelected(p));
        }

        if (iconChangeManager.isIconChangeRefresh(savedInstanceState)) {
            showIconChangeMessage();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        iconChangeManager.onSaveInstanceState(outState);
    }

    private void switchActivation(boolean enable) {
        findViewById(android.R.id.content).setVisibility(View.GONE);
        if (enable) {
            iconChangeManager.activateIconChange();
        } else {
            if (!iconChangeManager.deactivateIconChange()) {
                Toast.makeText(this, R.string.deactivate_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onAliasSelected(int position) {
        iconChangeManager.setCurrentAlias(iconChangeManager.getAliases().get(position));
    }

    private void showIconChangeMessage() {
        final String message = iconChangeManager.isIconChangeActivated() ?
                getString(R.string.message_alias_selected,
                        iconChangeManager.getCurrentAlias().getSimpleName()) :
                getString(R.string.message_alias_reset);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private static final class AliasAdapter extends ArrayAdapter<Alias> {
        private final Alias currentAlias;
        private final int drawableDimen;

        AliasAdapter(Context context, IconChangeManager manager) {
            super(context, R.layout.item_alias, manager.getAliases());
            this.currentAlias = manager.getCurrentAlias();
            this.drawableDimen =
                    context.getResources().getDimensionPixelSize(R.dimen.item_alias_image_size);
        }

        @Override
        @SuppressLint("UseCompatLoadingForDrawables")
        public View getView(int position, View convertView, ViewGroup parent) {
            final TextView view = (TextView) super.getView(position, convertView, parent);
            final Drawable drawable = getContext().getDrawable(getItem(position).iconResId);
            drawable.setBounds(0, 0, drawableDimen, drawableDimen);
            view.setCompoundDrawables(null, drawable, null, null);
            view.setAlpha(isEnabled(position) ? 1f : .3f);
            return view;
        }

        @Override
        public boolean isEnabled(int position) {
            return !getItem(position).equals(currentAlias);
        }
    }
}