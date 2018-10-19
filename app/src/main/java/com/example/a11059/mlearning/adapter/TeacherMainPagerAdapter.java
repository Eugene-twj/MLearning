package com.example.a11059.mlearning.adapter;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import com.example.a11059.mlearning.fragment.HomeFragment;
import com.example.a11059.mlearning.fragment.MineFragment;
import com.example.a11059.mlearning.fragment.StatisticFragment;
import com.example.a11059.mlearning.fragment.TeacherMineFragment;
import com.qmuiteam.qmui.widget.QMUIPagerAdapter;

public class TeacherMainPagerAdapter extends QMUIPagerAdapter {

    private FragmentManager mFragmentManager;

    private FragmentTransaction mCurrentTransaction;

    private Fragment mCurrentPrimaryItem = null;

    public TeacherMainPagerAdapter(FragmentManager fragmentManager){
        this.mFragmentManager = fragmentManager;
    }

    @Override
    protected Object hydrate(ViewGroup container, int position) {
        switch (position){
            default:
            case 0:
                return new StatisticFragment();
            case 1:
                return new HomeFragment();
            case 2:
                return new MineFragment();
        }
    }

    @Override
    protected void populate(ViewGroup container, Object item, int position) {
        String name = makeFragmentName(container.getId(), position);
        if (mCurrentTransaction == null) {
            mCurrentTransaction = mFragmentManager
                    .beginTransaction();
        }
        Fragment fragment = mFragmentManager.findFragmentByTag(name);
        if (fragment != null) {
            mCurrentTransaction.attach(fragment);
        } else {
            fragment = (Fragment) item;
            mCurrentTransaction.add(container.getId(), fragment, name);
        }
        if (fragment != mCurrentPrimaryItem) {
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
        }
    }

    @Override
    protected void destroy(ViewGroup container, int position, Object object) {
        if (mCurrentTransaction == null) {
            mCurrentTransaction = mFragmentManager
                    .beginTransaction();
        }
        mCurrentTransaction.detach((Fragment) object);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((Fragment) object).getView();
    }

    private String makeFragmentName(int viewId, long id) {
        return "QDFitSystemWindowViewPagerFragment:" + viewId + ":" + id;
    }

    @Override
    public void startUpdate(@NonNull ViewGroup container) {
        if (container.getId() == View.NO_ID) {
            throw new IllegalStateException("ViewPager with adapter " + this
                    + " requires a view id");
        }
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        Fragment fragment = (Fragment) object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }
            mCurrentPrimaryItem = fragment;
        }
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        if (mCurrentTransaction != null) {
            mCurrentTransaction.commitNowAllowingStateLoss();
            mCurrentTransaction = null;
        }
    }
}
