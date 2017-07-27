package com.gs.buluo.app.view.widget.panel;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;

import com.gs.buluo.app.R;
import com.gs.buluo.app.TribeApplication;
import com.gs.buluo.app.bean.BankCard;
import com.gs.buluo.app.bean.OrderPayment;
import com.gs.buluo.app.bean.Pay2MerchantRequest;
import com.gs.buluo.app.bean.PayChannel;
import com.gs.buluo.app.bean.WalletAccount;
import com.gs.buluo.app.network.MoneyApis;
import com.gs.buluo.app.network.TribeRetrofit;
import com.gs.buluo.app.utils.BFUtil;
import com.gs.buluo.app.utils.CommonUtils;
import com.gs.buluo.app.utils.DensityUtils;
import com.gs.buluo.app.utils.ToastUtils;
import com.gs.buluo.app.view.activity.NMainActivity;
import com.gs.buluo.app.view.activity.UpdateWalletPwdActivity;
import com.gs.buluo.app.view.widget.CustomAlertDialog;
import com.gs.buluo.common.network.ApiException;
import com.gs.buluo.common.network.BaseResponse;
import com.gs.buluo.common.network.BaseSubscriber;
import com.gs.buluo.common.widget.LoadingDialog;

import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hjn on 2016/12/7.
 */
public class NewPayPanel extends Dialog implements View.OnClickListener, BFUtil.OnBFPayStatusListener {
    private final OnPayPanelDismissListener onDismissListener;
    private Context mContext;
    @Bind(R.id.pay_way)
    TextView tvWay;
    @Bind(R.id.pay_money)
    TextView tvTotal;

    private PayChannel payWay = PayChannel.BALANCE;
    private String payWayString = PayChannel.BALANCE.toString();
    private View rootView;
    private String totalFee;
    private String targetId;
    private BankCard mBankCard;
    private String paymentType;

    public NewPayPanel(Context context) {
        this(context, null);
    }

    public NewPayPanel(Context context, OnPayPanelDismissListener onDismissListener) {
        super(context, R.style.my_dialog);
        mContext = context;
        this.onDismissListener = onDismissListener;
        initView();
    }

