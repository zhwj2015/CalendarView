package com.example.administrator.calendarexample;

import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.calendarexample.Adapter.CalendarViewAdapter;
import com.example.administrator.calendarexample.CalendarView.OnCellClickListener;
import com.example.administrator.calendarexample.Model.CustomDate;
import com.example.administrator.calendarexample.Util.DateUtil;

public class MainActivity extends Activity implements OnClickListener, OnCellClickListener{
    private ViewPager mViewPager;
    private int mCurrentIndex = 498;
    private CalendarView[] mShowViews;
    private CustomDate mShowDate;
    private CalendarViewAdapter<CalendarView> adapter;
    private SildeDirection mDirection = SildeDirection.NO_SILDE;
    private  CalendarView[] views;

    enum SildeDirection {
        RIGHT, LEFT, NO_SILDE;
    }

    private ImageButton preImgBtn, nextImgBtn;
    private TextView monthText;
    private TextView yearText;
//    private ImageButton closeImgBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mViewPager = (ViewPager) this.findViewById(R.id.vp_calendar);
        preImgBtn = (ImageButton) this.findViewById(R.id.btnPreMonth);
        nextImgBtn = (ImageButton) this.findViewById(R.id.btnNextMonth);
        monthText = (TextView) this.findViewById(R.id.tvCurrentMonth);
        yearText = (TextView) this.findViewById(R.id.tvCurrentYear);
//        closeImgBtn = (ImageButton) this.findViewById(R.id.btnClose);
        preImgBtn.setOnClickListener(this);
        nextImgBtn.setOnClickListener(this);
//        closeImgBtn.setOnClickListener(this);

       views = new CalendarView[3];
        for (int i = 0; i < 3; i++) {
            views[i] = new CalendarView(this, this);
        }
        adapter = new CalendarViewAdapter<>(views);
        setViewPager();
        mShowDate = new CustomDate();
        fillDate();
    }

    private void setViewPager() {
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(498);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                measureDirection(position);
                updateCalendarView(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPreMonth:
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
                break;
            case R.id.btnNextMonth:
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                break;
//            case R.id.btnClose:
//                finish();
//                break;
            default:
                break;
        }
    }
    @Override
    public void changeDate(CustomDate date) {
        monthText.setText(date.month + "月");
        yearText.setText(date.year + "年");

    }

    @Override
    public void clickDate(CustomDate date) {
        Toast.makeText(this, Integer.toString(date.day), Toast.LENGTH_LONG).show();
    }

    /**
     * 计算方向
     *
     * @param arg0
     */
    private void measureDirection(int arg0) {

        if (arg0 > mCurrentIndex) {
            mDirection = SildeDirection.RIGHT;

        } else if (arg0 < mCurrentIndex) {
            mDirection = SildeDirection.LEFT;
        }
        mCurrentIndex = arg0;
    }

    // 更新日历视图
    private void updateCalendarView(int arg0) {
        mShowViews = adapter.getAllItems();
        if (mDirection == SildeDirection.RIGHT) {
            if (mShowDate.month == 12) {
                mShowDate.month = 1;
                mShowDate.year += 1;
            } else {
                mShowDate.month += 1;
            }
        } else if (mDirection == SildeDirection.LEFT) {
//            mShowViews[arg0 % mShowViews.length].leftSlide();
            if (mShowDate.month == 1) {
                mShowDate.month = 12;
                mShowDate.year -= 1;
            } else {
                mShowDate.month -= 1;
            }
        }
        fillDate();
        mDirection = SildeDirection.NO_SILDE;
    }

    private void fillDate() {
        CalendarView currentView =  views[mViewPager.getCurrentItem() % adapter.getAllItems().length];
        CalendarView.Row[] rows = new CalendarView.Row[CalendarView.TOTAL_ROW];
        int monthDay = DateUtil.getCurrentMonthDay(); // 今天
        int lastMonthDays = DateUtil.getMonthDays(mShowDate.year,
                mShowDate.month - 1); // 上个月的天数
        int currentMonthDays = DateUtil.getMonthDays(mShowDate.year,
                mShowDate.month); // 当前月的天数
        int firstDayWeek = DateUtil.getWeekDayFromDate(mShowDate.year,
                mShowDate.month);
        boolean isCurrentMonth = false;
        if (DateUtil.isCurrentMonth(mShowDate)) {
            isCurrentMonth = true;
        }
        int day = 0;
        for (int j = 0; j < CalendarView.TOTAL_ROW; j++) {
            rows[j] = currentView.new Row(j);
            for (int i = 0; i < CalendarView.TOTAL_COL; i++) {
                int position = i + j * CalendarView.TOTAL_COL; // 单元格位置
                // 这个月的
                if (position >= firstDayWeek
                        && position < firstDayWeek + currentMonthDays) {
                    day++;
                    rows[j].cells[i] = currentView.new Cell(CustomDate.modifiDayForObject(
                            mShowDate, day), CalendarView.CUSTOM, i, j);
                    // 今天
                    if (isCurrentMonth && day == monthDay ) {
                        CustomDate date = CustomDate.modifiDayForObject(mShowDate, day);
                        rows[j].cells[i] = currentView.new Cell(date, CalendarView.ACTIVE, i, j);
                    }

                    if (isCurrentMonth && day > monthDay) { // 如果比这个月的今天要大，表示还没到
                        rows[j].cells[i] = currentView.new Cell(
                                CustomDate.modifiDayForObject(mShowDate, day),
                                CalendarView.UNREACH_DAY, i, j);
                    }

                    // 过去一个月
                } else if (position < firstDayWeek) {
                    rows[j].cells[i] = currentView.new Cell(new CustomDate(mShowDate.year,
                            mShowDate.month - 1, lastMonthDays
                            - (firstDayWeek - position - 1)),
                            CalendarView.PAST_MONTH_DAY, i, j);
                    // 下个月
                } else if (position >= firstDayWeek + currentMonthDays) {
                    rows[j].cells[i] = currentView.new Cell((new CustomDate(mShowDate.year,
                            mShowDate.month + 1, position - firstDayWeek
                            - currentMonthDays + 1)),
                            CalendarView.NEXT_MONTH_DAY, i, j);
                }
            }
        }
        currentView.setRows(rows);
        currentView.setCurrentDate(mShowDate);
        currentView.invalidate();
        changeDate(mShowDate);
    }


}
