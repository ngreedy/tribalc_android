package com.gs.buluo.app.view.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.gs.buluo.app.Constant;
import com.gs.buluo.app.R;
import com.gs.buluo.app.ResponseCode;
import com.gs.buluo.app.TribeApplication;
import com.gs.buluo.app.adapter.NewOrderAdapter;
import com.gs.buluo.app.bean.RequestBodyBean.NewOrderBean;
import com.gs.buluo.app.bean.RequestBodyBean.NewOrderRequestBody;
import com.gs.buluo.app.bean.ResponseBody.SimpleCodeResponse;
import com.gs.buluo.app.bean.ShoppingCart;
import com.gs.buluo.app.bean.UserAddressEntity;
import com.gs.buluo.app.bean.UserSensitiveEntity;
import com.gs.buluo.app.dao.AddressInfoDao;
import com.gs.buluo.app.dao.UserSensitiveDao;
import com.gs.buluo.app.eventbus.NewOrderEvent;
import com.gs.buluo.app.model.ShoppingModel;
import com.gs.buluo.app.utils.CommonUtils;
import com.gs.buluo.app.utils.SharePreferenceManager;
import com.gs.buluo.app.utils.ToastUtils;
import com.gs.buluo.app.view.widget.PayPanel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hjn on 2016/12/5.
 */
public class NewOrderActivity extends BaseActivity implements View.OnClickListener, PayPanel.OnPayPanelDismissListener {
    @Bind(R.id.new_order_detail_goods_list)
    ListView listView;
    @Bind(R.id.new_order_price_total)
    TextView tvTotal;
    @Bind(R.id.new_order_detail_address)
    TextView address;
    @Bind(R.id.new_order_detail_receiver)
    TextView receiver;
    @Bind(R.id.new_order_detail_phone)
    TextView phone;
    private float count;

    @Bind(R.id.new_order_pay_balance)
    RadioButton rbBalance;
    @Bind(R.id.new_order_pay_wechat)
    RadioButton rbWeChat;
    @Bind(R.id.new_order_pay_ali)
    RadioButton rbAli;

    private String payMethod;
    private String addressID;
    private List<ShoppingCart> carts;

    @Override
    protected void bindView(Bundle savedInstanceState) {
        payMethod =getString(R.string.pay_balance);
        findViewById(R.id.new_order_back).setOnClickListener(this);
        findViewById(R.id.new_order_finish).setOnClickListener(this);
        findViewById(R.id.new_order_detail_choose_address).setOnClickListener(this);
        count = getIntent().getFloatExtra("count",0);
        tvTotal.setText(count +"");
        UserSensitiveEntity userSensitiveEntity = new UserSensitiveDao().findFirst();
        addressID = userSensitiveEntity.getAddressID();
        UserAddressEntity entity = new AddressInfoDao().find(TribeApplication.getInstance().getUserInfo().getId(), addressID);
        if (entity!=null){
            address.setText(entity.getArea()+entity.getAddress());
        }
        phone.setText(userSensitiveEntity.getPhone());
        receiver.setText(userSensitiveEntity.getName());

        carts = (List<ShoppingCart>) getIntent().getSerializableExtra("cart");
        NewOrderAdapter adapter=new NewOrderAdapter(this, carts);
        listView.setAdapter(adapter);
        CommonUtils.setListViewHeightBasedOnChildren(listView);
        rbBalance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    rbBalance.setChecked(true);
                    rbAli.setChecked(false);
                    rbWeChat.setChecked(false);
                    payMethod=getString(R.string.pay_balance);
                }
            }
        });
        rbAli.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    rbBalance.setChecked(false);
                    rbAli.setChecked(true);
                    rbWeChat.setChecked(false);
                    payMethod=getString(R.string.pay_ali);
                }
            }
        });
        rbWeChat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    rbBalance.setChecked(false);
                    rbAli.setChecked(false);
                    rbWeChat.setChecked(true);
                    payMethod=getString(R.string.pay_wechat);
                }
            }
        });
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_new_order;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.new_order_back:
                finish();
                break;
            case R.id.new_order_finish:
                createNewOrder();
                break;
            case R.id.new_order_detail_choose_address:
                Intent intent = new Intent(NewOrderActivity.this, AddressListActivity.class);
                intent.putExtra(Constant.FROM_ORDER,true);
                startActivityForResult(intent, Constant.REQUEST_ADDRESS);
                break;
        }
    }

    private void createNewOrder() {
        NewOrderRequestBody body=new NewOrderRequestBody();
        body.addressId=addressID;
        body.itemList=new ArrayList<>();
        NewOrderBean bean;
        for (ShoppingCart cart:carts){
            for (ShoppingCart.ListGoodsListItem item:cart.goodsList){
                bean=new NewOrderBean();
                bean.goodsId=item.goods.id;
                bean.amount=item.amount;
                bean.shoppingCartId=cart.id;
                body.itemList.add(bean);
            }
        }
        new ShoppingModel().createNewOrder(body, new Callback<SimpleCodeResponse>() {
            @Override
            public void onResponse(Call<SimpleCodeResponse> call, Response<SimpleCodeResponse> response) {
                if (response.body()!=null&&response.body().code== ResponseCode.UPDATE_SUCCESS){
                    EventBus.getDefault().post(new NewOrderEvent());
                    if (payMethod.equals(getString(R.string.pay_balance))&&
                            SharePreferenceManager.getInstance(NewOrderActivity.this).getStringValue(Constant.WALLET_PWD)==null){
                        new AlertDialog.Builder(NewOrderActivity.this).setTitle("提示").setMessage("您还没有设置支付密码，请先去设置密码")
                                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(NewOrderActivity.this,UpdateWalletPwdActivity.class));
                                    }
                                }).setNegativeButton("取消",null);
                    }
                    showPayBoard();
                }
            }

            @Override
            public void onFailure(Call<SimpleCodeResponse> call, Throwable t) {
                ToastUtils.ToastMessage(NewOrderActivity.this,R.string.connect_fail);
            }
        });
    }

    private void showPayBoard() {
        PayPanel payBoard=new PayPanel(this,this);
        payBoard.setData(payMethod,count+"");
        payBoard.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constant.REQUEST_ADDRESS) {
            address.setText(data.getStringExtra(Constant.ADDRESS));
            receiver.setText(data.getStringExtra(Constant.RECEIVER));
            phone.setText(data.getStringExtra(Constant.PHONE));
            addressID=data.getStringExtra(Constant.ADDRESS_ID);
        }
    }

    @Override
    public void onPayPanelDismiss() {
        startActivity(new Intent(this,OrderActivity.class));
        finish();
    }
}
