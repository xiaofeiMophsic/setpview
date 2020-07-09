package com.mophsic.checkinview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import kotlin.math.abs

/**
 *  作者：xiaofei
 *  日期：2020/6/22
 */
class StepView: View {

    private var mWidth = 0
    private var mHeight = 0

    /** 是否展示双周签到数据 */
    private var isBiweekly = true
        set(value) {
            field = value
            missionIndicatorEndXOffset = if (value) {
                20f.dp
            }  else {
                5f.dp
            }
        }

    /** 上方已完成线条终点坐标 */
    private var mTopCompletedEndX: Float = 0f

    /** 上方已完成线条终点坐标 */
    private var mBottomCompletedStartX: Float = 0f

    /** 签到线条高度 */
    private val lineHeight = 10f.dp

    /** 指示点半径 */
    private val missionIndicatorRadius = 3.5f.dp

    /** 指示点起始坐标为小图标宽度一半 */
    private var missionIndicatorStartX = 0f
    /** 指示点终点坐标 */
    private var missionIndicatorEndX = 0f
    /** 指示点起始坐标相对left偏移值 */
    private val missionIndicatorStartXOffset = 5f.dp
    /** 指示点终点坐标相对right偏移值 */
    private var missionIndicatorEndXOffset = 5f.dp

    private val mStrokeWidth = 10f.dp

    /** 上方线条指示点间隔长度 */
    private var mTopSegment: Float = 0f
    /** 下方线条指示点间隔长度 */
    private var mBottomSegment: Float = 0f

    /** 金币图标 */
    private var mGoldIcon: Drawable

    /** 视频图标 */
    private var mVideoIcon: Drawable

    /** 宝箱图标 */
    private var mBoxIcon: Drawable

    /** 小图标宽高 */
    private val mIconHeight =  21.5f.dp

    private val mIconWidth =  21.5f.dp

    private var mLeft: Float = 0f
    private var mTop: Float = 0f
    private var mRight: Float = 0f
    private var mBottom: Float = 0f

    /** 上面线条纵坐标 */
    private var mTopLineY: Float = 0f
    /** 下方线条纵坐标 */
    private var mBottomLineY: Float = 0f

    /** 第一周显示信息 */
    var firstSevenDay = listOf<Bonus>()
    @UiThread set(value ){
            field = value
            computeTopSegment(value.size)
            invalidate()
        }
    /** 双周签到下一周显示信息 */
    var nextSevenDay = listOf<Bonus>()
    @UiThread set(value) {
            field = value
            computeBottomSegment(value.size)
            invalidate()
        }

    /** 当前签到天数 */
    var currentDay = 3
    @UiThread set(value) {
            field = value
            mTopCompletedEndX = computeTopCompletedEnd(value)
            if (nextSevenDay.map { it.date }.contains(value)) {
                mBottomCompletedStartX = computeBottomCompletedStart(value)
            }
            invalidate()
        }

    /** 日期画笔 */
    private val mDatePaint by lazy {
        Paint().apply {
            color = "#FF999999".toColorInt()
            textSize = 10f.sp
            typeface = Typeface.DEFAULT_BOLD
        }
    }

    /** 金币奖励 */
    private val mRewardPaint by lazy {
        Paint().apply {
            color = "#FFFF8B53".toColorInt()
            textSize = 12f.sp
            typeface = Typeface.createFromAsset(context.assets, "din_bold.otf")
        }
    }

    /** 已签到进度 */
    private val mCompletedProcessPaint by lazy {
        Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = mStrokeWidth
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
    }

    /** 未签到进度 */
    private val mUnCompletedProcessPaint by lazy {
        Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = mStrokeWidth
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
    }

    /** 指示点 */
    private val missionIndicatorPaint by lazy {
        Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
    }