    public void setData(String price, String storeId, String type) {
        tvWay.setText(payWay.value);
        this.totalFee = price;
        tvTotal.setText(price);
        this.targetId = storeId;
        paymentType = type;
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


    private void getWalletInfo() {
        LoadingDialog.getInstance().show(getContext(), "", true);
        String id = TribeApplication.getInstance().getUserInfo().getId();
        TribeRetrofit.getInstance().createApi(MoneyApis.class).
                getWallet(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<BaseResponse<WalletAccount>>() {
                    @Override
                    public void onNext(BaseResponse<WalletAccount> response) {
                        String password = response.data.password;
                        float balance = (float) response.data.balance;
                        if (password == null) {
                            showAlert();
                        } else {
                            if (Float.parseFloat(totalFee) > (balance)) {
                                showNotEnough(balance);
                            } else {
                                showPasswordPanel(password);
                            }
                        }
                    }
                });
    }

    private void showNotEnough(final float balance) {
        new CustomAlertDialog.Builder(getContext()).setTitle("提示").setMessage("您账户余额不足，请先去充值")
                .setPositiveButton("去充值", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RechargePanel panel = new RechargePanel(mContext,TribeApplication.getInstance().getUserInfo().getId());
                        panel.setData(balance);
                        panel.show();
                    }
                }).setNegativeButton("取消", null).create().show();
    }

    private void showAlert() {
        new CustomAlertDialog.Builder(getContext()).setTitle("提示").setMessage("您还没有设置支付密码，请先去设置密码")
                .setPositiveButton("去设置", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getContext().startActivity(new Intent(getContext(), UpdateWalletPwdActivity.class));
                    }
                }).setNegativeButton("取消", null).create().show();
    }

    private void showPasswordPanel(final String password) {
        NewPasswordPanel passwordPanel = new NewPasswordPanel(mContext, password, new NewPasswordPanel.OnPwdFinishListener() {
            @Override
            public void onPwdFinishListener(String strPassword) {
                createPayment(strPassword);
            }
        });
        passwordPanel.show();
        TranslateAnimation animation = new TranslateAnimation(0, -CommonUtils.getScreenWidth(mContext), 0, 0);
        animation.setDuration(500);
        animation.setFillAfter(true);
        animation.start();
        rootView.startAnimation(animation);
    }

    private void createPayment(final String password) {
        LoadingDialog.getInstance().show(mContext, "", true);
        Pay2MerchantRequest request = new Pay2MerchantRequest();
        request.payChannel = payWay.name();
        if (password != null) request.password = password;
        request.targetId = targetId;
        request.totalFee = totalFee;
        TribeRetrofit.getInstance().createApi(MoneyApis.class).pay2Merchant(TribeApplication.getInstance().getUserInfo().getId(), request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseResponse<OrderPayment>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        LoadingDialog.getInstance().dismissDialog();
                        if (e instanceof ApiException && ((ApiException) e).getCode() == 412) {
                            createDialog();
                            return;
                        }
                        if (e instanceof ApiException && ((ApiException) e).getDisplayMessage() != null) {
                            ToastUtils.ToastMessage(getContext(), ((ApiException) e).getDisplayMessage());
                        } else {
                            ToastUtils.ToastMessage(getContext(), R.string.connect_fail);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<OrderPayment> orderPaymentBaseResponse) {
                        setStatus(password, orderPaymentBaseResponse.data);
                    }
                });
    }

    private AlertDialog checkDialog;

    private void createDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.myCorDialog);
        View view = View.inflate(mContext, R.layout.pay2merchant_error, null);
        builder.setView(view);
        builder.setCancelable(true);
        Button button = (Button) view.findViewById(R.id.btn_pay2m_error_finish);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDialog.dismiss();
                dismiss();
                onDismissListener.onPayPanelDismiss();
            }
        });
        TextView tvContent = (TextView) view.findViewById(R.id.error_dialog_content);
        tvContent.setText("今日优惠买单金额已达上限，\n 暂不可用优惠买单进行支付！");
        checkDialog = builder.create();
        checkDialog.show();
        WindowManager.LayoutParams params = checkDialog.getWindow().getAttributes();
        params.width = DensityUtils.dip2px(mContext, 229);
        params.height = DensityUtils.dip2px(mContext, 223);
        checkDialog.getWindow().setAttributes(params);

    }

    public void setStatus(String password, final OrderPayment data) {
        if (password == null) {
            new BFUtil().doBFPay(mContext, data, mBankCard, this);
        } else {
            if (data.status == OrderPayment.PayStatus.FINISHED || data.status == OrderPayment.PayStatus.PAYED) {
                ToastUtils.ToastMessage(getContext(), R.string.pay_success);
                mContext.startActivity(new Intent(mContext, NMainActivity.class));
                dismiss();
            } else {
                rootView.postDelayed(new TimerTask() {
                    @Override
                    public void run() {
                        getPaymentInfo(data);
                    }
                }, 1000);
            }
        }
    }

    public void getPaymentInfo(OrderPayment data) {
        LoadingDialog.getInstance().show(mContext, "", true);
        TribeRetrofit.getInstance().createApi(MoneyApis.class).getPaymentStatus(TribeApplication.getInstance().getUserInfo().getId(), data.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<BaseResponse<OrderPayment>>() {
                    @Override
                    public void onNext(BaseResponse<OrderPayment> orderPaymentBaseResponse) {
                        setStatusAgain(orderPaymentBaseResponse.data);
                    }
                });
    }

    public void setStatusAgain(final OrderPayment data) {
        if (data.status == OrderPayment.PayStatus.FINISHED || data.status == OrderPayment.PayStatus.PAYED) {
            ToastUtils.ToastMessage(getContext(), R.string.pay_success);
        } else {
            ToastUtils.ToastMessage(getContext(), data.note);
        }
        dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pay_close:
                dismiss();
                break;
            case R.id.pay_finish:
                switch (payWay) {
                    case BALANCE:
                        getWalletInfo();
                        break;
                    case BF_BANKCARD:
                        createPayment(null);
                        break;
                    default:
                        break;
                }
                break;
            case R.id.pay_choose_area:
                PayChoosePanel payChoosePanel = new PayChoosePanel(mContext, new PayChoosePanel.onChooseFinish() {
                    @Override
                    public void onChoose(String payChannel, BankCard bankCard) {
                        payWayString = payChannel;
                        if (payChannel.contains("储蓄卡")) {
                            payWay = PayChannel.BF_BANKCARD;
                            tvWay.setText(payWayString);
                        } else if (payChannel.contains("BALANCE")) {
                            payWay = PayChannel.BALANCE;
                            tvWay.setText(payWay.value);
                        }
                        mBankCard = bankCard;
                    }
                });
                payChoosePanel.show();
                break;
        }
    }

    @Override
    public void onBFSuccess() {
        dismiss();
        Intent intent = new Intent();
        intent.setClass(mContext, NMainActivity.class);
        mContext.startActivity(intent);
    }

    public interface OnPayPanelDismissListener {
        void onPayPanelDismiss();
    }
}