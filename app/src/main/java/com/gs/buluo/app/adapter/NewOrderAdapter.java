package com.gs.buluo.app.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.gs.buluo.app.R;
import com.gs.buluo.app.bean.CartItem;
import com.gs.buluo.app.bean.ShoppingCart;
import com.gs.buluo.app.utils.CommonUtils;

import java.util.List;

/**
 * Created by hjn on 2016/12/7.
 */
public class NewOrderAdapter extends BaseAdapter {
    private Context context;
    private List<ShoppingCart> cartList;
    private float totalExpress;
    private float totalPrice = 0;
    private NewOrderAdapter.onAmountCalculateFinishListener onAmountCalculateFinishListener;
    public NewOrderAdapter(Context context, List<ShoppingCart> list, NewOrderAdapter.onAmountCalculateFinishListener onExpressFeeCalculateFinishListener){
        this.context=context;
        cartList=list;
        this.onAmountCalculateFinishListener =onExpressFeeCalculateFinishListener;
    }

    @Override
    public int getCount() {
        return cartList.size();
    }

    @Override
    public Object getItem(int position) {
        return cartList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ShoppingCart cart = cartList.get(position);
        GroupHolder groupHolder;
        if (convertView==null){
            groupHolder=new GroupHolder();
            convertView=groupHolder.getConvertView();
        }else {
            groupHolder= (GroupHolder) convertView.getTag();
        }
        groupHolder.name.setText(cart.store.name);
        float price = 0;
        float expressFee = 0;
        for (CartItem item:cart.goodsList){
            price+= Float.parseFloat(item.goods.salePrice)*100 *item.amount;
            expressFee = item.goods.expressFee > expressFee? item.goods.expressFee :expressFee;
        }
        totalExpress+=expressFee;
        totalPrice+=price;
        if (position == cartList.size()-1){
            onAmountCalculateFinishListener.onFinished(totalPrice/100 +totalExpress);
        }

        groupHolder.comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                cart.note = s.toString();
            }
        });

        groupHolder.express.setText(expressFee+"");
        groupHolder.price.setText(price/100+expressFee+"");
        NewOrderChildAdapter adapter = new NewOrderChildAdapter(context,cart.goodsList);
        groupHolder.listView.setAdapter(adapter);
        CommonUtils.setListViewHeightBasedOnChildren(groupHolder.listView);
        convertView.setTag(groupHolder);
        return convertView;
    }

    public interface onAmountCalculateFinishListener {
        void onFinished(float amount);
    }


    public class GroupHolder{
        public ListView listView;
        public TextView name;
        public TextView price;
        public TextView express;
        public EditText comment;

        public View getConvertView(){
            View view=View.inflate(context,R.layout.new_order_list_item,null);
            listView = (ListView) view.findViewById(R.id.new_order_child_list);
            name= (TextView) view.findViewById(R.id.new_order_item_name);
            price= (TextView) view.findViewById(R.id.new_order_price_total);
            express = (TextView) view.findViewById(R.id.new_order_send_price);
            comment = (EditText) view.findViewById(R.id.new_order_more);
            return view;
        }
    }
}
