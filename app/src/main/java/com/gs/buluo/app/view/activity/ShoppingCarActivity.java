package com.gs.buluo.app.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.gs.buluo.app.R;
import com.gs.buluo.app.ResponseCode;
import com.gs.buluo.app.adapter.CarListAdapter;
import com.gs.buluo.app.bean.CartItem;
import com.gs.buluo.app.bean.ResponseBody.ShoppingCartResponse;
import com.gs.buluo.app.bean.ResponseBody.SimpleCodeResponse;
import com.gs.buluo.app.bean.ShoppingCart;
import com.gs.buluo.app.eventbus.NewOrderEvent;
import com.gs.buluo.app.model.ShoppingModel;
import com.gs.buluo.app.presenter.BasePresenter;
import com.gs.buluo.app.presenter.ShoppingCarPresenter;
import com.gs.buluo.app.utils.ToastUtils;
import com.gs.buluo.app.view.impl.IShoppingView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by hjn on 2016/12/2.
 */
public class ShoppingCarActivity extends BaseActivity implements IShoppingView, View.OnClickListener, CarListAdapter.CheckInterface, CarListAdapter.UpdateInterface {
    @Bind(R.id.car_list)
    ExpandableListView expandableListView;
    @Bind(R.id.car_edit)
    TextView editButton;
    @Bind(R.id.car_total_price)
    TextView tvTotal;
    @Bind(R.id.car_select_all)
    CheckBox checkBox;
    @Bind(R.id.car_finish)
    TextView finish;


    private CarListAdapter adapter;
    private boolean isEdit;
    private List<ShoppingCart> data;
    private List<ShoppingCart> cartList;
    private float total = 0;


    @Override
    protected void bindView(Bundle savedInstanceState) {
        findViewById(R.id.car_edit).setOnClickListener(this);
        findViewById(R.id.car_back).setOnClickListener(this);
        if(!EventBus.getDefault().isRegistered(this))EventBus.getDefault().register(this);
        finish.setOnClickListener(this);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                adapter.setAllChecked(isChecked);
                calculateTotalPrice();
            }
        });
        showLoadingDialog();
        ((ShoppingCarPresenter) mPresenter).getShoppingListFirst();
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_shoppping_car;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(NewOrderEvent event) {
        removeItemWhichInOrder();
    }

    private void removeItemWhichInOrder() {
        for (int i = 0; i < cartList.size(); i++) {
            ShoppingCart cart = cartList.get(i);
            for (int j = 0; j < cart.goodsList.size(); j++) {
                CartItem item = cart.goodsList.get(j);
                if (item.isSelected) {
                    cart.goodsList.remove(item);
                }
            }
            if (cart.goodsList.size() == 0) {
                cartList.remove(cart);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected BasePresenter getPresenter() {
        return new ShoppingCarPresenter();
    }

    @Override
    public void showError(int res) {
        dismissDialog();
        ToastUtils.ToastMessage(this, res);
    }

    @Override
    public void getShoppingCarInfoSuccess(ShoppingCartResponse.ShoppingCartResponseBody body) {
        dismissDialog();
        cartList = body.content;
        adapter = new CarListAdapter(this, cartList);
        adapter.addCheckInterface(this);
        adapter.addGoodsChangedInterface(this);
        expandableListView.setAdapter(adapter);
        for (int i = 0; i < adapter.getGroupCount(); i++) {
            expandableListView.expandGroup(i);
        }
    }

    @Override
    public void onClick(View v) {
        if (adapter==null)return;
        switch (v.getId()) {
            case R.id.car_edit:
                if (isEdit) {
                    editButton.setText(R.string.edit);
                    finish.setText(getString(R.string.account));
                    isEdit = false;
                } else {
                    editButton.setText(R.string.finish);
                    finish.setText(getString(R.string.delete));
                    isEdit = true;
                }
                adapter.setEdit(isEdit);
                break;
            case R.id.car_finish:
                if (isEdit) {
                    deleteSelected();
                } else {
                    if (total==0){
                        ToastUtils.ToastMessage(this,"请选择结算商品");
                        return;
                    }
                    accountOrder();
                }
                break;
            case R.id.car_back:
                finish();
                break;
        }
    }

    private void accountOrder() {
        Intent intent = new Intent(ShoppingCarActivity.this, NewOrderActivity.class);
        ArrayList<ShoppingCart> carts = new ArrayList<>();
        for (int i = 0; i < cartList.size(); i++) {
            ShoppingCart cart = cartList.get(i);
            ShoppingCart newCart =new ShoppingCart();
            newCart.goodsList=new ArrayList<>();
            for (int j = 0; j < cart.goodsList.size(); j++) {
                CartItem item = cart.goodsList.get(j);
                if (item.isSelected) {
                    newCart.goodsList.add(item);
                }
            }
            if (newCart.goodsList.size() != 0) {
                newCart.id=cart.id;
                newCart.store=cart.store;
                carts.add(newCart);
            }
        }
        intent.putExtra("count", total / 100);
        intent.putParcelableArrayListExtra("cart", carts);
        startActivity(intent);

    }

    private void deleteSelected() {
        StringBuffer sb=new StringBuffer();
        for (ShoppingCart cart : cartList) {
            Iterator<CartItem> it2 = cart.goodsList.iterator();
            if (it2.next().isSelected) {
                sb.append(it2.next().goods.id).append(",");
            }
        }
        new ShoppingModel().deleteShoppingItem(sb.toString(), new Callback<SimpleCodeResponse>() {
            @Override
            public void onResponse(Call<SimpleCodeResponse> call, Response<SimpleCodeResponse> response) {
                if (response.body()!=null&&response.body().code== ResponseCode.DELETE_SUCCESS){
                    removeSelected();
                }
            }

            @Override
            public void onFailure(Call<SimpleCodeResponse> call, Throwable t) {
                ToastUtils.ToastMessage(ShoppingCarActivity.this,R.string.connect_fail);
            }
        });

    }

    public void removeSelected() {
        Iterator<ShoppingCart> it = cartList.iterator();
        while (it.hasNext()){
            ShoppingCart cart = it.next();
            Iterator<CartItem> it2 = cart.goodsList.iterator();
            if (it2.next().isSelected) {
                it.remove();
            }
            if (cart.goodsList.size()==0){
                cartList.remove(cart);
            }
            adapter.notifyDataSetChanged();
            calculateTotalPrice();
        }
    }

    @Override
    public void checkGroup(int groupPosition, boolean isChecked) {
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        total = 0;
        for (ShoppingCart cart : cartList) {
            for (CartItem item : cart.goodsList) {
                if (item.isSelected) {
                    total += Float.parseFloat(item.goods.salePrice) * 100 * item.amount;
                }
            }
        }
        tvTotal.setText(total / 100 + "");
    }

    @Override
    public void checkChild(int groupPosition, int childPosition, boolean isChecked) {
        CartItem item = (CartItem) adapter.getChild(groupPosition, childPosition);
        if (isChecked) {
            total += Float.parseFloat(item.goods.salePrice) * 100 * item.amount;
        } else {
            total -= Float.parseFloat(item.goods.salePrice) * 100 * item.amount;
        }

        tvTotal.setText(total / 100 + "");
    }

    @Override
    public void onUpdate() {
        calculateTotalPrice();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}