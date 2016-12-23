package com.gs.buluo.app.view.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.gs.buluo.app.Constant;
import com.gs.buluo.app.R;
import com.gs.buluo.app.bean.DetailStoreSetMeal;
import com.gs.buluo.app.bean.ResponseBody.StoreDetailResponse;
import com.gs.buluo.app.bean.StoreDetail;
import com.gs.buluo.app.model.CommunityModel;
import com.gs.buluo.app.model.ServeModel;
import com.gs.buluo.app.utils.FrescoImageLoader;
import com.gs.buluo.app.utils.FresoUtils;
import com.gs.buluo.app.utils.ToastUtils;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hjn on 2016/12/20.
 */
public class StoreDetailActivity extends BaseActivity implements Callback<StoreDetailResponse>, View.OnClickListener {
    Context mCtx;
    TextView tvName;
    private TextView tvPrice;
    private TextView tvCollectNum;
    private TextView tvAddress;
    private TextView tvPhone;
    private TextView tvReason;
    private TextView tvMarkplace;
    private TextView tvDistance;
    private TextView tvBrand;
    private TextView tvTime;
    private TextView tvTopic;
    private Banner banner;
    private String id;
    private SimpleDraweeView logo;
    @Override
    protected void bindView(Bundle savedInstanceState) {
        id = getIntent().getStringExtra(Constant.STORE_ID);
        getDetailInfo(id);
        mCtx=this;
        setBarColor(R.color.transparent);
        initContentView();
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_service_detail;
    }

    private void initContentView() {
        banner = (Banner) findViewById(R.id.server_detail_banner);
        tvName = (TextView)findViewById(R.id.server_detail_name);
        tvPrice =  (TextView)findViewById(R.id.server_detail_person_price);
        tvCollectNum = (TextView)findViewById(R.id.server_detail_collect);
        tvAddress = (TextView)findViewById(R.id.service_shop_address);
        tvPhone = (TextView)findViewById(R.id.service_shop_number);
        tvReason = (TextView)findViewById(R.id.server_detail_comment_reason);
        tvMarkplace = (TextView)findViewById(R.id.server_detail_markPlace);
        tvDistance = (TextView)findViewById(R.id.server_detail_distance);
        tvBrand = (TextView)findViewById(R.id.server_detail_category);
        tvTime = (TextView)findViewById(R.id.server_detail_work_time);
        tvTopic = (TextView)findViewById(R.id.server_detail_topic);
        logo= (SimpleDraweeView) findViewById(R.id.server_detail_logo);

        findViewById(R.id.service_phone_call).setOnClickListener(this);
        findViewById(R.id.service_location).setOnClickListener(this);
        findViewById(R.id.service_call_server).setOnClickListener(this);
        findViewById(R.id.service_booking_food).setOnClickListener(this);
        findViewById(R.id.service_booking_seat).setOnClickListener(this);
        findViewById(R.id.server_detail_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.service_phone_call:
                intent.setAction(Intent.ACTION_DIAL);
                Uri data = Uri.parse("tel:" + tvPhone.getText().toString());
                intent.setData(data);
                startActivity(intent);
                break;
            case R.id.service_location:
                intent.setClass(mCtx,MapActivity.class);
                startActivity(intent);
                break;
            case R.id.service_booking_food:
                if (!checkUser(mCtx))return;
                ToastUtils.ToastMessage(mCtx,"功能暂未开通，敬请期待");
                break;
            case R.id.service_booking_seat:
                intent.setClass(mCtx, BookingServeActivity.class);
                intent.putExtra(Constant.SERVE_ID,id);
                startActivity(intent);
                break;
            case R.id.server_detail_back:
                finish();
                break;
            case R.id.service_call_server:
                intent.setAction(Intent.ACTION_DIAL);
                Uri data1 = Uri.parse("tel:" + "123456789");
                intent.setData(data1);
                startActivity(intent);
                break;
        }
    }

    private void getDetailInfo(String id) {
        showLoadingDialog();
        new CommunityModel().getStoreDetail(id,this);
    }

    @Override
    public void onResponse(Call<StoreDetailResponse> call, Response<StoreDetailResponse> response) {
        dismissDialog();
        if (response.body()!=null&&response.body().code==200&&response.body().data!=null){
            StoreDetail data = response.body().data;
            setData(data);
        }
    }

    private void setData(StoreDetail data) {
        banner.setBannerStyle(BannerConfig.NUM_INDICATOR);
        banner.setIndicatorGravity(BannerConfig.RIGHT);
        banner.setImageLoader(new FrescoImageLoader());
        banner.isAutoPlay(false);
        banner.setImages(data.pictures);
        banner.start();
        tvName.setText(data.name);
        tvPhone.setText(data.phone);
        tvAddress.setText(data.address);
        tvCollectNum.setText(data.popularValue+"");
        tvMarkplace.setText(data.markPlace);
//        tvPrice.setText(data.personExpense);
//        tvReason.setText(data.recommendedReason);
        tvBrand.setText(data.brand);
        tvTime.setText("每天 "+data.businessHours);
        FresoUtils.loadImage(data.logo,logo);
//        tvTopic.setText(data.topics);
    }

    @Override
    public void onFailure(Call<StoreDetailResponse> call, Throwable t) {

    }
}
