package com.example.smmoney.views.charts.compose

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.smmoney.misc.PocketMoneyThemes
import com.example.smmoney.views.charts.items.ChartItem
import com.example.smmoney.views.charts.items.ReportChartItem
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun ChartWithTabs(
    modifier: Modifier = Modifier,
    items: List<ChartItem>,
    isPieChart: Boolean,
    onItemClick: (ChartItem) -> Unit = {}
) {
    var selectedTabIndex by remember { mutableIntStateOf(1) } // 0 for Income, 1 for Expenses
    val tabTitles = listOf("Income", "Expenses")

    val filteredItems = remember(items, selectedTabIndex) {
        val filtered = items.filter { item ->
            if (selectedTabIndex == 0) item.value > 0 else item.value < 0
        }
        // Calculate new total for percentages
        val total = filtered.sumOf { abs(it.value) }
        
        // Return a mapped list with updated percentages and positive values
        filtered.map { originalItem ->
            val newItem = ReportChartItem(
                abs(originalItem.value),
                (originalItem as? ReportChartItem)?.reportItem?.expense ?: "",
                originalItem.color
            ).apply {
                this.percent = if (total > 0) abs(originalItem.value) / total else 0.0
                this.reportItem = (originalItem as? ReportChartItem)?.reportItem
            }
            newItem
        }
    }

    androidx.compose.foundation.layout.Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = title) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (isPieChart) {
                ModernPieChart(
                    items = filteredItems,
                    onItemClick = onItemClick
                )
            } else {
                ModernBarChart(
                    items = filteredItems,
                    onItemClick = onItemClick
                )
            }
        }
    }
}

