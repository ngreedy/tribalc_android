package com.gs.buluo.app.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.gs.buluo.app.R;
import com.gs.buluo.app.bean.BankCard;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Solang on 2017/4/25.
 */

public class LiteBankCardListAdapter extends BaseAdapter {


    private List<BankCard> datas = new ArrayList<>();
    private Context mContext;
    private BankCardHolder holder;
    private SharedPreferences sharedPreferences;

    public LiteBankCardListAdapter(Context context) {
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences("last_item", Context.MODE_PRIVATE);
    }

    @Override
    public int getCount() {
        return datas.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        int last_item = sharedPreferences.getInt("last_item1", -1);
        if (convertView == null) {
            holder = new BankCardHolder();
            convertView = holder.getHolderView();
        } else {
            holder = (BankCardHolder) convertView.getTag();
        }
        if (position == 0) {
            holder.cardIcon.setImageResource(R.mipmap.pay_wechat);
            holder.bankName.setText(R.string.pay_wechat);
            holder.leftBracket.setText("");
            holder.rightBracket.setText("");
            holder.cardType.setText("");
            holder.cardNum.setText("");
        } else {
            if (datas.size()>0){
            BankCard card = datas.get(position-1);
            holder.bankName.setText(card.bankName);
            holder.cardNum.setText(card.bankCardNum.substring(card.bankCardNum.length() - 4, card.bankCardNum.length()));
                holder.leftBracket.setText("(");
                holder.rightBracket.setText(")");
                holder.cardType.setText("储蓄卡");

            switch (card.bankName) {
                case "农业银行":
                    holder.cardIcon.setImageResource(R.mipmap.bank_logo_abc);
                    break;
                case "中国银行":
                    holder.cardIcon.setImageResource(R.mipmap.bank_logo_bc);
                    break;
                case "中国建设银行":
                    holder.cardIcon.setImageResource(R.mipmap.bank_logo_ccb);
                    break;
                case "上海浦发银行":
                    holder.cardIcon.setImageResource(R.mipmap.bank_logo_spdb);
                    break;
                case "广东发展银行":
                    holder.cardIcon.setImageResource(R.mipmap.bank_logo_gdb);
                    break;
                case "中国光大银行":
                    holder.cardIcon.setImageResource(R.mipmap.bank_logo_ceb);
                    break;
                case "中国招商银行":
                    holder.cardIcon.setImageResource(R.mipmap.bank_logo_cmb);
                    break;
                case "华夏银行":
                    holder.cardIcon.setImageResource(R.mipmap.bank_logo_hb);
                    break;
                case "深圳发展银行":
                    holder.cardIcon.setImageResource(R.mipmap.bank_logo_sdb);
                    break;
                case "兴业银行":
                    holder.cardIcon.setImageResource(R.mipmap.bank_logo_cib);
                    break;
                case "民生银行":
                    holder.cardIcon.setImageResource(R.mipmap.bank_logo_cmsb);
                    break;
                case "恒丰银行":
                    holder.cardIcon.setImageResource(R.mipmap.bank_logo_egb);
                    break;
                case "中信银行":
                    holder.cardIcon.setImageResource(R.mipmap.bank_logo_citic);
                    break;
                case "中国农业发展银行":
                    holder.cardIcon.setImageResource(R.mipmap.bank_logo_adbc);
                    break;
                case "中国进出口银行":
                    holder.cardIcon.setImageResource(R.mipmap.bank_logo_eibc);
                    break;
            }
        }}
        if(last_item > 0 && last_item==position){
            holder.PayOrder.setChecked(true);
        }else{
            holder.PayOrder.setChecked(false);
        }

        convertView.setTag(holder);
        return convertView;
    }


    public void setData(List<BankCard> data) {
        this.datas = data;
        notifyDataSetChanged();
    }


    private class BankCardHolder {
        public TextView bankName;
        public TextView cardNum;
        public ImageView cardIcon;
        public TextView rightBracket;
        public TextView leftBracket;
        public TextView cardType;
        public RadioButton PayOrder;

        public View getHolderView() {
            View view = LayoutInflater.from(mContext).inflate(R.layout.lite_bank_card_item, null);
            cardType = (TextView) view.findViewById(R.id.card_type);
            bankName = (TextView) view.findViewById(R.id.card_bank_name);
            cardNum = (TextView) view.findViewById(R.id.card_number);
            cardIcon = (ImageView) view.findViewById(R.id.card_icon);
            leftBracket = (TextView) view.findViewById(R.id.tv_left_bracket);
            rightBracket = (TextView) view.findViewById(R.id.tv_right_bracket);
            PayOrder = (RadioButton) view.findViewById(R.id.recharge_pay_order);
            return view;
        }
    }




}
