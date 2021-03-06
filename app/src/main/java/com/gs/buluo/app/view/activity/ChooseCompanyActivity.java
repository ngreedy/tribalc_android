package com.gs.buluo.app.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gs.buluo.app.Constant;
import com.gs.buluo.app.R;
import com.gs.buluo.app.TribeApplication;
import com.gs.buluo.app.adapter.CompanyPickAdapter;
import com.gs.buluo.app.bean.CompanyPlate;
import com.gs.buluo.app.bean.ResponseBody.CompanyResponse;
import com.gs.buluo.app.network.CompanyApis;
import com.gs.buluo.app.network.TribeRetrofit;
import com.gs.buluo.common.utils.ToastUtils;
import com.gs.buluo.common.widget.StatusLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChooseCompanyActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    @BindView(R.id.pick_company_listView)
    ListView mListView;
    @BindView(R.id.pick_company_list_layout)
    StatusLayout mStatusLayout;
    List mList = new ArrayList<>();
    private CompanyPickAdapter mAdapter;
    private Context mContext;
    private String mCommunityID;

    @Override
    protected void bindView(Bundle savedInstanceState) {
        Intent intent = getIntent();
        mCommunityID = intent.getStringExtra(Constant.COMMUNITY_ID);
        mAdapter = new CompanyPickAdapter(this, mList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mContext = this;
        mStatusLayout.showProgressView();
        TribeRetrofit.getInstance().createApi(CompanyApis.class).getCompaniesList(mCommunityID).enqueue(new Callback<CompanyResponse>() {
            @Override
            public void onResponse(Call<CompanyResponse> call, Response<CompanyResponse> response) {
                if (response.body().code == 200) {
                    mStatusLayout.showContentView();
                    List<CompanyPlate> data = response.body().data;
                    mList.clear();
                    mList.addAll(data);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<CompanyResponse> call, Throwable t) {
//                ToastUtils.ToastMessage(mContext, "获取公司列表失败,请检查网络");
                mStatusLayout.showErrorView("获取公司列表失败,请检查网络");
            }
        });
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_company_pick;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CompanyPlate companyPlate = (CompanyPlate) mList.get(position);

        TribeApplication.getInstance().getUserInfo().setCommunityID(mCommunityID);
        TribeApplication.getInstance().getUserInfo().setCompanyName(companyPlate.companyName);

        EventBus.getDefault().post(companyPlate);
        finish();
    }

}
