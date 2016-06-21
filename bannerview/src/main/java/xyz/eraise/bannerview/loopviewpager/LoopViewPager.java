/*
 * Copyright (C) 2013 Leszek Mzyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.eraise.bannerview.loopviewpager;

import android.content.Context;
import android.graphics.PointF;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 无限滚动的ViewPager
 * A ViewPager subclass enabling infinte scrolling of the viewPager elements
 * 
 * When used for paginating views (in opposite to fragments), no code changes
 * should be needed only change xml's from <android.support.v4.view.ViewPager>
 * to <com.imbryk.viewPager.LoopViewPager>
 * 
 * If "blinking" can be seen when paginating to first or last view, simply call
 * seBoundaryCaching( true ), or change DEFAULT_BOUNDARY_CASHING to true
 * 
 * When using a FragmentPagerAdapter or FragmentStatePagerAdapter,
 * additional changes in the adapter must be done. 
 * The adapter must be prepared to create 2 extra items e.g.:
 * 
 * The original adapter creates 4 items: [0,1,2,3]
 * The modified adapter will have to create 6 items [0,1,2,3,4,5]
 * with mapping realPosition=(position-1)%count
 * [0->3, 1->0, 2->1, 3->2, 4->3, 5->0]
 */
public class LoopViewPager extends ViewPager {

    private static final boolean DEFAULT_BOUNDARY_CASHING = false;

    OnPageChangeListener mOuterPageChangeListener;
    private LoopPagerAdapterWrapper mAdapter;
    private boolean mBoundaryCaching = DEFAULT_BOUNDARY_CASHING;
    
    /** 触摸时按下的点 **/
    private PointF downP = new PointF();
    /** 触摸时当前的点 **/
    private PointF curP = new PointF();
    private OnSingleTouchListener onSingleTouchListener;
    
    private boolean isScrollable = true;
    
    /**
     * helper function which may be used when implementing FragmentPagerAdapter
     *   
     * @param position
     * @param count
     * @return (position-1)%count
     */
    public static int toRealPosition( int position, int count ){
        position = position-1;
        if( position < 0 ){
            position += count;
        }else{
            position = position%count;
        }
        return position;
    }
    
    /**
     * If set to true, the boundary views (i.e. first and last) will never be destroyed
     * This may help to prevent "blinking" of some views 
     * 
     * @param flag
     */
    public void setBoundaryCaching(boolean flag) {
        mBoundaryCaching = flag;
        if (mAdapter != null) {
            mAdapter.setBoundaryCaching(flag);
        }
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        mAdapter = new LoopPagerAdapterWrapper(adapter);
        mAdapter.setBoundaryCaching(mBoundaryCaching);
        super.setAdapter(mAdapter);
        setCurrentItem(0, false);
    }

    @Override
    public PagerAdapter getAdapter() {
        return mAdapter != null ? mAdapter.getRealAdapter() : mAdapter;
    }

    @Override
    public int getCurrentItem() {
        return mAdapter != null ? mAdapter.toRealPosition(super.getCurrentItem()) : 0;
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
    	if (mAdapter != null) {
    		int realItem = mAdapter.toInnerPosition(item);
    		super.setCurrentItem(realItem, smoothScroll);
    	}
    }

    @Override
    public void setCurrentItem(int item) {
        if (getCurrentItem() != item) {
            setCurrentItem(item, true);
        }
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mOuterPageChangeListener = listener;
    };

    public LoopViewPager(Context context) {
        super(context);
        init();
    }

