package com.fourmob.datetimepicker.date;

import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.MotionEvent;
import android.view.View;
import com.fourmob.datetimepicker.R;
import com.fourmob.datetimepicker.Utils;

import java.security.InvalidParameterException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;

public class SimpleMonthView extends View {

    public static final String VIEW_PARAMS_HEIGHT = "height";
    public static final String VIEW_PARAMS_MONTH = "month";
    public static final String VIEW_PARAMS_YEAR = "year";
    public static final String VIEW_PARAMS_SELECTED_DAY = "selected_day";
    public static final String VIEW_PARAMS_WEEK_START = "week_start";
    public static final String VIEW_PARAMS_NUM_DAYS = "num_days";
    public static final String VIEW_PARAMS_FOCUS_MONTH = "focus_month";
    public static final String VIEW_PARAMS_SHOW_WK_NUM = "show_wk_num";

    private static final int SELECTED_CIRCLE_ALPHA = 60;
    protected static int DEFAULT_HEIGHT = 32;
    protected static final int DEFAULT_NUM_ROWS = 6;
	protected static int DAY_SELECTED_CIRCLE_SIZE;
	protected static int DAY_SEPARATOR_WIDTH = 1;
	protected static int MINI_DAY_NUMBER_TEXT_SIZE;
	protected static int MIN_HEIGHT = 10;
	protected static int MONTH_DAY_LABEL_TEXT_SIZE;
	protected static int MONTH_HEADER_SIZE;
	protected static int MONTH_LABEL_TEXT_SIZE;

	protected static float mScale = 0.0F;

    private String mDayOfWeekTypeface;
    private String mMonthTitleTypeface;
    protected int mDayTextColor;
    private final Formatter mFormatter;

    protected int mFirstJulianDay = -1;
    protected int mFirstMonth = -1;
    protected int mLastMonth = -1;
    protected boolean mHasToday = false;
    private int mDayOfWeekStart = 0;

	private final Calendar mCalendar;
	private final Calendar mDayLabelCalendar;

    private int mNumRows = DEFAULT_NUM_ROWS;


	protected int mMonth;
	protected Paint mMonthDayLabelPaint;
	protected Paint mMonthNumPaint;
	protected int mMonthTitleBGColor;
	protected Paint mMonthTitleBGPaint;
	protected int mMonthTitleColor;
	protected Paint mMonthTitlePaint;
	protected int mNumCells = this.mNumDays;
	protected int mNumDays = 7;
	private OnDayClickListener mOnDayClickListener;
	protected int mPadding = 0;
	protected int mRowHeight = DEFAULT_HEIGHT;
	protected Paint mSelectedCirclePaint;
	protected int mSelectedDay = -1;
	protected int mSelectedLeft = -1;
	protected int mSelectedRight = -1;
	private final StringBuilder mStringBuilder;
	protected int mToday = -1;
	protected int mTodayNumberColor;
	protected int mWeekStart = 1;
	protected int mWidth;
	protected int mYear;
	protected int mDayDisabledTextColor;
	protected int mStartDay;
	protected int mEndDay;
	private DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols();


	public SimpleMonthView(Context context) {
		super(context);
		Resources resources = context.getResources();

		this.mDayLabelCalendar = Calendar.getInstance();
		this.mCalendar = Calendar.getInstance();
		this.mDayOfWeekTypeface = resources.getString(R.string.day_of_week_label_typeface);
		this.mMonthTitleTypeface = resources.getString(R.string.sans_serif);
		this.mDayTextColor = resources.getColor(R.color.date_picker_text_normal);
		this.mDayDisabledTextColor = resources.getColor(R.color.date_picker_text_disabled);
		this.mTodayNumberColor = resources.getColor(R.color.blue);
		this.mMonthTitleColor = resources.getColor(R.color.white);
		this.mMonthTitleBGColor = resources.getColor(R.color.circle_background);
		this.mStringBuilder = new StringBuilder(50);
		this.mFormatter = new Formatter(this.mStringBuilder, Locale.getDefault());

		MINI_DAY_NUMBER_TEXT_SIZE = resources.getDimensionPixelSize(R.dimen.day_number_size);
		MONTH_LABEL_TEXT_SIZE = resources.getDimensionPixelSize(R.dimen.month_label_size);
		MONTH_DAY_LABEL_TEXT_SIZE = resources.getDimensionPixelSize(R.dimen.month_day_label_text_size);
		MONTH_HEADER_SIZE = resources.getDimensionPixelOffset(R.dimen.month_list_item_header_height);
		DAY_SELECTED_CIRCLE_SIZE = resources.getDimensionPixelSize(R.dimen.day_number_select_circle_radius);

		mRowHeight = ((resources.getDimensionPixelOffset(R.dimen.date_picker_view_animator_height) - MONTH_HEADER_SIZE) / 6);

        initView();
	}

