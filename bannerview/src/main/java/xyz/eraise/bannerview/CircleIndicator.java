package xyz.eraise.bannerview;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.AnimatorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

/**
 * ViewPageer 的指示器
 * 2015年6月3日 下午2:42:00
 * Github 来源：https://github.com/ongakuer/CircleIndicator
 */
@SuppressLint("NewApi")
public class CircleIndicator extends LinearLayout implements OnPageChangeListener {

    private final static int DEFAULT_INDICATOR_WIDTH = 7;
    private ViewPager mViewpager;
    private OnPageChangeListener mViewPagerOnPageChangeListener;
    private int mIndicatorMargin = -1;
    private int mIndicatorWidth = -1;
    private int mIndicatorHeight = -1;
    @AnimatorRes
    private int mAnimatorResId = R.anim.scale_with_alpha;
    private int mAnimatorReverseResId = -1;
    private int mIndicatorBackground = R.drawable.white_radius;
    private int mIndicatorUnselectedBackground = R.drawable.white_radius;
    private int mCurrentPosition = 0;
    private Animator mAnimationOut;
    private Animator mAnimationIn;

    public CircleIndicator(Context context) {
        super(context);
        init(context, null);
    }

    public CircleIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

	private void init(Context context, AttributeSet attrs) {
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER);
        handleTypedArray(context, attrs);
        if (isSDK11()) {
        	mAnimationOut = AnimatorInflater.loadAnimator(context, mAnimatorResId);
        }
        if (mAnimatorReverseResId == -1) {
            mAnimationIn = AnimatorInflater.loadAnimator(context, mAnimatorResId);
            mAnimationIn.setInterpolator(new ReverseInterpolator());
        } else {
            mAnimationIn = AnimatorInflater.loadAnimator(context, mAnimatorReverseResId);
        }
    }
    
    /**
     * SDK11才支持动画
     * @return
     */
    private boolean isSDK11() {
    	return Build.VERSION.SDK_INT >= 11;
    }

    private void handleTypedArray(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray =
                    context.obtainStyledAttributes(attrs, R.styleable.CircleIndicator);
            mIndicatorWidth =
                    typedArray.getDimensionPixelSize(R.styleable.CircleIndicator_ci_width, -1);
            mIndicatorHeight =
                    typedArray.getDimensionPixelSize(R.styleable.CircleIndicator_ci_height, -1);
            mIndicatorMargin =
                    typedArray.getDimensionPixelSize(R.styleable.CircleIndicator_ci_margin, -1);

            mAnimatorResId = typedArray.getResourceId(R.styleable.CircleIndicator_ci_animator,
                    R.anim.scale_with_alpha);
            mAnimatorReverseResId =
                    typedArray.getResourceId(R.styleable.CircleIndicator_ci_animator_reverse, -1);
            mIndicatorBackground = typedArray.getResourceId(R.styleable.CircleIndicator_ci_drawable,
                    R.drawable.white_radius);
            mIndicatorUnselectedBackground =
                    typedArray.getResourceId(R.styleable.CircleIndicator_ci_drawable_unselected,
                            mIndicatorBackground);
            typedArray.recycle();
        }
        mIndicatorWidth =
                (mIndicatorWidth == -1) ? dip2px(DEFAULT_INDICATOR_WIDTH) : mIndicatorWidth;
        mIndicatorHeight =
                (mIndicatorHeight == -1) ? dip2px(DEFAULT_INDICATOR_WIDTH) : mIndicatorHeight;
        mIndicatorMargin =
                (mIndicatorMargin == -1) ? dip2px(DEFAULT_INDICATOR_WIDTH) : mIndicatorMargin;
    }

    public void setViewPager(ViewPager viewPager) {
        mViewpager = viewPager;
        mCurrentPosition = mViewpager.getCurrentItem();
        createIndicators(viewPager);
        mViewpager.setOnPageChangeListener(this);
        onPageSelected(mCurrentPosition);
    }

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        if (mViewpager == null) {
            throw new NullPointerException("can not find Viewpager , setViewPager first");
        }
        mViewPagerOnPageChangeListener = onPageChangeListener;
        mViewpager.setOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mViewPagerOnPageChangeListener != null) {
            mViewPagerOnPageChangeListener.onPageScrolled(position, positionOffset,
                    positionOffsetPixels);
        }
    }

	@Override public void onPageSelected(int position) {
        if (mViewPagerOnPageChangeListener != null) {
            mViewPagerOnPageChangeListener.onPageSelected(position);
        }

        if (isSDK11()) {
        	if (mAnimationIn.isRunning()) mAnimationIn.end();
        	if (mAnimationOut.isRunning()) mAnimationOut.end();
        }

        View currentIndicator = getChildAt(mCurrentPosition);
        if (null != currentIndicator) {
        	currentIndicator.setBackgroundResource(mIndicatorUnselectedBackground);
        	if (isSDK11()) {
        		mAnimationIn.setTarget(currentIndicator);
        		mAnimationIn.start();
        	}
        	
        	View selectedIndicator = getChildAt(position);
        	selectedIndicator.setBackgroundResource(mIndicatorBackground);
        	if (isSDK11()) {
        		mAnimationOut.setTarget(selectedIndicator);
        		mAnimationOut.start();
        	}
        }

        mCurrentPosition = position;
    }

    @Override public void onPageScrollStateChanged(int state) {
        if (mViewPagerOnPageChangeListener != null) {
            mViewPagerOnPageChangeListener.onPageScrollStateChanged(state);
        }
    }

    private void createIndicators(ViewPager viewPager) {
        removeAllViews();
        int count = viewPager.getAdapter().getCount();
        if (count <= 0) {
            return;
        }
        addIndicator(mIndicatorBackground, mAnimationOut);
        for (int i = 1; i < count; i++) {
            addIndicator(mIndicatorUnselectedBackground, mAnimationIn);
        }
    }

    private void addIndicator(@DrawableRes int backgroundDrawableId, Animator animator) {
    	if (isSDK11()) {
    		if (animator.isRunning()) animator.end();
    	}

        View Indicator = new View(getContext());
        Indicator.setBackgroundResource(backgroundDrawableId);
        addView(Indicator, mIndicatorWidth, mIndicatorHeight);
        LayoutParams lp = (LayoutParams) Indicator.getLayoutParams();
        lp.leftMargin = mIndicatorMargin;
        lp.rightMargin = mIndicatorMargin;
        Indicator.setLayoutParams(lp);

        if (isSDK11()) {
        	animator.setTarget(Indicator);
        	animator.start();
        }
    }

    private class ReverseInterpolator implements Interpolator {
        @Override public float getInterpolation(float value) {
            return Math.abs(1.0f - value);
        }
    }

    private int dip2px(int dip) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getContext().getResources().getDisplayMetrics());
	}
}
