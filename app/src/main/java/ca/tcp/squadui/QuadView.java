package ca.tcp.squadui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Saajid on 2015-05-05.
 */
public class QuadView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private int canvasWidth, canvasHeight;
    private int nwCenterX, nwCenterY, swCenterX, swCenterY, seCenterX, seCenterY, neCenterX, neCenterY;
    private Paint mPaint;
    private int backgroundColor = getResources().getColor(R.color.base);

    public QuadView(Context context) {
        super(context);
        ini();
    }

    public QuadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ini();
    }

    public QuadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ini();
    }

    private void ini() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(20);
        mPaint.setFakeBoldText(true);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        Canvas c = mHolder.lockCanvas();
        canvasWidth = c.getWidth();
        canvasHeight = c.getHeight();
        nwCenterX = canvasWidth/4 - 15;
        nwCenterY = canvasHeight/4 - 10;
        swCenterX = nwCenterX;
        swCenterY = canvasHeight-nwCenterY+10;
        seCenterX = canvasWidth-nwCenterX;
        seCenterY = swCenterY;
        neCenterX = seCenterX;
        neCenterY = nwCenterY;
        c.drawColor(backgroundColor);
        mPaint.setColor(Color.WHITE);
        c.drawText("NW", nwCenterX, nwCenterY, mPaint);
        c.drawText("SW", swCenterX, swCenterY, mPaint);
        c.drawText("SE", seCenterX, seCenterY, mPaint);
        c.drawText("NE", neCenterX, neCenterY, mPaint);
        mHolder.unlockCanvasAndPost(c);
    }

    public void setFourCordsValues(int nw, int sw, int se, int ne) {
        Canvas c = mHolder.lockCanvas();
        c.drawColor(backgroundColor);
        c.drawText(String.valueOf(nw), nwCenterX, nwCenterY, mPaint);
        c.drawText(String.valueOf(sw), swCenterX, swCenterY, mPaint);
        c.drawText(String.valueOf(se), seCenterX, seCenterY, mPaint);
        c.drawText(String.valueOf(ne), neCenterX, neCenterY, mPaint);
        mHolder.unlockCanvasAndPost(c);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
