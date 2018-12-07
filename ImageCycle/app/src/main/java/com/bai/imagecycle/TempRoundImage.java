package com.bai.imagecycle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;

/**
 * Created by bai on 2018/11/26.
 */
public class TempRoundImage extends android.support.v7.widget.AppCompatImageView {


    public TempRoundImage(Context context) {
        super(context);

    }

    public TempRoundImage(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public TempRoundImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        if (drawable.getClass() == NinePatchDrawable.class)
            return;

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        Bitmap b = ((BitmapDrawable) drawable).getBitmap();

        if(b==null){
            b= BitmapFactory.decodeResource(getResources(), R.mipmap.frag);
        }
            Bitmap reSizeImage = reSizeImage(b, width, height);
            canvas.drawBitmap(createRoundImage(reSizeImage, width, height),
                    0, 0, null);
    }

    private Bitmap createRoundImage(Bitmap source, int width, int height) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        RectF rect = new RectF(0, 0, width, height);
        //这里设定圆角为30
        canvas.drawRoundRect(rect, 30, 30, paint);
        // 核心代码取两个图片的交集部分
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }




    /**
     * 重设Bitmap的宽高
     */
    private Bitmap reSizeImage(Bitmap bitmap, int newWidth, int newHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }



}
