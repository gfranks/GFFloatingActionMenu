package com.github.gfranks.floatingactionmenu.sample;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.github.gfranks.floatingactionmenu.GFFloatingActionMenu;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

    private static final String ANCHORED = "anchored";

    private GFFloatingActionMenu mActionMenu;
    private boolean mIsAnchored;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(ANCHORED)) {
            mIsAnchored = getIntent().getBooleanExtra(ANCHORED, false);
        }

        if (mIsAnchored) {
            setContentView(R.layout.activity_main_anchor);
        } else {
            setContentView(R.layout.activity_main);
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mActionMenu = (GFFloatingActionMenu) findViewById(R.id.fam);
        findViewById(R.id.add_menu_option).setOnClickListener(this);
        findViewById(R.id.remove_menu_option).setOnClickListener(this);
        ((Spinner) findViewById(R.id.menu_direction)).setSelection(mActionMenu.getExpandDirection());
        ((Spinner) findViewById(R.id.menu_direction)).setOnItemSelectedListener(this);
        ((CheckBox) findViewById(R.id.menu_attach_anchor)).setChecked(mIsAnchored);
        ((CheckBox) findViewById(R.id.menu_attach_anchor)).setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.add_menu_option) {
            mActionMenu.addButton(getNewFloatingActionButton());
            Snackbar.make(findViewById(R.id.coordinator_layout), "Added a new menu option!", Snackbar.LENGTH_LONG).show();
        } else if (view.getId() == R.id.remove_menu_option) {
            mActionMenu.removeButton(mActionMenu.getMenuOptionCount() - 1);
            Snackbar.make(findViewById(R.id.coordinator_layout), "Removed a new menu option!", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (mActionMenu.getExpandDirection() == position) {
            return;
        }

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mActionMenu.getLayoutParams();
        switch (position) {
            case GFFloatingActionMenu.EXPAND_ARC_LEFT_UP:
            case GFFloatingActionMenu.EXPAND_LEFT:
            case GFFloatingActionMenu.EXPAND_UP:
                if (mIsAnchored) {
                    params.anchorGravity = Gravity.BOTTOM | Gravity.RIGHT | Gravity.END;
                    params.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.fab_margin), 0);
                } else {
                    params.gravity = Gravity.BOTTOM | Gravity.END;
                    params.topMargin = 0;
                }
                break;
            case GFFloatingActionMenu.EXPAND_ARC_LEFT_DOWN:
            case GFFloatingActionMenu.EXPAND_DOWN:
                if (mIsAnchored) {
                    params.anchorGravity = Gravity.BOTTOM | Gravity.RIGHT | Gravity.END;
                    params.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.fab_margin), 0);
                } else {
                    params.gravity = Gravity.TOP | Gravity.END;
                    params.topMargin = findViewById(R.id.menu_attach_anchor).getBottom() + adapterView.getHeight() + findViewById(R.id.toolbar).getHeight();
                }
                break;
            case GFFloatingActionMenu.EXPAND_ARC_RIGHT_DOWN:
                if (mIsAnchored) {
                    params.anchorGravity = Gravity.BOTTOM | Gravity.LEFT | Gravity.START;
                    params.setMargins(getResources().getDimensionPixelSize(R.dimen.fab_margin), 0, 0, 0);
                } else {
                    params.gravity = Gravity.TOP | Gravity.START;
                    params.topMargin = findViewById(R.id.menu_attach_anchor).getBottom() + adapterView.getHeight() + findViewById(R.id.toolbar).getHeight();
                }
                break;
            case GFFloatingActionMenu.EXPAND_ARC_RIGHT_UP:
            case GFFloatingActionMenu.EXPAND_RIGHT:
                if (mIsAnchored) {
                    params.anchorGravity = Gravity.BOTTOM | Gravity.LEFT | Gravity.START;
                    params.setMargins(getResources().getDimensionPixelSize(R.dimen.fab_margin), 0, 0, 0);
                } else {
                    params.gravity = Gravity.BOTTOM | Gravity.START;
                    params.topMargin = 0;
                }
                break;
        }
        mActionMenu.setLayoutParams(params);
        mActionMenu.setExpandDirection(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(ANCHORED, isChecked);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                    new Pair(findViewById(R.id.add_menu_option), findViewById(R.id.add_menu_option).getTransitionName()),
                    new Pair(findViewById(R.id.remove_menu_option), findViewById(R.id.remove_menu_option).getTransitionName()),
                    new Pair(findViewById(R.id.menu_direction), findViewById(R.id.menu_direction).getTransitionName()),
                    new Pair(findViewById(R.id.menu_attach_anchor), findViewById(R.id.menu_attach_anchor).getTransitionName())).toBundle());
        } else {
            startActivity(intent);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                supportFinishAfterTransition();
            }
        }, 1000);
    }

    private FloatingActionButton getNewFloatingActionButton() {
        FloatingActionButton actionButton = new FloatingActionButton(this);
        actionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent)));
        actionButton.setRippleColor(ContextCompat.getColor(this, R.color.colorAccentDark));
        Drawable drawable = DrawableCompat.wrap(ContextCompat.getDrawable(this, R.drawable.ic_plus));
        DrawableCompat.setTint(drawable, Color.WHITE);
        actionButton.setImageDrawable(drawable);
        return actionButton;
    }
}