@Composable
fun ModernPieChart(
    modifier: Modifier = Modifier,
    items: List<ChartItem>,
    onItemClick: (ChartItem) -> Unit = {}
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val animationProgress = remember { Animatable(0f) }

    var lastItems by remember { mutableStateOf(items) }

    LaunchedEffect(items, selectedIndex) {
        if (selectedIndex == -1 || lastItems != items) {
            lastItems = items
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }
    }



    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(items) {
                    detectTapGestures { tapOffset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = tapOffset.x - center.x
                        val dy = tapOffset.y - center.y
                        val radius = kotlin.math.min(size.width, size.height) / 2f * 0.65f - 25f
                        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                        if (distance <= radius + 15f) {
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f
                            angle = (angle + 90f) % 360f

                            var currentAngle = 0f
                            val totalValue = items.sumOf { it.value }
                            for (i in items.indices) {
                                val sweepAngle = (items[i].value / totalValue * 360).toFloat()
                                if (angle >= currentAngle && angle <= (currentAngle + sweepAngle)) {
                                    selectedIndex = if (selectedIndex == i) -1 else i
                                    if (selectedIndex != -1) {
                                        onItemClick(items[i])
                                    }
                                    break
                                }
                                currentAngle += sweepAngle
                            }
                        }
                    }
                }
        ) {
            val totalValue = items.sumOf { it.value }
            val radius = kotlin.math.min(size.width, size.height) / 2f * 0.60f - 25f
            val center = Offset(size.width / 2f, size.height / 2f)


            // Data class to hold properties for labels we intend to draw
            data class LabelData(val index: Int, val item: ChartItem, val sweepAngle: Float, val idealY: Float)
            
            var tempStartAngle = -90f
            val rightSideLabels = mutableListOf<LabelData>()
            val leftSideLabels = mutableListOf<LabelData>()
            
            items.forEachIndexed { index, item ->
                val sweepAngle = (item.value / totalValue * 360).toFloat()
                if (sweepAngle > 9f) {
                    val midAngle = tempStartAngle + sweepAngle / 2
                    val angleInRadians = (midAngle * PI / 180).toFloat()
                    val idealY = center.y + radius * sin(angleInRadians)
                    val labelData = LabelData(index, item, sweepAngle, idealY)
                    
                    if (cos(angleInRadians) > 0) {
                        rightSideLabels.add(labelData)
                    } else {
                        // Left side draws bottom-to-top because we are sweeping clockwise from -90
                        leftSideLabels.add(labelData)
                    }
                }
                tempStartAngle += sweepAngle
            }

            val minLabelSpacing = 35f // Minimum vertical space between labels
            
            // Proportional layout logic
            fun resolveOverlaps(labels: List<LabelData>, isRightSide: Boolean): List<Float> {
                if (labels.isEmpty()) return emptyList()
                if (labels.size == 1) return listOf(labels.first().idealY)

                val padding = 30f
                val canvasBottom = size.height - padding
                val adjustedYs = MutableList(labels.size) { 0f }

                // 1. Pin the ends
                // For the right side (top-to-bottom), the first item is the highest point.
                // For the left side (bottom-to-top), the first item is the lowest point.
                val highestLabel = if (isRightSide) labels.first() else labels.last()
                val lowestLabel = if (isRightSide) labels.last() else labels.first()

                var topPin = maxOf(padding, highestLabel.idealY - (labels.size * 5f)) // Give it a slight stretch up
                var bottomPin = minOf(canvasBottom, lowestLabel.idealY + (labels.size * 5f)) // Give it a slight stretch down
                
                // Ensure we have at least minimum spacing available overall
                val requiredTotalHeight = (labels.size - 1) * minLabelSpacing
                if (bottomPin - topPin < requiredTotalHeight) {
                    val expand = (requiredTotalHeight - (bottomPin - topPin)) / 2f
                    topPin = maxOf(padding, topPin - expand)
                    bottomPin = minOf(canvasBottom, bottomPin + expand)
                }

                // 2. Map intermediate angles proportionally
                val totalSweepBetweenPins = labels.sumOf { it.sweepAngle.toDouble() }.toFloat()
                val totalAvailableHeight = bottomPin - topPin

                var currentY = topPin
                // Right side processes top-to-bottom
                if (isRightSide) {
                    adjustedYs[0] = topPin
                    for (i in 1 until labels.size) {
                        val proportion = labels[i-1].sweepAngle / totalSweepBetweenPins
                        val gap = totalAvailableHeight * proportion
                        currentY += gap
                        adjustedYs[i] = currentY
                    }
                    // Force last one to exactly hit the pin
                    adjustedYs[adjustedYs.size - 1] = bottomPin 
                } else {
                    // Left side processes bottom-to-top.
                    // The first item in the list is the lowest visually.
                    adjustedYs[0] = bottomPin
                    currentY = bottomPin
                    for (i in 1 until labels.size) {
                        val proportion = labels[i-1].sweepAngle / totalSweepBetweenPins
                        val gap = totalAvailableHeight * proportion
                        currentY -= gap
                        adjustedYs[i] = currentY
                    }
                    adjustedYs[adjustedYs.size - 1] = topPin
                }

                // 3. Safety Nudge (Enforce minLabelSpacing)
                if (isRightSide) {
                    for (i in 1 until adjustedYs.size) {
                        if (adjustedYs[i] < adjustedYs[i - 1] + minLabelSpacing) {
                            adjustedYs[i] = adjustedYs[i - 1] + minLabelSpacing
                        }
                    }
                    // If nudging pushed us past bottom, pull everything up
                    if (adjustedYs.last() > canvasBottom) {
                        val overflow = adjustedYs.last() - canvasBottom
                        for (i in adjustedYs.indices) {
                            adjustedYs[i] -= overflow
                        }
                    }
                } else {
                    for (i in 1 until adjustedYs.size) {
                        if (adjustedYs[i] > adjustedYs[i - 1] - minLabelSpacing) {
                            adjustedYs[i] = adjustedYs[i - 1] - minLabelSpacing
                        }
                    }
                    // If nudging pushed us past top, push everything down
                    if (adjustedYs.last() < padding) {
                        val overflow = padding - adjustedYs.last()
                        for (i in adjustedYs.indices) {
                            adjustedYs[i] += overflow
                        }
                    }
                }

                return adjustedYs
            }

            val finalRightYs = resolveOverlaps(rightSideLabels, true)
            val finalLeftYs = resolveOverlaps(leftSideLabels, false)
            
            var rightIndex = 0
            var leftIndex = 0
            var startAngle = -90f

            items.forEachIndexed { index, item ->
                val sweepAngle = (item.value / totalValue * 360 * animationProgress.value).toFloat()
                val isSelected = index == selectedIndex
                
                val itemRadius = if (isSelected) radius + 15f else radius

                drawArc(
                    color = Color(item.color),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true, // Changed from false to true to make it a solid pie
                    topLeft = Offset(center.x - itemRadius, center.y - itemRadius),
                    size = Size(itemRadius * 2, itemRadius * 2),
                    style = androidx.compose.ui.graphics.drawscope.Fill // Changed from Stroke to Fill
                )

                // Draw Leader Lines and Labels
                // Ensure we only draw labels when the animation is mostly complete and the slice isn't tiny (9f is 2.5%)
                if (sweepAngle > 9f && animationProgress.value > 0.9f) {
                    val midAngle = startAngle + sweepAngle / 2
                    val angleInRadians = (midAngle * PI / 180).toFloat()
                    
                    // Start of leader line on outer edge of pie slice (now the edge is just 'itemRadius' away)
                    val lineStart = Offset(
                        center.x + itemRadius * cos(angleInRadians),
                        center.y + itemRadius * sin(angleInRadians)
                    )
                    
                    val isRightSide = cos(angleInRadians) > 0
                    
                    val targetY = if (isRightSide) {
                        if (rightIndex < finalRightYs.size) finalRightYs[rightIndex++] else lineStart.y
                    } else {
                        if (leftIndex < finalLeftYs.size) finalLeftYs[leftIndex++] else lineStart.y
                    }
                    
                    // Target X coordinate for the text column - widened to radius + 60f
                    val columnX = if (isRightSide) {
                        center.x + radius + 60f 
                    } else {
                        center.x - radius - 60f 
                    }
                    
                    // Bézier Curve Control points
                    // We draw a short straight line out radially, then curve to the target Y/X
                    val radialExitX = center.x + (itemRadius + 20f) * cos(angleInRadians)
                    val radialExitY = center.y + (itemRadius + 20f) * sin(angleInRadians)
                    
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(lineStart.x, lineStart.y)
                        lineTo(radialExitX, radialExitY) // Straight line exiting the pie radially
                        
                        // Bézier curve to the column - pull controls further out since column is further
                        cubicTo(
                            x1 = radialExitX + if (isRightSide) 40f else -40f, y1 = radialExitY, 
                            x2 = columnX - if (isRightSide) 40f else -40f, y2 = targetY,       
                            x3 = columnX, y3 = targetY                                         
                        )
                    }
                    
                    drawPath(
                        path = path,
                        color = Color.Gray,
                        style = Stroke(width = 2f)
                    )

                    var label = if (item is ReportChartItem && item.reportItem != null) item.reportItem.expense else "Label"
                    
                    // Text Truncation Logic
                    val paint = Paint().apply {
                        color = item.color
                        textSize = 30f
                        textAlign = if (isRightSide) Paint.Align.LEFT else Paint.Align.RIGHT
                        typeface = Typeface.DEFAULT_BOLD
                    }
                    
                    val textStartX = columnX + (if (isRightSide) 10f else -10f)
                    val availableWidth = if (isRightSide) size.width - textStartX - 10f else textStartX - 10f
                    
                    if (paint.measureText(label) > availableWidth && availableWidth > 0) {
                        // Binary search or iterative truncation could be used, but since it's just drawing, iterative is fine
                        while (label.length > 3 && paint.measureText("$label...") > availableWidth) {
                            label = label.dropLast(1)
                        }
                        label = "$label..."
                    }
                    
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        textStartX,
                        targetY + 10f, // vertical align
                        paint
                    )
                }

                startAngle += sweepAngle
            }
        }
    }
}

