package com.gs.buluo.app.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.gs.buluo.app.R;
import com.gs.buluo.app.TribeApplication;
import com.gs.buluo.app.adapter.GoodsListAdapter;
import com.gs.buluo.app.bean.GoodList;
import com.gs.buluo.app.bean.ListGoods;
import com.gs.buluo.app.presenter.BasePresenter;
import com.gs.buluo.app.presenter.GoodsPresenter;
import com.gs.buluo.app.view.activity.LoginActivity;
import com.gs.buluo.app.view.activity.ShoppingCarActivity;
import com.gs.buluo.app.view.impl.IGoodsView;
import com.gs.buluo.app.view.widget.RecycleViewDivider;
import com.gs.buluo.app.view.widget.loadMoreRecycle.Action;
import com.gs.buluo.app.view.widget.loadMoreRecycle.RefreshRecyclerView;
import com.gs.buluo.common.widget.StatusLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * Created by Solang on 2017/7/25.
 */

public class CommunityFragment extends BaseFragment implements IGoodsView {
    @Bind(R.id.goods_list_layout)
    StatusLayout statusLayout;
    @Bind(R.id.goods_list)
    RefreshRecyclerView recyclerView;
    List<ListGoods> list;
    private boolean hasMore;
    private GoodsListAdapter adapter;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_goods;
    }

    @Override
    protected void bindView(Bundle savedInstanceState) {
        list = new ArrayList<>();
        adapter = new GoodsListAdapter(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerView.addItemDecoration(new RecycleViewDivider(
                getActivity(), GridLayoutManager.HORIZONTAL, 16, getResources().getColor(R.color.tint_bg)));
        recyclerView.addItemDecoration(new RecycleViewDivider(
                getActivity(), GridLayoutManager.VERTICAL, 12, getResources().getColor(R.color.tint_bg)));

        ((GoodsPresenter) mPresenter).getGoodsList();
        statusLayout.showProgressView();

        recyclerView.setLoadMoreAction(new Action() {
            @Override
            public void onAction() {
                ((GoodsPresenter) mPresenter).loadMore();
            }
        });
        recyclerView.setRefreshAction(new Action() {
            @Override
            public void onAction() {
                adapter.clear();
                ((GoodsPresenter) mPresenter).getGoodsList();
            }
        });

        statusLayout.setErrorAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GoodsPresenter) mPresenter).getGoodsList();
            }
        });

        getActivity().findViewById(R.id.good_list_car).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TribeApplication.getInstance().getUserInfo() == null)
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                else
                    startActivity(new Intent(getActivity(), ShoppingCarActivity.class));
            }
        });
    }

    @Override
    public void getGoodsInfo(GoodList responseList) {
        recyclerView.dismissSwipeRefresh();
        list = responseList.content;
        adapter.addAll(list);
        hasMore = responseList.hasMore;
        statusLayout.showContentView();
        if (!hasMore) {
            adapter.showNoMore();
            return;
        }
        if (list.size() == 0) {
            statusLayout.showEmptyView(getString(R.string.no_goods));
        }
    }

    @Override
    public void showError(int res) {
        recyclerView.dismissSwipeRefresh();
        statusLayout.showErrorView(getString(res));
    }

    @Override
    protected BasePresenter getPresenter() {
        return new GoodsPresenter();
    }
}
