package com.gs.buluo.app.model;

import com.gs.buluo.app.bean.ResponseBody.CommunityDetailResponse;
import com.gs.buluo.app.bean.ResponseBody.CommunityResponse;
import com.gs.buluo.app.bean.ResponseBody.StoreDetailResponse;
import com.gs.buluo.app.network.CommunityService;
import com.gs.buluo.app.network.TribeRetrofit;

import retrofit2.Callback;

/**
 * Created by hjn on 2016/11/28.
 */
public class CommunityModel {
    public void getCommunitiesList(Callback<CommunityResponse> callback) {
        TribeRetrofit.getInstance().createApi(CommunityService.class).
                getCommunitiesList().enqueue(callback);
    }

    public void getCommunityDetail(String uid,Callback<CommunityDetailResponse> callback){
        TribeRetrofit.getInstance().createApi(CommunityService.class).
                getCommunityDetail(uid).enqueue(callback);
    }

    public void getStoreDetail(String strorId,Callback<StoreDetailResponse> callback){
        TribeRetrofit.getInstance().createApi(CommunityService.class).
                getStoreDetail(strorId).enqueue(callback);
    }
}