	private int calculateNumRows() {
        int offset = findDayOffset();
        int dividend = (offset + mNumCells) / mNumDays;
        int remainder = (offset + mNumCells) % mNumDays;
        return (dividend + (remainder > 0 ? 1 : 0));
	}

	private void drawMonthDayLabels(Canvas canvas) {
        int y = MONTH_HEADER_SIZE - (MONTH_DAY_LABEL_TEXT_SIZE / 2);
        int dayWidthHalf = (mWidth - mPadding * 2) / (mNumDays * 2);

        for (int i = 0; i < mNumDays; i++) {
            int calendarDay = (i + mWeekStart) % mNumDays;
            int x = (2 * i + 1) * dayWidthHalf + mPadding;
            mDayLabelCalendar.set(Calendar.DAY_OF_WEEK, calendarDay);
            canvas.drawText(mDateFormatSymbols.getShortWeekdays()[mDayLabelCalendar.get(Calendar.DAY_OF_WEEK)].toUpperCase(Locale.getDefault()), x, y, mMonthDayLabelPaint);
        }
	}

	private void drawMonthTitle(Canvas canvas) {
        int x = (mWidth + 2 * mPadding) / 2;
        int y = (MONTH_HEADER_SIZE - MONTH_DAY_LABEL_TEXT_SIZE) / 2 + (MONTH_LABEL_TEXT_SIZE / 3);
        canvas.drawText(getMonthAndYearString(), x, y, mMonthTitlePaint);
	}

