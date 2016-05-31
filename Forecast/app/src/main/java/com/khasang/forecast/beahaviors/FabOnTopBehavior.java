package com.khasang.forecast.beahaviors;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

public class FabOnTopBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {

    public FabOnTopBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FabOnTopBehavior() {
        super();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        return dependency instanceof AppBarLayout || super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        if (dependency instanceof AppBarLayout) {
            if (dependency.getBottom() == 0 && child.getVisibility() == View.VISIBLE) {
                child.hide();
                return true;
            } else if (dependency.getBottom() > 0 && child.getVisibility() != View.VISIBLE) {
                child.show();
                return true;
            }
            return false;
        } else {
            return super.onDependentViewChanged(parent, child, dependency);
        }
    }
}
