package com.github.gfranks.floatingactionmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.widget.TextView;

public class GFFloatingActionButton extends FloatingActionButton {

    private String mTitle;

    public GFFloatingActionButton(Context context) {
        this(context, null);
    }

    public GFFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GFFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    public void setVisibility(int visibility) {
        TextView label = getLabelView();
        if (label != null) {
            label.setVisibility(visibility);
        }

        super.setVisibility(visibility);
    }

    public void setTitle(String title) {
        mTitle = title;
        TextView label = getLabelView();
        if (label != null) {
            label.setText(title);
        }
    }

    TextView getLabelView() {
        return (TextView) getTag(R.id.fab_label);
    }

    public String getTitle() {
        return mTitle;
    }

    private void init(Context context, AttributeSet attributeSet) {
        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.GFFloatingActionButton, 0, 0);
        mTitle = attr.getString(R.styleable.GFFloatingActionButton_fab_title);
        attr.recycle();
    }
}