	private int findDayOffset() {
        return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays) : mDayOfWeekStart)
                - mWeekStart;
	}

	private String getMonthAndYearString() {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NO_MONTH_DAY;
        mStringBuilder.setLength(0);
        long millis = mCalendar.getTimeInMillis();
        return DateUtils.formatDateRange(getContext(), millis, millis, flags);
    }

	private void onDayClick(SimpleMonthAdapter.CalendarDay calendarDay) {
		if (mOnDayClickListener != null) {
			mOnDayClickListener.onDayClick(this, calendarDay);
        }
	}

	private boolean sameDay(int monthDay, Time time) {
		return (mYear == time.year) && (mMonth == time.month) && (monthDay == time.monthDay);
	}

	protected void drawMonthNums(Canvas canvas) {
		int y = (mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2 - DAY_SEPARATOR_WIDTH + MONTH_HEADER_SIZE;
		int paddingDay = (mWidth - 2 * mPadding) / (2 * mNumDays);
		int dayOffset = findDayOffset();
		int day = 1;

		while (day <= this.mNumCells) {
			int x = paddingDay * (1 + dayOffset * 2) + this.mPadding;
			if (this.mSelectedDay == day)
				canvas.drawCircle(x, y - MINI_DAY_NUMBER_TEXT_SIZE / 3, DAY_SELECTED_CIRCLE_SIZE, this.mSelectedCirclePaint);

			if ((this.mHasToday) && (this.mToday == day))
				this.mMonthNumPaint.setColor(this.mTodayNumberColor);
			else if (day < this.mStartDay || day > this.mEndDay)
				this.mMonthNumPaint.setColor(this.mDayDisabledTextColor);
			else
				this.mMonthNumPaint.setColor(this.mDayTextColor);
			canvas.drawText(String.format("%d", day), x, y, this.mMonthNumPaint);

			dayOffset++;
			if (dayOffset == mNumDays) {
				dayOffset = 0;
				y += mRowHeight;
			}
			day++;
		}
	}

	public SimpleMonthAdapter.CalendarDay getDayFromLocation(float x, float y) {
		int padding = mPadding;
		if ((x < padding) || (x > mWidth - mPadding)) {
			return null;
		}


		int yDay = (int) (y - MONTH_HEADER_SIZE) / this.mRowHeight;
		int day = 1 + ((int) ((x - padding) * this.mNumDays / (this.mWidth - padding - this.mPadding)) - findDayOffset()) + yDay * this.mNumDays;
		// If day out of range
		if (day < this.mStartDay || day > this.mEndDay)
			return null;
		return new SimpleMonthAdapter.CalendarDay(this.mYear, this.mMonth, day);
	}

	protected void initView() {
        mMonthTitlePaint = new Paint();
        mMonthTitlePaint.setFakeBoldText(true);
        mMonthTitlePaint.setAntiAlias(true);
        mMonthTitlePaint.setTextSize(MONTH_LABEL_TEXT_SIZE);
        mMonthTitlePaint.setTypeface(Typeface.create(mMonthTitleTypeface, Typeface.BOLD));
        mMonthTitlePaint.setColor(mDayTextColor);
        mMonthTitlePaint.setTextAlign(Align.CENTER);
        mMonthTitlePaint.setStyle(Style.FILL);

        mMonthTitleBGPaint = new Paint();
        mMonthTitleBGPaint.setFakeBoldText(true);
        mMonthTitleBGPaint.setAntiAlias(true);
        mMonthTitleBGPaint.setColor(mMonthTitleBGColor);
        mMonthTitleBGPaint.setTextAlign(Align.CENTER);
        mMonthTitleBGPaint.setStyle(Style.FILL);

        mSelectedCirclePaint = new Paint();
        mSelectedCirclePaint.setFakeBoldText(true);
        mSelectedCirclePaint.setAntiAlias(true);
        mSelectedCirclePaint.setColor(mTodayNumberColor);
        mSelectedCirclePaint.setTextAlign(Align.CENTER);
        mSelectedCirclePaint.setStyle(Style.FILL);
        mSelectedCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);

        mMonthDayLabelPaint = new Paint();
        mMonthDayLabelPaint.setAntiAlias(true);
        mMonthDayLabelPaint.setTextSize(MONTH_DAY_LABEL_TEXT_SIZE);
        mMonthDayLabelPaint.setColor(mDayTextColor);
        mMonthDayLabelPaint.setTypeface(Typeface.create(mDayOfWeekTypeface, Typeface.NORMAL));
        mMonthDayLabelPaint.setStyle(Style.FILL);
        mMonthDayLabelPaint.setTextAlign(Align.CENTER);
        mMonthDayLabelPaint.setFakeBoldText(true);

        mMonthNumPaint = new Paint();
        mMonthNumPaint.setAntiAlias(true);
        mMonthNumPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
        mMonthNumPaint.setStyle(Style.FILL);
        mMonthNumPaint.setTextAlign(Align.CENTER);
        mMonthNumPaint.setFakeBoldText(false);
	}

	protected void onDraw(Canvas canvas) {
		drawMonthTitle(canvas);
		drawMonthDayLabels(canvas);
		drawMonthNums(canvas);
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), mRowHeight * mNumRows + MONTH_HEADER_SIZE);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mWidth = w;
	}

	public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            SimpleMonthAdapter.CalendarDay calendarDay = getDayFromLocation(event.getX(), event.getY());
            if (calendarDay != null) {
                onDayClick(calendarDay);
            }
        }
        return true;
	}

	public void reuse() {
        mNumRows = DEFAULT_NUM_ROWS;
		requestLayout();
	}


	public void setMonthParams(HashMap<String, Integer> monthParams) {
		if ((!monthParams.containsKey(VIEW_PARAMS_MONTH)) && (!monthParams.containsKey(VIEW_PARAMS_YEAR)))
			throw new InvalidParameterException("You must specify the month and year for this view");
		setTag(monthParams);
		if (monthParams.containsKey(VIEW_PARAMS_HEIGHT)) {
			this.mRowHeight = ((Integer) monthParams.get(VIEW_PARAMS_HEIGHT)).intValue();
			if (this.mRowHeight < MIN_HEIGHT) {
				this.mRowHeight = MIN_HEIGHT;
            }
		}
		if (monthParams.containsKey(VIEW_PARAMS_SELECTED_DAY)) {
			this.mSelectedDay = ((Integer) monthParams.get(VIEW_PARAMS_SELECTED_DAY)).intValue();
        }
		this.mMonth = ((Integer) monthParams.get("month")).intValue();
		this.mYear = ((Integer) monthParams.get("year")).intValue();
		this.mStartDay = ((Integer) monthParams.get("start_day")).intValue();
		this.mEndDay = ((Integer) monthParams.get("end_day")).intValue();
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		this.mHasToday = false;
		this.mToday = -1;
		this.mCalendar.set(Calendar.MONTH, this.mMonth);
		this.mCalendar.set(Calendar.YEAR, this.mYear);
		this.mCalendar.set(Calendar.DAY_OF_MONTH, 1);
		this.mDayOfWeekStart = this.mCalendar.get(Calendar.DAY_OF_WEEK);
		if (monthParams.containsKey("week_start")) {
			this.mWeekStart = ((Integer) monthParams.get("week_start")).intValue();
		} else {
			this.mWeekStart = this.mCalendar.getFirstDayOfWeek();
		}
		this.mNumCells = Utils.getDaysInMonth(this.mMonth, this.mYear);
		for (int day = 0; day < this.mNumCells; day++) {
			final int monthDay = day + 1;
			if (sameDay(monthDay, today)) {
				this.mHasToday = true;
				this.mToday = monthDay;
			}
		}
		this.mNumRows = calculateNumRows();

	}

	public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
		mOnDayClickListener = onDayClickListener;
	}

	public static abstract interface OnDayClickListener {
		public abstract void onDayClick(SimpleMonthView simpleMonthView, SimpleMonthAdapter.CalendarDay calendarDay);
	}
}