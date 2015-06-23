package combitech.com.againstrumentcluster;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class BatteryRangeView extends View {
    public final static int MAX_RANGE = 50;
    private final int green = Color.parseColor("#5FBE26");
    private final int red = Color.parseColor("#FF0000");
    private final int blue = Color.parseColor("#00C9FF");
    private final int background = Color.parseColor("#282828");

    private int viewWidth;
    private int viewHeight;
    private double theta;
    private ContainedVector startPoint, center, newPoint;
    private Paint backgroundPaint;

    private int range;
    private int rangeRadius = 460;
    private float rangeSmallerCircleRadius = 70;
    private float rangePercentageFilled = 1f;
    private Paint rangePaint, rangeTextPaint;
    private int rangePaintStrokeWidth = 8;
    private float rangeStartAngle = 45;
    private float rangeSweepAngle = 360 - (rangeStartAngle * 2 + 14);
    private RectF rangeOuterCircle;
    private PointF rangeCircleCenter;

    private float batteryLevel;
    private Paint batteryPaint;
    private Paint batteryCirclePaint;
    private Paint batteryGradientPaint;
    private int batteryPaintStrokeWidth = 10;
    private int batteryRadius = 500;
    private float batteryPercentageFilled = 1f;
    private float batteryStartAngle = 30 + batteryPaintStrokeWidth * 0.1f;
    private float batteryStopAngle = -30 * 2 - batteryPaintStrokeWidth * 0.1f;
    private RectF batteryOuterCircle;
    private PointF batteryCircleCenter;
    private float batterySmallerCircleRadius = 15;

    public BatteryRangeView(Context context) {
        this(context, null);
    }

    public BatteryRangeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryRangeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        viewHeight = 1072;
        viewWidth = 1072;
        setupRange();
        setupBattery();

        startPoint = new ContainedVector(2);
        center = new ContainedVector(2);
        newPoint = new ContainedVector(2);
        backgroundPaint = new Paint();
        backgroundPaint.setDither(true);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(background);
        backgroundPaint.setStyle(Paint.Style.FILL);
    }

    private void setupRange() {
        rangePaint = new Paint();
        rangePaint.setColor(green);
        rangePaint.setStyle(Paint.Style.STROKE);
        rangePaint.setStrokeJoin(Paint.Join.ROUND);
        rangePaint.setStrokeCap(Paint.Cap.ROUND);
        rangePaint.setAntiAlias(true);
        rangePaint.setDither(true);
        rangePaint.setStrokeWidth(rangePaintStrokeWidth);

        rangeTextPaint = new Paint();
        rangeTextPaint.setColor(green);
        rangeTextPaint.setAntiAlias(true);
        rangeTextPaint.setDither(true);
        rangeTextPaint.setTextSize(50);

        rangeCircleCenter = new PointF(viewWidth / 2, viewHeight / 2);
        float x = viewWidth / 2 - rangeRadius;
        float y = viewHeight / 2 - rangeRadius;
        float circleWidth = viewWidth / 2 + rangeRadius;
        float circleHeight = viewHeight / 2 + rangeRadius;
        rangeOuterCircle = new RectF(x, y, circleWidth, circleHeight);
    }

    private void setupBattery() {
        batteryPaint = new Paint();
        batteryPaint.setColor(blue);
        batteryPaint.setStyle(Paint.Style.STROKE);
        batteryPaint.setStrokeJoin(Paint.Join.ROUND);
        batteryPaint.setStrokeCap(Paint.Cap.ROUND);
        batteryPaint.setAntiAlias(true);
        batteryPaint.setStrokeWidth(batteryPaintStrokeWidth);

        batteryCirclePaint = new Paint();
        batteryCirclePaint.setColor(blue);
        batteryCirclePaint.setStyle(Paint.Style.FILL);
        batteryCirclePaint.setAntiAlias(true);
        batteryCirclePaint.setTextSize(50);

        batteryGradientPaint = new Paint();
        batteryGradientPaint.setColor(Color.BLACK);
        batteryGradientPaint.setStrokeWidth(1);
        batteryGradientPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        batteryGradientPaint.setShader(new RadialGradient(viewWidth / 2, viewHeight / 2, viewHeight / 3, Color.TRANSPARENT, Color.BLUE, Shader.TileMode.CLAMP));

        batteryCircleCenter = new PointF(viewWidth / 2, viewHeight / 2);
        float x = viewWidth / 2 - batteryRadius;
        float y = viewHeight / 2 - batteryRadius;
        float circleWidth = viewWidth / 2 + batteryRadius;
        float circleHeight = viewHeight / 2 + batteryRadius;
        batteryOuterCircle = new RectF(x, y, circleWidth, circleHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();

       // drawBorder(canvas);
        drawBattery(canvas);
        drawRange(canvas);

        canvas.restore();
    }

    private void drawBorder(Canvas canvas) {
        Paint tmpPaint = new Paint();
        tmpPaint.setColor(Color.WHITE);
        tmpPaint.setStrokeWidth(5);
        tmpPaint.setStyle(Paint.Style.STROKE);
        RectF tmpRect = new RectF(5, 5, viewWidth - 5, viewHeight - 5);
        canvas.drawRect(tmpRect, tmpPaint);
    }

    private void drawRange(Canvas canvas) {
        theta = -Math.toRadians(rangeSweepAngle * rangePercentageFilled);
        startPoint.set(
                rangeCircleCenter.x + (rangeRadius) * cos(0.290 * PI),
                rangeCircleCenter.y + (rangeRadius) * sin(0.290 * PI)
        );
        center.set(rangeCircleCenter.x, rangeCircleCenter.y);
        startPoint.minus(center);
        newPoint.set(
                cos(theta) * startPoint.cartesian(0) + sin(theta) * startPoint.cartesian(1),
                -sin(theta) * startPoint.cartesian(0) + cos(theta) * startPoint.cartesian(1)
        );
        center.plus(newPoint);
        newPoint.set(center.cartesian(0), center.cartesian(1));
        rangePaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(rangeOuterCircle, rangeStartAngle, rangeSweepAngle * rangePercentageFilled, false, rangePaint);
        canvas.drawCircle(
                (float) newPoint.cartesian(0),
                (float) newPoint.cartesian(1),
                (float) (rangeSmallerCircleRadius),
                backgroundPaint
        );
        canvas.drawCircle(
                (float) newPoint.cartesian(0),
                (float) newPoint.cartesian(1),
                (float) (rangeSmallerCircleRadius),
                rangePaint
        );
        if (range > 9) {
            canvas.drawText(
                    String.valueOf(range),
                    (float) newPoint.cartesian(0) - 27,
                    (float) newPoint.cartesian(1) + 15,
                    rangeTextPaint
            );
        } else {
            canvas.drawText(
                    String.valueOf(range),
                    (float) newPoint.cartesian(0) - 15,
                    (float) newPoint.cartesian(1) + 15,
                    rangeTextPaint
            );
        }
        rangePaint.setStyle(Paint.Style.FILL);
        //canvas.drawRect(rangeOuterCircle, rangePaint);
    }

    private void drawBattery(Canvas canvas) {
        theta = -Math.toRadians(batteryStopAngle * batteryPercentageFilled);
        startPoint.set(
                batteryCircleCenter.x + (batteryRadius) * cos(PI / 6 * 1.0),
                batteryCircleCenter.y + (batteryRadius) * sin(PI / 6 * 1.0)
        );
        center.set(batteryCircleCenter.x, batteryCircleCenter.y);
        startPoint.minus(center);
        newPoint.set(
                cos(theta) * startPoint.cartesian(0) + sin(theta) * startPoint.cartesian(1),
                -sin(theta) * startPoint.cartesian(0) + cos(theta) * startPoint.cartesian(1)
        );
        center.plus(newPoint);
        newPoint.set(center.cartesian(0), center.cartesian(1));

        canvas.drawArc(batteryOuterCircle, batteryStartAngle, batteryStopAngle * batteryPercentageFilled, false, batteryPaint);

        canvas.drawCircle(
                (float) newPoint.cartesian(0),
                (float) newPoint.cartesian(1),
                batterySmallerCircleRadius,
                batteryCirclePaint
        );
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(viewWidth, viewHeight);
    }

    public void setRange(int range) {
        setRange(range, true);
    }

    private void setRange(int range, boolean updatePercentage) {
        this.range = range;
        if (updatePercentage)
            setRangePercentageFilled((float) range / (float) MAX_RANGE, false);
        postInvalidate();
    }

    public void setRangePercentageFilled(float rangePercentageFilled) {
        setRangePercentageFilled(rangePercentageFilled, true);
    }

    private void setRangePercentageFilled(float rangePercentageFilled, boolean updateRange) {
        this.rangePercentageFilled = rangePercentageFilled;
        if (updateRange)
            setRange((int) (MAX_RANGE * rangePercentageFilled + 0.5f), false);
        if (rangePercentageFilled < 0.2) {
            rangePaint.setColor(red);
            rangeTextPaint.setColor(red);
        } else {
            rangePaint.setColor(green);
            rangeTextPaint.setColor(green);
        }
        postInvalidate();
    }

    public void setBatteryLevel(float batteryLevel) {
        setBatteryLevel(batteryLevel, true);
    }

    private void setBatteryLevel(float batteryLevel, boolean updatePercentage) {
        this.batteryLevel = batteryLevel;
        if (updatePercentage)
            setBatteryPercentageFilled(batteryLevel, false);
        postInvalidate();
    }

    public void setBatteryPercentageFilled(float batteryPercentageFilled) {
        setBatteryPercentageFilled(batteryPercentageFilled, true);
    }

    private void setBatteryPercentageFilled(float batteryPercentageFilled, boolean updateBatteryLevel) {
        this.batteryPercentageFilled = batteryPercentageFilled;
        if (updateBatteryLevel)
            setBatteryLevel((batteryPercentageFilled), false);
        if (batteryPercentageFilled < 0.2f) {
            batteryPaint.setColor(red);
            batteryCirclePaint.setColor(red);
        } else {
            batteryPaint.setColor(blue);
            batteryCirclePaint.setColor(blue);
        }
        postInvalidate();
    }
}
