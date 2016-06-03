package com.aibaide.waveview;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 耿其权
 * 2016年6月2日16:16:48
 * 水波显示百分比控件
 */
public class WaveView extends SurfaceView implements SurfaceHolder.Callback {

    Point mCentrePoint;
    int mNowHeight = 0;//当前水位
    int mRadius = 0;
    boolean mStart = false;//是否开始
    float mTextSise = 60;//文字大小
    Context mContext;
    int mTranX = 0;//水波平移量
    private Paint mCirclePaint;
    private Paint mOutCirclePaint;
    private Paint mWavePaint;
    private Paint mTextPaint;
    private SurfaceHolder holder;
    private RenderThread renderThread;
    private boolean isDraw = false;// 控制绘制的开关
    private int mCircleColor = Color.parseColor("#ff6600");//背景内圆颜色
    private int mOutCircleColor = Color.parseColor("#f5e6dc");//背景外圆颜色
    private int mWaveColor = Color.parseColor("#ff944d");//水波颜色
    private int mWaterLevel;// 水目标高度
    private int flowNum = 60;//水目标占百分比这里是整数。
    private int mWaveSpeed = 5;//水波起伏速度
    private int mUpSpeed = 2;//水面上升速度

    /**
     * @param context
     */
    public WaveView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        mContext = context;
        init(mContext);
    }

    /**
     * @param context
     * @param attrs
     */
    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mContext = context;
        init(mContext);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
        mContext = context;
        init(mContext);
    }

    private void init(Context context) {
        mContext = context;
        setZOrderOnTop(true);
        holder = this.getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSLUCENT);
        renderThread = new RenderThread();

        mCirclePaint = new Paint();
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setAntiAlias(true);

        mOutCirclePaint = new Paint();
        mOutCirclePaint.setColor(mOutCircleColor);
        mOutCirclePaint.setStyle(Paint.Style.FILL);
        mOutCirclePaint.setAntiAlias(true);

        mWavePaint = new Paint();
        mWavePaint.setStrokeWidth(1.0F);
        mWavePaint.setColor(mWaveColor);
        mWavePaint.setStyle(Paint.Style.FILL);
        mWavePaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setStrokeWidth(1.0F);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(mTextSise);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setAntiAlias(true);


    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mRadius = (int) (0.5 * width * 0.92);
        mCentrePoint = new Point(width / 2, height / 2);
        mWaterLevel = (int) (2 * mRadius * flowNum / 100f);//算出目标水位高度
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isDraw = true;
        if (renderThread != null && !renderThread.isAlive())
            renderThread.start();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isDraw = false;

    }

    /**
     * 绘制界面的线程
     *
     * @author Administrator
     */
    private class RenderThread extends Thread {
        @Override
        public void run() {
            // 不停绘制界面，这里是异步绘制，不采用外部通知开启绘制的方式，水波根据数据更新才会开始增长
            while (isDraw) {
                if (mWaterLevel > mNowHeight) {
                    mNowHeight = mNowHeight + mUpSpeed;
                }
                if (mStart) {
                    if (mTranX > mRadius) {
                        mTranX = 0;
                    }
                    mTranX = mTranX - mWaveSpeed;
                }
                drawUI();
            }
            super.run();
        }
    }

    /**
     * 界面绘制
     */
    public void drawUI() {
        Canvas canvas = holder.lockCanvas();
        try {
            drawCanvas(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null)
                holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawCanvas(Canvas canvas) {
        //画背景圆圈
        canvas.drawCircle(mCentrePoint.x, mCentrePoint.y, mRadius / 0.92f, mOutCirclePaint);
        canvas.drawCircle(mCentrePoint.x, mCentrePoint.y, mRadius, mCirclePaint);
        if (mStart) {
            //计算正弦曲线的路径
            int mH = mCentrePoint.y + mRadius - mNowHeight;
            int left = - mRadius / 2;
            int length = 4 * mRadius;
            Path path2 = new Path();
            path2.moveTo(left, mH);

            for (int i = left; i < length; i++) {
                int x = i;
                int y = (int) (Math.sin(Math.toRadians(x + mTranX) / 2) * mRadius / 4);
                path2.lineTo(x, mH + y);
            }
            path2.lineTo(length, mH);
            path2.lineTo(length, mCentrePoint.y + mRadius);
            path2.lineTo(0, mCentrePoint.y + mRadius);
            path2.lineTo(0, mH);

            canvas.save();
            //这里与圆形取交集，除去正弦曲线多画的部分
            Path pc = new Path();
            pc.addCircle(mCentrePoint.x, mCentrePoint.y, mRadius, Path.Direction.CCW);
            canvas.clipPath(pc, Region.Op.INTERSECT);
            canvas.drawPath(path2, mWavePaint);
            canvas.restore();
            //绘制文字
            canvas.drawText(flowNum + "%", mCentrePoint.x, mCentrePoint.y, mTextPaint);
        }
    }

    public void setFlowNum(int num) {
        flowNum = num;
        mStart = true;
    }

    public void setTextSise(float s) {
        mTextSise = s;
        mTextPaint.setTextSize(s);
    }

    //设置水波起伏速度
    public void setWaveSpeed(int speed) {
        mWaveSpeed = speed;
    }

    //设置水面上升速度
    public void setUpSpeed(int speed) {
        mUpSpeed = speed;
    }

    public void setColor(int waveColor, int circleColor, int outcircleColor) {
        mWaveColor = waveColor;
        mCircleColor = circleColor;
        mOutCircleColor = outcircleColor;
        mWavePaint.setColor(mWaveColor);
        mCirclePaint.setColor(mCircleColor);
        mOutCirclePaint.setColor(mOutCircleColor);
    }
//精确算法，每次正弦曲线从曲线与圆的交集处开始
//    private int getX(double h) {
//        int x = 0;
//        int R = mRadius;
//        if (h < R) {
//            double t = 2 * R * h - h * h;
//            x = (int) (R - Math.abs(Math.sqrt(t)));
//        } else {
//            double t = -2 * R * h + h * h;
//            x = (int) (R - Math.abs(Math.sqrt(t)));
//        }
//        return x;
//    }
}