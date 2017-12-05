package com.gs.buluo.app.adapter;

import android.support.annotation.Nullable;
import android.view.View;

import com.gs.buluo.app.R;
import com.gs.buluo.app.bean.ContactsPersonEntity;
import com.gs.buluo.app.view.widget.recyclerHelper.BaseHolder;
import com.gs.buluo.app.view.widget.recyclerHelper.BaseQuickAdapter;

import java.util.List;

/**
 * Created by Solang on 2017/10/24.
 */

public class ContactsDoneAdapter extends BaseQuickAdapter<ContactsPersonEntity,BaseHolder> {
    List<ContactsPersonEntity> data;
    public ContactsDoneAdapter(int layoutResId, @Nullable List<ContactsPersonEntity> data) {
        super(layoutResId, data);
        this.data = data;
    }

    @Override
    protected void convert(final BaseHolder helper, final ContactsPersonEntity item) {
        helper.setText(R.id.tv_name, item.name).setText(R.id.tv_number, item.phone);
        helper.getView(R.id.item_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data.remove(item);
                notifyItemRemoved(helper.getAdapterPosition());
            }
        });
    }

}
