package ca.tcp.squadui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Saajid on 2015-05-05.
 */
public class AngleView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private int canvasWidth, canvasHeight;
    private int canvasCenterX, canvasCenterY;
    private Paint mPaint;

    public AngleView(Context context) {
        super(context);
        ini();
    }

    public AngleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ini();
    }

    public AngleView(Context context, AttributeSet attrs, int defStyle) {
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
        mPaint.setStrokeWidth(2.5f);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        Canvas c = mHolder.lockCanvas();
        canvasWidth = c.getWidth();
        canvasHeight = c.getHeight();
        canvasCenterX = c.getWidth()/2;
        canvasCenterY = c.getHeight()/2;
        c.drawColor(Color.BLACK);
        mPaint.setColor(Color.rgb(87, 59, 12));
        c.drawRect(0, canvasCenterY, canvasWidth, canvasHeight, mPaint);
        mPaint.setColor(Color.rgb(135, 206, 250));
        c.drawRect(0, 0, canvasWidth, canvasCenterY, mPaint);
        mPaint.setColor(Color.WHITE);
        c.drawLine(0, canvasCenterY, canvasWidth, canvasCenterY, mPaint);
        mHolder.unlockCanvasAndPost(c);
    }

    public void setPitchRollValues(int pitch, int roll) {
        roll = (int)(canvasCenterX*Math.tan((-roll) * (Math.PI / 180)));
        pitch = (int)((pitch/45d)*canvasCenterY);
        int rightEnd = canvasCenterY-pitch+roll;
        int leftEnd = canvasCenterY-pitch-roll;
        int lowestEnd = Math.max(rightEnd, leftEnd);
        Canvas c = mHolder.lockCanvas();
        c.drawColor(Color.BLACK);
        mPaint.setColor(Color.rgb(135, 206, 250));
        c.drawRect(0, 0, canvasWidth, lowestEnd, mPaint);
        mPaint.setColor(Color.rgb(87, 59, 12));
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(0, leftEnd);
        path.lineTo(canvasWidth, rightEnd);
        path.lineTo(canvasWidth,canvasHeight);
        path.lineTo(0, canvasHeight);
        path.close();
        c.drawPath(path, mPaint);
        mPaint.setColor(Color.WHITE);
        c.drawLine(0, leftEnd, canvasWidth, rightEnd, mPaint);
        mHolder.unlockCanvasAndPost(c);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
