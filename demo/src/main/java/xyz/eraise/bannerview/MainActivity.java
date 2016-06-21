package xyz.eraise.bannerview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity {
	
	private ListView lvContent;
	private BannerView banner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(xyz.eraise.bannerviewdemo.R.layout.activity_main);
		
		findViews();
		initUI();
	}
	
	private void findViews() {
		lvContent = (ListView) findViewById(xyz.eraise.bannerviewdemo.R.id.lv_content);
	}
	
	private void initUI() {
		banner = (BannerView) getLayoutInflater().inflate(xyz.eraise.bannerviewdemo.R.layout.header_layout, lvContent, false);
		lvContent.addHeaderView(banner);
		
		MyAdapter _adapter = new MyAdapter(this);
		lvContent.setAdapter(_adapter);
		
		// 异步加载后需要重新设置Adapter，并且调用 startAutoScroll
		final BannerAdapterImpl _adAdapter = new BannerAdapterImpl(this);
		/* 模拟异步请求，过上几分钟再把数据添加到界面上 */
		new AsyncTask<Void, Void, ArrayList<AdEntity>>() {

			@Override
			protected ArrayList<AdEntity> doInBackground(Void... params) {
				SystemClock.sleep(3000);
				ArrayList<AdEntity> _adList = new ArrayList<AdEntity>();
				_adList.add(new AdEntity("http://img.sj33.cn/uploads/allimg/201105/20110517103510573.jpg", "http://www.sj33.cn/dphoto/stsy/weiju/201105/27977_5.html"));
				_adList.add(new AdEntity("http://img.pconline.com.cn/images/upload/upc/tx/wallpaper/1307/12/c0/23245412_1373609400454.jpg", "http://www.baidu.com"));
				_adList.add(new AdEntity("http://img.sj33.cn/uploads/allimg/201105/20110517104536824.jpg", "http://www.sina.com"));
				return _adList;
			}
			
			@Override
			protected void onPostExecute(ArrayList<AdEntity> result) {
				super.onPostExecute(result);
				_adAdapter.setData(result);
				// 将Adapter设置到banner上
				banner.setAdapter(_adAdapter);
				banner.startAutoScroll();
			}
		}.execute();
		
		// 设置点击事件
		banner.setOnBannerClickListener(new BannerView.OnBannerClickListener() {
			
			@Override
			public void onClick(BannerView view, int position) {
				// 通过position获取当前被点击的广告执行相应的操作
				Intent _intent = new Intent(Intent.ACTION_VIEW, Uri.parse(_adAdapter.getItem(position).url));
				startActivity(_intent);
			}
		});
	}
	
	private int dip2px(int dip) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getResources().getDisplayMetrics());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (null != banner) {
			banner.startAutoScroll();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (null != banner) {
			banner.stopAutoScroll();
		}
	}
	
	/**
	 * 虚拟数据
	 * @author hcq
	 *
	 */
	static class MyAdapter extends BaseAdapter {
		
		Context context;
		
		MyAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return 100;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			TextView tv;
			if (convertView == null) {
				view = LayoutInflater.from(context).inflate(android.R.layout.test_list_item, parent, false);
			} else {
				view = convertView;
			}
			tv = (TextView) view.findViewById(android.R.id.text1);
			tv.setText("Item ----- " + position);
			return tv;
		}
		
	}

}