@Composable
fun ModernBarChart(
    modifier: Modifier = Modifier,
    items: List<ChartItem>,
    onItemClick: (ChartItem) -> Unit = {}
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val animationProgress = remember { Animatable(0f) }

    var lastItems by remember { mutableStateOf(items) }

    LaunchedEffect(items, selectedIndex) {
        if (selectedIndex == -1 || lastItems != items) {
            lastItems = items
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }
    }

    Box(modifier = modifier.padding(16.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(items) {
                    detectTapGestures { tapOffset ->
                        val barWidth = size.width / (items.size * 2)
                        var currentX = barWidth / 2

                        for (i in items.indices) {
                            if (tapOffset.x >= currentX && tapOffset.x <= (currentX + barWidth)) {
                                selectedIndex = if (selectedIndex == i) -1 else i
                                if (selectedIndex != -1) {
                                    onItemClick(items[i])
                                }
                                break
                            }
                            currentX += barWidth * 2
                        }
                    }
                }
        ) {
            val maxAbsValue = items.maxOfOrNull { abs(it.value) } ?: 0.0
            val maxValue = if (maxAbsValue > 0) maxAbsValue else 1.0 // Prevent division by zero
            val barWidth = size.width / (items.size * 2)
            var currentX = barWidth / 2

            // Draw Grid Lines
            val numGridLines = 5
            for (i in 0..numGridLines) {
                val y = size.height - (i * size.height / numGridLines)
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
                
                // Optional: Draw Y-axis labels
                drawContext.canvas.nativeCanvas.drawText(
                    String.format(Locale.getDefault(), "%.0f", maxValue * i / numGridLines),
                    0f,
                    y - 10f,
                    Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 24f
                    }
                )
            }

            // Draw Bars
            items.forEachIndexed { index, item ->
                // Use absolute value for bar height calculation to handle negative net worth
                val absoluteValue = abs(item.value)
                val barHeight = (absoluteValue / maxValue * size.height * animationProgress.value).toFloat()
                
                val isSelected = index == selectedIndex
                val color = if (isSelected) Color(item.color).copy(alpha = 0.7f) else Color(item.color)

                val maxWidthPx = 50.dp.toPx()
                
                drawRect(
                    color = color,
                    topLeft = Offset(currentX, size.height - barHeight),
                    size = Size(barWidth.coerceAtMost(maxWidthPx), barHeight)
                )

                // Re-draw rounded rect on top for just the top corners
                val cornerRadius = (barWidth.coerceAtMost(maxWidthPx)) / 4
                drawRoundRect(
                    color = color,
                    topLeft = Offset(currentX, size.height - barHeight),
                    size = Size(barWidth.coerceAtMost(maxWidthPx), barHeight),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )

                // Optional: Draw X-axis labels
                 drawContext.canvas.nativeCanvas.drawText(
                        item.label ?: "", // Fallback to item.label since reportItem might be null for Net Worth
                        currentX + barWidth / 2,
                        size.height + 40f,
                        Paint().apply {
                            this.color = android.graphics.Color.GRAY
                            textSize = 24f
                            textAlign = Paint.Align.CENTER
                        }
                    )


                currentX += barWidth * 2
            }
        }
    }
}

