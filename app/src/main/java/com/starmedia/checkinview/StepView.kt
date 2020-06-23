package com.starmedia.checkinview

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.RequiresApi

/**
 *  作者：xiaofei
 *  日期：2020/6/22
 */
class StepView: View {

    private var mWidth = 0
    private var mHeight = 0
    private val days = 1..7
    private val secondDays = 14 downTo 8
    private val currentDay = 5

    private val mDatePaint: Paint = Paint()
    private val mRewardPaint: Paint = Paint()

    private val mCompletedProcessPaint: Paint = Paint()
    private val mUnCompletedProcessPaint: Paint = Paint()

    private var completedEndX: Float = 0f

    private val reactHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        10f, resources.displayMetrics)

    private val missionIndicatorPaint = Paint()
    private val missionIndicatorRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        (5 / 2).toFloat(), resources.displayMetrics)
    // 圆点起始坐标为小图标宽度一半
    private var missionIndicatorStartX = 0f
    private val missionIndicatorStartXOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        10f, resources.displayMetrics)
    private var missionIndicatorEndX = 0f

    private val reactRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        10f, resources.displayMetrics)

    // 每一段长度
    private var segment: Float = 0f

    private val topOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        60f, resources.displayMetrics)
    private val bottomOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        35f, resources.displayMetrics)

    constructor(context: Context): this(context, null)
    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, R.attr.actionBarSplitStyle)
    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        mCompletedProcessPaint.color = Color.parseColor("#FFFFCC1F")
        mCompletedProcessPaint.style = Paint.Style.STROKE
        mCompletedProcessPaint.strokeWidth = reactRadius
        mCompletedProcessPaint.strokeCap = Paint.Cap.ROUND
        mCompletedProcessPaint.strokeJoin = Paint.Join.ROUND

        mUnCompletedProcessPaint.color = Color.parseColor("#FFECECEC")
        mUnCompletedProcessPaint.style = Paint.Style.STROKE
        mUnCompletedProcessPaint.strokeWidth = reactRadius
        mUnCompletedProcessPaint.strokeCap = Paint.Cap.ROUND
        mUnCompletedProcessPaint.strokeJoin = Paint.Join.ROUND

        missionIndicatorPaint.color = Color.WHITE
        missionIndicatorPaint.style = Paint.Style.FILL

        mDatePaint.color = Color.parseColor("#FF999999")
        mDatePaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
            11f, resources.displayMetrics)
        mDatePaint.typeface = Typeface.DEFAULT_BOLD

        mRewardPaint.color = Color.parseColor("#FFFF8B53")
        mRewardPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
            13f, resources.displayMetrics)
        mRewardPaint.typeface = Typeface.DEFAULT_BOLD
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mHeight = MeasureSpec.getSize(heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(mWidth, mHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        missionIndicatorStartX = paddingLeft + missionIndicatorStartXOffset
        val s = (width - paddingRight - missionIndicatorStartXOffset) / 6.5f

        missionIndicatorEndX = width - paddingRight - (s / 2)
        segment = (missionIndicatorEndX - missionIndicatorStartX) / 6
        completedEndX = paddingLeft + (currentDay - 1) * segment + missionIndicatorStartXOffset + 2 * missionIndicatorRadius
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            // 未完成曲线
            canvas.drawLine(paddingLeft.toFloat(), paddingTop.toFloat() + reactHeight / 2 + topOffset, width - paddingRight.toFloat(), paddingTop.toFloat() + reactHeight / 2 + topOffset, mUnCompletedProcessPaint)
            canvas.drawLine(width - paddingRight.toFloat(), paddingTop.toFloat() + reactHeight / 2 + topOffset, width - paddingRight.toFloat(), height - paddingBottom.toFloat() - reactHeight / 2 - bottomOffset, mUnCompletedProcessPaint)
            canvas.drawLine(paddingLeft.toFloat(), height - paddingBottom.toFloat() - reactHeight / 2 - bottomOffset, width - paddingRight.toFloat(), height - paddingBottom.toFloat() - reactHeight / 2 - bottomOffset, mUnCompletedProcessPaint)

            // 已完成曲线
            canvas.drawLine(paddingLeft.toFloat(), paddingTop.toFloat() + reactHeight / 2 + topOffset, completedEndX, paddingTop.toFloat() + reactHeight / 2 + topOffset, mCompletedProcessPaint)

            // 绘制上矩形任务圆点
            for((i, day) in days.withIndex()) {
                val x = missionIndicatorStartX + (segment * i)
                val y = paddingTop + (reactHeight / 2) + topOffset
                canvas.drawCircle(x, y, missionIndicatorRadius, missionIndicatorPaint)
                // 绘制下方天数
                val fontY = y + reactHeight
                val fontMetric = mDatePaint.fontMetrics
                canvas.drawText("${day}天", x - reactHeight, fontY - fontMetric.top, mDatePaint)
                // 绘制上方奖励
                canvas.drawText("588", x - reactHeight, y - reactHeight, mRewardPaint)
            }
            // 绘制下矩形任务圆点
            for((i, day) in secondDays.withIndex()) {
                val x = missionIndicatorStartX + (segment * i)
                val y = height - paddingBottom - (reactHeight / 2) - bottomOffset
                canvas.drawCircle(x, y, missionIndicatorRadius, missionIndicatorPaint)
                val fontY = y + reactHeight
                val fontMetric = mDatePaint.fontMetrics
                canvas.drawText("${day}天", x - reactHeight, fontY - fontMetric.top, mDatePaint)
                // 绘制上方奖励
                canvas.drawText("566", x - reactHeight, y - reactHeight, mRewardPaint)

            }
        }
    }
}