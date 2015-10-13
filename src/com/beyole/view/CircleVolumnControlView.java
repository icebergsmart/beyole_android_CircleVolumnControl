package com.beyole.view;

import com.beyole.circlevolumncontrol.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class CircleVolumnControlView extends View {

	// 第一种颜色
	private int mFirstColor;
	// 第二种颜色
	private int mNextColor;
	// 小点个数
	private int mDotCount;
	// 圆环宽度
	private int mCircleWidth;
	// 两点之间的间隙
	private int mSplitSize;
	// 背景图
	private Bitmap mBackground;
	// 当前进度
	private int mCurrentProgress = 3;
	private Rect mRect;
	private Paint mPaint;

	public CircleVolumnControlView(Context context) {
		this(context, null);
	}

	public CircleVolumnControlView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * 在此构造方法中获取我们自定义的属性值
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public CircleVolumnControlView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleVolumnControlView, defStyle, 0);
		int n = a.getIndexCount();
		for (int i = 0; i < n; i++) {
			int attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.CircleVolumnControlView_firstColor:
				mFirstColor = a.getColor(attr, Color.GRAY);
				break;
			case R.styleable.CircleVolumnControlView_nextColor:
				mNextColor = a.getColor(attr, Color.CYAN);
				break;
			case R.styleable.CircleVolumnControlView_dotCount:
				mDotCount = a.getInt(attr, 20);
				break;
			case R.styleable.CircleVolumnControlView_splitSize:
				mSplitSize = a.getInt(attr, 20);
				break;
			case R.styleable.CircleVolumnControlView_circleWidth:
				mCircleWidth = a.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()));
				break;
			case R.styleable.CircleVolumnControlView_background:
				mBackground = BitmapFactory.decodeResource(getResources(), a.getResourceId(attr, 0));
				break;
			}
		}
		a.recycle();
		mPaint = new Paint();
		mRect = new Rect();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		mPaint.setAntiAlias(true);// 消除锯齿
		mPaint.setStrokeWidth(mCircleWidth);// 设置圆环宽度
		mPaint.setStrokeCap(Paint.Cap.ROUND);// 设置线段为圆头
		mPaint.setStyle(Paint.Style.STROKE);// 设置为空心
		// 获取圆心x的坐标
		int center = getWidth() / 2;
		int radius = center - mCircleWidth / 2;// 半径
		drawOval(canvas, center, radius);
		// 内置图片

		// 计算内切正方形的位置
		int rectRadius = radius - mCircleWidth / 2;// 计算内圆的半径
		mRect.left = mCircleWidth + (int) (rectRadius - Math.sqrt(2) * 1.0f / 2 * rectRadius);
		mRect.top = mCircleWidth + (int) (rectRadius - Math.sqrt(2) * 1.0f / 2 * rectRadius);
		mRect.right = (int) (mRect.left + Math.sqrt(2) * rectRadius);
		mRect.bottom = (int) (mRect.left + Math.sqrt(2) * rectRadius);

		// 如果图片比较小，则将图片放到正中心
		if (mBackground.getWidth() < Math.sqrt(2) * rectRadius) {
			mRect.left = (int) (mRect.left + Math.sqrt(2) * rectRadius * 1.0f / 2 - mBackground.getWidth() * 1.0f / 2);
			mRect.top = (int) (mRect.top + Math.sqrt(2) * rectRadius * 1.0f / 2 - mBackground.getHeight() * 1.0f / 2);
			mRect.right = (int) (mRect.left + mBackground.getWidth());
			mRect.bottom = (int) (mRect.top + mBackground.getHeight());
		}
		// 绘图
		canvas.drawBitmap(mBackground, null, mRect, mPaint);
	}

	/**
	 * 根据参数画出小块
	 * 
	 * @param canvas
	 * @param center
	 * @param radius
	 */
	private void drawOval(Canvas canvas, int center, int radius) {
		float itemSize = (360 * 1.0f - mSplitSize * mDotCount) / mDotCount;
		// 定义圆弧的形状和界限
		RectF rectF = new RectF(center - radius, center - radius, center + radius, center + radius);
		// 设置圆环颜色
		mPaint.setColor(mFirstColor);
		for (int i = 0; i < mDotCount; i++) {
			canvas.drawArc(rectF, i * (itemSize + mSplitSize), itemSize, false, mPaint);
		}
		// 根据当前进度绘制进度圆环
		mPaint.setColor(mNextColor);
		for (int i = 0; i < mCurrentProgress; i++) {
			canvas.drawArc(rectF, i * (itemSize + mSplitSize), itemSize, false, mPaint);
		}
	}

	// 记录手指按下和抬起的位置
	private int xDown = 0;
	private int xUp = 0;

	// private int xMove = 0;

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDown = (int) event.getY();
			break;
		case MotionEvent.ACTION_UP:
			xUp = (int) event.getY();
			if (xUp > xDown) { // 下滑
				if (mCurrentProgress > 0) {
					down();
				}
			} else {
				if (mCurrentProgress < mDotCount) {
					up();
				}
			}
			break;
		/*
		 * case MotionEvent.ACTION_MOVE: xMove = (int) event.getY(); if ((xMove -
		 * xUp) > 20) { // 下滑 down(); } else if ((xMove - xUp) < -20) { up(); }
		 * break;
		 */
		}
		return true;
	}

	private void up() {
		mCurrentProgress++;
		postInvalidate();
	}

	private void down() {
		mCurrentProgress--;
		postInvalidate();
	}
}
