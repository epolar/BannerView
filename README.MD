# BannerView

![Demo](https://github.com/epolar/BannerView/blob/master/screenshot/screenshot-1.png?raw=true)

### 简介
基于 [LooperViewPager](https://github.com/imbryk/LoopingViewPager) 和 [CircleIndicator](https://github.com/ongakuer/CircleIndicator) 封装的广告轮播图。

### 特性
1. 无限循环
2. 圆形指示
3. 指示器为单张时不显示或永远不显示或永远显示
4. 高度为宽度的比例

### 导入
```
dependencies {
    compile 'xyz.eraise:bannerview:1.0.5'
}
```

### 使用
设置 Adapter 就好了，为方便使用，写了一个 BaseBannerAdapter ，可以继承它使用。点击事件请设置 setOnBannerClickListener

### xml 属性
- indicator_gravit: center/left/right 指示器的位置是在底部的中/左/右</br>
- indicator_height: 指示器的高度，默认10dp，因为动画可能是缩放大小的，所以这个不能用wrap_content，而应该根据ci_width ci_height来判断，否则可能显示不全
- indicator_margin/indicator_marginLeft/indicator_marginRight/indicator_marginTop/indicator_marginBottom: 指示器margin
- height_ratio: 高度/宽度 得到的比例
- ci_drawable: 指示当前页面
- ci_drawable_unselected: 其他页面
- ci_animator: 动画效果
- ci_width: 指示器圆点的宽，默认5dp
- ci_height: 指示器圆点的高，默认5dp
- ci_margin: 指示器圆点的间距，默认5dp，这个间距是的实质是设置圆点的 marginLeft 和 marginRight，所以它是两倍大