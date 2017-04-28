package com.gs.buluo.app.view.widget.panel;

import android.app.Dialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.gs.buluo.app.R;
import com.gs.buluo.app.TribeApplication;
import com.gs.buluo.app.bean.BankOrderResponse;
import com.gs.buluo.app.bean.ConfirmOrderRequest;
import com.gs.buluo.app.bean.QueryOrderRequest;
import com.gs.buluo.app.network.MoneyApis;
import com.gs.buluo.app.network.TribeRetrofit;
import com.gs.buluo.app.utils.DensityUtils;
import com.gs.buluo.app.utils.ToastUtils;
import com.gs.buluo.common.network.ApiException;
import com.gs.buluo.common.network.BaseResponse;
import com.gs.buluo.common.network.BaseSubscriber;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Solang on 2017/4/26.
 */


public class BfRechargeVerifyCodePanel extends Dialog {
    private final Context mContext;
    private final String mResult;
    @Bind(R.id.et_verify_code)
    EditText etVerifyCode;
    @Bind(R.id.reGet_verify_code)
    TextView reGetVerifyCode;
    @Bind(R.id.tv_finish)
    TextView tvFinish;
    @Bind(R.id.tv_phone)
    TextView tvPhone;
    private RechargePanel mRechargePanel;
    private String mPhoneNumber;

    public BfRechargeVerifyCodePanel(Context context, String phoneNumber,String result, RechargePanel rechargePanel) {
        super(context, R.style.pay_dialog);
        mContext = context;
        mResult = result;
        mRechargePanel = rechargePanel;
        mPhoneNumber = phoneNumber;

        initView();
    }

    private void initView() {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.verify_board, null);
        setContentView(rootView);
        ButterKnife.bind(this);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = DensityUtils.dip2px(mContext, 450);
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        timing();
        tvPhone.setText(mPhoneNumber.substring(0,3)+"****"+mPhoneNumber.substring(7,11));
        tvFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFinish();
            }
        });


        rootView.findViewById(R.id.pwd_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        reGetVerifyCode.setClickable(false);
        reGetVerifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerifyCode("");
            }
        });
    }

    private void jumpOnSuccess() {
        dismiss();
        mRechargePanel.dismiss();
    }

    private void onFinish() {
        ConfirmOrderRequest request = new ConfirmOrderRequest();
        request.rechargeId = mResult;
        request.vcode = etVerifyCode.getText().toString().trim();
        TribeRetrofit.getInstance().createApi(MoneyApis.class).confirmOrder(TribeApplication.getInstance().getUserInfo().getId(), request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<BaseResponse<BankOrderResponse>>() {
                    @Override
                    public void onNext(BaseResponse<BankOrderResponse> bankOrderResponseBaseResponse) {
                        switch (bankOrderResponseBaseResponse.data.result) {
                            case "1":
                                ToastUtils.ToastMessage(mContext, R.string.success);
                                jumpOnSuccess();
                                break;
                            case "2":
                                ToastUtils.ToastMessage(mContext, R.string.failure);
                                break;
                            case "3":
                                ToastUtils.ToastMessage(mContext, R.string.dealing);
                                new CountDownTimer(3000, 1000) {
                                    @Override
                                    public void onTick(long l) {

                                    }

                                    @Override
                                    public void onFinish() {
                                        QueryOrderRequest request = new QueryOrderRequest();
                                        request.value = mResult;
                                        TribeRetrofit.getInstance().createApi(MoneyApis.class).queryOrder(TribeApplication.getInstance().getUserInfo().getId(), request)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new BaseSubscriber<BaseResponse<BankOrderResponse>>() {
                                                    @Override
                                                    public void onNext(BaseResponse<BankOrderResponse> bankOrderResponseBaseResponse) {
                                                        switch (bankOrderResponseBaseResponse.data.result) {
                                                            case "1":
                                                                ToastUtils.ToastMessage(mContext, R.string.success);
                                                                jumpOnSuccess();
                                                                break;
                                                            case "2":
                                                                ToastUtils.ToastMessage(mContext, R.string.failure);
                                                                break;
                                                        }
                                                    }
                                                });
                                    }
                                }.start();
                                break;
                            case "4":
                                ToastUtils.ToastMessage(mContext, R.string.unpay);
                                break;
                        }
                    }

                    @Override
                    public void onFail(ApiException e) {
                        super.onFail(e);
                        etVerifyCode.setText("");
                    }
                });
    }

    private void sendVerifyCode(String phone) {
//        if (TextUtils.isEmpty(phone)) {
//            ToastUtils.ToastMessage(mContext, R.string.phone_not_empty);
//            return;
//        }
        dealWithIdentify(202);
    }


    private void dealWithIdentify(int code) {
        switch (code) {
            case 202:
                reGetVerifyCode.setText("60s");
                timing();
                break;
            case 400:
                ToastUtils.ToastMessage(mContext, mContext.getString(R.string.wrong_number));
                break;
        }
    }

    private void timing() {
        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                reGetVerifyCode.setClickable(false);
                reGetVerifyCode.setText("(" + millisUntilFinished / 1000 + ")重新获取");
            }

            @Override
            public void onFinish() {
                reGetVerifyCode.setText("重新获取");
                reGetVerifyCode.setClickable(true);
            }
        }.start();
    }

}