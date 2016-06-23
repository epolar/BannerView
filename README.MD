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
    compile 'xyz.eraise:bannerview:1.0.3'
}
```

### 使用
设置 Adapter 就好了，为方便使用，写了一个 BaseBannerAdapter ，可以继承它使用。点击事件请设置 setOnBannerClickListener

### xml 属性
indicator_gravit: center/left/right 指示器的位置是在底部的中/左/右</br>
height_ratio： 高度/宽度 得到的比例