package xyz.eraise.bannerview;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import xyz.eraise.bannerview.loopviewpager.LoopViewPager;

/**
 * 组合控件，方便做广告轮播图
 * FIXME 2016-6-23 滚动到一半点击跳转到其他页面，返回回来以后依然停留在原来的滚动未完成的地方，这是个bug
 */
public class BannerView extends FrameLayout {
	
//	private static final int WHAT_NEXT_PAGE = 1;
	
	/**
	 * 广告内容
	 */
	private LoopViewPager mViewPager;
	
	/**
	 * 广告下标指示
	 */
	private CircleIndicator mIndicator;
	
	/**
	 * 轮播速度
	 */
	private int autoScrollDelay = 3000;
	
	/**
	 * 自动滚动的Runnable
	 */
	private Runnable mScrollRunnable = new Runnable() {

		@Override
		public void run() {
			mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
			timerNext();
		}
		
	};

	/**
	 * 当前滚动中(即使手指按到控件，暂停了滚动，这个值也不会变)
	 */
	private boolean isAutoScroll;
	
	private OnBannerClickListener mOnBannerClickListener;
	
	/**
	 * 为true表示指示器在只有一页的时候也显示
	 */
	private boolean isShowOne;

	/**
	 * 显示指示器
	 */
	private boolean isShowIndicator = true;
	
	/**
	 * 数据Adapter
	 */
	private PagerAdapter mAdapter;

	private float heightRatio;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public BannerView(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}

	public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	public BannerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public BannerView(Context context) {
		super(context);
		init(context, null);
	}
	
