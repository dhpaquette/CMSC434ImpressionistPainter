package edu.umd.hcil.impressionistpainter434;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {

    private ImageView _imageView;

    public Canvas _offScreenCanvas = null;
    public Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();
    VelocityTracker mVelocityTracker = null;

    private int _alpha = 150;
    private int _defaultRadius = 10;
    private int offset = 25;
    private Point _lastPoint = null;
    private long _lastPointTime = -1;
    private boolean _useMotionSpeedForBrushStrokeSize = true;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Square;
    private float _minBrushRadius = 5;

    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);

    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle){

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(4);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);

        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){
        _imageView = imageView;
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType){
        _brushType = brushType;
    }

    /**
     * Clears the painting
     */
    public void clearPainting(){
        //Inspiration for this from http://stackoverflow.com/questions/6320294/how-to-clean-up-a-bitmap
        _offScreenBitmap.eraseColor(android.graphics.Color.TRANSPARENT);
        invalidate();
    }
    public int rate(){

        int count = 0;
        boolean alreadyCounted;

        _imageView.buildDrawingCache();
        Bitmap bmap = _imageView.getDrawingCache();
        int[] pix1 = new int[bmap.getWidth() * bmap.getHeight()];
        int[] pix2 = new int[_offScreenBitmap.getWidth() * _offScreenBitmap.getHeight()];
        bmap.getPixels(pix1,0,bmap.getWidth(),0,0,bmap.getWidth(),bmap.getHeight());
        _offScreenBitmap.getPixels(pix2, 0, _offScreenBitmap.getWidth(), 0, 0, _offScreenBitmap.getWidth(), _offScreenBitmap.getHeight());

        for(int i = 0;i < pix1.length;i++){
            alreadyCounted = false;
            int redVal = Color.red(pix1[i]);
            int blueVal = Color.blue(pix1[i]);
            int greenVal = Color.green(pix1[i]);

            int redVal2 = Color.red(pix2[i]);
            int blueVal2 = Color.blue(pix2[i]);
            int greenVal2 = Color.green(pix2[i]);

            int redDiff = (int) Math.abs(redVal-redVal2);
            int blueDiff = (int) Math.abs(blueVal-blueVal2);
            int greenDiff = (int) Math.abs(greenVal-greenVal2);

            if(redDiff >=50 || blueDiff >= 50 || greenDiff>=50){
                count++;
                alreadyCounted = true;
            }
            if((redDiff >= 35 && blueDiff >=35 && !alreadyCounted) ||(redDiff >=35 && blueDiff >=35 && !alreadyCounted)||
                    (greenDiff >=35 && blueDiff >=35 && !alreadyCounted)){
                count++;
            }

        }
        int similarityCount = pix1.length - count;
        int result = 0;

        float percent = (similarityCount * 100.0f) / pix1.length;

        if(percent <= 5){
            result = 0;
        }else if(percent >5 && percent <=11){
            result = 1;
        }else if(percent > 11 && percent <=20){
            result = 2;
        }else if(percent > 20 && percent <=30){
            result = 3;
        }else if(percent > 30 && percent <=40){
            result = 4;
        }else if(percent > 40 && percent <=50){
            result = 5;
        }else if(percent > 50 && percent <=60){
            result = 6;
        }else if(percent > 60 && percent <=70){
            result = 7;
        }else if(percent > 70 && percent <=80){
            result = 8;
        }else if(percent > 80 && percent <=90){
            result = 9;
        }else if(percent > 90 && percent <=100){
            result = 10;
        }

        return result;

    }




    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){

        //TODO
        //Basically, the way this works is to listen for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location

        float x = motionEvent.getX();
        float y =  motionEvent.getY();

        //Basic Format for VelocityTracker taken from http://developer.android.com/training/gestures/movement.html
        /*However, the example did not account for the IllegalStateException: Already in the pool.
        * This was fixed by setting mVelocityTracker to null after recycling it.*/
        int index = motionEvent.getActionIndex();
        int action = motionEvent.getActionMasked();
        int pointerId = motionEvent.getPointerId(index);

        switch (action){
            case MotionEvent.ACTION_DOWN:
                if(mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                }
                else {
                    mVelocityTracker.clear();
                }
                // Add a user's movement to the tracker.
                mVelocityTracker.addMovement(motionEvent);
                break;
            case MotionEvent.ACTION_MOVE:

                mVelocityTracker.addMovement(motionEvent);
                mVelocityTracker.computeCurrentVelocity(1000);

                float xVelocity = VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId);
                float yVelocity = VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId);

                Bitmap imgViewBitmap = _imageView.getDrawingCache();
                int colorAtTouchPixelInImage = 0;
                int moveEvents = motionEvent.getHistorySize();

                for (int idx = 0; idx < moveEvents; idx++) {

                    float movingX = motionEvent.getHistoricalX(idx);
                    float movingY = motionEvent.getHistoricalY(idx);

                    //Method used on the project page description for grabbing a pixel
                    if (x >=0 && y >=0 && x<=_offScreenBitmap.getWidth() && y<=_offScreenBitmap.getHeight()) {
                        colorAtTouchPixelInImage = imgViewBitmap.getPixel((int) x, (int) y);
                    }

                    //http://stackoverflow.com/questions/14920303/how-to-get-color-at-the-spotor-pixel-of-a-image-on-touch-event-in-android

                    int redVal = Color.red(colorAtTouchPixelInImage);
                    int blueVal = Color.blue(colorAtTouchPixelInImage);
                    int greenVal = Color.green(colorAtTouchPixelInImage);

                    //http://developer.android.com/reference/android/graphics/Color.html#argb(int, int, int, int)
                    int color = Color.argb(_alpha, redVal, greenVal, blueVal);

                    _paint.setColor(color);
                    _offScreenCanvas.drawPoint(movingX, movingY, _paint);

                }

                switch(_brushType){
                    case Square:
                        _offScreenCanvas.drawRect(x, y, x+offset, y+offset, _paint);
                        break;
                    case Circle:
                        _offScreenCanvas.drawCircle( x, y, _defaultRadius, _paint);
                        break;
                    case Line:
                        _offScreenCanvas.drawLine(x,y-(yVelocity/10),x,y+(yVelocity/10),_paint);

                }


                //To force a view to draw, call invalidate()...
                //From http://developer.android.com/reference/android/view/View.html
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                break;

        }


        return true;
    }




    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }



}

