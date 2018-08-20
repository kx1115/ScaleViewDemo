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
    private int mMinValue=0, mValue = 50, mMaxValue = 100;

    private int mGap = 4;// 最小刻度的像素宽度
    private int mUnit=10;// 长刻度相对于最小刻度的倍数

    private int mLastX, mMove;
    private int mWidth, mHeight;

    private int mMinVelocity;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private OnValueChangeListener mListener;

    public WineView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mScroller = new Scroller(getContext());
        mDensity = getContext().getResources().getDisplayMetrics().density;

        mMinVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
    }
    /**
     * 设置用于接收结果的监听器
     *
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
    }
    /**
     * 获取当前刻度值
     *
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
    }


    /**
     * 从中间往两边开始画刻度线
     *
     * @param canvas
     */
    private void drawScaleLine(Canvas canvas, Paint linePaint) {
        canvas.save();

        //mValue
        int mFixValue=mValue/mUnit*mUnit;
        int left=mFixValue-mMinValue;
        int right=mMaxValue-mFixValue;

        int fixVuleX=mWidth/2 - Math.round((mValue-mFixValue) * mGap * mDensity);
        int startX= Math.max(0,fixVuleX- Math.round(left* mGap * mDensity));
        int endX= Math.min(fixVuleX+ Math.round(right* mGap * mDensity),mWidth);
        //画横线
        canvas.drawLine(startX,0,endX,0,linePaint);


        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(TEXT_SIZE * mDensity);
        int width = mWidth, drawCount = 0;
        float xPosition = 0, textWidth = Layout.getDesiredWidth("0", textPaint);

        for(int i=mMinValue;i<=mMaxValue;i++){
            xPosition=fixVuleX+ Math.round((i-mFixValue) * mGap * mDensity);
            if(xPosition<-100)continue;
            if(xPosition>width+100)return;

            if(i%mUnit==0){
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
        // 常量太多，暂时放这，最终会放在类的开始，放远了怕很快忘记
        int gap = 12, indexWidth = 8, indexTitleWidth = 24, indexTitleHight = 10, shadow = 6;
        String color = "#66999999";

        canvas.save();

        Paint redPaint = new Paint();
        redPaint.setStrokeWidth(indexWidth);
        redPaint.setColor(Color.RED);
        canvas.drawLine(mWidth / 2, 0, mWidth / 2, mHeight, redPaint);

        /*Paint ovalPaint = new Paint();
        ovalPaint.setColor(Color.RED);
        ovalPaint.setStrokeWidth(indexTitleWidth);
        canvas.drawLine(mWidth / 2, 0, mWidth / 2, indexTitleHight, ovalPaint);
        canvas.drawLine(mWidth / 2, mHeight - indexTitleHight, mWidth / 2, mHeight, ovalPaint);*/

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
            // break;
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
            mScroller.fling(0, 0, (int) xVelocity, 0, mMinValue, mMaxValue, 0, 0);
        }
    }

    private void changeMoveAndValue() {
        int tValue = (int) (mMove / (mGap * mDensity));
        if (Math.abs(tValue) > 0) {
            mValue += tValue;
            mMove -= tValue * mGap * mDensity;
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
        int roundMove = Math.round(mMove / (mGap * mDensity));
        mValue = mValue + roundMove;
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