	private int dip2px(int dip) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getContext().getResources().getDisplayMetrics());
	}
	
	private void init(Context context, AttributeSet attrs) {
		mViewPager = new LoopViewPager(context, attrs);
		mIndicator = new CircleIndicator(context, attrs);
		
		mViewPager.setBoundaryCaching(true);

		int indicatorGravity = 0;
		int indicatorHeight = 0;
		int indicatorMarginDefault = dip2px(10);
		int indicatorMarginLeft = 0;
		int indicatorMarginRight = 0;
		int indicatorMarginTop = 0;
		int indicatorMarginBottom = 0;
		TypedArray ta = null;
		try {
			ta = context.obtainStyledAttributes(attrs, R.styleable.BannerView);
			heightRatio = ta.getFloat(R.styleable.BannerView_height_ratio, -1f);
			autoScrollDelay = ta.getInteger(R.styleable.BannerView_scroll_delay, 2000);
			indicatorGravity = ta.getInt(R.styleable.BannerView_indicator_gravity, 0);
			indicatorHeight = ta.getDimensionPixelSize(R.styleable.BannerView_indicator_height, dip2px(10));
			indicatorMarginDefault = ta.getDimensionPixelSize(R.styleable.BannerView_indicator_margin, indicatorMarginDefault);
			indicatorMarginLeft = ta.getDimensionPixelSize(R.styleable.BannerView_indicator_marginLeft, indicatorMarginDefault);
			indicatorMarginRight = ta.getDimensionPixelSize(R.styleable.BannerView_indicator_marginRight, indicatorMarginDefault);
			indicatorMarginTop = ta.getDimensionPixelSize(R.styleable.BannerView_indicator_marginTop, indicatorMarginDefault);
			indicatorMarginBottom = ta.getDimensionPixelSize(R.styleable.BannerView_indicator_marginBottom, indicatorMarginDefault);
		} finally {
			if (ta != null)
				ta.recycle();
		}

		LayoutParams _indicatorParams = new LayoutParams(LayoutParams.MATCH_PARENT, indicatorHeight);
		_indicatorParams.bottomMargin = indicatorMarginLeft;
		_indicatorParams.rightMargin = indicatorMarginRight;
		_indicatorParams.leftMargin = indicatorMarginTop;
		_indicatorParams.topMargin = indicatorMarginBottom;
		if (indicatorGravity == 0) {
			_indicatorParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
		} else if (indicatorGravity == 1) {
			_indicatorParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
		} else if (indicatorGravity == 2) {
			_indicatorParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
		}

		addView(mViewPager, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		addView(mIndicator, _indicatorParams);
		
		// 设置ViewPager的触摸事件，用户触摸时不进行滚动
		mViewPager.setOnSingleTouch(new LoopViewPager.OnSingleTouchListener() {
			
			@Override
			public void onTouchUp() {
				if (isAutoScroll) {
					timerNext();
				}
			}
			
			@Override
			public void onTouchDown() {
				if (isAutoScroll) {
					cancelTimerNext();
				}
			}
			
			@Override
			public void onSingleTouch(int num) {
				if (mOnBannerClickListener != null) {
					mOnBannerClickListener.onClick(BannerView.this, (num - 1) % mAdapter.getCount());
				}
			}
		});
	}
	
	public void setAdapter(PagerAdapter adapter) {
		this.mAdapter = adapter;
		mViewPager.setAdapter(adapter);
		if (adapter != null) {
			mIndicator.setViewPager(mViewPager);
			if (isShowIndicator && (adapter.getCount() > 1 || isShowOne)) {
				mIndicator.setVisibility(View.VISIBLE);
			} else {
				mIndicator.setVisibility(View.GONE);
			}
		} else {
			mIndicator.setVisibility(View.GONE);
		}
	}
	
	public PagerAdapter getAdapter() {
		return mAdapter;
	}

	/**
	 * 滚动到下一页
	 */
	private void timerNext() {
		cancelTimerNext();
		postDelayed(mScrollRunnable, autoScrollDelay);
	}

	private void cancelTimerNext() {
		removeCallbacks(mScrollRunnable);
	}

	/**
	 * 开始滚动
	 */
	public void startAutoScroll() {
		// 大于一张才能滚动
		if (mViewPager.getAdapter() != null && mViewPager.getAdapter().getCount() > 1) {
			isAutoScroll = true;
			timerNext();
		}
	}
	
	/**
	 * 停止滚动
	 */
	public void stopAutoScroll() {
		isAutoScroll = false;
		cancelTimerNext();
		if (getHandler() != null) {
			getHandler().removeCallbacksAndMessages(null);
		}
	}
	
	/**
	 * 当前自动滚动中
	 * @return
	 */
	public boolean isAutoScrolling() {
		return isAutoScroll;
	}
	
	/**
	 * 自动轮播的时间间隔
	 * @param autoScrollDelay
	 */
	public void setAutoScrollDelay(int autoScrollDelay) {
		this.autoScrollDelay = autoScrollDelay;
	}
	
	/**
	 * 获取轮播速度
	 * @return
	 */
	public int getAutoScrollDelay() {
		return autoScrollDelay;
	}
	
	public void setOnBannerClickListener(OnBannerClickListener listener) {
		this.mOnBannerClickListener = listener;
	}
	
	public OnBannerClickListener getOnBannerClickListener() {
		return mOnBannerClickListener;
	}
	
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		if (isAutoScroll && visibility == View.VISIBLE) {
			startAutoScroll();
		} else {
			stopAutoScroll();
		}
	}
	
	@Override
	protected void onDetachedFromWindow() {
		if (isAutoScroll) {
			stopAutoScroll();
//			mHandler = null;
			if (getHandler() != null) {
				getHandler().removeCallbacksAndMessages(null);
			}
		}
		super.onDetachedFromWindow();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (heightRatio > 0) {
			int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
			super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec((int) (width * heightRatio), MeasureSpec.EXACTLY));
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	public void setShowOne(boolean showOne) {
		isShowOne = showOne;
		if (mIndicator != null && mIndicator.getChildCount() <= 1)
			mIndicator.setVisibility(isShowOne ? View.VISIBLE : View.GONE);
	}

	public void setShowIndicator(boolean showIndicator) {
		isShowIndicator = showIndicator;
		if (mIndicator != null)
			mIndicator.setVisibility(isShowIndicator ? View.VISIBLE : View.GONE);
	}

	public void setScrollable(boolean scrollable) {
		mViewPager.setScrollable(scrollable);
	}

	public boolean isScrollable() {
		return mViewPager.isScrollable();
	}

	public void setHeightRatio(float ratio) {
		this.heightRatio = ratio;
	}

	public static interface OnBannerClickListener {
		/**
		 *
		 * @param position 被点击的是第 position 个
		 */
		void onClick(BannerView view, int position);
	}
}
