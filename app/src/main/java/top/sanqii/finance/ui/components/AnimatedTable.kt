package top.sanqii.finance.ui.components

import android.graphics.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.pow

/*
@Composable
fun AnimatedTable(record: List<Float>, timeLabel: List<String>) {
    // x 属于 [0, totalCount)
    fun Float.xCoordination(x: Int, totalCount: Int): Float {
        return this * ((x + 1).toFloat() / (totalCount + 1))
    }

    // y 属于 [0, maxValue]
    // 0.83作为数据显示区域占整个区域的的高度比例,减去的0.04作为x轴上移的占高度比
    fun Float.yCoordination(y: Float, maxValue: Float): Float {
        return this - y * (this / maxValue) * .83f - this * .05f
    }

    var startAnimate by remember { mutableStateOf(false) }
    val animateColor by animateColorAsState(
        targetValue = if (startAnimate) Color.Gray else Color.Cyan, animationSpec =
        tween(
            delayMillis = 500,
            durationMillis = 3600,
        )
    )
    val animateOffset by animateFloatAsState(
        targetValue = if (startAnimate) 1f else 0f, animationSpec =
        tween(
            delayMillis = 500,
            durationMillis = 3600,
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val maxValue = record.maxOf { it }.let {
            val power = floor(log(it, 10f)).toInt()
            val startNumber = it.toLong() / 10f.pow(power).toLong()
            // Log.d("MAXVALUE", "$it+++$power+++$startNumber")
            (startNumber + 1) * 10f.pow(power)
        }
        val points = record.mapIndexed { index, it -> Pair(index, it) }

        // 画笔对象
        val xLabelPaint = Paint().apply {
            isAntiAlias = true
            textSize = 12.dp.toPx()
        }
        val yLabelPaint = Paint().apply {
            isAntiAlias = true
            textSize = 8.dp.toPx()
        }
        val axisPaint = Paint().apply {
            isAntiAlias = true
            strokeWidth = 3f
        }
        val coordinatePaint = Paint().apply {
            isAntiAlias = true
            pathEffect = DashPathEffect(floatArrayOf(9f, 9f), 1f)
        }

        // 画板的宽高
        val height = size.height - 16.dp.toPx()
        val width = size.width

        // 绘制折线图的坐标轴的坐标基准
        val startX = width.xCoordination(0, record.size) - 2.dp.toPx()
        val startY = height.yCoordination(0f, maxValue) + 2.dp.toPx()
        val endX = width.xCoordination(record.size - 1, record.size) + 2.dp.toPx()
        val endY = height.yCoordination(maxValue, maxValue) - 2.dp.toPx()


        // 绘制横坐标
        drawIntoCanvas {
            val xLabels =
                if (timeLabel.size > 7) timeLabel.filterIndexed { index, _ -> index % 2 == 1 } else timeLabel
            val xLabelPositions = xLabels.mapIndexed { index, _ ->
                PointF(
                    width.xCoordination(index, xLabels.size),
                    height.yCoordination(0f, maxValue) + height / 10
                )
            }
            // 纵坐标五等分,绘制横坐标和横坐标标识
            xLabelPositions.forEachIndexed { index, pointF ->
                it.nativeCanvas.apply {
                    drawText(
                        xLabels[index],
                        pointF.x - xLabelPaint.measureText(xLabels[index]) / 2,
                        pointF.y,
                        xLabelPaint
                    )
                    drawLine(startX, startY, endX, startY, axisPaint)
                }
            }
        }
        // 绘制纵坐标
        drawIntoCanvas {
            it.nativeCanvas.apply {
                // 绘制坐标轴
                drawLine(startX, startY, startX, endY, axisPaint)
                // 纵坐标五等分,绘制虚线和纵坐标标识
                for (i in 0 until 5) {
                    val presentY = startY + i * (endY - startY) / 5
                    drawLine(startX, presentY, endX, presentY, coordinatePaint)
                    val text = i * maxValue / 5
                    drawText(
                        text.toLong().toString(),
                        startX - yLabelPaint.measureText(text.toLong().toString()) * 1.37f,
                        presentY,
                        yLabelPaint
                    )
                }
            }
        }

        // 绘制数值曲线
        val path = Path()
        val paint = Paint().apply {
            color = animateColor.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 8f
            strokeCap = Paint.Cap.ROUND
        }
        /*
        // 若数据中存在连续零值,则重复插入,使其成为一条直线(但节点处未收敛好)
        var repCount = 0
        val zeroCount = points.count { it.second == 0f }
        val bezierSpline = BezierSpline(points.size + zeroCount * 2)
        for (knot in 0 until bezierSpline.knots() - zeroCount * 2) {
            val x = width.xCoordination(points[knot].first, record.size)
            val y = height.yCoordination(points[knot].second, maxValue)
            bezierSpline.set(knot + repCount, x, y)
            if (points[knot].second == 0f) {
                bezierSpline.set(knot + repCount + 1, x, y)
                bezierSpline.set(knot + repCount + 2, x, y)
                repCount += 2
            }
        }
         */
        // 使用B样条插值得到曲线路径
        val bezierSpline = BezierSpline(points.size)
        for (knot in 0 until bezierSpline.knots()) {
            val x = width.xCoordination(points[knot].first, record.size)
            val y = height.yCoordination(points[knot].second, maxValue)
            bezierSpline.set(knot, x, y)
        }
        bezierSpline.applyToPath(path)
        val pathMeasure = PathMeasure(path, false)
        drawIntoCanvas {
            // 开始动画
            startAnimate = true
            val temPath = Path()
            pathMeasure.getSegment(0f, pathMeasure.length * animateOffset, temPath, true)
            it.nativeCanvas.drawPath(temPath, paint)
        }
    }
}
*/

