package com.railscon.englishnotebook;


import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.nfc.Tag;
import android.os.Looper;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.railscon.englishnotebook.R.string;
import com.railscon.englishnotebook.R.styleable;
import com.railscon.englishnotebook.model.LineInfo;
import com.railscon.englishnotebook.model.LyricInfo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class LyricView extends View {
    private static final String TAG = "LyricView";
    public static final int LEFT = 0;
    public static final int CENTER = 1;
    public static final int RIGHT = 2;
    private static final float SLIDE_COEFFICIENT = 0.2F;
    private static final int UNITS_SECOND = 1000;
    private static final int UNITS_MILLISECOND = 1;
    private static final int FLING_ANIMATOR_DURATION = 500;
    private static final int THRESHOLD_Y_VELOCITY = 1600;
    private static final int INDICATOR_ICON_PLAY_MARGIN_LEFT = 7;
    private static final int INDICATOR_ICON_PLAY_WIDTH = 15;
    private static final int INDICATOR_LINE_MARGIN = 10;
    private static final int INDICATOR_TIME_TEXT_SIZE = 10;
    private static final int INDICATOR_TIME_MARGIN_RIGHT = 7;
    private static final int DEFAULT_TEXT_SIZE = 16;
    private static final int DEFAULT_MAX_LENGTH = 300;
    private static final int DEFAULT_LINE_SPACE = 25;
    private int mHintColor;
    private int mDefaultColor;
    private int mHighLightColor;
    private int mTextAlign;
    private int mLineCount;
    private int mTextSize;
    private float mLineHeight;
    private LyricInfo mLyricInfo;
    private String mDefaultHint;
    private int mMaxLength;
    private TextPaint mTextPaint;
    private Paint mBtnPlayPaint;
    private Paint mLinePaint;
    private Paint mTimerPaint;
    private boolean mFling = false;
    private ValueAnimator mFlingAnimator;
    private float mScrollY = 0.0F;
    private float mLineSpace = 0.0F;
    private boolean mIsShade;
    private float mShaderWidth = 0.0F;
    private int mCurrentPlayLine = 0;
    private boolean mShowIndicator;
    private VelocityTracker mVelocityTracker;
    private float mVelocity = 0.0F;
    private float mDownX;
    private float mDownY;
    private float mLastScrollY;
    private boolean mUserTouch = false;
    private int maxVelocity;
    private int mLineNumberUnderIndicator = 0;
    private Rect mBtnPlayRect = new Rect();
    private Rect mTimerRect;
    private String mDefaultTime = "00:00";
    private int mLineColor = Color.parseColor("#333333");
    private int mBtnColor = Color.parseColor("#666666");
    private List<Integer> mLineFeedRecord = new ArrayList();
    private boolean mEnableLineFeed = false;
    private int mExtraHeight = 0;
    private int mTextHeight;
    private float defMaxLength=300;
    private String mCurrentLyricFilePath = null;
    private LyricView.OnPlayerClickListener mClickListener;
    Runnable hideIndicator = new Runnable() {
        public void run() {
            LyricView.this.setUserTouch(false);
            LyricView.this.invalidateView();
        }
    };

    public LyricView(Context context) {
        super(context);
        this.initMyView(context);
    }

    public LyricView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.getAttrs(context, attributeSet);
        this.initMyView(context);
    }

    public LyricView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.getAttrs(context, attributeSet);
        this.initMyView(context);
    }
    public void moveToTop(int height){

        smoothScrollTo(height);
        Log.d(TAG,Integer.toString(height));
    }

    public int getCurrentPlayLine(){
        return mCurrentPlayLine;
    }
    public LyricInfo getLyricInfo(){
        return mLyricInfo;
    }
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if(this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }

        this.mVelocityTracker.addMovement(event);
        switch(event.getAction()) {
            case 0:
                this.actionDown(event);
                break;
            case 1:
                this.actionUp(event);
                break;
            case 2:
                this.actionMove(event);
                break;
            case 3:
                this.actionCancel(event);
        }

        this.invalidateView();
        return true;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.mBtnPlayRect.set((int)this.getRawSize(1, 7.0F),
                (int)((float)this.getHeight() * 0.5F - this.getRawSize(2, 15.0F) * 0.5F),
                (int)(this.getRawSize(2, 15.0F) + this.getRawSize(1, 7.0F)),
                (int)((float)this.getHeight() * 0.5F + this.getRawSize(2, 15.0F) * 0.5F));
        this.mShaderWidth = (float)this.getWidth() * 0.4F;
        this.mMaxLength = this.getWidth()- 60;
    }

    protected void onDraw(Canvas canvas) {
        if(this.scrollable()) {
            if(this.mShowIndicator) {
                this.drawIndicator(canvas);
            }

            for(int i = 0; i < this.mLineCount; ++i) {
                float x = 0.0F;
                switch(this.mTextAlign) {
                    case 0:
                        x = (float)(17 + this.mBtnPlayRect.width());
                        break;
                    case 1:
                        x = (float)this.getWidth() * 0.5F;
                        break;
                    case 2:
                        x = (float)(this.getWidth() - 20 - this.mTimerRect.width() - 7);
                }

                float y;
                if(this.mEnableLineFeed && i > 0) {
                    y = (float)this.getMeasuredHeight() * 0.5F + (float)i * this.mLineHeight - this.mScrollY + (float)((Integer)this.mLineFeedRecord.get(i - 1)).intValue();
                } else {
                    y = (float)this.getMeasuredHeight() * 0.5F + (float)i * this.mLineHeight - this.mScrollY;
                }

                if(y >= 0.0F) {
                    if(y > (float)this.getHeight()) {
                        break;
                    }

                    if(i == this.mCurrentPlayLine - 1) {
                        this.mTextPaint.setColor(this.mHighLightColor);
                    } else if(i == this.mLineNumberUnderIndicator && this.mShowIndicator) {
                        this.mTextPaint.setColor(-3355444);
                    } else {
                        this.mTextPaint.setColor(this.mDefaultColor);
                    }

                    if(!this.mIsShade || y <= (float)this.getHeight() - this.mShaderWidth && y >= this.mShaderWidth) {
                        this.mTextPaint.setAlpha(255);
                    } else if(y < this.mShaderWidth) {
                        this.mTextPaint.setAlpha(26 + (int)(23000.0F * y / this.mShaderWidth * 0.01F));
                    } else {
                        this.mTextPaint.setAlpha(26 + (int)(23000.0F * ((float)this.getHeight() - y) / this.mShaderWidth * 0.01F));
                    }

                    if(this.mEnableLineFeed) {
                        StaticLayout staticLayout = new StaticLayout(((LineInfo)this.mLyricInfo.songLines.get(i)).content, this.mTextPaint, this.mMaxLength, android.text.Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
                        canvas.save();
                        canvas.translate(x, y);
                        staticLayout.draw(canvas);
                        canvas.restore();
                    } else {
                        canvas.drawText(((LineInfo)this.mLyricInfo.songLines.get(i)).content, x, y, this.mTextPaint);
                    }
                }
            }
        } else {
            this.mTextPaint.setColor(this.mHintColor);
            canvas.drawText(this.mDefaultHint, (float)(this.getMeasuredWidth() / 2), (float)(this.getMeasuredHeight() / 2), this.mTextPaint);
        }


    }

    private void getAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, styleable.LyricView);
        this.mIsShade = ta.getBoolean(styleable.LyricView_fadeInFadeOut, false);
        this.mDefaultHint = ta.getString(styleable.LyricView_hint) != null?ta.getString(styleable.LyricView_hint):this.getResources().getString(string.default_hint);
        this.mHintColor = ta.getColor(styleable.LyricView_hintColor, Color.parseColor("#FFFFFF"));
        this.mDefaultColor = ta.getColor(styleable.LyricView_textColor, Color.parseColor("#8D8D8D"));
        this.mHighLightColor = ta.getColor(styleable.LyricView_highlightColor, Color.parseColor("#FFFFFF"));
        this.mTextSize = ta.getDimensionPixelSize(styleable.LyricView_textSize, (int)this.getRawSize(2, 16.0F));
        this.mTextAlign = ta.getInt(styleable.LyricView_textAlign, 1);
        this.mMaxLength = ta.getDimensionPixelSize(styleable.LyricView_maxLength, (int)this.getRawSize(1, 300F));
        this.mLineSpace = (float)ta.getDimensionPixelSize(styleable.LyricView_lineSpace, (int)this.getRawSize(1, 25.0F));
        ta.recycle();
    }

    public void setOnPlayerClickListener(LyricView.OnPlayerClickListener mClickListener) {
        this.mClickListener = mClickListener;
    }

    public void setAlignment(int alignment) {
        this.mTextAlign = alignment;
    }

    public void setCurrentTimeMillis(long current) {
        this.scrollToCurrentTimeMillis(current);
    }


    public void setLyricFile(LyricInfo lyricInfo, List<LineInfo> songLines) {
       //TODO CHECK!!!!
                this.setupLyricResource(lyricInfo, songLines);

                for(int i = 0; i < this.mLyricInfo.songLines.size(); ++i) {
                    StaticLayout staticLayout = new StaticLayout(((LineInfo)this.mLyricInfo.songLines.get(i)).content, this.mTextPaint, (int)this.getRawSize(1, 300.0F), android.text.Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
                    if(staticLayout.getLineCount() > 1) {
                        this.mEnableLineFeed = true;
                        this.mExtraHeight += (staticLayout.getLineCount() - 1) * this.mTextHeight;
                    }

                    this.mLineFeedRecord.add(i, Integer.valueOf(this.mExtraHeight));
                }


    }

    private void setLineSpace(float lineSpace) {
        if(this.mLineSpace != lineSpace) {
            this.mLineSpace = this.getRawSize(1, lineSpace);
            this.measureLineHeight();
            this.mScrollY = this.measureCurrentScrollY(this.mCurrentPlayLine);
            this.invalidateView();
        }

    }

    public void reset() {
        this.resetView();
    }

    private void actionCancel(MotionEvent event) {
        this.releaseVelocityTracker();
    }

    private void actionDown(MotionEvent event) {
        this.removeCallbacks(this.hideIndicator);
        this.mLastScrollY = this.mScrollY;
        this.mDownX = event.getX();
        this.mDownY = event.getY();
        if(this.mFlingAnimator != null) {
            this.mFlingAnimator.cancel();
            this.mFlingAnimator = null;
        }

        this.setUserTouch(true);
    }

    private boolean overScrolled() {
        return this.scrollable() && (this.mScrollY > this.mLineHeight * (float)(this.mLineCount - 1) + (float)((Integer)this.mLineFeedRecord.get(this.mLineCount - 1)).intValue() + (float)(this.mEnableLineFeed?this.mTextHeight:0) || this.mScrollY < 0.0F);
    }

    private void actionMove(MotionEvent event) {
        if(this.scrollable()) {
            VelocityTracker tracker = this.mVelocityTracker;
            tracker.computeCurrentVelocity(1000, (float)this.maxVelocity);
            this.mScrollY = this.mLastScrollY + this.mDownY - event.getY();
            this.mVelocity = tracker.getYVelocity();
            this.measureCurrentLine();
        }

    }

    private void actionUp(MotionEvent event) {
        this.postDelayed(this.hideIndicator, 3000L);
        this.releaseVelocityTracker();
        if(this.scrollable()) {
            if(this.overScrolled() && this.mScrollY < 0.0F) {
                this.smoothScrollTo(0.0F);
                return;
            }

            if(this.overScrolled() && this.mScrollY > this.mLineHeight * (float)(this.mLineCount - 1) + (float)((Integer)this.mLineFeedRecord.get(this.mLineCount - 1)).intValue() + (float)(this.mEnableLineFeed?this.mTextHeight:0)) {
                this.smoothScrollTo(this.mLineHeight * (float)(this.mLineCount - 1) + (float)((Integer)this.mLineFeedRecord.get(this.mLineCount - 1)).intValue() + (float)(this.mEnableLineFeed?this.mTextHeight:0));
                return;
            }

            if(Math.abs(this.mVelocity) > 1600.0F) {
                this.doFlingAnimator(this.mVelocity);
                return;
            }

            if(this.mShowIndicator && this.clickPlayer(event) && this.mLineNumberUnderIndicator != this.mCurrentPlayLine) {
                this.mShowIndicator = false;
                if(this.mClickListener != null) {
                    this.setUserTouch(false);
                    this.mClickListener.onPlayerClicked(((LineInfo)this.mLyricInfo.songLines.get(this.mLineNumberUnderIndicator)).start, ((LineInfo)this.mLyricInfo.songLines.get(this.mLineNumberUnderIndicator)).content);
                }
            }
        }

    }

    private String measureCurrentTime() {
        DecimalFormat format = new DecimalFormat("00");
        return this.mLyricInfo != null && this.mLineCount > 0 && this.mLineNumberUnderIndicator - 1 < this.mLineCount && this.mLineNumberUnderIndicator > 0?format.format(((LineInfo)this.mLyricInfo.songLines.get(this.mLineNumberUnderIndicator - 1)).start / 1000L / 60L) + ":" + format.format(((LineInfo)this.mLyricInfo.songLines.get(this.mLineNumberUnderIndicator - 1)).start / 1000L % 60L):(this.mLyricInfo != null && this.mLineCount > 0 && this.mLineNumberUnderIndicator - 1 >= this.mLineCount?format.format(((LineInfo)this.mLyricInfo.songLines.get(this.mLineCount - 1)).start / 1000L / 60L) + ":" + format.format(((LineInfo)this.mLyricInfo.songLines.get(this.mLineCount - 1)).start / 1000L % 60L):(this.mLyricInfo != null && this.mLineCount > 0 && this.mLineNumberUnderIndicator - 1 <= 0?format.format(((LineInfo)this.mLyricInfo.songLines.get(0)).start / 1000L / 60L) + ":" + format.format(((LineInfo)this.mLyricInfo.songLines.get(0)).start / 1000L % 60L):this.mDefaultTime));
    }

    private void drawIndicator(Canvas canvas) {
        Path pathPlay = new Path();
        float yCoordinate = (float)this.mBtnPlayRect.left + (float)Math.sqrt(Math.pow((double)this.mBtnPlayRect.width(), 2.0D) - Math.pow((double)((float)this.mBtnPlayRect.width() * 0.5F), 2.0D));
        float remainWidth = (float)this.mBtnPlayRect.right - yCoordinate;
        pathPlay.moveTo((float)this.mBtnPlayRect.centerX() - (float)this.mBtnPlayRect.width() * 0.5F, (float)this.mBtnPlayRect.centerY() - (float)this.mBtnPlayRect.height() * 0.5F);
        pathPlay.lineTo((float)this.mBtnPlayRect.centerX() - (float)this.mBtnPlayRect.width() * 0.5F, (float)this.mBtnPlayRect.centerY() + (float)this.mBtnPlayRect.height() * 0.5F);
        pathPlay.lineTo(yCoordinate, (float)this.mBtnPlayRect.centerY());
        pathPlay.lineTo((float)this.mBtnPlayRect.centerX() - (float)this.mBtnPlayRect.width() * 0.5F, (float)this.mBtnPlayRect.centerY() - (float)this.mBtnPlayRect.height() * 0.5F);
        canvas.drawPath(pathPlay, this.mBtnPlayPaint);

        Path pathLine = new Path();
        pathLine.moveTo((float)this.mBtnPlayRect.right + this.getRawSize(1, 10.0F) - remainWidth, (float)this.getMeasuredHeight() * 0.5F-this.getRawSize(1, 20.0F));
        pathLine.lineTo((float)(this.getWidth() - this.mTimerRect.width()) - this.getRawSize(1, 7.0F) - this.getRawSize(1, 10.0F), (float)this.getHeight() * 0.5F-this.getRawSize(1, 20.0F));

        pathLine.moveTo((float)this.mBtnPlayRect.right + this.getRawSize(1, 10.0F) - remainWidth, (float)this.getMeasuredHeight() * 0.5F+this.getRawSize(1, 20.0F));
        pathLine.lineTo((float)(this.getWidth() - this.mTimerRect.width()) - this.getRawSize(1, 7.0F) - this.getRawSize(1, 10.0F), (float)this.getHeight() * 0.5F+this.getRawSize(1, 20.0F));
        canvas.drawPath(pathLine, this.mLinePaint);
        canvas.drawText(this.measureCurrentTime(), (float)this.getWidth() - this.getRawSize(1, 7.0F), (float)(this.getHeight() + this.mTimerRect.height()) * 0.5F, this.mTimerPaint);
    }

    private boolean clickPlayer(MotionEvent event) {
        if(this.mBtnPlayRect != null && this.mDownX > (float)(this.mBtnPlayRect.left - 7) && this.mDownX < (float)(this.mBtnPlayRect.right + this.getRawSize(1, mMaxLength)) && this.mDownY > (float)(this.mBtnPlayRect.top - this.getRawSize(1, 40.0F)) && this.mDownY < (float)(this.mBtnPlayRect.bottom + this.getRawSize(1, 40.0F))) {
            float upX = event.getX();
            float upY = event.getY();
            return upX > (float)(this.mBtnPlayRect.left - 7) && upX < (float)(this.mBtnPlayRect.right + this.getRawSize(1, mMaxLength)) && upY > (float)(this.mBtnPlayRect.top - this.getRawSize(1, 40.0F)) && upY < (float)(this.mBtnPlayRect.bottom + this.getRawSize(1, 40.0F));
        } else {
            return false;
        }
    }

    private void doFlingAnimator(float velocity) {
        float distance = velocity / Math.abs(velocity) * Math.abs(velocity) * 0.2F;
        float to = Math.min(Math.max(0.0F, this.mScrollY - distance), (float)(this.mLineCount - 1) * this.mLineHeight + (float)((Integer)this.mLineFeedRecord.get(this.mLineCount - 1)).intValue() + (float)(this.mEnableLineFeed?this.mTextHeight:0));
        this.mFlingAnimator = ValueAnimator.ofFloat(new float[]{this.mScrollY, to});
        this.mFlingAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                LyricView.this.mScrollY = ((Float)animation.getAnimatedValue()).floatValue();
                LyricView.this.measureCurrentLine();
                LyricView.this.invalidateView();
            }
        });
        this.mFlingAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                LyricView.this.mVelocity = 0.0F;
                LyricView.this.mFling = true;
            }

            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                LyricView.this.mFling = false;
            }

            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }
        });
        this.mFlingAnimator.setDuration(500L);
        this.mFlingAnimator.setInterpolator(new DecelerateInterpolator());
        this.mFlingAnimator.start();
    }

    private void setUserTouch(boolean isUserTouch) {
        if(isUserTouch) {
            this.mUserTouch = true;
            this.mShowIndicator = true;
        } else {
            this.mUserTouch = false;
            this.mShowIndicator = false;
        }

    }

    private void releaseVelocityTracker() {
        if(this.mVelocityTracker != null) {
            this.mVelocityTracker.clear();
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }

    }

    private void initMyView(Context context) {
        this.maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        this.initPaint();
        this.initAllBounds();
    }

    private void initAllBounds() {
        this.setRawTextSize((float)this.mTextSize);
        this.setLineSpace(this.mLineSpace);
        this.measureLineHeight();
        this.mTimerRect = new Rect();
        this.mTimerPaint.getTextBounds(this.mDefaultTime, 0, this.mDefaultTime.length(), this.mTimerRect);
    }

    private void initPaint() {
        defMaxLength = getWidth() - 37f;
        this.mTextPaint = new TextPaint();
        this.mTextPaint.setDither(true);
        this.mTextPaint.setAntiAlias(true);
        switch(this.mTextAlign) {
            case 0:
                this.mTextPaint.setTextAlign(Align.LEFT);
                break;
            case 1:
                this.mTextPaint.setTextAlign(Align.CENTER);
                break;
            case 2:
                this.mTextPaint.setTextAlign(Align.RIGHT);
        }

        this.mBtnPlayPaint = new Paint();
        this.mBtnPlayPaint.setDither(true);
        this.mBtnPlayPaint.setAntiAlias(true);
        this.mBtnPlayPaint.setColor(this.mBtnColor);
        this.mBtnPlayPaint.setStyle(Style.FILL_AND_STROKE);
        this.mBtnPlayPaint.setAlpha(128);
        this.mLinePaint = new Paint();
        this.mLinePaint.setDither(true);
        this.mLinePaint.setAntiAlias(true);
        this.mLinePaint.setColor(this.mLineColor);
        this.mLinePaint.setAlpha(64);
        this.mLinePaint.setStrokeWidth(1.0F);
        this.mLinePaint.setStyle(Style.STROKE);
        this.mTimerPaint = new Paint();
        this.mTimerPaint.setDither(true);
        this.mTimerPaint.setAntiAlias(true);
        this.mTimerPaint.setColor(this.mLineColor);
        this.mTimerPaint.setTextAlign(Align.RIGHT);
        this.mTimerPaint.setTextSize(this.getRawSize(2, 10.0F));
    }

    private float measureCurrentScrollY(int line) {
        return this.mEnableLineFeed && line > 1?(float)(line - 1) * this.mLineHeight + (float)((Integer)this.mLineFeedRecord.get(line - 1)).intValue():(float)(line - 1) * this.mLineHeight;
    }

    private void invalidateView() {
        if(Looper.getMainLooper() == Looper.myLooper()) {
            this.invalidate();
        } else {
            this.postInvalidate();
        }

    }

    private void measureLineHeight() {
        Rect lineBound = new Rect();
        this.mTextPaint.getTextBounds(this.mDefaultHint, 0, this.mDefaultHint.length(), lineBound);
        this.mTextHeight = lineBound.height();
        this.mLineHeight = (float)this.mTextHeight + this.mLineSpace;
    }

    private void measureCurrentLine() {
        float baseScrollY = this.mScrollY + this.mLineHeight * 0.5F;
        if(this.mEnableLineFeed) {
            for(int i = this.mLyricInfo.songLines.size(); i >= 0; --i) {
                if((double)baseScrollY > (double)this.measureCurrentScrollY(i) + (double)this.mLineSpace * 0.2D) {
                    this.mLineNumberUnderIndicator = i - 1;
                    break;
                }
            }
        } else {
            this.mLineNumberUnderIndicator = (int)(baseScrollY / this.mLineHeight);
        }

    }

    private void smoothScrollTo(float toY) {
        ValueAnimator animator = ValueAnimator.ofFloat(new float[]{this.mScrollY, toY});
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                LyricView.this.mScrollY = ((Float)valueAnimator.getAnimatedValue()).floatValue();
                LyricView.this.invalidateView();
            }
        });
        animator.addListener(new AnimatorListener() {
            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                LyricView.this.mFling = false;
                LyricView.this.measureCurrentLine();
                LyricView.this.invalidateView();
            }

            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
                LyricView.this.mFling = true;
            }
        });
        animator.setDuration(640L);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    private boolean scrollable() {
        return this.mLyricInfo != null && this.mLyricInfo.songLines != null && this.mLyricInfo.songLines.size() > 0;
    }

    private void scrollToCurrentTimeMillis(long time) {
        int position = 0;
        if(this.scrollable()) {
            int i = 0;

            for(int size = this.mLineCount; i < size; ++i) {
                LineInfo lineInfo = (LineInfo)this.mLyricInfo.songLines.get(i);
                if(lineInfo != null && lineInfo.start >= time) {
                    position = i;
                    break;
                }

                if(i == this.mLineCount - 1) {
                    position = this.mLineCount;
                }
            }
        }

        if(this.mCurrentPlayLine != position) {
            this.mCurrentPlayLine = position;
            if(!this.mFling && !this.mUserTouch) {
                this.smoothScrollTo(this.measureCurrentScrollY(position));
            }
        }

    }

    public void setupLyricResource(LyricInfo lyricInfo, List<LineInfo> songLines) {
        if(songLines != null) {

               //TODO: 参数类型要改 LyricInfo 现在还是 private 需要改为public
                lyricInfo.songLines = songLines;
                this.mLyricInfo = lyricInfo;
                this.mLineCount = this.mLyricInfo.songLines.size();
                this.invalidateView();

        } else {
            this.invalidateView();
        }

    }


    //TODO 需要删掉 不过要先借鉴下歌词判断逻辑
    private void analyzeLyric(LyricInfo lyricInfo, String line) {
        int index = line.lastIndexOf("]");
        if(line.startsWith("[offset:")) {
            lyricInfo.songOffset = Long.parseLong(line.substring(8, index).trim());
        } else if(line.startsWith("[ti:")) {
            lyricInfo.songTitle = line.substring(4, index).trim();
        } else if(line.startsWith("[ar:")) {
            lyricInfo.songArtist = line.substring(4, index).trim();
        } else if(line.startsWith("[al:")) {
            lyricInfo.songAlbum = line.substring(4, index).trim();
        } else if(!line.startsWith("[by:")) {
            if(index >= 9 && line.trim().length() > index + 1) {
                LineInfo lineInfo = new LineInfo();
                lineInfo.content = line.substring(10, line.length());
                lineInfo.start = this.measureStartTimeMillis(line.substring(0, index));
                lyricInfo.songLines.add(lineInfo);
            }

        }
    }

    private long measureStartTimeMillis(String str) {
        long minute = Long.parseLong(str.substring(1, 3));
        long second = Long.parseLong(str.substring(4, 6));
        long millisecond = Long.parseLong(str.substring(7, 9));
        return millisecond + second * 1000L + minute * 60L * 1000L;
    }

    private void resetLyricInfo() {
        if(this.mLyricInfo != null) {
            if(this.mLyricInfo.songLines != null) {
                this.mLyricInfo.songLines.clear();
                this.mLyricInfo.songLines = null;
            }

            this.mLyricInfo = null;
        }

    }

    private void resetView() {
        this.mCurrentPlayLine = 0;
        this.resetLyricInfo();
        this.invalidateView();
        this.mLineCount = 0;
        this.mScrollY = 0.0F;
        this.mEnableLineFeed = false;
        this.mLineFeedRecord.clear();
        this.mExtraHeight = 0;
    }

    private float getRawSize(int unit, float size) {
        Context context = this.getContext();
        Resources resources;
        if(context == null) {
            resources = Resources.getSystem();
        } else {
            resources = context.getResources();
        }

        return TypedValue.applyDimension(unit, size, resources.getDisplayMetrics());
    }

    private void setRawTextSize(float size) {
        if(size != this.mTextPaint.getTextSize()) {
            this.mTextPaint.setTextSize(size);
            this.measureLineHeight();
            this.mScrollY = this.measureCurrentScrollY(this.mCurrentPlayLine);
            this.invalidateView();
        }

    }

    public interface OnPlayerClickListener {
        void onPlayerClicked(long var1, String var3);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Alignment {
    }

}
