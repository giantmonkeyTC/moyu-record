package cn.troph.tomon.ui.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Random;

import cn.troph.tomon.R;


public class WelcomeView extends FrameLayout {

    public static final int ROTATE_DEGREE = 360;

    private static final float X_DEVIATION_RATIO = 1.0f / 10;
    private static final float Y_DEVIATION_RATIO = 1.0f / 10;

    private static final int[] SELECTED_DRAWABLES_PINK = {
            R.drawable.ic_welcome_camera_pink,
            R.drawable.ic_welcome_communicate_pink,
            R.drawable.ic_welcome_game_pink,
            R.drawable.ic_welcome_heart_pink,
            R.drawable.ic_welcome_music_pink,
            R.drawable.ic_welcome_star_pink
    };
    private static final int[] SELECTED_DRAWABLES_BLUE = {
            R.drawable.ic_welcome_camera_blue,
            R.drawable.ic_welcome_communicate_blue,
            R.drawable.ic_welcome_game_blue,
            R.drawable.ic_welcome_heart_blue,
            R.drawable.ic_welcome_music_blue,
            R.drawable.ic_welcome_star_blue
    };
    private static final int[] SELECTED_DRAWABLES_YELLOW = {
            R.drawable.ic_welcome_camera_yellow,
            R.drawable.ic_welcome_communicate_yellow,
            R.drawable.ic_welcome_game_yellow,
            R.drawable.ic_welcome_heart_yellow,
            R.drawable.ic_welcome_music_yellow,
            R.drawable.ic_welcome_star_yellow
    };
    private static final int[][] SELECTED_DRAWABLES = {
            SELECTED_DRAWABLES_PINK,
            SELECTED_DRAWABLES_BLUE,
            SELECTED_DRAWABLES_YELLOW
    };
    private static final float[] SCALE_RATIOS = {
            1f,
            0.7f,
            0.5f,
    };
    public static final int DRAWABLE_MISS_DURATION_IN_MILLIS = 4000;
    public static final int DRAWABLE_OCCUR_DURATION_IN_MILLIS = 3000;
    public static final int MAX_OCCUR_JITTER_IN_SECONDS = 10;
    public static int sOccurJitterInSeconds = 4;

    private ImageView mIvTopLeft;
    private ImageView mIvTopRight;
    private ImageView mIvBottomLeft;
    private ImageView mIvBottomRight;
    private ImageView mIvLeftTop;
    private ImageView mIvLeftCenter;
    private ImageView mIvLeftBottom;
    private ImageView mIvRightTop;
    private ImageView mIvRightCenter;
    private ImageView mIvRightBottom;
    private ObjectAnimator mOccurAnim;
    private ObjectAnimator mMoveXAnim;
    private ObjectAnimator mMoveYAnim;
    private ObjectAnimator mScaleXAnim;
    private ObjectAnimator mScaleYAnim;
    private ObjectAnimator mMissAnim;

    public WelcomeView(@NonNull Context context) {
        this(context, null);
    }

