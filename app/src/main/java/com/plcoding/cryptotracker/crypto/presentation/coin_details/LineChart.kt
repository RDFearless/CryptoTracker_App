package com.plcoding.cryptotracker.crypto.presentation.coin_details

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import com.plcoding.cryptotracker.crypto.domain.CoinPrice
import com.plcoding.cryptotracker.ui.theme.CryptoTrackerTheme
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun LineChart(
    dataPoints: List<DataPoint>,
    style: ChartStyle,
    visibleDataPointIndices: IntRange,
    unit: String,
    modifier: Modifier = Modifier,
    selectedDataPoint: DataPoint? = null,
    onSelectedDataPoint: (DataPoint) -> Unit = {},
    onXLabelWidthChange: (Float) -> Unit = {},
    showHelperLines: Boolean = true
) {
    val textStyle = LocalTextStyle.current.copy(
        fontSize = style.labelFontSize
    )
    val visibleDataPoints = remember(dataPoints, visibleDataPointIndices) {
        dataPoints.slice(visibleDataPointIndices)
    }
    val minYLabelValue = remember(visibleDataPoints) {
        visibleDataPoints.minOfOrNull { it.y } ?: 0f
    }
    val maxYLabelValue = remember(visibleDataPoints) {
        visibleDataPoints.maxOfOrNull { it.y } ?: 0f
    }
    val measurer = rememberTextMeasurer()
    var xLabelWidth by remember {
        mutableFloatStateOf(0f)
    }
    LaunchedEffect(key1 = xLabelWidth) {
        onXLabelWidthChange(xLabelWidth)
    }
    val selectedDataPointIndex = remember(selectedDataPoint) {
        dataPoints.indexOf(selectedDataPoint)
    }
    var drawPoint by remember {
        mutableStateOf(listOf<DataPoint>())
    }
    var isShowingDataPoints by remember {
        mutableStateOf(selectedDataPoint != null)
    }
    var viewPortShift by remember {
        mutableFloatStateOf(0f)
    }
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(drawPoint, xLabelWidth) {
                detectHorizontalDragGestures { change, _ ->
                    val newSelectedDataPointIndex = getSelectedDataPoint(
                        touchOffsetX = change.position.x,
                        triggerWidth = xLabelWidth,
                        drawPoint = drawPoint
                    )
                    isShowingDataPoints =
                        (newSelectedDataPointIndex + visibleDataPointIndices.first()) in
                                visibleDataPointIndices

                    if(isShowingDataPoints) {
                        onSelectedDataPoint(dataPoints[newSelectedDataPointIndex])
                    }
                }
            }
    ) {
        val minYLabelSpacingPx = style.minYLabelSpacing.toPx()
        val xAxisLabelSpacingPx = style.xAxisLabelSpacing.toPx()

        val verticalPaddingPx = style.verticalPadding.toPx()
        val horizontalPaddingPx = style.horizontalPadding.toPx()

        val xLabelTextLayoutResult = visibleDataPoints.map {
            measurer.measure(
                text = it.xLabel,
                style = textStyle.copy(textAlign = TextAlign.Center)
            )
        }

        val maxXLabelWidth = xLabelTextLayoutResult.maxOfOrNull { it.size.width } ?: 0
        val maxXLabelHeight = xLabelTextLayoutResult.maxOfOrNull { it.size.height } ?: 0
        val maxXLabelLineCount = xLabelTextLayoutResult.maxOfOrNull { it.lineCount } ?: 0
        val xLabelLineHeight = maxXLabelHeight / maxXLabelLineCount

        val viewPortHeightPx = size.height -
                (maxXLabelHeight + 2 * verticalPaddingPx
                        + xLabelLineHeight +xAxisLabelSpacingPx)

        // View Port(Green Box) Coordinates
        val viewPortTopY = verticalPaddingPx + xLabelLineHeight + 10f
        val viewPortBottomY = viewPortTopY + viewPortHeightPx

        val viewPortRightX = size.width
        val viewPortLeftX = 2f * horizontalPaddingPx + viewPortShift // viewPortShift is calculated while calculating maxYLabelWidth

        // X LABELS
        xLabelWidth = maxXLabelWidth + xAxisLabelSpacingPx

        xLabelTextLayoutResult.fastForEachIndexed { index, result ->
            val x = viewPortLeftX + xAxisLabelSpacingPx / 2f +
                    xLabelWidth * index
            drawText(
                textLayoutResult = result,
                topLeft = Offset(
                    x = x,
                    y = viewPortBottomY + xAxisLabelSpacingPx
                ),
                color = if(index == selectedDataPointIndex) {
                    style.selectedColor
                } else {
                    style.unselectedColor
                }
            )

            // X HELPER LINES
            drawLine(
                color = if(index == selectedDataPointIndex) {
                    style.selectedColor
                }else {
                    style.unselectedColor
                },
                start = Offset(
                    x = x + result.size.width.toFloat() / 2f,
                    y = viewPortBottomY
                ),
                end = Offset(
                    x = x + result.size.width.toFloat() / 2f,
                    y = viewPortTopY
                ),
                strokeWidth = if(index == selectedDataPointIndex) {
                    style.helperLineThicknessPx * 1.8f
                } else style.helperLineThicknessPx
            )

            if(index == selectedDataPointIndex) {
                val valueLabel = ValueLabel(
                    value = visibleDataPoints[index].y,
                    unit = unit
                )

                val valueResult = measurer.measure(
                    text = valueLabel.formatted(),
                    style = textStyle.copy(
                        color = style.selectedColor
                    ),
                    maxLines = 1
                )

                val textPositionX = if(index == visibleDataPointIndices.last) {
                    x - valueResult.size.width
                } else {
                    x - valueResult.size.width / 2f
                } + result.size.width / 2f // To center the label

                val isTextInVisibleRange =
                    (size.width - textPositionX).roundToInt() in 0..size.width.roundToInt()

                if(isTextInVisibleRange) {
                    drawText(
                        textLayoutResult = valueResult,
                        topLeft = Offset(
                            x = textPositionX,
                            y = viewPortTopY - valueResult.size.height - 10f
                        )
                    )
                }
            }
        }

        // Y LABELS
        val labelViewPortHeightPx = viewPortHeightPx + xLabelLineHeight

        val yLabelCountExcludingLastLabel = ((labelViewPortHeightPx / (xLabelLineHeight + minYLabelSpacingPx))).toInt()

        val labelIncrement = (maxYLabelValue - minYLabelValue) / yLabelCountExcludingLastLabel

        val yLabels = (0..yLabelCountExcludingLastLabel).map {
            ValueLabel(
                value = maxYLabelValue - (labelIncrement * it),
                unit = unit
            )
        }

        val yLabelTextLayoutResult = yLabels.map {
            measurer.measure(
                text = it.formatted(),
                style = textStyle
            )
        }
        val heightReqForLabels = xLabelLineHeight *
                (yLabelCountExcludingLastLabel + 1)

        val remainingHeightForLabels = labelViewPortHeightPx - heightReqForLabels

        val spaceBetweenLabels = remainingHeightForLabels / yLabelCountExcludingLastLabel

        val maxYLabelWidth = yLabelTextLayoutResult.maxOfOrNull { it.size.width } ?: 0
        viewPortShift = maxYLabelWidth.toFloat()
        yLabelTextLayoutResult.forEachIndexed { index, result ->
            val x = horizontalPaddingPx + maxYLabelWidth - result.size.width.toFloat()
            val y = viewPortTopY +
                    index * (xLabelLineHeight + spaceBetweenLabels) -
                    xLabelLineHeight / 2f
            drawText(
                textLayoutResult = result,
                topLeft = Offset(
                    x = x,
                    y = y
                ),
                color = style.unselectedColor
            )
            
            // Y HELPER LINES
            if(showHelperLines) {
                drawLine(
                    color = style.unselectedColor,
                    start = Offset(
                        x = viewPortLeftX,
                        y = y + result.size.height.toFloat() / 2f
                    ),
                    end = Offset(
                        x = viewPortRightX,
                        y = y + result.size.height.toFloat() / 2f
                    ),
                    strokeWidth = style.helperLineThicknessPx
                )
            }
        }

        // Mapping our (x,y) (i.e., dataPoints) to Canvas original (x,y) coordinates
        drawPoint = visibleDataPointIndices.map {
            val x = viewPortLeftX + (it - visibleDataPointIndices.first) *
                    xLabelWidth + xLabelWidth / 2f

            // [minYValue, maxYValue] -> (0..1) for a particular Y
            val ratio = (dataPoints[it].y - minYLabelValue) / (maxYLabelValue - minYLabelValue)

            val y = viewPortBottomY - (ratio * viewPortHeightPx)

            DataPoint(
                x = x,
                y = y,
                xLabel = dataPoints[it].xLabel
            )

        }

        // Curved Lines -> CUBIC BEZIER
        val conPoint1 = mutableListOf<DataPoint>() // Helper points for connections
        val conPoint2 = mutableListOf<DataPoint>()

        // Mapping each helper point in Canvas
        for(i in 1 until visibleDataPoints.size) {
            val p0 = drawPoint[i - 1]
            val p1 = drawPoint[i]

            val x = (p1.x + p0.x) / 2f
            val y1 = p0.y
            val y2 = p1.y

            conPoint1.add(DataPoint(x, y1, ""))
            conPoint2.add(DataPoint(x, y2, ""))
        }

        // Build cubic bezier
        val linePath = Path().apply {
            if(drawPoint.isNotEmpty()) {
                moveTo(drawPoint.first().x, drawPoint.first().y)

                for(i in 1 until drawPoint.size) {
                    cubicTo(
                        x1 = conPoint1[i - 1].x,
                        y1 = conPoint1[i - 1].y,
                        x2 = conPoint2[i - 1].x,
                        y2 = conPoint2[i - 1].y,
                        x3 = drawPoint[i].x,
                        y3 = drawPoint[i].y,
                    )
                }
            }
        }

        // Drawing curved path
        drawPath(
            path = linePath,
            color = style.chartLineColor,
            style = Stroke(
                width = 5f,
                cap = StrokeCap.Round
            )
        )

        // Showing Black Dots
        drawPoint.forEachIndexed { index, point ->
            val circleOffset = Offset(
                x = point.x,
                y = point.y,
            )

            if(isShowingDataPoints) {
                drawCircle(
                    color = style.unselectedColor,
                    radius = 10f,
                    center = circleOffset
                )

                if(index == selectedDataPointIndex) {
                    drawCircle(
                        color = Color.White,
                        radius = 15f,
                        center = circleOffset,
                    )
                    drawCircle(
                        color = style.selectedColor,
                        radius = 15f,
                        center = circleOffset,
                        style = Stroke(
                            width = 3f
                        )
                    )
                }
            }
        }
    }
}

