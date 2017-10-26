package com.gs.buluo.app.view.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.gs.buluo.app.Constant;
import com.gs.buluo.app.R;
import com.gs.buluo.app.adapter.TagAdapter;
import com.gs.buluo.app.bean.BoardroomFilterBean;
import com.gs.buluo.app.bean.TagSelectBean;
import com.gs.buluo.app.utils.AutoLineFeedLayoutManager;
import com.gs.buluo.app.utils.DensityUtils;
import com.gs.buluo.app.view.widget.panel.DatePickPanel;
import com.gs.buluo.app.view.widget.recyclerHelper.BaseQuickAdapter;
import com.gs.buluo.common.utils.ToastUtils;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;

/**
 * Created by hjn on 2017/10/19.
 */

public class BoardroomFilterActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.boardroom_filter_start_floor)
    EditText boardroomFilterStartFloor;
    @BindView(R.id.boardroom_filter_end_floor)
    EditText boardroomFilterEndFloor;
    @BindView(R.id.boardroom_filter_person)
    EditText boardroomFilterPerson;
    @BindView(R.id.boardroom_filter_duration)
    EditText boardroomFilterDuration;
    @BindView(R.id.boardroom_filter_start_time)
    TextView boardroomFilterStartTime;
    @BindView(R.id.boardroom_filter_end_time)
    TextView boardroomFilterEndTime;
    @BindView(R.id.boardroom_equipment_list)
    RecyclerView boardroomEquipmentList;
    private ArrayList<TagSelectBean> beanList;

    @Override
    protected void bindView(Bundle savedInstanceState) {
        boardroomFilterStartTime.setOnClickListener(this);
        boardroomFilterEndTime.setOnClickListener(this);

        boardroomEquipmentList.setLayoutManager(new AutoLineFeedLayoutManager(true));
        beanList = new ArrayList<>();
        beanList.add(new TagSelectBean("窗户"));
        beanList.add(new TagSelectBean("窗户3"));
        beanList.add(new TagSelectBean("窗户44"));
        beanList.add(new TagSelectBean("窗户1231"));
        beanList.add(new TagSelectBean("窗户41"));
        beanList.add(new TagSelectBean("窗户545"));
        beanList.add(new TagSelectBean("窗户536"));
        beanList.add(new TagSelectBean("窗户675"));
        beanList.add(new TagSelectBean("窗户453"));
        beanList.add(new TagSelectBean("窗户123"));
        beanList.add(new TagSelectBean("窗户333"));
        beanList.add(new TagSelectBean("窗户666"));
        beanList.add(new TagSelectBean("窗户777"));
        beanList.add(new TagSelectBean("窗户7778"));
        beanList.add(new TagSelectBean("窗户888"));

        TagAdapter adapter = new TagAdapter(R.layout.tag_item, beanList);
        boardroomEquipmentList.setAdapter(adapter);
        boardroomEquipmentList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.left = DensityUtils.dip2px(getCtx(), 10);
                outRect.top = DensityUtils.dip2px(getCtx(), 10);
            }
        });
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                beanList.get(position).isSelected = !beanList.get(position).isSelected;
                adapter.notifyItemChanged(position);
            }
        });
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_boardroom_filter;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.boardroom_filter_start_time:
                showTimePicker(0, boardroomFilterStartTime);
                break;
            case R.id.boardroom_filter_end_time:
                showTimePicker(1, boardroomFilterEndTime);
                break;
        }
    }

    Calendar date;
    private long startTime = System.currentTimeMillis();
    private long endTime;

    private void showTimePicker(final int flag, final TextView textView) {
        DatePickPanel pickPanel = new DatePickPanel(this, new DatePickPanel.OnSelectedFinished() {
            @Override
            public void onSelected(int year, int month, int day) {
                date = Calendar.getInstance();
                date.set(Calendar.YEAR, year);
                date.set(Calendar.MONTH, month - 1);
                date.set(Calendar.DAY_OF_MONTH, day);
                if (flag == 0) {
                    startTime = date.getTimeInMillis();
                } else {
                    if (date.getTimeInMillis() < startTime) {
                        ToastUtils.ToastMessage(getCtx(), "最晚会议时间不能小于当前时间或最早时间");
                        return;
                    } else {
                        endTime = date.getTimeInMillis();
                    }
                }
                textView.setText(String.valueOf(year) + "年" + month + "月" + day + "日");
            }
        });
        date = Calendar.getInstance();
        date.setTimeInMillis(System.currentTimeMillis());
        pickPanel.setCurrentDate(date.get(Calendar.YEAR) + "", date.get(Calendar.MONTH) + 1 + "", date.get(Calendar.DAY_OF_MONTH) + "");
        pickPanel.show();
    }

    //next
    public void doBoardroomFilter(View view) {
        BoardroomFilterBean bean = new BoardroomFilterBean();
        bean.startFloor = boardroomFilterStartFloor.getText().toString().trim();
        bean.endFloor = boardroomFilterEndFloor.getText().toString().trim();
        bean.attendance = boardroomFilterPerson.getText().toString().trim();
        bean.startDate = startTime;
        bean.endDate = endTime;
        String duration = boardroomFilterDuration.getText().toString().trim();


        if (!TextUtils.isEmpty(duration)) {
            bean.duration = Integer.parseInt(duration) * 3600 + "";
        }

        StringBuilder sb = new StringBuilder();
        for (TagSelectBean tag : beanList) {
            if (tag.isSelected) {
                sb.append(",").append(tag.content);
            }
        }
        if (sb.length() > 0) {
            bean.equipments = sb.toString().substring(1);
        }

        Intent intent = new Intent(this, BoardroomFilterResultActivity.class);
        intent.putExtra(Constant.ROOM_FILTER, bean);
        startActivity(intent);
    }
}
