package com.gs.buluo.app.view.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gs.buluo.app.R;
import com.gs.buluo.app.bean.CompanyAccount;
import com.gs.buluo.app.presenter.BasePresenter;
import com.gs.buluo.app.presenter.CompanyManagerPresenter;
import com.gs.buluo.app.utils.ToastUtils;
import com.gs.buluo.app.view.impl.ICompanyManagerView;
import com.gs.buluo.app.view.widget.MoneyTextView;
import com.gs.buluo.common.widget.LoadingDialog;

import butterknife.Bind;

/**
 * Created by Solang on 2017/7/24.
 */

public class CompanyManagerActivity extends BaseActivity implements View.OnClickListener, ICompanyManagerView, DialogInterface.OnDismissListener {
    @Bind(R.id.company_balance)
    MoneyTextView mBalance;
    @Bind(R.id.company_credit_limit)
    TextView tvCredit;
    @Bind(R.id.company_available_limit)
    TextView tvAvaAccount;
    Context mCtx;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_company_manager;
    }

    @Override
    protected void bindView(Bundle savedInstanceState) {
        mCtx = this;
        findViewById(R.id.company_recharge).setOnClickListener(this);
        findViewById(R.id.company_bill).setOnClickListener(this);
        findViewById(R.id.company_credit).setOnClickListener(this);
        findViewById(R.id.company_pay_rent).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoadingDialog();
        ((CompanyManagerPresenter) mPresenter).getCompanyInfo();
    }

    @Override
    protected BasePresenter getPresenter() {
        return new CompanyManagerPresenter();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        ((CompanyManagerPresenter) mPresenter).getCompanyInfo();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.company_recharge:

                break;
            case R.id.company_bill:

                break;
            case R.id.company_credit:
                intent.setClass(mCtx, CompanyCreditActivity.class);
                startActivity(intent);
                break;
            case R.id.company_pay_rent:


                break;
        }
    }

    @Override
    public void showError(int res) {
        ToastUtils.ToastMessage(this, getString(res));
    }

    @Override
    public void getCompanyInfoFinished(CompanyAccount account) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoadingDialog.getInstance().dismissDialog();
    }
}
