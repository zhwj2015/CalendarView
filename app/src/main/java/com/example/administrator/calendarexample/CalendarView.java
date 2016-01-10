package com.example.administrator.calendarexample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.example.administrator.calendarexample.Model.CustomDate;

/**
 * Created by Administrator on 2016/1/10.
 */
public class CalendarView extends View {

    public final static int ACTIVE = 1;
    public final static int CUSTOM = 0;
    public final static int UNREACH_DAY = 2;
    public final static int PAST_MONTH_DAY = 3;
    public final static int NEXT_MONTH_DAY = 4;
    public final static int TOTAL_COL = 6;
    public final static int TOTAL_ROW = 7;

    private Paint onPaint;
    private Paint circlePaint;
    private Row[] rows = new Row[TOTAL_ROW];
    private int mViewWidth;
    private int mViewHeight;
    private int mCellSpace;
    private float mDownX;
    private float mDownY;
    private int touchSlop;
    private Cell mClickCell;
    private static CustomDate mShowDate;
    private OnCellClickListener mCellClickListener;

    public CalendarView(Context context) {
        super(context);
        init(context);
    }
    public CalendarView(Context context, OnCellClickListener listener) {
        super(context);
        this.mCellClickListener = listener;
        init(context);
    }

    public CalendarView(Context context, OnCellClickListener listener, Row[] rows) {
        super(context);
        this.mCellClickListener = listener;
        this.rows = rows;
        init(context);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
        mCellSpace = Math.min(mViewHeight / TOTAL_ROW, mViewWidth / TOTAL_COL);
//        if (!callBackCellSpace) {
//            callBackCellSpace = true;
//        }
        onPaint.setTextSize(mCellSpace / 3);
        circlePaint.setTextSize(mCellSpace / 3);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < TOTAL_ROW; i++) {
            if (rows[i] != null) {
                rows[i].drawCells(canvas);
            }
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float disX = event.getX() - mDownX;
                float disY = event.getY() - mDownY;
                if (Math.abs(disX) < touchSlop && Math.abs(disY) < touchSlop) {
                    int col = (int) (mDownX / mCellSpace);
                    int row = (int) (mDownY / mCellSpace);
                    measureClickCell(col, row);
                }
                break;
            default:
                break;
        }

        return true;
    }

    /**
     * 计算点击的单元格
     * @param col
     * @param row
     */
    private void measureClickCell(int col, int row) {
        if (col >= TOTAL_COL || row >= TOTAL_ROW)
            return;
        if (mClickCell != null) {
            rows[mClickCell.j].cells[mClickCell.i] = mClickCell;
        }
        if (rows[row] != null) {
            mClickCell = new Cell(rows[row].cells[col].date,
                    rows[row].cells[col].state, rows[row].cells[col].i,
                    rows[row].cells[col].j);

            CustomDate date = rows[row].cells[col].date;
            date.week = col;
            mCellClickListener.clickDate(date);

            // 刷新界面
            invalidate();
        }
    }

    private void init(Context context) {
        onPaint = new Paint();
        onPaint.setColor(getResources().getColor(R.color.canlendar_text_color));
        circlePaint = new Paint();
        circlePaint.setColor(getResources().getColor(R.color.error_color));
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setRows(Row[] rows) {
        this.rows = rows;
        invalidate();
    }
    public void setOnCellClickListener (OnCellClickListener listener) {
        this.mCellClickListener = listener;
    }

    public void setCurrentDate(CustomDate date) {
        this.mShowDate = date;
    }

    /**
     * 组元素
     *
     * @author wuwenjie
     *
     */
    public class Row {
        public int j;

        Row(int j) {
            this.j = j;
        }

        public Cell[] cells = new Cell[TOTAL_COL];

        // 绘制单元格
        public void drawCells(Canvas canvas) {
            for (int i = 0; i < cells.length; i++) {
                if (cells[i] != null) {
                    cells[i].drawSelf(canvas);
                }
            }
        }

    }

    /**
     * 单元格元素
     *
     * @author wuwenjie
     *
     */
   public class Cell {
        public CustomDate date;
        public int state;
        public int i;
        public int j;

        public Cell(CustomDate date, int state, int i, int j) {
            super();
            this.date = date;
            this.state = state;
            this.i = i;
            this.j = j;
        }

        public void drawSelf(Canvas canvas) {
            switch (state) {
                case ACTIVE: // 今天
                    onPaint.setColor(Color.parseColor("#fffffe"));
                    canvas.drawCircle((float) (mCellSpace * (i + 0.5)),
                            (float) ((j + 0.5) * mCellSpace), mCellSpace / 3,
                            circlePaint);
                    break;
                case CUSTOM: // 当前月日期
                   onPaint.setColor(Color.BLACK);
                    break;
                case PAST_MONTH_DAY: // 过去一个月
                case NEXT_MONTH_DAY: // 下一个月
                    onPaint.setColor(Color.parseColor("#fffffe"));
                    break;
                case UNREACH_DAY: // 还未到的天
                    onPaint.setColor(Color.GRAY);
                    break;
                default:
                    break;
            }
            // 绘制文字
            String content = date.day + "";
            canvas.drawText(content,
                    (float) ((i + 0.5) * mCellSpace - onPaint.measureText(content) / 2),
                    (float) ((j + 0.7) * mCellSpace - onPaint.measureText(content, 0, 1) / 2),
                    onPaint);
        }
    }
    // 从左往右划，上一个月
    public void leftSlide() {
        if (mShowDate.month == 1) {
            mShowDate.month = 12;
            mShowDate.year -= 1;
        } else {
            mShowDate.month -= 1;
        }
        invalidate();
    }

    // 从右往左划，下一个月
    public void rightSlide() {
        if (mShowDate.month == 12) {
            mShowDate.month = 1;
            mShowDate.year += 1;
        } else {
            mShowDate.month += 1;
        }
        invalidate();
    }
    public interface OnCellClickListener {
        void clickDate(CustomDate date); // 回调点击的日期
        void changeDate(CustomDate date); // 回调滑动ViewPager改变的日期
    }

}
