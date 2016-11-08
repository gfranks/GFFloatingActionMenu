package com.github.gfranks.floatingactionmenu;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;
import java.util.List;

@CoordinatorLayout.DefaultBehavior(GFFloatingActionMenu.Behavior.class)
public class GFFloatingActionMenu extends ViewGroup {

    private static Interpolator sExpandInterpolator = new OvershootInterpolator();
    private static Interpolator sCollapseInterpolator = new DecelerateInterpolator(3f);
    private static Interpolator sAlphaExpandInterpolator = new DecelerateInterpolator();

    @IntDef({EXPAND_UP, EXPAND_DOWN, EXPAND_LEFT, EXPAND_RIGHT, EXPAND_ARC_LEFT_UP, EXPAND_ARC_LEFT_DOWN, EXPAND_ARC_RIGHT_UP, EXPAND_ARC_RIGHT_DOWN})
    public @interface ExpandDirection {}
    public static final int EXPAND_UP = 0;
    public static final int EXPAND_DOWN = 1;
    public static final int EXPAND_LEFT = 2;
    public static final int EXPAND_RIGHT = 3;
    public static final int EXPAND_ARC_LEFT_UP = 4;
    public static final int EXPAND_ARC_LEFT_DOWN = 5;
    public static final int EXPAND_ARC_RIGHT_UP = 6;
    public static final int EXPAND_ARC_RIGHT_DOWN = 7;

    private static final int ANIMATION_DURATION = 300;

    private int mMenuRippleColor;
    private int mMenuBackgroundTint;
    private int mMenuIconTint;
    private Drawable mMenuIcon;
    private int mMenuElevation;
    private int mExpandDirection;
    private float mCollapsedIconRotation = 0f;
    private float mExpandedIconRotation = 90f + 45f;
    private int mButtonSpacing;
    private boolean mExpanded;

    private AnimatorSet mExpandAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
    private AnimatorSet mCollapseAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
    private FloatingActionButton mMenuButton;
    private RotatingDrawable mRotatingDrawable;
    private int mMaxButtonWidth;
    private int mMaxButtonHeight;
    private int mButtonsCount;

    private TouchDelegateGroup mTouchDelegateGroup;

    private OnFloatingActionsMenuUpdateListener mListener;

    public interface OnFloatingActionsMenuUpdateListener {
        void onMenuExpanded();
        void onMenuCollapsed();
    }

    public GFFloatingActionMenu(Context context) {
        this(context, null);
    }

    public GFFloatingActionMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GFFloatingActionMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        mButtonSpacing = (int) (15 * getResources().getDisplayMetrics().density);

