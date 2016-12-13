package com.gs.buluo.app.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.gs.buluo.app.Constant;
import com.gs.buluo.app.R;
import com.gs.buluo.app.TribeApplication;
import com.gs.buluo.app.bean.CompanyDetail;
import com.gs.buluo.app.bean.ResponseBody.CompanyQueryResponse;
import com.gs.buluo.app.bean.ResponseBody.UploadAccessResponse;
import com.gs.buluo.app.eventbus.SelfEvent;
import com.gs.buluo.app.network.CompanyService;
import com.gs.buluo.app.network.TribeRetrofit;
import com.gs.buluo.app.network.TribeUploader;
import com.gs.buluo.app.presenter.BasePresenter;
import com.gs.buluo.app.presenter.MinePresenter;
import com.gs.buluo.app.utils.FresoUtils;
import com.gs.buluo.app.utils.ToastUtils;
import com.gs.buluo.app.view.activity.CompanyActivity;
import com.gs.buluo.app.view.activity.CompanyDetailActivity;
import com.gs.buluo.app.view.activity.LoginActivity;
import com.gs.buluo.app.view.activity.OrderActivity;
import com.gs.buluo.app.view.activity.PropertyActivity;
import com.gs.buluo.app.view.activity.ReserveActivity;
import com.gs.buluo.app.view.activity.SelfActivity;
import com.gs.buluo.app.view.activity.SettingActivity;
import com.gs.buluo.app.view.activity.VerifyActivity;
import com.gs.buluo.app.view.activity.WalletActivity;
import com.gs.buluo.app.view.widget.ChoosePhotoPanel;
import com.gs.buluo.app.view.widget.pulltozoom.PullToZoomScrollViewEx;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by admin on 2016/11/1.
 */
public class MineFragment extends BaseFragment implements View.OnClickListener {

    SimpleDraweeView mHead;
    LinearLayout llLogin;
    LinearLayout llUnLogin;
    TextView mNick;

