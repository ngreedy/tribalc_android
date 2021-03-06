package com.gs.buluo.app.view.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gs.buluo.app.TribeApplication;
import com.gs.buluo.app.presenter.BasePresenter;
import com.gs.buluo.app.view.activity.LoginActivity;
import com.gs.buluo.app.view.impl.IBaseView;
import com.gs.buluo.common.widget.LoadingDialog;

import butterknife.ButterKnife;

/**
 * Created by admin on 2016/11/1.
 */
public abstract class BaseFragment<T extends IBaseView> extends Fragment {

    protected View mRootView;
    protected Context mContext;
    protected BasePresenter mPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mPresenter = getPresenter();
        if (mPresenter != null && this instanceof IBaseView) {
            mPresenter.attach((IBaseView) this);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView != null) {
            ViewGroup parent = (ViewGroup) mRootView.getParent();
            if (parent != null) {
                parent.removeView(mRootView);
            }
        } else {
            mRootView = createView(inflater, container, savedInstanceState);
        }
        mContext = mRootView.getContext();
        return mRootView;
    }

    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getContentLayout(), container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bindView(savedInstanceState);
        isViewInitiated = true;
        prepareFetchData();
    }

    public boolean prepareFetchData() {
        return prepareFetchData(false);
    }

    protected boolean isViewInitiated;
    protected boolean isVisibleToUser;
    protected boolean isDataInitiated;
    public boolean prepareFetchData(boolean forceUpdate) {
        if (isVisibleToUser && isViewInitiated && (!isDataInitiated || forceUpdate)) {
            fetchData();
            isDataInitiated = true;
            return true;
        }
        return false;
    }
    public void fetchData(){}
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        prepareFetchData();
    }

    public View getmRootView() {
        return mRootView;
    }

    protected abstract int getContentLayout();

    protected abstract void bindView(Bundle savedInstanceState);

    @Override
    public void onDestroy() {
        mContext = null;
        if (mPresenter != null) {
            mPresenter.detachView();
        }
        super.onDestroy();
    }

    protected boolean checkUser(Context context) {
        if (TribeApplication.getInstance().getUserInfo() == null) {
            startActivity(new Intent(context, LoginActivity.class));
            return false;
        }
        return true;
    }

    protected void showLoadingDialog() {
        if (!LoadingDialog.getInstance().isShowing())
            LoadingDialog.getInstance().show(mContext, "", true);
    }

    protected void dismissDialog() {
        LoadingDialog.getInstance().dismissDialog();
    }

    protected BasePresenter getPresenter() {
        return null;
    }
}