    constructor(context: Context): this(context, null)
    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, R.attr.actionBarSplitStyle)
    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {

        context.obtainStyledAttributes(attributes, R.styleable.StepView).apply {
            isBiweekly = getBoolean(R.styleable.StepView_isBiweekly, true)
            mUnCompletedProcessPaint.color = getColor(R.styleable.StepView_defaultLineColor, "#FFECECEC".toColorInt())
            mCompletedProcessPaint.color = getColor(R.styleable.StepView_activeLineColor, "#FFFFCC1F".toColorInt())
            mGoldIcon = getDrawable(R.styleable.StepView_defaultBonusIcon)?:ContextCompat.getDrawable(context, R.mipmap.dialog_checkin_gold)!!
            mVideoIcon = getDrawable(R.styleable.StepView_currentBonusIcon)?:ContextCompat.getDrawable(context, R.mipmap.dialog_checkin_video)!!
            mBoxIcon = getDrawable(R.styleable.StepView_addedBonusIcon)?:ContextCompat.getDrawable(context, R.mipmap.dialog_checkin_box)!!
            recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mHeight = MeasureSpec.getSize(heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(mWidth, mHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        mLeft = paddingLeft.toFloat()
        mTop = paddingTop.toFloat()
        mRight = width -  paddingRight.toFloat()
        mBottom = height - paddingBottom.toFloat()

        missionIndicatorStartX = mLeft + missionIndicatorStartXOffset
        missionIndicatorEndX = mRight - missionIndicatorEndXOffset // 最后一个指示点距离view right 的距离

        computeTopSegment(firstSevenDay.size)
        computeBottomSegment(nextSevenDay.size)

        // 计算上方线条已完成进度，默认为第一天进度
        mTopCompletedEndX = computeTopCompletedEnd(currentDay)

        // 上方线条 y 为 paddingTop + 小图标高度 + 奖励文字高度 + 间隔高度 + 1/2*线条高度
        mTopLineY = mTop + mIconHeight + getTextHeight(mRewardPaint) + 1/2*lineHeight + DEFAULT_TEXT_MARGIN

        // 下方线条 y 为 height - paddingBottom - 天数文本高度 - 间隔高度 - 1/2*线条高度
        mBottomLineY = mBottom - getTextHeight(mDatePaint) - 1/2*lineHeight - DEFAULT_TEXT_MARGIN
    }

    private fun computeTopSegment(value: Int){
        if (value <= 0) {
            return
        }

        mTopSegment = (missionIndicatorEndX - missionIndicatorStartX) / (value - 1)
    }

    private fun computeBottomSegment(value: Int){
        if (value <= 0) {
            return
        }
        // 底部栏显示2天及以下数据，则平分线段长度
        mBottomSegment = if (value <= BOTTOM_LINE_INDICATORS_COUNT_MAX) {
            (mRight - mLeft) / (value + 1)
        } else {
            (missionIndicatorEndX - missionIndicatorStartX) / (value - 1)
        }
    }

    private fun computeTopCompletedEnd(value: Int): Float {
        val days = firstSevenDay.map { it.date }
        // 单行签到信息如果签到最后一天需要填满整条线
        if ((!isBiweekly && value == days.last()) || nextSevenDay.map { it.date }.contains(value)) {
            return mRight
        }
        return if (days.contains(value)) {
            val i= days.indexOf(value)
            missionIndicatorStartX + i * mTopSegment + missionIndicatorRadius
        } else{
            missionIndicatorStartX + missionIndicatorRadius
        }
    }

    private fun computeBottomCompletedStart(value: Int): Float {
        val days = nextSevenDay.map { it.date }
        if (!days.contains(value)) {
            return 0f
        }
        if (value == days.last()) {
            return mLeft
        }
        var i = (nextSevenDay.size - 1) - days.indexOf(value)
        if (nextSevenDay.size <= BOTTOM_LINE_INDICATORS_COUNT_MAX) {
            i += 1
        }
        return missionIndicatorStartX + i * mBottomSegment - missionIndicatorRadius
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            // 未完成曲线
            drawUnCompleteProgressLine(canvas)
            // 已完成曲线
            drawCompletedProgressLine(canvas)

            // 绘制上方线条任务圆点
            for((i, bonus) in firstSevenDay.withIndex()) {
                // 指示点 x 坐标，文本和金币图标均以指示点 y 坐标为基准绘制
                val x = missionIndicatorStartX + (mTopSegment * i)
                // 指示点 y 坐标
                val y = mTopLineY
                // 绘制指示点
                drawIndicators(canvas, x, y)
                // 绘制下方天数和上方奖励
                drawText(canvas, bonus, x, y)
                // 绘制上方icon
                drawIcon(canvas, bonus, x, y)
            }
            // 双周展示绘制下方线条任务圆点
            if (isBiweekly) {
                for ((i, bonus) in nextSevenDay.reversed().withIndex()) {
                    // 如果最后剩下两天，则i的值加1
                    val index = if (nextSevenDay.size <= BOTTOM_LINE_INDICATORS_COUNT_MAX) i + 1 else i
                    val x = missionIndicatorStartX + (mBottomSegment * index)
                    val y = mBottomLineY
                    drawIndicators(canvas, x, y)
                    drawText(canvas, bonus, x, y)
                    drawIcon(canvas, bonus, x, y)
                }
            }
        }
    }

    private fun drawIcon(canvas: Canvas, bonus: Bonus, x: Float, y: Float){
        val iconLeft = (x - mIconWidth / 2).toInt()
        val iconTop = (y - DEFAULT_TEXT_MARGIN - getTextHeight(mRewardPaint) - mIconHeight).toInt()
        val iconRight = (x + mIconWidth / 2).toInt()
        val iconBottom = (y - DEFAULT_TEXT_MARGIN - getTextHeight(mRewardPaint)).toInt()
        val rect = Rect(iconLeft, iconTop, iconRight, iconBottom)
        if (bonus.date != currentDay) {
            if (bonus.date != 0 && bonus.date % 7 == 0) {
                mBoxIcon.bounds = rect
                mBoxIcon.draw(canvas)
            } else {
                mGoldIcon.bounds = rect
                mGoldIcon.draw(canvas)
            }
        } else {
            mVideoIcon.bounds = rect
            mVideoIcon.draw(canvas)
        }
    }

    private fun drawIndicators(canvas: Canvas, x: Float, y: Float){
        canvas.drawCircle(x, y, missionIndicatorRadius, missionIndicatorPaint)
    }

    private fun drawText(canvas: Canvas, bonus: Bonus, x: Float, y: Float){
        // 绘制下方天数
        val fontMetric = mDatePaint.fontMetrics
        // 以文本左上角顶点为基准绘制文本（默认左下角，底下同理）
        val fontY = y + DEFAULT_TEXT_MARGIN - fontMetric.top
        canvas.drawText("${bonus.date}天", x - mDatePaint.measureText("${bonus.date}天") / 2, fontY, mDatePaint)
        // 绘制上方奖励
        canvas.drawText(bonus.gold.toString(), x - mRewardPaint.measureText(bonus.gold.toString()) / 2, y - DEFAULT_TEXT_MARGIN, mRewardPaint)
    }

    private fun getTextHeight(paint: Paint): Float = abs(paint.fontMetrics.top - paint.fontMetrics.bottom)

    /** 未完成线条 */
    private fun drawUnCompleteProgressLine(canvas: Canvas){
        // 上
        canvas.drawLine(mLeft, mTopLineY, mRight, mTopLineY, mUnCompletedProcessPaint)
        if (isBiweekly) {
            // 右
            canvas.drawLine(mRight, mTopLineY, mRight, mBottomLineY, mUnCompletedProcessPaint)
            // 下
            canvas.drawLine(mLeft, mBottomLineY, mRight, mBottomLineY, mUnCompletedProcessPaint)
        }
    }

    /** 已完成进度条 */
    private fun drawCompletedProgressLine(canvas: Canvas) {

        canvas.drawLine(mLeft, mTopLineY, mTopCompletedEndX, mTopLineY, mCompletedProcessPaint)
        if (isBiweekly && nextSevenDay.map { it.date }.contains(currentDay)) {
            // 绘制右边竖线
            canvas.drawLine(mRight, mTopLineY, mRight, mBottomLineY, mCompletedProcessPaint)
            // 绘制底部线
            canvas.drawLine(mBottomCompletedStartX, mBottomLineY, mRight, mBottomLineY, mCompletedProcessPaint)
        }
    }

    companion object {
        val DEFAULT_TEXT_MARGIN = 10f.dp

        const val BOTTOM_LINE_INDICATORS_COUNT_MAX = 2
    }
}