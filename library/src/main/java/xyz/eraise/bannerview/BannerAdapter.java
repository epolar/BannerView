package xyz.eraise.bannerview;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.LinkedList;

public abstract class BannerAdapter extends PagerAdapter {
	
	/**
	 * 可以重用的View
	 */
	private LinkedList<View> scrapViews;

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	public abstract View createView(ViewGroup parent);

	public abstract void bindView(int position, View convertView);

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View v;
		if (null == scrapViews || scrapViews.isEmpty()) {
			v = createView(container);
		} else {
			v = scrapViews.removeFirst();
		}
		bindView(position, v);
		if (v.getParent() != null) {
			((ViewGroup) v.getParent()).removeView(v);
		}
		container.addView(v);
		return v;
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		ImageView v = (ImageView)object;
		container.removeView(v);
		if (scrapViews == null) {
			scrapViews = new LinkedList<>();
		}
		scrapViews.add(v);
	}
	
}
