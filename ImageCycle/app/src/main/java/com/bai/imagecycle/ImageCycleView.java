package com.bai.imagecycle;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/12/06 .
 */

public class ImageCycleView extends LinearLayout {



    /**
     * 上下文
     */
    private Context mContext;
    /**
     * 轮播视图
     */
    private ViewPager mAdvPager = null;
    /**
     * 滚动视图适配
     */
    private ImageCycleAdapter mAdvAdapter;
    /**
     * 图片轮播指示器控件
     */
    private ViewGroup mGroup;
    /**
     * 图片轮播指示个图
     */
    private ImageView mImageView = null;
    /**
     * 滚动图片指示视图列表
     */
    private ImageView[] mImageViews = null;

    /**
     * 手机密度
     */
    private float mScale;

    /**
     * 滚动视图下标
     */
    private int flag=-999;

    /**
     * 自定义可点击视图
     */
    private View view;


    public ImageCycleView(Context context) {
        super(context);
    }


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

    @Override
    public boolean performClick() {
        return super.performClick();
    }


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

    /**
     * 图片轮播(手动控制自动轮播与否，便于资源控件）
     */
    public void startImageCycle() {
        startImageTimerTask();
    }

    /**
     * 暂停轮播—用于节省资源
     */
    public void pushImageCycle() {
        stopImageTimerTask();
    }

    /**
     * 图片滚动任务
     */
    private void startImageTimerTask() {
        stopImageTimerTask();//保证在activity后台时停止滚动任务
        // 图片滚动
        mHandler.postDelayed(mImageTimerTask, 5000);
    }

    /**
     * 停止图片滚动任务
     */
    private void stopImageTimerTask() {
        mHandler.removeCallbacks(mImageTimerTask);
    }

    private Handler mHandler = new Handler();

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


    private final class viewPageChangeListener implements ViewPager.OnPageChangeListener {
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
        public void onPageScrolled(int arg0, float arg1, int arg2) {
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
    }

    private class ImageCycleAdapter extends PagerAdapter {
        /**
         * 图片视图缓存列表
         */
        private ArrayList<TempRoundImage> mImageViewCacheList;
        /**
         * 图片资源列表
         */
        private ArrayList<String> mAdList;


        /**
         * 广告图片点击监听
         */
        private ImageCycleViewListener mImageCycleViewListener;
        private Context mContext;
        private int layoutId;

        public ImageCycleAdapter(Context context, ArrayList<String> adList,int layoutId,
                                 ImageCycleViewListener imageCycleViewListener) {
            this.mContext = context;
            this.mAdList = adList;
            this.layoutId = layoutId;
            mImageCycleViewListener = imageCycleViewListener;
            mImageViewCacheList = new ArrayList<>();

        }

        @Override
        public int getCount() {
             return mAdList.size()+3;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

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

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (position == 1 || position == getCount()-1){
                mAdvPager.removeView((View)object);
            }else {
//                TempRoundImage view = (TempRoundImage) object;
                mAdvPager.removeView((TempRoundImage) object);
                mImageViewCacheList.add((TempRoundImage) object);
            }

        }
    }


    /**
     * 轮播控件的监听事件
     */
    
    public interface ImageCycleViewListener {
        /**
         * 加载图片资源--可以在使用该轮播图的activity中实现
         */
        void displayImage(String imageURL, TempRoundImage imageView);

        /**
         * 单击图片事件
         */
        void onImageClick(int position, View imageView);

        /**
         * 自定义可点击视图--该视图在调用方法setImageResources时可传可不传
         */
        View onView(int layoutId);
    }


}

