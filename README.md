# ImageCycle
轮播图控件

![](https://github.com/bai134/ImageCycle/blob/master/test.gif)


一般用ViewPager作轮播图写法有两种（个人认为）：

1：在PagerAdapter中的getCount返回个很大的值，然后从ViewPager.setCurrentItem(getCount/2)开始，因为getCount的值很大，滑动的时候就需要很长的时间才会滑到边，也就造成无限循环的错觉;

2：例如视图页只有4页，但是需要getCount返回个6页，第1页填充第5页一样的内容，第6页填充第1页一样的内容，一开始从第2页开始播放，当向左滑，滑到第1页时，使用setCurrentItem()方法跳转到第5页，由于第1页和第5页一样的内容，所以就形成了循环，向右滑也是一样的道理 ![](https://oscimg.oschina.net/oscnet/e5bfe5d791f413053a62779ec47ddedb683.jpg)

本文主要说的是第2种方法

首先自定义类继承LinearLayout，然后绑定带有ViewPager控件以及滚动视图指示器（可有可无，可以用LinearLayout添加前后景图来做指示器）的视图， 然后给该Viewpager添加适配器PagerAdapter、添加自定义页面改变监听viewPageChangeListener，这里加入了自动切换功能，每隔5秒进行切换到下一页面，我用的是handler的postDelayed()方法，新建了个线程来做切换任务。

定义轮播视图，添加手势监听，滑动的时候不进行自动轮播，离开时开始自动轮播。

```java
public ImageCycleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mScale = context.getResources().getDisplayMetrics().density;
        LayoutInflater.from(context).inflate(R.layout.cycle_view, this);
        mAdvPager = findViewById(R.id.adv_pager);
        mAdvPager.setOnPageChangeListener(new viewPageChangeListener());
        mAdvPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        // 开始图片滚动
                        startImageTimerTask();
                        mAdvPager.performClick();
                        break;
                    default:
                        // 停止图片滚动
                        stopImageTimerTask();
                        break;
                }
                return false;
            }

        });
        // 滚动图片右下指示器视图
        mGroup = findViewById(R.id.viewGroup);
    }

    });
    // 滚动图片右下指示器视图
    mGroup = findViewById(R.id.viewGroup);
}


```

外部方法，动态改变轮播图内容，方法内参数一为轮播图链接，二为可点击控件视图（仿中国yd的首页），三为轮播图加载点击监听

```java
public void setImageResources(ArrayList<String> imageUrlList,int layoutId, ImageCycleViewListener imageCycleViewListener) {
        mGroup.removeAllViews();
        // 轮播下标数量
        final int imageCount = imageUrlList.size()+1;//+1为可点击控件轮播页
        mImageViews = new ImageView[imageCount];
        for (int i = 0; i < imageCount; i++) {
            mImageView = new ImageView(mContext);
            int imageParams = (int) (mScale * 20 + 0.5f);// XP与DP转换，适应应不同分辨率
            int imagePadding = (int) (mScale * 10 + 0.5f);
            LayoutParams params = new LayoutParams(imageParams, imageParams);
            params.leftMargin = 30;
            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mImageView.setLayoutParams(params);
            mImageView.setPadding(imagePadding, imagePadding, imagePadding, imagePadding);
            mImageViews[i] = mImageView;
            if (i == 0) {
                mImageViews[i].setBackgroundResource(R.mipmap.anniu);
            } else {
                mImageViews[i].setBackgroundResource(R.mipmap.anniu01);
            }
            mGroup.addView(mImageViews[i]);
        }
        mAdvAdapter = new ImageCycleAdapter(mContext, imageUrlList, layoutId, imageCycleViewListener);
        mAdvPager.setAdapter(mAdvAdapter);

        //从第2个视图开始
        mAdvPager.setCurrentItem(1,false);
        //启动自动轮播
        startImageTimerTask();
    }
```

PagerAdapter内主要方法，第二页和最后一页内容相同，第一页和倒数第二页内容相同，TempRoundImage为圆角图片控件

```java
@Override
        public Object instantiateItem(ViewGroup container, final int position) {
            if (position == 1||position==getCount()-1){
                View view = mImageCycleViewListener.onView(layoutId);
                container.addView(view);
                return view;
            }else {
                if (mAdList.size() == 0) return null;
                String imageUrl;
                if (position==0){
                    imageUrl = mAdList.get(mAdList.size()-1);

                }else {
                    imageUrl = mAdList.get(position-2);

                }
                TempRoundImage imageView;

                if (mImageViewCacheList.isEmpty()) {
                    imageView = new TempRoundImage(mContext);
                    imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    imageView = mImageViewCacheList.remove(0);
                }
                // 设置图片点击监听

                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mImageCycleViewListener.onImageClick(position-2, v);
                    }
                });
                container.addView(imageView);
                mImageCycleViewListener.displayImage(imageUrl, imageView);
                return imageView;
            }

        }
```

实现ViewPager.OnPageChangeListener，主要方法onPageSelected和onPageScrollStateChanged，前者在滑动时轮播图指示器的改变，当滑向第1页或者最后一页时，后者在动画播放完成后立即跳转到第2页或者倒数第2页，如果没有后者那么跳转会很突兀。

```java
@Override
        public void onPageScrollStateChanged(int state) {

            if (state != ViewPager.SCROLL_STATE_IDLE) {
                return;
            }
            //滚动动画完成时开始下一个滚动任务
            startImageTimerTask();
            if (flag == 0)
                mAdvPager.setCurrentItem(mAdvAdapter.getCount() - 2, false);
            if (flag == mAdvAdapter.getCount() - 1)
                mAdvPager.setCurrentItem(1, false);
        }

        @Override
        public void onPageSelected(int index) {
            if (mImageViews.length == 0) return;
            flag=index;
            if (index == 0) {
                index = mAdvAdapter.getCount()-3;
            }else if (index == mAdvAdapter.getCount()-1){
                index = 0;
            }else{
                index--;
            }
            mImageViews[index].setBackgroundResource(R.mipmap.anniu);

            for (int i = 0; i < mImageViews.length; i++) {
                if (index != i) {
                    mImageViews[i].setBackgroundResource(R.mipmap.anniu01);
                }
            }
        }
```

自动轮播方法

```java
/**
     * 图片自动轮播Task
     */
    private Runnable mImageTimerTask = new Runnable() {
        @Override
        public void run() {
            if (mImageViews != null) {
                if (mAdvPager.getCurrentItem()==mAdvAdapter.getCount()-1){//轮播到最后一张图，就重新开始
                    mAdvPager.setCurrentItem(1);
                }else {
                    mAdvPager.setCurrentItem((mAdvPager.getCurrentItem() % mAdvAdapter.getCount()) + 1);
                }
            }
        }
    };
```

使用很简单，直接xml中添加，然后在Activity中调用初始化方法、加载、点击等方法即可
```java

protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    IC = findViewById(R.id.IC);

    list = new ArrayList<>();
    list.add("http://seopic.699pic.com/photo/50035/0520.jpg_wh1200.jpg");
    list.add("https://img.pc841.com/2018/0815/20180815101229911.jpg");
    list.add("http://s1.sinaimg.cn/large/001vhiLJzy7dNoP6PHl0b");

    IC.setImageResources(list,R.layout.layout,listener);
}

private ImageCycleView.ImageCycleViewListener listener = new ImageCycleView.ImageCycleViewListener(){

    @Override
    public void displayImage(String imageURL, TempRoundImage imageView) {
        Picasso.with(MainActivity.this).load(imageURL).into(imageView);
    }

    @Override
    public void onImageClick(int position, View imageView) {
        Toast.makeText(MainActivity.this,"第"+position+"张图",Toast.LENGTH_SHORT).show();
    }

@Override
    public View onView(int layoutId) {
        View view = LayoutInflater.from(MainActivity.this).inflate(layoutId,null);
        bu\_1 = view.findViewById(R.id.bu\_1);
        bu\_2 = view.findViewById(R.id.bu\_2);
        bu_1.setOnClickListener(new View.OnClickListener() {
            [@Override](https://my.oschina.net/u/1162528)
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"button1", Toast.LENGTH_SHORT).show();
            }
        });
        bu_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"button2", Toast.LENGTH_SHORT).show();
            }
        });
        return  view;
    }
};
```
当然，在activity后台时要停止自动轮播

```java
@Override
    protected void onResume() {
        super.onResume();
        if (IC!=null)
            IC.startImageCycle();
    }

    @Override
    protected void onPause() {
        if (IC!=null)
            IC.pushImageCycle();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (IC!=null)
            IC.pushImageCycle();
        super.onDestroy();
    }
```
