package xyz.eraise.bannerview;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import xyz.eraise.bannerviewdemo.R;

/**
 * 创建日期： 2016/6/20.
 */
public class BannerAdapterImpl extends BaseBannerAdapter {

    private LayoutInflater mInflater;
    private List<AdEntity> data;

    public BannerAdapterImpl(@NonNull Activity activity) {
        this.mInflater = activity.getLayoutInflater();
    }

    public AdEntity getItem(int position) {
        return data.get(position);
    }

    public void setData(List<AdEntity> data) {
        this.data = data;
    }

    @Override
    public View createView(ViewGroup parent) {
        return mInflater.inflate(R.layout.item_banner, parent, false);
    }

    @Override
    public void bindView(int position, View convertView) {
        AdEntity entity = data.get(position);
        Glide.with(mInflater.getContext())
                .load(entity.pic)
                .into((ImageView)convertView);
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }
}