@Composable
fun AnimatedTable(record: List<Float>, timeLabel: List<String>) {
    // x 属于 [0, totalCount)
    fun Float.xCoordination(x: Int, totalCount: Int): Float {
        return this * ((x + 1).toFloat() / (totalCount + 1))
    }

    // y 属于 [0, maxValue]
    // 0.83作为数据显示区域占整个区域的的高度比例,减去的0.04作为x轴上移的占高度比
    fun Float.yCoordination(y: Float, maxValue: Float): Float {
        return this - y * (this / maxValue) * .83f - this * .05f
    }

    var startAnimate by remember { mutableStateOf(false) }
    val animateColor by animateColorAsState(
        targetValue = if (startAnimate) Color.Gray else Color.Cyan, animationSpec =
        tween(
            delayMillis = 500,
            durationMillis = 3600,
        )
    )
    val animateOffset by animateFloatAsState(
        targetValue = if (startAnimate) 1f else 0f, animationSpec =
        tween(
            delayMillis = 500,
            durationMillis = 3600,
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val maxValue = record.maxOf { it }.let {
            val power = floor(log(it, 10f)).toInt()
            val startNumber = it / 10f.pow(power)
            (startNumber.toInt() + 1) * 10f.pow(power)
        }
        val points = record.mapIndexed { index, it -> Pair(index, it) }

        // 画笔对象
        val xLabelPaint = Paint().apply {
            isAntiAlias = true
            textSize = 12.dp.toPx()
        }
        val yLabelPaint = Paint().apply {
            isAntiAlias = true
            textSize = 8.dp.toPx()
        }
        val axisPaint = Paint().apply {
            isAntiAlias = true
            strokeWidth = 3f
        }
        val coordinatePaint = Paint().apply {
            isAntiAlias = true
            pathEffect = DashPathEffect(floatArrayOf(9f, 9f), 1f)
        }

        // 画板的宽高
        val height = size.height - 16.dp.toPx()
        val width = size.width

        // 绘制折线图的坐标轴的坐标基准
        val startX = width.xCoordination(0, record.size) - 2.dp.toPx()
        val startY = height.yCoordination(0f, maxValue) + 2.dp.toPx()
        val endX = width.xCoordination(record.size - 1, record.size) + 2.dp.toPx()
        val endY = height.yCoordination(maxValue, maxValue) - 2.dp.toPx()


        // 绘制横坐标
        drawIntoCanvas {
            val xLabels =
                if (timeLabel.size > 7) timeLabel.filterIndexed { index, _ -> index % 2 == 1 } else timeLabel
            val xLabelPositions = xLabels.mapIndexed { index, _ ->
                PointF(
                    width.xCoordination(index, xLabels.size),
                    height.yCoordination(0f, maxValue) + height / 10
                )
            }
            // 纵坐标五等分,绘制横坐标和横坐标标识
            xLabelPositions.forEachIndexed { index, pointF ->
                it.nativeCanvas.apply {
                    drawText(
                        xLabels[index],
                        pointF.x - xLabelPaint.measureText(xLabels[index]) / 2,
                        pointF.y,
                        xLabelPaint
                    )
                    drawLine(startX, startY, endX, startY, axisPaint)
                }
            }
        }
        // 绘制纵坐标
        drawIntoCanvas {
            it.nativeCanvas.apply {
                // 绘制坐标轴
                drawLine(startX, startY, startX, endY, axisPaint)
                // 纵坐标五等分,绘制虚线和纵坐标标识
                for (i in 0 until 5) {
                    val presentY = startY + i * (endY - startY) / 5
                    drawLine(startX, presentY, endX, presentY, coordinatePaint)
                    val text = i * maxValue / 5
                    drawText(
                        text.toLong().toString(),
                        startX - yLabelPaint.measureText(text.toLong().toString()) * 1.37f,
                        presentY,
                        yLabelPaint
                    )
                }
            }
        }

        // 绘制数值曲线
        val path = Path()
        val paint = Paint().apply {
            color = animateColor.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 8f
            strokeCap = Paint.Cap.ROUND
            // 使用pathEffort更改曲线连续度
            pathEffect = CornerPathEffect(20f)
        }
        path.moveTo(
            width.xCoordination(points[0].first, points.size),
            height.yCoordination(points[0].second, maxValue)
        )
        for (knot in 1 until points.size) {
            val x = width.xCoordination(points[knot].first, points.size)
            val y = height.yCoordination(points[knot].second, maxValue)
            path.lineTo(x, y)
        }
        val pathMeasure = PathMeasure(path, false)
        drawIntoCanvas {
            // 开始动画
            startAnimate = true
            val temPath = Path()
            pathMeasure.getSegment(0f, pathMeasure.length * animateOffset, temPath, true)
            it.nativeCanvas.drawPath(temPath, paint)
        }
    }
}