@Composable
fun NetWorthBarChart(
    modifier: Modifier = Modifier,
    items: List<ChartItem>,
    onItemClick: (ChartItem) -> Unit = {}
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val animationProgress = remember { Animatable(0f) }

    var lastItems by remember { mutableStateOf(items) }

    LaunchedEffect(items, selectedIndex) {
        if (selectedIndex == -1 || lastItems != items) {
            lastItems = items
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }
    }

    Box(modifier = modifier.padding(16.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(items) {
                    detectTapGestures { tapOffset ->
                        val barWidth = size.width / (items.size * 2)
                        var currentX = barWidth / 2

                        for (i in items.indices) {
                            if (tapOffset.x >= currentX && tapOffset.x <= (currentX + barWidth)) {
                                selectedIndex = if (selectedIndex == i) -1 else i
                                if (selectedIndex != -1) {
                                    onItemClick(items[i])
                                }
                                break
                            }
                            currentX += barWidth * 2
                        }
                    }
                }
        ) {
            if (items.isEmpty()) return@Canvas

            val maxValue = items.maxOf { it.value }
            val minValue = items.minOf { it.value }

            // 1. Initial rough bounds
            // We no longer force artificial space above 0 if all values are negative, 
            // or space below 0 if all values are positive.
            val paddedMax = if (maxValue > 0) maxValue * 1.05 else if (minValue < 0) 0.0 else 50.0
            val paddedMin = if (minValue < 0) minValue * 1.05 else if (maxValue > 0) 0.0 else -50.0

            // 2. Calculate a "Nice Step" for the grid
            val rawRange = paddedMax - paddedMin
            val rawStep = rawRange / 5.0
            val exponent = floor(log10(rawStep))
            val fraction = rawStep / 10.0.pow(exponent)
            val niceFraction = when {
                fraction < 1.5 -> 1.0
                fraction < 3.0 -> 2.0
                fraction < 7.0 -> 5.0
                else -> 10.0
            }
            val niceStep = niceFraction * 10.0.pow(exponent)

            // 3. Snap bounds tightly to the nice step based on the actual data
            // We use the actual maxValue and minValue (not padded) so we don't accidentally
            // jump to the next interval just because of padding.
            val maxBound = if (maxValue <= 0.0) 0.0 else ceil(maxValue / niceStep) * niceStep
            val minBound = if (minValue >= 0.0) 0.0 else floor(minValue / niceStep) * niceStep
            
            val totalRange = maxBound - minBound
            val rangeToUse = if (totalRange > 0) totalRange else 100.0 // Prevent div by 0

            val zeroY = (size.height * (maxBound / rangeToUse)).toFloat()

            val barWidth = size.width / (items.size * 2)
            var currentX = barWidth / 2

            val maxAbsLabel = maxOf(abs(maxBound), abs(minBound))

            // Draw Grid Lines (Dynamic based on nice step)
            var currentGridValue = maxBound
            while (currentGridValue >= minBound - (niceStep * 0.1)) { // tolerance for double math
                val y = (size.height * ((maxBound - currentGridValue) / rangeToUse)).toFloat()
                
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
                
                // Compare with an epsilon tolerance due to double floating point inaccuracies
                val isTop = abs(currentGridValue - maxBound) < 0.001
                val textY = if (isTop) y + 10f else y + 10f
                
                val safeVal = if (abs(currentGridValue) < 0.001) 0.0 else currentGridValue
                val labelStr = when {
                    maxAbsLabel >= 1_000_000 -> {
                        String.format(Locale.getDefault(), "%.1fM", safeVal / 1_000_000.0).replace(".0M", "M")
                    }
                    maxAbsLabel >= 10_000 -> {
                        String.format(Locale.getDefault(), "%.1fk", safeVal / 1_000.0).replace(".0k", "k")
                    }
                    else -> {
                        String.format(Locale.getDefault(), "%.0f", safeVal)
                    }
                }
                
                drawContext.canvas.nativeCanvas.drawText(
                    labelStr,
                    -20f,
                    textY,
                    Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 24f
                        textAlign = Paint.Align.RIGHT
                    }
                )
                
                currentGridValue -= niceStep
            }

            // Draw the Zero Line more prominently
            drawLine(
                color = Color.Gray,
                start = Offset(0f, zeroY),
                end = Offset(size.width, zeroY),
                strokeWidth = 2f
            )

            drawLine(
                color = Color.LightGray,
                start = Offset(0f, 0f), // Top of the canvas (maxBound)
                end = Offset(0f, size.height), // Bottom of the canvas (minBound)
                strokeWidth = 2f
            )

            // Draw Bars
            items.forEachIndexed { index, item ->
                // Calculate height relative to the total range
                val absoluteValue = abs(item.value)
                val rawBarHeight = (absoluteValue / rangeToUse * size.height).toFloat()
                val barHeight = rawBarHeight * animationProgress.value
                
                val isSelected = index == selectedIndex
                
                // Use the exact colors defined from PocketMoneyThemes based on positive/negative value
                // PocketMoneyThemes are accessible here via standard integer colors.
                // We use the same theme methods that the DataSources use!
                val themeColorInt = if (item.value >= 0) {
                    PocketMoneyThemes.greenBarColor()
                } else {
                    PocketMoneyThemes.redBarColor()
                }
                
                val baseColor = Color(themeColorInt)
                val color = if (isSelected) baseColor.copy(alpha = 0.7f) else baseColor

                val maxWidthPx = 50.dp.toPx()
                val actualBarWidth = barWidth.coerceAtMost(maxWidthPx)
                
                // Determine top-left corner for the rect
                val rectTopY = if (item.value >= 0) {
                    zeroY - barHeight // Bar goes UP from zero line
                } else {
                    zeroY // Bar goes DOWN from zero line, so top is at zero
                }
                
                // Adjust currentX slightly to push bars to the right of the Y-axis labels
                val barStartX = currentX + 60f 

                // Only draw the bar if it actually has a visible height (prevents 1px anti-aliased artifacts for 0.0 values)
                if (barHeight > 0.5f && abs(item.value) > 0.005) {
                    drawRect(
                        color = color,
                        topLeft = Offset(barStartX, rectTopY),
                        size = Size(actualBarWidth, barHeight)
                    )
                }

                // Draw X-axis labels at the very bottom of the chart
                val labelY = size.height + 40f
                
                drawContext.canvas.nativeCanvas.drawText(
                    item.label ?: "",
                    barStartX + actualBarWidth / 2,
                    labelY,
                    Paint().apply {
                        this.color = android.graphics.Color.GRAY
                        textSize = 24f
                        textAlign = Paint.Align.CENTER
                    }
                )

                currentX += barWidth * 2
            }
        }
    }
}

