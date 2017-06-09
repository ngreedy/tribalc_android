package com.gs.buluo.app.view.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.gs.buluo.app.Constant;
import com.gs.buluo.app.R;
import com.gs.buluo.app.TribeApplication;
import com.gs.buluo.app.bean.AppConfigInfo;
import com.gs.buluo.app.bean.ConfigInfo;
import com.gs.buluo.app.bean.PromotionInfo;
import com.gs.buluo.app.network.MainApis;
import com.gs.buluo.app.network.TribeRetrofit;
import com.gs.buluo.app.utils.SharePreferenceManager;
import com.gs.buluo.common.UpdateEvent;
import com.gs.buluo.common.network.BaseResponse;
import com.gs.buluo.common.network.BaseSubscriber;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by hjn on 2016/11/3.
 */
public class AppStartActivity extends BaseActivity {
    public DetailLocationListener myListener = new DetailLocationListener();

    @Bind(R.id.version_name)
    TextView version;
    @Bind(R.id.app_start_bg)
    ImageView viewBg;
    @Bind(R.id.start_second)
    TextView tvSecond;
    @Bind(R.id.start_jump_area)
    View secondView;
    private LocationClient mLocClient;
    private String versionName;
    private Subscriber<Long> subscriber;
    private List<PromotionInfo> promotionInfos;
    private PromotionInfo current = null;
    @Override
    protected void bindView(Bundle savedInstanceState) {
        setBarColor(R.color.transparent);
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            version.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        mLocClient.start();

        String json = SharePreferenceManager.getInstance(getApplicationContext()).getStringValue(Constant.APP_START);
        promotionInfos = JSON.parseArray(json, PromotionInfo.class);
        long currentTime = System.currentTimeMillis();
        if (promotionInfos != null && promotionInfos.size() != 0) {
            Iterator<PromotionInfo> iterator = promotionInfos.iterator();
            while (iterator.hasNext()) {
                PromotionInfo info = iterator.next();
                if (currentTime > info.endTime) {
                    iterator.remove();
                    continue;
                }
                if (currentTime >= info.startTime && currentTime <= info.endTime) {
                    current = info;
                }
            }
        }
        if (current != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.click_jump_area).setVisibility(View.VISIBLE);
                    setData(current);
                }
            },1500);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    beginActivity();
                }
            }, 2000);
        }

        String uid = TribeApplication.getInstance().getUserInfo() == null ? null : TribeApplication.getInstance().getUserInfo().getId();
        TribeRetrofit.getInstance().createApi(MainApis.class).getConfig(uid, versionName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<BaseResponse<ConfigInfo>>(false) {
                    @Override
                    public void onNext(BaseResponse<ConfigInfo> response) {
                        saveData(response.data);


                    }
                });
    }

    private void saveData(ConfigInfo data) {
        if (promotionInfos != null && promotionInfos.size() != 0) {
            if (!TextUtils.equals(data.promotions.url, promotionInfos.get(promotionInfos.size() - 1).url)) {
                promotionInfos.add(data.promotions);
            }
        } else {
            promotionInfos = new ArrayList<>();
            promotionInfos.add(data.promotions);
        }
        SharePreferenceManager.getInstance(getApplicationContext()).setValue(Constant.APP_START, JSON.toJSONString(promotionInfos));

        TribeApplication.getInstance().setBf_recharge(data.switches.bf_recharge);
        TribeApplication.getInstance().setBf_withdraw(data.switches.bf_withdraw);

        final AppConfigInfo app = data.app;
        if (TextUtils.equals(app.lastVersion,SharePreferenceManager.getInstance(getCtx()).getStringValue(Constant.CANCEL_UPDATE_VERSION))){
            return;
        }
        if (!TextUtils.equals(app.lastVersion.substring(0,app.lastVersion.lastIndexOf(".")), versionName.substring(0,app.lastVersion.lastIndexOf(".")))) {
            EventBus.getDefault().postSticky(new UpdateEvent(app.supported, app.lastVersion, app.releaseNote));
        }
    }

    private void beginActivity() {
        if (SharePreferenceManager.getInstance(this).getBooeanValue("guide" + getVersionCode())) {
            SharePreferenceManager.getInstance(this).setValue("guide" + getVersionCode(), false);
            startActivity(new Intent(AppStartActivity.this, GuideActivity.class));
            finish();
        } else {
            startActivity(new Intent(AppStartActivity.this, MainActivity.class));
            finish();
        }
    }


    @Override
    protected void init() {
        // 判断是否从推送通知栏打开的
        XGPushClickedResult click = XGPushManager.onActivityStarted(this);
        if (click != null) {

            //从推送通知栏打开-Service打开Activity会重新执行Laucher流程
            //查看是不是全新打开的面板
//            Toast.makeText(this, click.getCustomContent(), Toast.LENGTH_SHORT).show();
            if (isTaskRoot()) {
                return;
            }
            //如果有面板存在则关闭当前的面板
            finish();
        }
        File file = new File(Constant.DIR_PATH);
        if (!file.exists()) file.mkdirs();
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_start;
    }


    public int getVersionCode() {
        PackageManager manager;
        PackageInfo info = null;
        manager = this.getPackageManager();
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setData(PromotionInfo data) {
        if (data.canSkip) {
            secondView.setVisibility(View.VISIBLE);
            findViewById(R.id.click_jump_area).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    beginActivity();
                }
            });
        }
        Glide.with(this).load(data.url).into(viewBg);
        startTime = data.skipSeconds;
        startCounter();
    }

    int startTime = 1;

    private void startCounter() {
        tvSecond.setText(startTime + "");
        subscriber = new Subscriber<Long>() {
            @Override
            public void onCompleted() {
                beginActivity();
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Long aLong) {
                tvSecond.setText(aLong + "");
            }
        };
        Observable.interval(0, 1, TimeUnit.SECONDS).take(startTime + 1)
                .map(new Func1<Long, Long>() {
                    @Override
                    public Long call(Long time) {
                        return startTime - time;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }


    public class DetailLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location != null) {
                LatLng myPos = new LatLng(location.getLatitude(),
                        location.getLongitude());

                TribeApplication.getInstance().setPosition(myPos);
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocClient.stop();
        if (subscriber != null) subscriber.unsubscribe();
    }

}