// This will give index where we are selecting on Canvas
private fun getSelectedDataPoint (
    touchOffsetX: Float,
    triggerWidth: Float,
    drawPoint: List<DataPoint>
): Int {
    val triggerWidthLeft = touchOffsetX - triggerWidth / 2f
    val triggerWidthRight = touchOffsetX + triggerWidth / 2f

    return drawPoint.indexOfFirst {
        it.x in triggerWidthLeft..triggerWidthRight
    }
}

@Preview(widthDp = 1000)
@Composable
private fun LineChartPreview() {
    CryptoTrackerTheme {
        val randomCoinHistory = remember {
            (1..20).map {
                CoinPrice(
                    priceUsd = Random.nextFloat() * 1000.0,
                    dateTime = ZonedDateTime.now().plusHours(it.toLong())
                )
            }
        }

        val style = ChartStyle(
            chartLineColor = Color.Black,
            unselectedColor = Color(0xFF7C7C7C),
            selectedColor = Color.Black,
            helperLineThicknessPx = 1.5f,
            axisLineThicknessPx = 5f,
            labelFontSize = 14.sp,
            minYLabelSpacing = 25.dp,
            verticalPadding = 8.dp,
            horizontalPadding = 8.dp,
            xAxisLabelSpacing = 8.dp
        )

        val dataPoints = remember {
            randomCoinHistory.map {
                DataPoint(
                    x = it.dateTime.hour.toFloat(),
                    y = it.priceUsd.toFloat(),
                    xLabel = DateTimeFormatter
                        .ofPattern("ha\nM/d")
                        .format(it.dateTime)
                )
            }
        }

        LineChart(
            dataPoints = dataPoints,
            style = style,
            visibleDataPointIndices = 0..19,
            unit = "$",
            modifier = Modifier
                .width(700.dp)
                .height(300.dp)
                .background(Color.White),
            selectedDataPoint = dataPoints[1]
        )
    }
}