package com.yy.scalewineview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;


public class WineView extends View {
    private static final int TEXT_SIZE = 18;
    private static final int ITEM_MAX_HEIGHT = 50;
    private static final int ITEM_MIN_HEIGHT = 20;

    private float mDensity;
    //最小值，当前值，最大值
    private int mMinValue=0, mValue = 50, mMaxValue = 500;
    // 选择的值为此数的倍数
    private int mValueUnit=50;
    //每个小刻度表示的value大小
    private int mMinUnit = 10;

    // 最小刻度的像素宽度
    private int mGap = 8;
    // 长刻度相对于最小刻度的倍数
    private int mUnit = 5;

    private int mLastX, mMove;
    private int mWidth, mHeight;

    private int mMinVelocity;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private OnValueChangeListener mListener;

    //值换算成画线的宽度计算用的
    private float distance;

    public WineView(Context context, AttributeSet attrs) {
        super(context, attrs);



        mScroller = new Scroller(getContext());
        mDensity = getContext().getResources().getDisplayMetrics().density;

        mMinVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
    }

    /**
     * 设置值
     * @param defaultValue 默认指向的值
     * @param minValue 最小值
     * @param maxValue 最大值
     */
    public void setValue(int defaultValue,int minValue,int maxValue) {
        this.mMinValue = minValue;
        this.mValue=defaultValue;
        this.mMaxValue=maxValue;
    }

    /**
     * 设置返回值的规定
     * @param mValueUnit 选择的值为此值的倍数
     */
    public void setValueUnit(int mValueUnit) {
        this.mValueUnit = mValueUnit;
    }

    /**
     * 设置刻度
     * @param minUnit 最小刻度表示的值大小，比如10表示一个最小刻度对应大小为10
     * @param gap 两个最小刻度间隔宽度
     * @param maxUint 多少个最小刻度对应一个长刻度
     */
    public void setScale(int minUnit,int gap,int maxUint){
        mMinUnit=minUnit;
        mGap=gap;
        mUnit=maxUint;
    }

    /**
     * 设置用于接收结果的监听器
     * @param listener
     */
    public void setValueChangeListener(OnValueChangeListener listener) {
        mListener = listener;
    }
    public void initViewParam(int defaultValue, int maxValue) {

        mValue = defaultValue;
        mMaxValue = maxValue;
        invalidate();

        mLastX = 0;
        mMove = 0;
        notifyValueChange();

        return;
    }
    /**
     * 获取当前刻度值
     * @return
     */
    public float getValue() {
        return mValue;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mWidth = getWidth();
        mHeight = getHeight();
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        distance=mGap * mDensity /mMinUnit;//value转成像素长度参数

        // 画笔
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        // 抗锯齿
        paint.setAntiAlias(true);
        // 设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰
        paint.setDither(true);
        // 空心
        paint.setStyle(Paint.Style.STROKE);
        // 文字居中
        paint.setTextAlign(Paint.Align.CENTER);

        //画刻度
        drawScaleLine(canvas,paint);
        //画中间线
        drawMiddleLine(canvas,paint);
        return;
    }


    /**
     * 从中间往两边开始画刻度线
     *
     * @param canvas
     */
    private void drawScaleLine(Canvas canvas, Paint linePaint) {
        canvas.save();

        int maxUnit = mMinUnit*mUnit;

        //mValue
        int mFixValue=mValue/maxUnit*maxUnit;
        int left=mFixValue-mMinValue;
        int right=mMaxValue-mFixValue;



        int fixVuleX=mWidth/2 - Math.round((mValue-mFixValue) * distance);
        int startX= Math.max(0,fixVuleX- Math.round(left* distance));
        int endX= Math.min(fixVuleX+ Math.round(right* distance),mWidth);
        //画横线
        canvas.drawLine(startX,0,endX,0,linePaint);


        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(TEXT_SIZE * mDensity);
        int width = mWidth, drawCount = 0;
        float xPosition = 0, textWidth = Layout.getDesiredWidth("0", textPaint);

        for(int i=mMinValue;i<=mMaxValue;i+=mMinUnit){
            xPosition=fixVuleX+ Math.round((i-mFixValue) * distance);
            if(xPosition<-100)continue;
            if(xPosition>width+100)return;

            if(i%maxUnit==0){
                canvas.drawLine(xPosition, getPaddingTop(), xPosition, mDensity * ITEM_MAX_HEIGHT, linePaint);
                int numSize= String.valueOf(i).length();
                canvas.drawText(String.valueOf(i), xPosition - (textWidth * numSize / 2), getHeight() - textWidth, textPaint);
            } else {
                canvas.drawLine(xPosition, getPaddingTop(), xPosition, mDensity * ITEM_MIN_HEIGHT, linePaint);
            }
        }



        canvas.restore();
    }


    /**
     * 画中间的红色指示线、阴影等。指示线两端简单的用了两个矩形代替
     *
     * @param canvas
     */
    private void drawMiddleLine(Canvas canvas, Paint paint) {
        canvas.save();

        Paint redPaint = new Paint();
        redPaint.setStrokeWidth(4);
        redPaint.setColor(Color.RED);
        canvas.drawLine(mWidth / 2, 0, mWidth / 2, mHeight, redPaint);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int xPosition = (int) event.getX();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mScroller.forceFinished(true);
                mLastX = xPosition;
                mMove = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                mMove += (mLastX - xPosition);
                changeMoveAndValue();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                countMoveEnd();
                countVelocityTracker(event);
                return false;
            default:
                break;
        }

        mLastX = xPosition;
        return true;
    }

    private void countVelocityTracker(MotionEvent event) {
        mVelocityTracker.computeCurrentVelocity(1000);
        float xVelocity = mVelocityTracker.getXVelocity();
        if (Math.abs(xVelocity) > mMinVelocity) {
            mScroller.fling(0, 0, (int) xVelocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
        }
    }

    private void changeMoveAndValue() {
        int tValue = (int) (mMove / (distance));
        if (Math.abs(tValue) > 0) {
            mValue += tValue;
            mMove -= tValue * distance;
            if (mValue <= 0 || mValue > mMaxValue) {
                mValue = mValue <= 0 ? 0 : mMaxValue;
                mMove = 0;
                mScroller.forceFinished(true);
            }
            notifyValueChange();
        }
        postInvalidate();
    }

    private void countMoveEnd() {
        int roundMove = Math.round(mMove / (distance));
        mValue = mValue + roundMove;

        mValue = Math.round(1.0f*mValue/mValueUnit)*mValueUnit;

        mValue = mValue <= 0 ? 0 : mValue;
        mValue = mValue > mMaxValue ? mMaxValue : mValue;

        mLastX = 0;
        mMove = 0;



        notifyValueChange();
        postInvalidate();
    }

    private void notifyValueChange() {
        if (null != mListener) {
            mListener.onValueChange(mValue);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            if (mScroller.getCurrX() == mScroller.getFinalX()) { // over
                countMoveEnd();
            } else {
                int xPosition = mScroller.getCurrX();
                mMove += (mLastX - xPosition);
                changeMoveAndValue();
                mLastX = xPosition;
            }
        }
    }
    public interface OnValueChangeListener {
        public void onValueChange(float value);
    }
}
