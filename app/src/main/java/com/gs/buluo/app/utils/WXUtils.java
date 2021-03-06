package com.gs.buluo.app.utils;

import com.gs.buluo.app.Constant;
import com.gs.buluo.app.TribeApplication;
import com.gs.buluo.app.bean.OrderPayment;
import com.gs.buluo.app.bean.PrepareOrderRequest;
import com.gs.buluo.app.bean.WxPayResponse;
import com.gs.buluo.app.network.MoneyApis;
import com.gs.buluo.app.network.TribeRetrofit;
import com.gs.buluo.common.network.ApiException;
import com.gs.buluo.common.network.BaseResponse;
import com.gs.buluo.common.network.BaseSubscriber;
import com.gs.buluo.common.utils.ToastUtils;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.UUID;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hjn on 2016/12/30.
 */

public class WXUtils {
    private static WXUtils wxPayUtils;
    private static IWXAPI msgApi = null;


    private WXUtils() {
        msgApi = WXAPIFactory.createWXAPI(TribeApplication.getInstance().getApplicationContext(), null);
        // 将该app注册到微信
        msgApi.registerApp(Constant.Base.WX_ID);
    }

    public static WXUtils getInstance() {
        if (wxPayUtils == null) {
            wxPayUtils = new WXUtils();
        }
        return wxPayUtils;
    }

    public void payInWechat(OrderPayment data) {
        PrepareOrderRequest request = new PrepareOrderRequest();
        request.totalFee = data.totalAmount;
        request.paymentId = data.id;
        request.targetId = data.ownerAccountId;
        TribeRetrofit.getInstance().createApi(MoneyApis.class).preparePayInWx(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<BaseResponse<WxPayResponse>>() {
                    @Override
                    public void onNext(BaseResponse<WxPayResponse> wxPayResponseBaseResponse) {
                        doPay(wxPayResponseBaseResponse.data);
                    }

                    @Override
                    public void onFail(ApiException e) {
                        ToastUtils.ToastMessage(TribeApplication.getInstance().getApplicationContext(), e.getDisplayMessage());
                    }
                });
    }

    public void doPay(WxPayResponse data) {
        PayReq request = new PayReq();
        request.appId = data.appid;
        request.partnerId = data.partnerid;
        request.prepayId = data.prepayid;
        request.packageValue = data.packageValue;
        request.nonceStr = data.noncestr;
        request.timeStamp = data.timestamp;
        request.sign = data.sign;
        msgApi.sendReq(request);

//        msgApi.registerApp(Constant.WX_ID);
//        PayReq request = new PayReq();
//        request.appId = Constant.WX_ID;
//        request.partnerId = Constant.WX_SHOP_ID;
//        request.prepayId= "1101000000140415649af9fc314aa427";
//        request.packageValue = "Sign=WXPay";
//        request.nonceStr= CommonUtils.getRandomString(32);
//        request.timeStamp= SystemClock.currentThreadTimeMillis()/1000+"";
//        request.sign= Constant.WX_SIGN;
    }

    public void doLogin() {
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = UUID.randomUUID().toString();
        msgApi.sendReq(req);
    }
}