@Composable
fun CashFlowBarChart(
    modifier: Modifier = Modifier,
    assets: List<ChartItem>, // Income
    liabilities: List<ChartItem>, // Expenses
    onItemClick: (ChartItem) -> Unit = {}
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(assets) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Box(modifier = modifier.padding(16.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(assets, liabilities) {
                    detectTapGestures { tapOffset ->
                        val drawWidth = size.width - 60f
                        val actualPeriodWidth = drawWidth / assets.size
                        val barWidth = (actualPeriodWidth * 0.35f).coerceAtMost(40.dp.toPx())
                        val gapBetweenPairedBars = 2.dp.toPx()
                        var currentPeriodX = 60f

                        for (i in assets.indices) {
                            val incomeX = currentPeriodX + (actualPeriodWidth / 2f) - barWidth - (gapBetweenPairedBars / 2f)
                            val expenseX = currentPeriodX + (actualPeriodWidth / 2f) + (gapBetweenPairedBars / 2f)

                            // Check if tapped inside Income Bar horizontal bounds
                            if (tapOffset.x >= incomeX && tapOffset.x <= incomeX + barWidth) {
                                onItemClick(assets[i])
                                break
                            }
                            // Check if tapped inside Expense Bar horizontal bounds
                            else if (tapOffset.x >= expenseX && tapOffset.x <= expenseX + barWidth) {
                                onItemClick(liabilities[i])
                                break
                            }
                            
                            currentPeriodX += actualPeriodWidth
                        }
                    }
                }
        ) {
            if (assets.isEmpty() || liabilities.isEmpty()) return@Canvas
            if (assets.size != liabilities.size) return@Canvas

            // 1. Find Max Value across BOTH income and expenses
            val maxIncome = assets.maxOf { it.value }
            val maxExpense = liabilities.maxOf { it.value }
            val absoluteMax = maxOf(maxIncome, maxExpense)

            // 2. Calculate "Nice Step"
            val paddedMax = if (absoluteMax > 0) absoluteMax * 1.05 else 50.0
            
            val rawStep = paddedMax / 5.0
            val exponent = floor(log10(rawStep))
            val fraction = rawStep / 10.0.pow(exponent)
            val niceFraction = when {
                fraction < 1.5 -> 1.0
                fraction < 3.0 -> 2.0
                fraction < 7.0 -> 5.0
                else -> 10.0
            }
            val niceStep = niceFraction * 10.0.pow(exponent)

            // 3. Snap max bound
            val maxBound = ceil(paddedMax / niceStep) * niceStep
            val rangeToUse = if (maxBound > 0) maxBound else 100.0

            // For Cash Flow, zero is ALWAYS at the bottom
            val zeroY = size.height

            // Calculate widths
            // We need space for 2 bars per period + gap between periods + gap between bars
            val periodWidth = size.width / assets.size
            val barWidth = (periodWidth * 0.35f).coerceAtMost(40.dp.toPx()) // 35% of period width, max 40px
            val gapBetweenPairedBars = 2.dp.toPx()

            // Draw Grid Lines
            var currentGridValue = 0.0
            while (currentGridValue <= maxBound + (niceStep * 0.1)) {
                val y = (size.height * ((maxBound - currentGridValue) / rangeToUse)).toFloat()
                
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
                
                val isTop = abs(currentGridValue - maxBound) < 0.001
                val textY = if (isTop) y + 10f else if (currentGridValue == 0.0) y + 10f else y + 10f
                
                val safeVal = if (abs(currentGridValue) < 0.001) 0.0 else currentGridValue
                val labelStr = when {
                    maxBound >= 1_000_000 -> String.format(Locale.getDefault(), "%.1fM", safeVal / 1_000_000.0).replace(".0M", "M")
                    maxBound >= 10_000 -> String.format(Locale.getDefault(), "%.1fk", safeVal / 1_000.0).replace(".0k", "k")
                    else -> String.format(Locale.getDefault(), "%.0f", safeVal)
                }


                
                drawContext.canvas.nativeCanvas.drawText(
                    labelStr,
                    -20f,
                    textY,
                    Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 24f
                        textAlign = Paint.Align.RIGHT
                    }
                )
                
                currentGridValue += niceStep
            }

            // Draw Zero Line prominent
            drawLine(
                color = Color.Gray,
                start = Offset(0f, zeroY),
                end = Offset(size.width, zeroY),
                strokeWidth = 2f
            )

            drawLine(
                color = Color.LightGray,
                start = Offset(0f, zeroY),
                end = Offset(0f, 0f), // Draw from bottom to top
                strokeWidth = 2f
            )

            // Draw Paired Bars
            var currentPeriodX = 60f // Starting gutter for Y-axis labels
            val drawWidth = size.width - 60f
            val actualPeriodWidth = drawWidth / assets.size

            assets.forEachIndexed { index, incomeItem ->
                val expenseItem = liabilities[index]

                // Income Bar (Left)
                val incomeHeight = ((incomeItem.value / rangeToUse) * size.height * animationProgress.value).toFloat()
                val incomeX = currentPeriodX + (actualPeriodWidth / 2f) - barWidth - (gapBetweenPairedBars / 2f)
                
                if (incomeHeight > 0.5f && abs(incomeItem.value) > 0.005) {
                    drawRect(
                        color = Color(incomeItem.color),
                        topLeft = Offset(incomeX, zeroY - incomeHeight),
                        size = Size(barWidth, incomeHeight)
                    )
                }

                // Expense Bar (Right)
                val expenseHeight = ((expenseItem.value / rangeToUse) * size.height * animationProgress.value).toFloat()
                val expenseX = currentPeriodX + (actualPeriodWidth / 2f) + (gapBetweenPairedBars / 2f)
                
                if (expenseHeight > 0.5f && abs(expenseItem.value) > 0.005) {
                    drawRect(
                        color = Color(expenseItem.color),
                        topLeft = Offset(expenseX, zeroY - expenseHeight),
                        size = Size(barWidth, expenseHeight)
                    )
                }

                // X-axis label
                drawContext.canvas.nativeCanvas.drawText(
                    incomeItem.label ?: "",
                    currentPeriodX + (actualPeriodWidth / 2f),
                    size.height + 40f,
                    Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 24f
                        textAlign = Paint.Align.CENTER
                    }
                )

                currentPeriodX += actualPeriodWidth
            }
        }
    }
}

// Interop functions for Java
fun setPieChartContent(
    view: androidx.compose.ui.platform.ComposeView,
    items: List<ChartItem>,
    onItemClick: (ChartItem) -> Unit
) {
    view.setContent {
        ChartWithTabs(
            modifier = Modifier.fillMaxSize(),
            items = items,
            isPieChart = true,
            onItemClick = onItemClick
        )
    }
}

fun setBarChartContent(
    view: androidx.compose.ui.platform.ComposeView,
    items: List<ChartItem>,
    onItemClick: (ChartItem) -> Unit
) {
    view.setContent {
        ChartWithTabs(
            modifier = Modifier.fillMaxSize(),
            items = items,
            isPieChart = false,
            onItemClick = onItemClick
        )
    }
}