    public LoopViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @SuppressWarnings("deprecation")
	private void init() {
        super.setOnPageChangeListener(onPageChangeListener);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //当拦截触摸事件到达此位置的时候，返回true，
        //说明将onTouch拦截在此控件，进而执行此控件的onTouchEvent

        return super.onInterceptTouchEvent(ev);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        //每次进行onTouch事件都记录当前的按下的坐标
        curP.x = arg0.getX();
        curP.y = arg0.getY();

        if(arg0.getAction() == MotionEvent.ACTION_DOWN){
            //记录按下时候的坐标
            //切记不可用 downP = curP ，这样在改变curP的时候，downP也会改变
            downP.x = arg0.getX();
            downP.y = arg0.getY();
            //此句代码是为了通知他的父ViewPager现在进行的是本控件的操作，不要对我的操作进行干扰
            getParent().requestDisallowInterceptTouchEvent(true);
            if (onSingleTouchListener != null) {
            	onSingleTouchListener.onTouchDown();
            }
        }

        if(arg0.getAction() == MotionEvent.ACTION_MOVE){
        	// 纵向滑动距离大，就是竖滑，竖滑父控件处理
        	int distanceX = (int) Math.abs(downP.x - curP.x);
        	int distanceY = (int) Math.abs(downP.y - curP.y);
        	if (distanceY > distanceX) {
        		getParent().requestDisallowInterceptTouchEvent(false);
        	} else {
        		if (!isScrollable || mAdapter.getRealCount() == 1) {
        			return true;
        		} else {
        			//此句代码是为了通知他的父ViewPager现在进行的是本控件的操作，不要对我的操作进行干扰
        			getParent().requestDisallowInterceptTouchEvent(true);
        		}
        	}
        }

        if(arg0.getAction() == MotionEvent.ACTION_UP){
        	if (onSingleTouchListener != null) {
        		onSingleTouchListener.onTouchUp();
        	}
            //在up时判断是否按下和松手的坐标为一个点
            //如果是一个点，将执行点击事件，这是我自己写的点击事件，而不是onclick
           // if(downP.x==curP.x && downP.y==curP.y){
             if(Math.abs(downP.x-curP.x)<5 && Math.abs(downP.y-curP.y)<5){//做了小小的修改
                onSingleTouch(super.getCurrentItem());
                return true;
            }
        }

        return super.onTouchEvent(arg0);
    }
    
    public void setOnSingleTouch(OnSingleTouchListener listener) {
    	this.onSingleTouchListener = listener;
    }
    
    /**
     * 单击
     */
    public void onSingleTouch(int num) {
        if (onSingleTouchListener!= null) {

            onSingleTouchListener.onSingleTouch(num);
        }
    }
    
    public void setScrollable(boolean isScrollable) {
    	this.isScrollable = isScrollable;
    }
    
    public boolean isScrollable() {
    	return isScrollable;
    }

    /**
     * 创建点击事件接口
     * @author wanpg
     *
     */
    public interface OnSingleTouchListener {
        public void onSingleTouch(int num);
        public void onTouchDown();
        public void onTouchUp();
    }

    private OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
        private float mPreviousOffset = -1;
        private float mPreviousPosition = -1;

        @Override
        public void onPageSelected(int position) {

            int realPosition = mAdapter.toRealPosition(position);
            if (mPreviousPosition != realPosition) {
                mPreviousPosition = realPosition;
                if (mOuterPageChangeListener != null) {
                    mOuterPageChangeListener.onPageSelected(realPosition);
                }
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                int positionOffsetPixels) {
            int realPosition = position;
            if (mAdapter != null) {
                realPosition = mAdapter.toRealPosition(position);

                if (positionOffset == 0
                        && mPreviousOffset == 0
                        && (position == 0 || position == mAdapter.getCount() - 1)) {
                    setCurrentItem(realPosition, false);
                }
            }

            mPreviousOffset = positionOffset;
            if (mOuterPageChangeListener != null) {
                if (realPosition != mAdapter.getRealCount() - 1) {
                    mOuterPageChangeListener.onPageScrolled(realPosition,
                            positionOffset, positionOffsetPixels);
                } else {
                    if (positionOffset > .5) {
                        mOuterPageChangeListener.onPageScrolled(0, 0, 0);
                    } else {
                        mOuterPageChangeListener.onPageScrolled(realPosition,
                                0, 0);
                    }
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (mAdapter != null) {
                int position = LoopViewPager.super.getCurrentItem();
                int realPosition = mAdapter.toRealPosition(position);
                if (state == ViewPager.SCROLL_STATE_IDLE
                        && (position == 0 || position == mAdapter.getCount() - 1)) {
                    setCurrentItem(realPosition, false);
                }
            }
            if (mOuterPageChangeListener != null) {
                mOuterPageChangeListener.onPageScrollStateChanged(state);
            }
        }
    };

}