    SimpleDraweeView mCover;
    PullToZoomScrollViewEx scrollView;
    private static final String TAG = "MineFragment";

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_mine;
    }

    @Override
    protected void bindView(Bundle savedInstanceState) {
        initZoomView();
        if (TribeApplication.getInstance().getUserInfo()!=null){
            setLoginState(true);
        }
        getActivity().findViewById(R.id.mine_wallet).setOnClickListener(this);

        EventBus.getDefault().register(this);
    }

    private void initZoomView() {
        View zoomView = LayoutInflater.from(getActivity()).inflate(R.layout.self_zoom_layout, null, false);
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.self_content_layout, null, false);
        View headView = LayoutInflater.from(getActivity()).inflate(R.layout.self_head_layout, null, false);
        scrollView= (PullToZoomScrollViewEx) getActivity().findViewById(R.id.self_scroll_view);
        mHead= (SimpleDraweeView) headView.findViewById(R.id.mine_head);
        mHead.setOnClickListener(this);
        headView.findViewById(R.id.mine_login).setOnClickListener(this);
        headView.findViewById(R.id.mine_register).setOnClickListener(this);

        initContentView(contentView);
        zoomView.findViewById(R.id.mine_setting).setOnClickListener(this);
        zoomView.findViewById(R.id.mine_cover).setOnClickListener(this);
        zoomView.findViewById(R.id.rl_head_bg);

        contentView.findViewById(R.id.mine_tenement).setOnClickListener(this);
        contentView.findViewById(R.id.mine_company).setOnClickListener(this);

        llLogin= (LinearLayout) headView.findViewById(R.id.self_ll_login);
        llUnLogin= (LinearLayout) headView.findViewById(R.id.self_ll_un_login);
        mNick= (TextView) headView.findViewById(R.id.self_nickname);
        mCover= (SimpleDraweeView) zoomView.findViewById(R.id.rl_head_bg);


        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
        int mScreenWidth = localDisplayMetrics.widthPixels;
        LinearLayout.LayoutParams localObject = new LinearLayout.LayoutParams(mScreenWidth, (int) (12.0F * (mScreenWidth / 16.0F)));
        scrollView.setHeaderLayoutParams(localObject);
        scrollView.setHeaderView(headView);
        scrollView.setZoomView(zoomView);
        scrollView.setScrollContentView(contentView);
    }

    private void initContentView(View contentView) {
        contentView.findViewById(R.id.mine_verify).setOnClickListener(this);
        contentView.findViewById(R.id.mine_pay).setOnClickListener(this);
        contentView.findViewById(R.id.mine_receive).setOnClickListener(this);
        contentView.findViewById(R.id.mine_finish).setOnClickListener(this);
        contentView.findViewById(R.id.mine_order).setOnClickListener(this);
        contentView.findViewById(R.id.mine_reserve).setOnClickListener(this);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SelfEvent event) {
        setLoginState(true);
    }

    @Override
    protected BasePresenter getPresenter() {
        return new MinePresenter();
    }

    @Override
    public void onClick(View view) {
        if (!checkUser(getActivity()))return;
        final Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.mine_head:
                intent.setClass(getActivity(),SelfActivity.class);
                startActivity(intent);
                break;
            case R.id.mine_login:
                intent.setClass(getActivity(),LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.mine_register:
                intent.setClass(getActivity(),LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.mine_verify:
                intent.setClass(getActivity(),VerifyActivity.class);
                startActivity(intent);
                break;
            case R.id.mine_setting:
                intent.setClass(getActivity(),SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.mine_cover:
                ChoosePhotoPanel window=new ChoosePhotoPanel(getContext(), new ChoosePhotoPanel.OnSelectedFinished() {
                    @Override
                    public void onSelected(String path) {
                        TribeUploader.getInstance().uploadFile("cover.jpeg", "", new File(path), new TribeUploader.UploadCallback() {
                            @Override
                            public void uploadSuccess(UploadAccessResponse.UploadResponseBody url) {
                                ToastUtils.ToastMessage(mContext,mContext.getString(R.string.upload_success));
                                FresoUtils.loadImage(url.url,mCover);
                            }

                            @Override
                            public void uploadFail() {
                                ToastUtils.ToastMessage(mContext,mContext.getString(R.string.upload_fail));
                            }
                        });
                    }
                });
                window.show();
                break;
            case R.id.mine_wallet:
                intent.setClass(getActivity(),WalletActivity.class);
                startActivity(intent);
                break;
            case R.id.mine_order:
                intent.setClass(getActivity(),OrderActivity.class);
                startActivity(intent);
                break;
            case R.id.mine_reserve:
                intent.setClass(getActivity(),ReserveActivity.class);
                startActivity(intent);
                break;
            case R.id.mine_company:
                TribeRetrofit.getIntance().createApi(CompanyService.class).queryCompany(TribeApplication.getInstance().getUserInfo().getId())
                        .enqueue(new Callback<CompanyQueryResponse>() {
                            @Override
                            public void onResponse(Call<CompanyQueryResponse> call, Response<CompanyQueryResponse> response) {
                                if (response.body().code==200) {
                                    CompanyDetail detail = response.body().data;
                                    if (detail ==null) {
                                        //未绑定
                                        intent.setClass(mContext,CompanyActivity.class);
                                        startActivity(intent);
                                    }else {
                                        Log.d(TAG, "onResponse: "+detail);
                                        intent.setClass(mContext, CompanyDetailActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable(Constant.COMPANY_FLAG,detail);
                                        intent.putExtras(bundle);
                                        startActivity(intent);
                                    }
                                }else if(response.body().code==404){
                                    ToastUtils.ToastMessage(mContext,"找不到用户信息");
                                }
                            }

                            @Override
                            public void onFailure(Call<CompanyQueryResponse> call, Throwable t) {

                            }
                        });

                break;
            case R.id.mine_tenement:
                intent.setClass(getActivity(),PropertyActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void setLoginState(boolean loginState) {
        if (null==llUnLogin||null==llLogin)return;
        if (loginState){
            llLogin.setVisibility(View.VISIBLE);
            llUnLogin.setVisibility(View.GONE);
            String nickname = TribeApplication.getInstance().getUserInfo().getNickname();
            if (!TextUtils.isEmpty(nickname)){
                mNick.setText(nickname);
            }else {
                mNick.setText("");
            }
            FresoUtils.loadImage(TribeApplication.getInstance().getUserInfo().getPicture(),mHead);
        }else {
            llLogin.setVisibility(View.GONE);
            llUnLogin.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