        mTouchDelegateGroup = new TouchDelegateGroup(this);
        setTouchDelegate(mTouchDelegateGroup);

        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.GFFloatingActionMenu, 0, 0);
        mMenuRippleColor = attr.getColor(R.styleable.GFFloatingActionMenu_fam_rippleColor, Color.DKGRAY);
        mMenuBackgroundTint = attr.getColor(R.styleable.GFFloatingActionMenu_fam_backgroundTint, Color.GRAY);
        mMenuIconTint = attr.getColor(R.styleable.GFFloatingActionMenu_fam_iconTint, Color.WHITE);
        int menuIcon = attr.getResourceId(R.styleable.GFFloatingActionMenu_fam_icon, R.drawable.ic_plus);
        mMenuElevation = attr.getDimensionPixelSize(R.styleable.GFFloatingActionMenu_fam_elevation, -1);
        mExpandDirection = attr.getInt(R.styleable.GFFloatingActionMenu_fam_expandDirection, EXPAND_UP);
        mExpandedIconRotation = attr.getFloat(R.styleable.GFFloatingActionMenu_fam_expandIconRotation, mExpandedIconRotation);
        mCollapsedIconRotation = attr.getFloat(R.styleable.GFFloatingActionMenu_fam_collapseIconRotation, mCollapsedIconRotation);
        attr.recycle();

        mMenuIcon = ContextCompat.getDrawable(getContext(), menuIcon);

        createAddButton(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int width = 0;
        int height = 0;

        mMaxButtonWidth = 0;
        mMaxButtonHeight = 0;

        for (int i = 0; i < mButtonsCount; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            switch (mExpandDirection) {
                case EXPAND_UP:
                case EXPAND_DOWN:
                    mMaxButtonWidth = Math.max(mMaxButtonWidth, child.getMeasuredWidth());
                    height += child.getMeasuredHeight();
                    break;
                case EXPAND_LEFT:
                case EXPAND_RIGHT:
                    width += child.getMeasuredWidth();
                    mMaxButtonHeight = Math.max(mMaxButtonHeight, child.getMeasuredHeight());
                    break;
                case EXPAND_ARC_LEFT_UP:
                case EXPAND_ARC_LEFT_DOWN:
                case EXPAND_ARC_RIGHT_UP:
                case EXPAND_ARC_RIGHT_DOWN:
                    mMaxButtonWidth = Math.max(mMaxButtonWidth, child.getMeasuredWidth());
                    height += child.getMeasuredHeight();
                    width += child.getMeasuredWidth();
                    mMaxButtonHeight = Math.max(mMaxButtonHeight, child.getMeasuredHeight());
                    break;
            }
        }

        if (expandsHorizontally()) {
            height = mMaxButtonHeight;
        } else if (expandsVertically()) {
            width = mMaxButtonWidth;
        } else {
            height = mMaxButtonHeight;
            width = mMaxButtonWidth;
        }

        switch (mExpandDirection) {
            case EXPAND_UP:
            case EXPAND_DOWN:
                height += mButtonSpacing * (mButtonsCount - 1);
                height = adjustForOvershoot(height);
                break;
            case EXPAND_LEFT:
            case EXPAND_RIGHT:
                width += mButtonSpacing * (mButtonsCount - 1);
                width = adjustForOvershoot(width);
                break;
            case EXPAND_ARC_LEFT_UP:
            case EXPAND_ARC_LEFT_DOWN:
            case EXPAND_ARC_RIGHT_UP:
            case EXPAND_ARC_RIGHT_DOWN:
                height += mButtonSpacing * mButtonsCount * 2;
                height = adjustForOvershoot(height);
                width += mButtonSpacing * mButtonsCount * 2;
                width = adjustForOvershoot(width);
                break;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        switch (mExpandDirection) {
            case EXPAND_UP:
            case EXPAND_DOWN: {
                boolean expandUp = mExpandDirection == EXPAND_UP;

                if (changed) {
                    mTouchDelegateGroup.clearTouchDelegates();
                }

                int addButtonY;
                if (expandUp) {
                    addButtonY = getAdjustedYCoordinateForAppBarLayoutAnchor(b - t - mMenuButton.getMeasuredHeight());
                } else {
                    addButtonY = getAdjustedYCoordinateForAppBarLayoutAnchor(0);
                }
                // Ensure mMenuButton is centered on the line where the buttons should be
                int buttonsHorizontalCenter = r - l - mMaxButtonWidth / 2;
                int addButtonLeft = buttonsHorizontalCenter - mMenuButton.getMeasuredWidth() / 2;
                mMenuButton.layout(addButtonLeft, addButtonY, addButtonLeft + mMenuButton.getMeasuredWidth(), addButtonY + mMenuButton.getMeasuredHeight());

                int nextY = expandUp ?
                        addButtonY - mButtonSpacing :
                        addButtonY + mMenuButton.getMeasuredHeight() + mButtonSpacing;

                for (int i = mButtonsCount - 1; i >= 0; i--) {
                    final View child = getChildAt(i);

                    if (child == mMenuButton || child.getVisibility() == GONE) continue;

                    int childX = buttonsHorizontalCenter - child.getMeasuredWidth() / 2;
                    int childY;
                    if (expandUp) {
                        childY = nextY - child.getMeasuredHeight();
                    } else {
                        childY = nextY;
                    }
                    child.layout(childX, childY, childX + child.getMeasuredWidth(), childY + child.getMeasuredHeight());

                    float collapsedTranslation = addButtonY - childY;
                    float expandedTranslation = 0f;

                    child.setTranslationY(mExpanded ? expandedTranslation : collapsedTranslation);
                    child.setAlpha(mExpanded ? 1f : 0f);

                    LayoutParams params = (LayoutParams) child.getLayoutParams();
                    params.mCollapseYDir.setFloatValues(expandedTranslation, collapsedTranslation);
                    params.mExpandYDir.setFloatValues(collapsedTranslation, expandedTranslation);
                    params.setAnimationsTarget(child);

                    nextY = expandUp ?
                            childY - mButtonSpacing :
                            childY + child.getMeasuredHeight() + mButtonSpacing;
                }
                break;
            }
            case EXPAND_LEFT:
            case EXPAND_RIGHT: {
                boolean expandLeft = mExpandDirection == EXPAND_LEFT;

                int addButtonX;
                if (expandLeft) {
                    addButtonX = r - l - mMenuButton.getMeasuredWidth();
                } else {
                    addButtonX = 0;
                }
                // Ensure mMenuButton is centered on the line where the buttons should be
                int addButtonTop = getAdjustedYCoordinateForAppBarLayoutAnchor(b - t - mMaxButtonHeight + (mMaxButtonHeight - mMenuButton.getMeasuredHeight()) / 2);
                mMenuButton.layout(addButtonX, addButtonTop, addButtonX + mMenuButton.getMeasuredWidth(), addButtonTop + mMenuButton.getMeasuredHeight());

                int nextX = expandLeft ?
                        addButtonX - mButtonSpacing :
                        addButtonX + mMenuButton.getMeasuredWidth() + mButtonSpacing;

                for (int i = mButtonsCount - 1; i >= 0; i--) {
                    final View child = getChildAt(i);

                    if (child == mMenuButton || child.getVisibility() == GONE) continue;

                    int childX;
                    if (expandLeft) {
                        childX = nextX - child.getMeasuredWidth();
                    } else {
                        childX = nextX;
                    }
                    int childY = addButtonTop + (mMenuButton.getMeasuredHeight() - child.getMeasuredHeight()) / 2;
                    child.layout(childX, childY, childX + child.getMeasuredWidth(), childY + child.getMeasuredHeight());

                    float collapsedTranslation = addButtonX - childX;
                    float expandedTranslation = 0f;

                    child.setTranslationX(mExpanded ? expandedTranslation : collapsedTranslation);
                    child.setAlpha(mExpanded ? 1f : 0f);

                    LayoutParams params = (LayoutParams) child.getLayoutParams();
                    params.mCollapseXDir.setFloatValues(expandedTranslation, collapsedTranslation);
                    params.mExpandXDir.setFloatValues(collapsedTranslation, expandedTranslation);
                    params.setAnimationsTarget(child);

                    nextX = expandLeft ?
                            childX - mButtonSpacing :
                            childX + child.getMeasuredWidth() + mButtonSpacing;
                }
                break;
            }
            case EXPAND_ARC_LEFT_DOWN:
            case EXPAND_ARC_LEFT_UP:
            case EXPAND_ARC_RIGHT_UP:
            case EXPAND_ARC_RIGHT_DOWN: {
                boolean expandLeft = mExpandDirection == EXPAND_ARC_LEFT_UP || mExpandDirection == EXPAND_ARC_LEFT_DOWN;
                boolean expandUp = mExpandDirection == EXPAND_ARC_LEFT_UP || mExpandDirection == EXPAND_ARC_RIGHT_UP;

                int addButtonX;
                if (expandLeft) {
                    addButtonX = r - l - mMenuButton.getMeasuredWidth();
                } else {
                    addButtonX = 0;
                }
                int addButtonY;
                if (expandUp) {
                    addButtonY = getAdjustedYCoordinateForAppBarLayoutAnchor(b - t - mMenuButton.getMeasuredHeight());
                } else {
                    addButtonY = getAdjustedYCoordinateForAppBarLayoutAnchor(0);
                }
                mMenuButton.layout(addButtonX, addButtonY, addButtonX + mMenuButton.getMeasuredWidth(), addButtonY + mMenuButton.getMeasuredHeight());

                double eachAngle;
                if (mButtonsCount == 1) {
                    eachAngle = 0;
                } else {
                    int angle;
                    if (expandLeft) {
                        angle = 90;
                    } else {
                        angle = -90;
                    }
                    eachAngle = (double) angle / (mButtonsCount - 2);
                }

                int leftPoint, topPoint, left, top;
                for (int i = 0; i < mButtonsCount; i++) {
                    final View child = getChildAt(i);

                    if (child == mMenuButton || child.getVisibility() == GONE) continue;

                    double totalAngleForChild = eachAngle * (i);
                    leftPoint = (int) ((getMeasuredWidth() * Math.cos(Math.toRadians(totalAngleForChild))) / 2);
                    topPoint = (int) ((getMeasuredHeight() * Math.sin(Math.toRadians(totalAngleForChild))) / 2);

                    if (expandLeft) {
                        left = addButtonX - leftPoint;
                        if (expandUp) {
                            top = addButtonY - topPoint;
                        } else {
                            top = addButtonY + topPoint;
                        }
                    } else {
                        left = addButtonX + leftPoint;
                        if (expandUp) {
                            top = addButtonY + topPoint;
                        } else {
                            top = addButtonY - topPoint;
                        }
                    }

                    child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());

                    float collapsedXTranslation = addButtonX - left;
                    float collapsedYTranslation = addButtonY - top;
                    float expandedXTranslation = 0f;
                    float expandedYTranslation = 0f;

                    child.setTranslationX(mExpanded ? expandedXTranslation : collapsedXTranslation);
                    child.setTranslationY(mExpanded ? expandedYTranslation : collapsedYTranslation);
                    child.setAlpha(mExpanded ? 1f : 0f);

                    LayoutParams params = (LayoutParams) child.getLayoutParams();
                    params.mCollapseXDir.setFloatValues(expandedXTranslation, collapsedXTranslation);
                    params.mCollapseYDir.setFloatValues(expandedYTranslation, collapsedYTranslation);
                    params.mExpandXDir.setFloatValues(collapsedXTranslation, expandedXTranslation);
                    params.mExpandYDir.setFloatValues(collapsedYTranslation, expandedYTranslation);
                    params.setAnimationsTarget(child);
                }
                break;
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        bringChildToFront(mMenuButton);
        mButtonsCount = getChildCount();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        mMenuButton.setEnabled(enabled);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mExpanded = mExpanded;

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            mExpanded = savedState.mExpanded;
            mTouchDelegateGroup.setEnabled(mExpanded);

            if (mRotatingDrawable != null) {
                mRotatingDrawable.setRotation(mExpanded ? mExpandedIconRotation : mCollapsedIconRotation);
            }

            super.onRestoreInstanceState(savedState.getSuperState());
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(super.generateDefaultLayoutParams());
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(super.generateLayoutParams(attrs));
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(super.generateLayoutParams(p));
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return super.checkLayoutParams(p);
    }

    public void setOnFloatingActionsMenuUpdateListener(OnFloatingActionsMenuUpdateListener listener) {
        mListener = listener;
    }

    public FloatingActionButton getMenuActionButton() {
        return mMenuButton;
    }

    public @ExpandDirection int getExpandDirection() {
        return mExpandDirection;
    }

    public void setExpandDirection(@ExpandDirection int expandDirection) {
        mExpandDirection = expandDirection;
        requestLayout();
    }

    public int getMenuOptionCount() {
        return mButtonsCount - 1;
    }

    public void setMenuIcon(@DrawableRes int drawableResId) {
        setMenuIcon(ContextCompat.getDrawable(getContext(), drawableResId));
    }

    public void setMenuIcon(Drawable drawable) {
        mMenuButton.setImageDrawable(getMenuDrawable(drawable));
    }

    public void setExpandedIconRotation(float expandedIconRotation) {
        mExpandedIconRotation = expandedIconRotation;
        RotatingDrawable drawable = (RotatingDrawable) mMenuButton.getDrawable();
        mMenuButton.setImageDrawable(getMenuDrawable(drawable.getDrawable(0)));
    }

    public void setCollapsedIconRotation(float collapsedIconRotation) {
        mCollapsedIconRotation = collapsedIconRotation;
        RotatingDrawable drawable = (RotatingDrawable) mMenuButton.getDrawable();
        mMenuButton.setImageDrawable(getMenuDrawable(drawable.getDrawable(0)));
    }

    public void addButton(FloatingActionButton button) {
        addView(button, mButtonsCount - 1);
        mButtonsCount++;
    }

    public void addButton(FloatingActionButton button, int index) {
        addView(button, index);
        mButtonsCount++;
        bringChildToFront(mMenuButton);
    }

    public void removeButton(FloatingActionButton button) {
        if (button == mMenuButton) {
            throw new IllegalStateException("You cannot remove the menu button");
        }

        removeView(button);
        mButtonsCount--;
    }

    public void removeButton(int index) {
        if (index == 0) {
            throw new IllegalStateException("You cannot remove the menu button");
        }

        removeViewAt(index);
        mButtonsCount--;
    }

    public void toggle() {
        if (mExpanded) {
            collapse();
        } else {
            expand();
        }
    }

    public void expand() {
        if (!mExpanded) {
            mExpanded = true;
            mTouchDelegateGroup.setEnabled(true);
            mCollapseAnimation.cancel();
            mExpandAnimation.start();

            if (mListener != null) {
                mListener.onMenuExpanded();
            }
        }
    }

    public void collapse() {
        collapse(false);
    }

    public void collapseImmediately() {
        collapse(true);
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    public void show() {
        for (int i = 0; i < mButtonsCount; i++) {
            View child = getChildAt(i);
            child.setEnabled(true);
            if (child != mMenuButton) {
                child.setVisibility(View.VISIBLE);
            }
        }
        mMenuButton.show();
    }

    public void hide() {
        if (isExpanded()) {
            collapse();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    hide();
                }
            }, ANIMATION_DURATION * 5);
        } else {
            for (int i = 0; i < mButtonsCount; i++) {
                View child = getChildAt(i);
                child.setEnabled(false);
                if (child != mMenuButton) {
                    child.setVisibility(View.GONE);
                }
            }

            mMenuButton.hide();
        }
    }

    private void collapse(boolean immediately) {
        if (mExpanded) {
            mExpanded = false;
            mTouchDelegateGroup.setEnabled(false);
            mCollapseAnimation.setDuration(immediately ? 0 : ANIMATION_DURATION);
            mCollapseAnimation.start();
            mExpandAnimation.cancel();

            if (mListener != null) {
                mListener.onMenuCollapsed();
            }
        }
    }

    private boolean expandsHorizontally() {
        return mExpandDirection == EXPAND_LEFT || mExpandDirection == EXPAND_RIGHT;
    }

    private boolean expandsVertically() {
        return mExpandDirection == EXPAND_UP || mExpandDirection == EXPAND_DOWN;
    }

    private void createAddButton(Context context) {
        mMenuButton = new FloatingActionButton(context);
        if (mMenuElevation != -1) {
            mMenuButton.setCompatElevation(mMenuElevation);
        }
        mMenuButton.setRippleColor(mMenuRippleColor);
        mMenuButton.setBackgroundTintList(ColorStateList.valueOf(mMenuBackgroundTint));
        Drawable drawable = DrawableCompat.wrap(mMenuIcon);
        DrawableCompat.setTint(drawable, mMenuIconTint);
        mMenuButton.setImageDrawable(getMenuDrawable(drawable));
        mMenuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });

        addView(mMenuButton, super.generateDefaultLayoutParams());
        mButtonsCount++;
    }

    private RotatingDrawable getMenuDrawable(Drawable drawable) {
        mRotatingDrawable = new RotatingDrawable(drawable);
        final OvershootInterpolator interpolator = new OvershootInterpolator();
        final ObjectAnimator collapseAnimator = ObjectAnimator.ofFloat(mRotatingDrawable, "rotation", mExpandedIconRotation, mCollapsedIconRotation);
        final ObjectAnimator expandAnimator = ObjectAnimator.ofFloat(mRotatingDrawable, "rotation", mCollapsedIconRotation, mExpandedIconRotation);
        collapseAnimator.setInterpolator(interpolator);
        expandAnimator.setInterpolator(interpolator);
        mExpandAnimation.play(expandAnimator);
        mCollapseAnimation.play(collapseAnimator);
        return mRotatingDrawable;
    }

    private int adjustForOvershoot(int dimension) {
        return dimension * 12 / 10;
    }

    private int getAdjustedYCoordinateForAppBarLayoutAnchor(int initialY) {
        if (!(getLayoutParams() instanceof CoordinatorLayout.LayoutParams) || !(getParent() instanceof CoordinatorLayout)) {
            return initialY;
        } else if (((CoordinatorLayout.LayoutParams) getLayoutParams()).getAnchorId() != NO_ID) {
            CoordinatorLayout parent = (CoordinatorLayout) getParent();
            final CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) getLayoutParams();
            if (parent.findViewById(lp.getAnchorId()) != null) {
                if (parent.findViewById(lp.getAnchorId()) instanceof AppBarLayout) {
                    int newY = initialY;
                    if (mExpandDirection == EXPAND_UP || mExpandDirection == EXPAND_ARC_LEFT_UP || mExpandDirection == EXPAND_ARC_RIGHT_UP) {
                        newY -= getMeasuredHeight() / 2;
                        newY += mMenuButton.getMeasuredHeight() / 2;
                    } else {
                        newY += getMeasuredHeight() / 2;
                        newY -= mMenuButton.getMeasuredHeight() / 2;
                    }
                    return newY;
                } else {
                    throw new IllegalStateException("Anchoring to a non AppBarLayout is not currently supported");
                }
            }
        }
        return initialY;
    }

    private static class RotatingDrawable extends LayerDrawable {

        RotatingDrawable(Drawable drawable) {
            super(new Drawable[] { drawable });
        }

        private float mRotation;

        @SuppressWarnings("UnusedDeclaration")
        float getRotation() {
            return mRotation;
        }

        @SuppressWarnings("UnusedDeclaration")
        void setRotation(float rotation) {
            mRotation = rotation;
            invalidateSelf();
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.save();
            canvas.rotate(mRotation, getBounds().centerX(), getBounds().centerY());
            super.draw(canvas);
            canvas.restore();
        }
    }

    private class LayoutParams extends ViewGroup.LayoutParams {

        private ObjectAnimator mExpandXDir = new ObjectAnimator();
        private ObjectAnimator mExpandYDir = new ObjectAnimator();
        private ObjectAnimator mExpandAlpha = new ObjectAnimator();
        private ObjectAnimator mCollapseXDir = new ObjectAnimator();
        private ObjectAnimator mCollapseYDir = new ObjectAnimator();
        private ObjectAnimator mCollapseAlpha = new ObjectAnimator();
        private boolean animationsSetToPlay;

        LayoutParams(ViewGroup.LayoutParams source) {
            super(source);

            mExpandXDir.setInterpolator(sExpandInterpolator);
            mExpandYDir.setInterpolator(sExpandInterpolator);
            mExpandAlpha.setInterpolator(sAlphaExpandInterpolator);
            mCollapseXDir.setInterpolator(sCollapseInterpolator);
            mCollapseYDir.setInterpolator(sCollapseInterpolator);
            mCollapseAlpha.setInterpolator(sCollapseInterpolator);

            mCollapseAlpha.setProperty(View.ALPHA);
            mCollapseAlpha.setFloatValues(1f, 0f);

            mExpandAlpha.setProperty(View.ALPHA);
            mExpandAlpha.setFloatValues(0f, 1f);

            mCollapseXDir.setProperty(View.TRANSLATION_X);
            mExpandXDir.setProperty(View.TRANSLATION_X);
            mCollapseYDir.setProperty(View.TRANSLATION_Y);
            mExpandYDir.setProperty(View.TRANSLATION_Y);
        }

        void setAnimationsTarget(View view) {
            view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            mCollapseAlpha.setTarget(view);
            mExpandAlpha.setTarget(view);
            mCollapseXDir.setTarget(view);
            mCollapseYDir.setTarget(view);
            mExpandXDir.setTarget(view);
            mExpandYDir.setTarget(view);

            // Now that the animations have targets, set them to be played
            if (!animationsSetToPlay) {

                mCollapseAnimation.play(mCollapseAlpha);
                mExpandAnimation.play(mExpandAlpha);
                switch (mExpandDirection) {
                    case EXPAND_UP:
                    case EXPAND_DOWN:
                        mCollapseAnimation.play(mCollapseYDir);
                        mExpandAnimation.play(mExpandYDir);
                        break;
                    case EXPAND_LEFT:
                    case EXPAND_RIGHT:
                        mCollapseAnimation.play(mCollapseXDir);
                        mExpandAnimation.play(mExpandXDir);
                        break;
                    case EXPAND_ARC_LEFT_UP:
                    case EXPAND_ARC_LEFT_DOWN:
                    case EXPAND_ARC_RIGHT_UP:
                    case EXPAND_ARC_RIGHT_DOWN:
                        mCollapseAnimation.play(mCollapseYDir);
                        mCollapseAnimation.play(mCollapseXDir);
                        mExpandAnimation.play(mExpandYDir);
                        mExpandAnimation.play(mExpandXDir);
                        break;
                }

                animationsSetToPlay = true;
            }
        }
    }

    private static class SavedState extends BaseSavedState {
        boolean mExpanded;

        SavedState(Parcelable parcel) {
            super(parcel);
        }

        private SavedState(Parcel in) {
            super(in);
            mExpanded = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mExpanded ? 1 : 0);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    private static class TouchDelegateGroup extends TouchDelegate {

        private static final Rect USELESS_HACKY_RECT = new Rect();
        private final ArrayList<TouchDelegate> mTouchDelegates = new ArrayList<>();
        private TouchDelegate mCurrentTouchDelegate;
        private boolean mEnabled;

        TouchDelegateGroup(View uselessHackyView) {
            super(USELESS_HACKY_RECT, uselessHackyView);
        }

        void clearTouchDelegates() {
            mTouchDelegates.clear();
            mCurrentTouchDelegate = null;
        }

        @Override
        public boolean onTouchEvent(@NonNull MotionEvent event) {
            if (!mEnabled) return false;

            TouchDelegate delegate = null;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    for (int i = 0; i < mTouchDelegates.size(); i++) {
                        TouchDelegate touchDelegate = mTouchDelegates.get(i);
                        if (touchDelegate.onTouchEvent(event)) {
                            mCurrentTouchDelegate = touchDelegate;
                            return true;
                        }
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    delegate = mCurrentTouchDelegate;
                    break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    delegate = mCurrentTouchDelegate;
                    mCurrentTouchDelegate = null;
                    break;
            }

            return delegate != null && delegate.onTouchEvent(event);
        }

        public void setEnabled(boolean enabled) {
            mEnabled = enabled;
        }
    }

    public static class Behavior extends CoordinatorLayout.Behavior<GFFloatingActionMenu> {

        private Rect mTmpRect;

        public Behavior() {
            super();
        }

        public Behavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, GFFloatingActionMenu child, View dependency) {
            return dependency instanceof AppBarLayout || isBottomSheet(dependency);
        }

        @Override
        public void onAttachedToLayoutParams(@NonNull CoordinatorLayout.LayoutParams lp) {
            if (lp.dodgeInsetEdges == Gravity.NO_GRAVITY) {
                lp.dodgeInsetEdges = Gravity.BOTTOM;
            }
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, GFFloatingActionMenu child, View dependency) {
            if (dependency instanceof AppBarLayout) {
                updateFamVisibilityForAppBarLayout(parent, (AppBarLayout) dependency, child);
            } else if (isBottomSheet(dependency)) {
                updateFamVisibilityForBottomSheet(dependency, child);
            }

            return false;
        }

        @Override
        public boolean onLayoutChild(CoordinatorLayout parent, GFFloatingActionMenu child, int layoutDirection) {
            final List<View> dependencies = parent.getDependencies(child);
            for (int i = 0, count = dependencies.size(); i < count; i++) {
                final View dependency = dependencies.get(i);
                if (dependency instanceof AppBarLayout) {
                    if (updateFamVisibilityForAppBarLayout(parent, (AppBarLayout) dependency, child)) {
                        break;
                    }
                } else if (isBottomSheet(dependency)) {
                    if (updateFamVisibilityForBottomSheet(dependency, child)) {
                        break;
                    }
                }
            }
            parent.onLayoutChild(child, layoutDirection);
            return true;
        }

        private static boolean isBottomSheet(@NonNull View view) {
            final ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp instanceof CoordinatorLayout.LayoutParams) {
                return ((CoordinatorLayout.LayoutParams) lp)
                        .getBehavior() instanceof BottomSheetBehavior;
            }
            return false;
        }

        private boolean updateFamVisibilityForAppBarLayout(CoordinatorLayout parent, AppBarLayout appBarLayout,
                                                           GFFloatingActionMenu child) {
            if (!shouldUpdateVisibility(appBarLayout, child)) {
                return false;
            }

            if (mTmpRect == null) {
                mTmpRect = new Rect();
            }

            // First, let's get the visible rect of the dependency
            final Rect rect = mTmpRect;
            getDescendantRect(parent, appBarLayout, rect);

            if (rect.bottom < getMinimumHeightForVisibleOverlappingContent(appBarLayout)) {
                child.hide();
            } else {
                child.show();
            }
            return true;
        }

        private boolean updateFamVisibilityForBottomSheet(View bottomSheet, GFFloatingActionMenu child) {
            float translationY = Math.min(0, bottomSheet.getTop() - child.getBottom() - ((CoordinatorLayout.LayoutParams) child.getLayoutParams()).bottomMargin);
            child.setTranslationY(translationY);
            return true;
        }

        private boolean shouldUpdateVisibility(View dependency, GFFloatingActionMenu child) {
            final CoordinatorLayout.LayoutParams lp =
                    (CoordinatorLayout.LayoutParams) child.getLayoutParams();

            if (lp.getAnchorId() != dependency.getId()) {
                // The anchor ID doesn't match the dependency, so we won't automatically
                // show/hide the FAB
                return false;
            }

            //noinspection RedundantIfStatement
            if (child.getVisibility() != VISIBLE) {
                // The view isn't set to be visible so skip changing its visibility
                return false;
            }

            return true;
        }

        private void getDescendantRect(ViewGroup parent, View descendant, Rect out) {
            out.set(0, 0, descendant.getWidth(), descendant.getHeight());
            parent.offsetDescendantRectToMyCoords(descendant, out);
            out.offset(descendant.getScrollX(), descendant.getScrollY());
        }

        private int getMinimumHeightForVisibleOverlappingContent(AppBarLayout appBarLayout) {
            final int topInset = 0; // getTopInset(); // not a public method >:|
            final int minHeight = ViewCompat.getMinimumHeight(appBarLayout);
            if (minHeight != 0) {
                return (minHeight * 2) + topInset;
            }

            final int childCount = appBarLayout.getChildCount();
            final int lastChildMinHeight = childCount >= 1
                    ? ViewCompat.getMinimumHeight(appBarLayout.getChildAt(childCount - 1)) : 0;
            if (lastChildMinHeight != 0) {
                return ((int) (lastChildMinHeight * 2.5)) + topInset;
            }

            return appBarLayout.getHeight() / 3;
        }
    }
}
