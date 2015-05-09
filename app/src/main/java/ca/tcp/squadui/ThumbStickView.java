package ca.tcp.squadui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Saajid on 2015-05-01.
 */
public class ThumbStickView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private int thumbSize = 40;
    private int halfThumbSize = thumbSize/2;
    private int canvasWidth, canvasHeight;
    private int canvasCenterX, canvasCenterY;
    private int currentX, currentY;
    private Paint mPaint;
    private int backgroundColor = getResources().getColor(R.color.base);
    private int touchedColor = getResources().getColor(R.color.base2);
    private int stoppedColor = getResources().getColor(R.color.base3);

    public ThumbStickView(Context context) {
        super(context);
        ini();
    }

    public ThumbStickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ini();
    }

    public ThumbStickView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ini();
    }

    private void ini() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        Canvas c = mHolder.lockCanvas();
        canvasWidth = c.getWidth();
        canvasHeight = c.getHeight();
        canvasCenterX = canvasWidth/2;
        canvasCenterY = canvasHeight/2;
        currentX = canvasCenterX;
        currentY = canvasCenterY;
        c.drawColor(backgroundColor);
        mPaint.setColor(stoppedColor);
        c.drawCircle(canvasCenterX, canvasCenterY, thumbSize, mPaint);
        mHolder.unlockCanvasAndPost(c);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void moveCircle(float x, float y, int color) {
        Canvas c = mHolder.lockCanvas();
        c.drawColor(backgroundColor);
        mPaint.setColor(color);
        c.drawCircle(x, y, thumbSize, mPaint);
        currentX = (int)x;
        currentY = (int)y;
        mHolder.unlockCanvasAndPost(c);
    }

    public void centerCircle() {
        Canvas c = mHolder.lockCanvas();
        c.drawColor(backgroundColor);
        mPaint.setColor(stoppedColor);
        c.drawCircle(canvasCenterX, canvasCenterY, thumbSize, mPaint);
        currentX = canvasCenterX;
        currentY = canvasCenterY;
        mHolder.unlockCanvasAndPost(c);
    }

    public int getCanvasCenterX() {
        return canvasCenterX;
    }

    public int getCanvasCenterY() {
        return canvasCenterY;
    }

    private boolean didTouchDown = false;

    public boolean onTouch(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isTouchValid(x,y)) {
                didTouchDown = true;
                moveCircle(canvasCenterX, canvasCenterY, touchedColor);
                return true;
            }
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (didTouchDown && isTouchInBound(x, y)) {
                this.moveCircle(x, y, touchedColor);
                return true;
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            this.centerCircle();
            didTouchDown = false;
            return true;
        }
        return false;
    }

    public boolean isTouchValid(int x, int y) {
        if (Math.abs(x-currentX) <= halfThumbSize && Math.abs(y-currentY) <= halfThumbSize) {
            return true;
        }
        return false;
    }

    public boolean isTouchInBound(int x, int y) {
        if (x >= 0 && x <= canvasWidth) {
            if (y >= 0 && y <= canvasHeight){
                return true;
            }
        }
        return false;
    }

}