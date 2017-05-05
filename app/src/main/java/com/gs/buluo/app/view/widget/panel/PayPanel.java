package com.gs.buluo.app.view.widget.panel;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.gs.buluo.app.R;
import com.gs.buluo.app.TribeApplication;
import com.gs.buluo.app.bean.BankCard;
import com.gs.buluo.app.bean.BankOrderResponse;
import com.gs.buluo.app.bean.OrderBean;
import com.gs.buluo.app.bean.OrderPayment;
import com.gs.buluo.app.bean.PrepareOrderRequest;
import com.gs.buluo.app.bean.RequestBodyBean.NewPaymentRequest;
import com.gs.buluo.app.bean.WalletAccount;
import com.gs.buluo.app.network.MoneyApis;
import com.gs.buluo.app.network.TribeRetrofit;
import com.gs.buluo.app.utils.CommonUtils;
import com.gs.buluo.app.utils.DensityUtils;
import com.gs.buluo.app.view.activity.UpdateWalletPwdActivity;
import com.gs.buluo.app.view.widget.CustomAlertDialog;
import com.gs.buluo.common.network.BaseResponse;
import com.gs.buluo.common.network.BaseSubscriber;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hjn on 2016/12/7.
 */
public class PayPanel extends Dialog implements PasswordPanel.OnPasswordPanelDismissListener, View.OnClickListener {
    private final OnPayPanelDismissListener onDismissListener;
    private Context mContext;
    @Bind(R.id.pay_way)
    TextView tvWay;
    @Bind(R.id.pay_money)
    TextView tvTotal;

    private OrderBean.PayChannel payWay = OrderBean.PayChannel.BALANCE;
    private String payWayString = OrderBean.PayChannel.BALANCE.toString();
    private List<String> orderId;
    private View rootView;
    private String price;
    private String type;
    private BankCard mBankCard;

    public PayPanel(Context context, OnPayPanelDismissListener onDismissListener) {
        super(context, R.style.my_dialog);
        mContext = context;
        this.onDismissListener = onDismissListener;
        initView();
    }

    public void setData(String price, List<String> orderId, String type) {
        tvWay.setText(payWay.toString());
        this.price = price;
        tvTotal.setText(price);
        this.orderId = orderId;
        this.type = type;
    }

    private void initView() {
        rootView = LayoutInflater.from(mContext).inflate(R.layout.pay_board, null);
        setContentView(rootView);
        ButterKnife.bind(this);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = DensityUtils.dip2px(mContext, 400);
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);

        rootView.findViewById(R.id.pay_ask).setOnClickListener(this);
        rootView.findViewById(R.id.pay_close).setOnClickListener(this);
        rootView.findViewById(R.id.pay_finish).setOnClickListener(this);
        rootView.findViewById(R.id.pay_choose_area).setOnClickListener(this);
    }


    public void getWalletInfo() {
        TribeRetrofit.getInstance().createApi(MoneyApis.class).
                getWallet(TribeApplication.getInstance().getUserInfo().getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<BaseResponse<WalletAccount>>() {
                    @Override
                    public void onNext(BaseResponse<WalletAccount> response) {
                        String password = response.data.password;
                        String balance = response.data.balance;
                        if (password == null) {
                            showAlert();
                        } else {
                            if (Float.parseFloat(price) > Float.parseFloat(balance)) {
                                showNotEnough(balance);
                            } else {
                                showPasswordPanel(password);
                            }
                        }
                    }
                });
    }

    private void showNotEnough(final String balance) {
        new CustomAlertDialog.Builder(getContext()).setTitle("提示").setMessage("您账户余额不足，请先去充值")
                .setPositiveButton("去充值", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RechargePanel panel = new RechargePanel(mContext);
                        panel.setData(balance);
                        panel.show();
                    }
                }).setNegativeButton("取消", null).create().show();
    }

    private void showAlert() {
        new CustomAlertDialog.Builder(getContext()).setTitle("提示").setMessage("您还没有设置支付密码，请先去设置密码")
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getContext().startActivity(new Intent(getContext(), UpdateWalletPwdActivity.class));
                    }
                }).setNegativeButton("取消", null).create().show();
    }

    private void showPasswordPanel(String password) {
        PasswordPanel passwordPanel = new PasswordPanel(mContext, password, orderId, payWay, type, this);
        passwordPanel.show();
        TranslateAnimation animation = new TranslateAnimation(0, -CommonUtils.getScreenWidth(mContext), 0, 0);
        animation.setDuration(500);
        animation.setFillAfter(true);
        animation.start();
        rootView.startAnimation(animation);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pay_close:
                dismiss();
                break;
            case R.id.pay_finish:
                if (payWayString.equals(OrderBean.PayChannel.BALANCE.toString()))
                    getWalletInfo();
                else if (payWayString.equals(OrderBean.PayChannel.WEICHAT.toString())) {
//                    payInWx();
                } else if (!payWayString.equals("")) {
                    applyBankCardPay();
                } else {
                    dismiss();
                }
                break;
            case R.id.pay_choose_area:
                PayChoosePanel payChoosePanel = new PayChoosePanel(mContext, new PayChoosePanel.onChooseFinish() {
                    @Override
                    public void onChoose(String payChannel, BankCard bankCard) {
                        payWayString = payChannel;
                        tvWay.setText(payWayString);
                        mBankCard = bankCard;
                    }
                });
                payChoosePanel.show();
                break;
        }
    }

    private void applyBankCardPay() {
        NewPaymentRequest request = new NewPaymentRequest();
        request.orderIds = orderId;
        request.payChannel = "BF_BANKCARD";
        TribeRetrofit.getInstance().createApi(MoneyApis.class).
                createPayment(TribeApplication.getInstance().getUserInfo().getId(), type, request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<BaseResponse<OrderPayment>>() {
                    @Override
                    public void onNext(BaseResponse<OrderPayment> response) {
                        PrepareOrderRequest prepareOrderRequest = new PrepareOrderRequest();
                        prepareOrderRequest.bankCardId = mBankCard.id;
                        prepareOrderRequest.totalFee = response.data.totalAmount;
                        prepareOrderRequest.paymentId = response.data.id;
                        TribeRetrofit.getInstance().createApi(MoneyApis.class).
                                prepareOrder(TribeApplication.getInstance().getUserInfo().getId(), prepareOrderRequest)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new BaseSubscriber<BaseResponse<BankOrderResponse>>() {
                                    @Override
                                    public void onNext(BaseResponse<BankOrderResponse> response) {
                                        new BfPayVerifyCodePanel(mContext, mBankCard.phone, response.data.result, PayPanel.this).show();
                                    }
                                });
                    }
                });
    }

    @Override
    public void onPasswordPanelDismiss(boolean successful) {
        dismiss();
    }

    public interface OnPayPanelDismissListener {
        void onPayPanelDismiss();
    }

    @Override
    public void dismiss() {
        if (onDismissListener != null) {
            onDismissListener.onPayPanelDismiss();
        }
        super.dismiss();
    }
}