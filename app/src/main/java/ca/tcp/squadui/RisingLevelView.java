package ca.tcp.squadui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Saajid on 2015-05-01.
 */
public class RisingLevelView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private int canvasWidth, canvasHeight;
    private Paint mPaint;
    private int handleSize = 40;
    private int halfHandleSize = handleSize/2;
    private int currentY = Integer.MIN_VALUE;

    private int backgroundColor = getResources().getColor(R.color.base);
    private int barColor = getResources().getColor(R.color.accent);
    private int touchedColor = getResources().getColor(R.color.base2);
    private int stoppedColor = getResources().getColor(R.color.base3);

    public RisingLevelView(Context context) {
        super(context);
        ini();
    }

    public RisingLevelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ini();
    }

    public RisingLevelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ini();
    }

    private void ini() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        //mPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void fillBottomUp(float y, int color) {
        Canvas c = mHolder.lockCanvas();
        c.drawColor(backgroundColor);
        mPaint.setColor(barColor);
        c.drawRect(0, y + halfHandleSize, canvasWidth, canvasHeight, mPaint);
        mPaint.setColor(color);
        c.drawRect(0, y - halfHandleSize, canvasWidth, y + halfHandleSize, mPaint);
        currentY = (int)y;
        mHolder.unlockCanvasAndPost(c);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        Canvas c = mHolder.lockCanvas();
        canvasWidth = c.getWidth();
        canvasHeight = c.getHeight();
        c.drawColor(backgroundColor);
        if (currentY == Integer.MIN_VALUE) {currentY=canvasHeight;}
        mPaint.setColor(barColor);
        c.drawRect(0, currentY + halfHandleSize, canvasWidth, canvasHeight, mPaint);
        mPaint.setColor(stoppedColor);
        c.drawRect(0, currentY - halfHandleSize, canvasWidth, currentY + halfHandleSize, mPaint);
        mHolder.unlockCanvasAndPost(c);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public int getCanvasHeight() {
        return canvasHeight;
    }

    private boolean didTouchDown = false;

    public boolean onTouch(MotionEvent event) {
        int y = (int)event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isTouchValid(y)) {
                this.fillBottomUp(currentY, touchedColor);
                didTouchDown = true;
            }
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (didTouchDown  && isTouchInBound(y)) {
                this.fillBottomUp(y, touchedColor);
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            didTouchDown = false;
            this.fillBottomUp(currentY, stoppedColor);
            return true;
        }
        return false;
    }

    public boolean isTouchValid(int y) {
        int deltaY = y-currentY;
        if (Math.abs(deltaY) <= halfHandleSize) {
            return true;
        }
        return false;
    }

    public boolean isTouchInBound(int y) {
        if (y >= 0 && y <= canvasHeight) {
            return true;
        }
        return false;
    }

}
