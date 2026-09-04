package com.example.ui.graphics

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale

@Composable
fun FloatingGraphicCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "rotating_finance_rings")

    // Rotation angle animations (outer/inner clockwise, middle counter-clockwise)
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rotation"
    )

    val middleRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "middle_rotation"
    )

    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rotation"
    )

    // Subtle vertical bouncing transition for the center
    val bounceY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "canvas_bounce"
    )

    // Pulsing alpha for the radial gradient glow
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // PREMIUM ENTRY ANIMATIONS
    // 1. Scale from 0f to 1f with a low stiffness spring
    val entryScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entryScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // 2. Sweep angle draw-in over 1500ms using FastOutSlowInEasing curve
    var startDrawing by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startDrawing = true
    }

    val outerSweepAngle by animateFloatAsState(
        targetValue = if (startDrawing) 280f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "outer_sweep"
    )

    val middleSweepAngle by animateFloatAsState(
        targetValue = if (startDrawing) 240f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "middle_sweep"
    )

    val innerSweepAngle by animateFloatAsState(
        targetValue = if (startDrawing) 300f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "inner_sweep"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width > 0f && height > 0f) {
            val centerX = width / 2f
            val centerY = (height / 2f) + bounceY

            // 1. Glowing radial background behind the rings (scaled sequentially)
            val glowRadius = width * 0.45f * entryScale.value
            if (glowRadius > 1.0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6366F1).copy(alpha = (0.22f * pulseAlpha * entryScale.value).coerceIn(0f, 1f)),
                            Color(0xFFC06240).copy(alpha = (0.12f * pulseAlpha * entryScale.value).coerceIn(0f, 1f)),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = Offset(centerX, centerY)
                )
            }

            // Define ring properties
            val outerRadius = width * 0.32f
            val middleRadius = width * 0.24f
            val innerRadius = width * 0.16f

            // 2. Draw outer ring: BrandLime with rotating gradient stroke & custom sweep
            val outerGradient = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFFE2F163),
                    Color(0xFFE2F163).copy(alpha = 0.10f),
                    Color(0xFFE2F163)
                ),
                center = Offset(centerX, centerY)
            )

            withTransform({
                scale(scaleX = entryScale.value, scaleY = entryScale.value, pivot = Offset(centerX, centerY))
                rotate(degrees = outerRotation, pivot = Offset(centerX, centerY))
            }) {
                drawArc(
                    brush = outerGradient,
                    startAngle = 0f,
                    sweepAngle = outerSweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - outerRadius, centerY - outerRadius),
                    size = Size(outerRadius * 2f, outerRadius * 2f),
                    style = Stroke(width = 12f)
                )
                // Draw dynamic dot that rides the drawing tip of the arc
                if (outerSweepAngle > 0f) {
                    val angleRad = Math.toRadians(outerSweepAngle.toDouble())
                    val dotX = centerX + outerRadius * Math.cos(angleRad).toFloat()
                    val dotY = centerY + outerRadius * Math.sin(angleRad).toFloat()
                    drawCircle(
                        color = Color(0xFFE2F163),
                        radius = 6f,
                        center = Offset(dotX, dotY)
                    )
                }
            }

            // 3. Draw middle ring: SunsetOrangeGlow with reversed rotating gradient stroke & custom sweep
            val middleGradient = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFFC06240),
                    Color(0xFFC06240).copy(alpha = 0.10f),
                    Color(0xFFC06240)
                ),
                center = Offset(centerX, centerY)
            )

            withTransform({
                scale(scaleX = entryScale.value, scaleY = entryScale.value, pivot = Offset(centerX, centerY))
                rotate(degrees = middleRotation, pivot = Offset(centerX, centerY))
            }) {
                drawArc(
                    brush = middleGradient,
                    startAngle = 0f,
                    sweepAngle = middleSweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - middleRadius, centerY - middleRadius),
                    size = Size(middleRadius * 2f, middleRadius * 2f),
                    style = Stroke(width = 10f)
                )
                if (middleSweepAngle > 0f) {
                    val angleRad = Math.toRadians(middleSweepAngle.toDouble())
                    val dotX = centerX + middleRadius * Math.cos(angleRad).toFloat()
                    val dotY = centerY + middleRadius * Math.sin(angleRad).toFloat()
                    drawCircle(
                        color = Color(0xFFC06240),
                        radius = 5f,
                        center = Offset(dotX, dotY)
                    )
                }
            }

            // 4. Draw inner ring: Indigo with rotating gradient stroke & custom sweep
            val innerGradient = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFF6366F1),
                    Color(0xFF6366F1).copy(alpha = 0.10f),
                    Color(0xFF6366F1)
                ),
                center = Offset(centerX, centerY)
            )

            withTransform({
                scale(scaleX = entryScale.value, scaleY = entryScale.value, pivot = Offset(centerX, centerY))
                rotate(degrees = innerRotation, pivot = Offset(centerX, centerY))
            }) {
                drawArc(
                    brush = innerGradient,
                    startAngle = 0f,
                    sweepAngle = innerSweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - innerRadius, centerY - innerRadius),
                    size = Size(innerRadius * 2f, innerRadius * 2f),
                    style = Stroke(width = 8f)
                )
                if (innerSweepAngle > 0f) {
                    val angleRad = Math.toRadians(innerSweepAngle.toDouble())
                    val dotX = centerX + innerRadius * Math.cos(angleRad).toFloat()
                    val dotY = centerY + innerRadius * Math.sin(angleRad).toFloat()
                    drawCircle(
                        color = Color(0xFF6366F1),
                        radius = 4f,
                        center = Offset(dotX, dotY)
                    )
                }
            }

            // 5. Draw modern translucent credit card floating in the very center
            val cardWidth = width * 0.44f * entryScale.value
            val cardHeight = cardWidth / 1.58f
            
            if (cardWidth > 1f && cardHeight > 1f) {
                withTransform({
                    scale(scaleX = entryScale.value, scaleY = entryScale.value, pivot = Offset(centerX, centerY))
                    // Subtle tilting animation reacting to bounceY
                    rotate(degrees = -10f + (bounceY * 0.5f), pivot = Offset(centerX, centerY))
                }) {
                    val cardTopLeft = Offset(centerX - cardWidth / 2f, centerY - cardHeight / 2f)
                    
                    // Glassmorphic translucent body
                    drawRoundRect(
                        color = Color(0x24FFFFFF),
                        topLeft = cardTopLeft,
                        size = Size(cardWidth, cardHeight),
                        cornerRadius = CornerRadius(16f, 16f)
                    )
                    
                    // Dark inner core for contrast
                    drawRoundRect(
                        color = Color(0xAE121118),
                        topLeft = cardTopLeft,
                        size = Size(cardWidth, cardHeight),
                        cornerRadius = CornerRadius(16f, 16f)
                    )

                    // Glowing neon-sunset gradient border
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE2F163).copy(alpha = 0.8f),
                                Color(0xFF8B5CF6).copy(alpha = 0.3f),
                                Color(0xFFC06240).copy(alpha = 0.8f)
                            )
                        ),
                        topLeft = cardTopLeft,
                        size = Size(cardWidth, cardHeight),
                        cornerRadius = CornerRadius(16f, 16f),
                        style = Stroke(width = 2.5f)
                    )

                    // Draw a cute shiny golden chip
                    val chipWidth = cardWidth * 0.15f
                    val chipHeight = chipWidth * 0.72f
                    val chipTopLeft = Offset(cardTopLeft.x + cardWidth * 0.12f, cardTopLeft.y + cardHeight * 0.38f)
                    
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFFEA79), Color(0xFFD4AF37))
                        ),
                        topLeft = chipTopLeft,
                        size = Size(chipWidth, chipHeight),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    
                    drawRoundRect(
                        color = Color(0x35000000),
                        topLeft = chipTopLeft,
                        size = Size(chipWidth, chipHeight),
                        cornerRadius = CornerRadius(6f, 6f),
                        style = Stroke(width = 1f)
                    )

                    // Stylized double overlapping circle logo (SunsetOrange and NeonLime)
                    val emblemRadius = cardWidth * 0.05f
                    val emblemCenter1 = Offset(cardTopLeft.x + cardWidth * 0.76f, cardTopLeft.y + cardHeight * 0.34f)
                    val emblemCenter2 = Offset(cardTopLeft.x + cardWidth * 0.82f, cardTopLeft.y + cardHeight * 0.34f)
                    
                    drawCircle(
                        color = Color(0xFFC06240).copy(alpha = 0.85f),
                        radius = emblemRadius,
                        center = emblemCenter1
                    )
                    drawCircle(
                        color = Color(0xFFE2F163).copy(alpha = 0.85f),
                        radius = emblemRadius,
                        center = emblemCenter2
                    )

                    // Dynamic chip contact lines for absolute high detail
                    drawLine(
                        color = Color(0x3D000000),
                        start = Offset(chipTopLeft.x + chipWidth / 2f, chipTopLeft.y),
                        end = Offset(chipTopLeft.x + chipWidth / 2f, chipTopLeft.y + chipHeight),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = Color(0x3D000000),
                        start = Offset(chipTopLeft.x, chipTopLeft.y + chipHeight / 2f),
                        end = Offset(chipTopLeft.x + chipWidth, chipTopLeft.y + chipHeight / 2f),
                        strokeWidth = 1f
                    )
                }
            }
        }
    }
}
