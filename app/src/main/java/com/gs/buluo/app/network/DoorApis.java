package com.gs.buluo.app.network;

import com.gs.buluo.app.bean.LockEquip;
import com.gs.buluo.app.bean.LockKey;
import com.gs.buluo.app.bean.RequestBodyBean.LockRequest;
import com.gs.buluo.app.bean.ResponseBody.CodeResponse;
import com.gs.buluo.app.bean.VisitorActiveBean;
import com.gs.buluo.common.network.BaseResponse;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by hjn on 2017/3/13.
 */

public interface DoorApis {
    @GET("locks?type=owner")
    Observable<BaseResponse<List<LockEquip>>> getEquipList(@Query("me") String uid);

    @POST("keys")
    Observable<BaseResponse<LockKey>> getLockKey(@Query("me") String uid, @Body() LockRequest request);

    @DELETE("keys/{id}")
    Observable<BaseResponse<CodeResponse>> deleteEquip(@Path("id")String id,@Query("me") String uid);

    @GET("keys")
    Observable<BaseResponse<List<VisitorActiveBean>>> getVisitorList(@Query("me") String uid);
 }