    public WelcomeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public WelcomeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addViews();
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mOccurAnim != null) {
            return;
        }
        fillViews(w, h);
    }

    private void fillViews(int w, int h) {
        int deviationX = (int) (w * X_DEVIATION_RATIO);
        int deviationY = (int) (h * Y_DEVIATION_RATIO);

        // top 2 views
        fillTopLeftView(w, 0, deviationX, deviationY);

        fillTopRightView(w, 0, deviationX, deviationY);

        // bottom 2 views
        fillBottomLeftView(w, h, deviationX, deviationY);

        fillBottomRightView(w, h, deviationX, deviationY);

        // left 3 views
        fillLeftBottomView(w, h, deviationX, deviationY);

        fillLeftCenterView(w, h, deviationX, deviationY);

        fillLeftTopView(w, h, deviationX, deviationY);

        //right 3 views
        fillRightBottomView(w, h, deviationX, deviationY);

        fillRightCenterView(w, h, deviationX, deviationY);

        fillRightTopView(w, h, deviationX, deviationY);

        mIvRightTop.post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });
    }

    private void fillRightTopView(int w, int h, int deviationX, int deviationY) {
        int rightTopX = (int) (getX() + w * 3 / 4 + new Random(getSeed()).nextInt(2 * deviationX) - deviationX);
        int rightTopY = (int) (getY() + h / 4 + new Random(getSeed()).nextInt(2 * deviationY) - deviationY);
        setWidthAndHeight(w, h, mIvRightTop);
        mIvRightTop.setAlpha(0f);
        mIvRightTop.setX(rightTopX);
        mIvRightTop.setY(rightTopY);
        mIvRightTop.setRotation(new Random(getSeed()).nextInt(ROTATE_DEGREE));
        randomScale(mIvRightTop);
        mIvRightTop.setImageResource(randomGetDrawable());
        startAnim(mIvRightTop, w, h, deviationX, deviationY);
    }


    private Random mSeedProducer = new Random(System.currentTimeMillis());
    private long getSeed() {
        return System.currentTimeMillis() + mSeedProducer.nextInt(48);
    }

    private void fillRightCenterView(int w, int h, int deviationX, int deviationY) {
        int rightCenterX = (int) (getX() + w * 3 / 4 + new Random(getSeed()).nextInt(2 * deviationX) - deviationX);
        int rightCenterY = (int) (getY() + h / 2 + new Random(getSeed()).nextInt(2 * deviationY) - deviationY);
        setWidthAndHeight(w, h, mIvRightCenter);
        mIvRightCenter.setAlpha(0f);
        mIvRightCenter.setX(rightCenterX);
        mIvRightCenter.setY(rightCenterY);
        mIvRightCenter.setRotation(new Random(getSeed()).nextInt(360));
        randomScale(mIvRightCenter);
        mIvRightCenter.setImageResource(randomGetDrawable());
        startAnim(mIvRightCenter, w, h, deviationX, deviationY);
    }

    private void fillRightBottomView(int w, int h, int deviationX, int deviationY) {
        int rightBottomX = (int) (getX() + w * 3 / 4 + new Random(getSeed()).nextInt(2 * deviationX) - deviationX);
        int rightBottomY = (int) (getY() + h * 3 / 4 + new Random(getSeed()).nextInt(2 * deviationY) - deviationY);
        setWidthAndHeight(w, h, mIvRightBottom);
        mIvRightBottom.setAlpha(0f);
        mIvRightBottom.setX(rightBottomX);
        mIvRightBottom.setY(rightBottomY);
        mIvRightBottom.setRotation(new Random(getSeed()).nextInt(360));
        randomScale(mIvRightBottom);
        mIvRightBottom.setImageResource(randomGetDrawable());
        startAnim(mIvRightBottom, w, h, deviationX, deviationY);
    }

    private void fillLeftTopView(int w, int h, int deviationX, int deviationY) {
        int leftTopX = (int) (getX() + w / 4 + new Random(getSeed()).nextInt(2 * deviationX) - deviationX);
        int leftTopY = (int) (getY() + h / 4 + new Random(getSeed()).nextInt(2 * deviationY) - deviationY);
        setWidthAndHeight(w, h, mIvLeftTop);
        mIvLeftTop.setAlpha(0f);
        mIvLeftTop.setX(leftTopX);
        mIvLeftTop.setY(leftTopY);
        mIvLeftTop.setRotation(new Random(getSeed()).nextInt(360));
        randomScale(mIvLeftTop);
        mIvLeftTop.setImageResource(randomGetDrawable());
        startAnim(mIvLeftTop, w, h, deviationX, deviationY);
    }

    private void fillLeftCenterView(int w, int h, int deviationX, int deviationY) {
        int leftCenterX = (int) (getX() + w / 4 + new Random(getSeed()).nextInt(2 * deviationX) - deviationX);
        int leftCenterY = (int) (getY() + h / 2 + new Random(getSeed()).nextInt(2 * deviationY) - deviationY);
        setWidthAndHeight(w, h, mIvLeftCenter);
        mIvLeftCenter.setAlpha(0f);
        mIvLeftCenter.setX(leftCenterX);
        mIvLeftCenter.setY(leftCenterY);
        mIvLeftCenter.setRotation(new Random(getSeed()).nextInt(360));
        randomScale(mIvLeftCenter);
        mIvLeftCenter.setImageResource(randomGetDrawable());
        startAnim(mIvLeftCenter, w, h, deviationX, deviationY);
    }

    private void fillLeftBottomView(int w, int h, int deviationX, int deviationY) {
        int leftBottomX = (int) (getX() + w / 4 + new Random(getSeed()).nextInt(2 * deviationX) - deviationX);
        int leftBottomY = (int) (getY() + h * 3 / 4 + new Random(getSeed()).nextInt(2 * deviationY) - deviationY);
        setWidthAndHeight(w, h, mIvLeftBottom);
        mIvLeftBottom.setAlpha(0f);
        mIvLeftBottom.setX(leftBottomX);
        mIvLeftBottom.setY(leftBottomY);
        mIvLeftBottom.setRotation(new Random(getSeed()).nextInt(360));
        randomScale(mIvLeftBottom);
        mIvLeftBottom.setImageResource(randomGetDrawable());
        startAnim(mIvLeftBottom, w, h, deviationX, deviationY);
    }

    private int calculateViewLength(View view) {
        return (int) Math.sqrt(Math.pow(view.getWidth(), 2) + Math.pow(view.getHeight(), 2));
    }

    private void fillBottomRightView(int w, int h, int deviationX, int deviationY) {
        setWidthAndHeight(w, h, mIvBottomRight);
        int bottomRightViewX = (int) (getX() + w * 3 / 4 + new Random(getSeed()).nextInt(2 * deviationX) - deviationX);
        int bottomRightViewY = (int) (getY() + h - calculateViewLength(mIvBottomRight) - new Random(getSeed()).nextInt(deviationY));
        mIvBottomRight.setAlpha(0f);
        mIvBottomRight.setX(bottomRightViewX);
        mIvBottomRight.setY(bottomRightViewY);
        mIvBottomRight.setRotation(new Random(getSeed()).nextInt(360));
        randomScale(mIvBottomRight);
        mIvBottomRight.setImageResource(randomGetDrawable());
        startAnim(mIvBottomRight, w, h, deviationX, deviationY);
    }

    private void fillBottomLeftView(int w, int h, int deviationX, int deviationY) {
        setWidthAndHeight(w, h, mIvBottomLeft);
        int bottomLeftViewX = (int) (getX() + w / 4 + new Random(getSeed()).nextInt(2 * deviationX) - deviationX);
        int bottomLeftViewY = (int) (getY() + h - calculateViewLength(mIvBottomLeft) - new Random(getSeed()).nextInt(deviationY));
        mIvBottomLeft.setAlpha(0f);
        mIvBottomLeft.setX(bottomLeftViewX);
        mIvBottomLeft.setY(bottomLeftViewY);
        mIvBottomLeft.setRotation(new Random(getSeed()).nextInt(360));
        randomScale(mIvBottomLeft);
        mIvBottomLeft.setImageResource(randomGetDrawable());
        startAnim(mIvBottomLeft, w, h, deviationX, deviationY);
    }

    private void fillTopRightView(int w, int h, int deviationX, int deviationY) {
        int topRightViewX = w * 3 / 4 + new Random(getSeed()).nextInt(2 * deviationX) - deviationX;
        int topRightViewY = new Random(getSeed()).nextInt(deviationY);
        setWidthAndHeight(w, h, mIvTopRight);
        mIvTopRight.setAlpha(0f);
        mIvTopRight.setX(topRightViewX);
        mIvTopRight.setY(topRightViewY);
        mIvTopRight.setRotation(new Random(getSeed()).nextInt(360));
        randomScale(mIvTopRight);
        mIvTopRight.setImageResource(randomGetDrawable());
        startAnim(mIvTopRight, w, h, deviationX, deviationY);
    }

    private void fillTopLeftView(int w, int h, int deviationX, int deviationY) {
        int topLeftViewX = (int) (getX() + w / 4 + new Random(getSeed()).nextInt(2 * deviationX) - deviationX);
        int topLeftViewY = (int) (getY() + new Random(getSeed()).nextInt(deviationY));
        mIvTopLeft.setAlpha(0f);
        setWidthAndHeight(w, h, mIvTopLeft);
        mIvTopLeft.setX(topLeftViewX);
        mIvTopLeft.setY(topLeftViewY);
        mIvTopLeft.setRotation(new Random(getSeed()).nextInt(360));
        randomScale(mIvTopLeft);
        mIvTopLeft.setImageResource(randomGetDrawable());
        startAnim(mIvTopLeft, w, h, deviationX, deviationY);
    }

    private void startAnim(final View view, final int w, final int h, final int deviationX, final int deviationY) {
        mOccurAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1.0f);
        mOccurAnim.setDuration(DRAWABLE_OCCUR_DURATION_IN_MILLIS);
        mOccurAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mMoveXAnim = ObjectAnimator.ofFloat(view, "x",
                        view.getX(),
                        getX() + getWidth() / 2 - view.getWidth() / 2);
                mMoveYAnim = ObjectAnimator.ofFloat(view, "y",
                        view.getY(),
                        getY() + getHeight() / 2 - view.getHeight() / 2);
                mMoveXAnim.setDuration(DRAWABLE_MISS_DURATION_IN_MILLIS);
                mMoveYAnim.setDuration(DRAWABLE_MISS_DURATION_IN_MILLIS);
                mScaleXAnim = ObjectAnimator.ofFloat(view, "scaleX", view.getScaleX(), view.getScaleX()*0.6f);
                mScaleYAnim = ObjectAnimator.ofFloat(view, "scaleY", view.getScaleY(), view.getScaleY()*0.6f);
                mScaleXAnim.setDuration(DRAWABLE_MISS_DURATION_IN_MILLIS);
                mScaleYAnim.setDuration(DRAWABLE_MISS_DURATION_IN_MILLIS);
                mMissAnim = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f);
                mMissAnim.setDuration(DRAWABLE_MISS_DURATION_IN_MILLIS);
                mMissAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (view == mIvTopLeft) {
                            fillTopLeftView(w, h, deviationX, deviationY);
                        } else if (view == mIvTopRight) {
                            fillTopRightView(w, h, deviationX, deviationY);
                        } else if (view == mIvBottomLeft) {
                            fillBottomLeftView(w, h, deviationX, deviationY);
                        } else if (view == mIvBottomRight) {
                            fillBottomRightView(w, h, deviationX, deviationY);
                        } else if (view == mIvLeftTop) {
                            fillLeftTopView(w, h, deviationX, deviationY);
                        } else if (view == mIvLeftCenter) {
                            fillLeftCenterView(w, h, deviationX, deviationY);
                        } else if (view == mIvLeftBottom) {
                            fillLeftBottomView(w, h, deviationX, deviationY);
                        } else if (view == mIvRightTop) {
                            fillRightTopView(w, h, deviationX, deviationY);
                        } else if (view == mIvRightCenter) {
                            fillRightCenterView(w, h, deviationX, deviationY);
                        } else if (view == mIvRightBottom) {
                            fillRightBottomView(w, h, deviationX, deviationY);
                        }

                    }
                });
                mScaleXAnim.start();
                mScaleYAnim.start();
                mMoveXAnim.start();
                mMoveYAnim.start();
                mMissAnim.start();
            }
        });
        mOccurAnim.setStartDelay(new Random(getSeed()).nextInt(Math.min(sOccurJitterInSeconds++, MAX_OCCUR_JITTER_IN_SECONDS)) * 1000);
        mOccurAnim.start();
    }

    private static void setWidthAndHeight(int w, int h, ImageView view) {
        LayoutParams topLeftLp = (LayoutParams) view.getLayoutParams();
        topLeftLp.width = Math.min(w, h) / 10;
        topLeftLp.height = Math.min(w, h) / 10;
        view.setLayoutParams(topLeftLp);
    }

    private void addViews() {
        if (mIvTopLeft == null) {
            mIvTopLeft = new ImageView(getContext());
        }
        if (mIvTopRight == null) {
            mIvTopRight = new ImageView(getContext());
        }
        if (mIvBottomRight == null) {
            mIvBottomRight = new ImageView(getContext());
        }
        if (mIvLeftBottom == null) {
            mIvLeftBottom = new ImageView(getContext());
        }
        if (mIvLeftCenter == null) {
            mIvLeftCenter = new ImageView(getContext());
        }
        if (mIvLeftTop == null) {
            mIvLeftTop = new ImageView(getContext());
        }
        if (mIvRightBottom == null) {
            mIvRightBottom = new ImageView(getContext());
        }
        if (mIvRightCenter == null) {
            mIvRightCenter = new ImageView(getContext());
        }
        if (mIvRightTop == null) {
            mIvRightTop = new ImageView(getContext());
        }
        if (mIvBottomLeft == null) {
            mIvBottomLeft = new ImageView(getContext());
        }

        addView(mIvTopLeft);
        addView(mIvTopRight);
        addView(mIvBottomLeft);
        addView(mIvBottomRight);
        addView(mIvLeftTop);
        addView(mIvLeftCenter);
        addView(mIvLeftBottom);
        addView(mIvRightTop);
        addView(mIvRightCenter);
        addView(mIvRightBottom);
    }

    private void randomScale(ImageView view) {
        float scaleRatio = SCALE_RATIOS[new Random(getSeed()).nextInt(SCALE_RATIOS.length)];
        view.setScaleX(scaleRatio);
        view.setScaleY(scaleRatio);
    }

    private int randomGetDrawable() {
        int[] colorDrawables = SELECTED_DRAWABLES[new Random(getSeed()).nextInt(SELECTED_DRAWABLES.length)];
        return colorDrawables[new Random(getSeed()).nextInt(colorDrawables.length)];
    }
}
