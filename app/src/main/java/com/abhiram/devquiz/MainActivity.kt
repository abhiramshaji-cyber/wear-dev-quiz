package com.abhiram.devquiz

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Text
import kotlinx.coroutines.delay

// A centered box this fraction of the diameter, square, fits entirely inside the circle:
// (f/2)^2 + (f/2)^2 <= (1/2)^2 holds for f up to 0.707.
private const val SAFE_FRACTION = 0.70f

// Width the snippet card gets, and the advance of one monospace character per sp, measured on
// a 233dp round watch. Snippets are authored to 24 columns, which lands on the 8sp floor.
private const val CODE_WIDTH_DP = 145f
private const val OPTION_WIDTH_DP = 143f
private val LISTEN_ROOM = 26.dp
private const val CHAR_PER_SP = 0.75f

private val Ink = Color(0xFF000000)
private val Card = Color(0xFF17171C)
private val Right = Color(0xFF1B5E20)
private val RightInk = Color(0xFF7CF29C)
private val Wrong = Color(0xFF5C1616)
private val WrongInk = Color(0xFFFF8A80)
private val Muted = Color(0xFF6E6E78)
private val Faded = Color(0xFF3A3A42)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent { QuizScreen() }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuizScreen(vm: QuizViewModel = viewModel()) {
    val q = vm.question
    val scroll = rememberScrollState()
    LaunchedEffect(vm.turn) { scroll.scrollTo(0) }
    LaunchedEffect(vm.verdict) {
        if (vm.verdict == Verdict.WRONG) {
            // maxValue is only correct after the reason has been laid out.
            delay(90)
            scroll.animateScrollTo(scroll.maxValue)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .combinedClickable(
                interactionSource = null,
                indication = null,
                onClick = { if (vm.verdict == Verdict.WRONG) vm.advance() },
                onLongClick = { vm.restart() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        // The glass is a circle inside a square framebuffer, so the scroll viewport is capped
        // to the inscribed square. Overflow scrolls INSIDE this box rather than sliding text
        // up into the corners, where the circle is only a few pixels wide.
        Column(
            modifier = Modifier
                .fillMaxWidth(SAFE_FRACTION)
                .fillMaxHeight(SAFE_FRACTION)
                // Keeps scrolling content from sliding under the pinned speaker.
                .padding(top = if (vm.canSpeak) LISTEN_ROOM else 0.dp)
                .verticalScroll(scroll),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Header(q.topic, vm.right, vm.asked, vm.accuracy)
            Spacer(Modifier.height(5.dp))
            q.code?.let {
                Snippet(it)
                Spacer(Modifier.height(5.dp))
            }
            Text(
                text = q.prompt,
                color = if (q.code == null) Color(0xFFF2F2F4) else Color(0xFFB9B9C4),
                fontSize = if (q.code == null) 14.sp else 12.sp,
                lineHeight = if (q.code == null) 18.sp else 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))

            val mono = q.lane == Lane.TS
            val top = if (vm.correctOnTop) q.correct else q.wrong
            val bottom = if (vm.correctOnTop) q.wrong else q.correct
            Option(top, mono, vm.correctOnTop, vm.verdict, { vm.answer(vm.correctOnTop) }, vm::advance)
            Spacer(Modifier.height(5.dp))
            Option(bottom, mono, !vm.correctOnTop, vm.verdict, { vm.answer(!vm.correctOnTop) }, vm::advance)

            if (vm.verdict == Verdict.WRONG) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = q.why,
                    color = Color(0xFFB9B9C4),
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    text = "TAP TO CONTINUE",
                    color = Muted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        if (vm.canSpeak) {
            Listen(
                vm.speakUnlocked,
                vm::say,
                Modifier.align(Alignment.TopCenter).padding(top = 20.dp),
            )
        }
    }
}

@Composable
private fun Header(topic: Topic, right: Int, asked: Int, accuracy: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = topic.label,
            color = topic.color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "  $right/$asked  $accuracy%",
            color = Faded,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun Listen(unlocked: Boolean, onSpeak: () -> Unit, modifier: Modifier = Modifier) {
    val tint = if (unlocked) Color(0xFF9FE8C0) else Faded
    Canvas(
        modifier = modifier
            .size(34.dp)
            .clickable(enabled = unlocked, onClick = onSpeak),
    ) {
        val w = size.width
        val h = size.height
        val cone = Path().apply {
            moveTo(w * 0.30f, h * 0.36f)
            lineTo(w * 0.52f, h * 0.14f)
            lineTo(w * 0.52f, h * 0.86f)
            lineTo(w * 0.30f, h * 0.64f)
            close()
        }
        drawRect(tint, Offset(w * 0.10f, h * 0.36f), Size(w * 0.21f, h * 0.28f))
        drawPath(cone, tint)
        val stroke = Stroke(width = w * 0.07f, cap = StrokeCap.Round)
        listOf(0.24f, 0.40f).forEach { r ->
            drawArc(
                color = tint,
                startAngle = -50f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(w * 0.52f - w * r, h * 0.5f - w * r),
                size = Size(w * r * 2f, w * r * 2f),
                style = stroke,
            )
        }
    }
}

@Composable
private fun Snippet(code: String) {
    val columns = remember(code) { code.lines().maxOf { it.length } }
    val size = (CODE_WIDTH_DP / (columns * CHAR_PER_SP)).coerceIn(8f, 11f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Card)
            .padding(horizontal = 9.dp, vertical = 6.dp),
    ) {
        Text(
            text = code,
            color = Color(0xFF9FE8C0),
            fontSize = size.sp,
            lineHeight = (size * 1.35f).sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
private fun Option(
    text: String,
    mono: Boolean,
    isCorrect: Boolean,
    verdict: Verdict,
    onPick: () -> Unit,
    onContinue: () -> Unit,
) {
    val revealed = verdict != Verdict.NONE
    val fill = when {
        revealed && isCorrect -> Right
        verdict == Verdict.WRONG -> Wrong
        else -> Card
    }
    val ink = when {
        revealed && isCorrect -> RightInk
        verdict == Verdict.WRONG -> WrongInk
        revealed -> Muted
        else -> Color(0xFFE6E6EA)
    }
    val size = if (mono) (OPTION_WIDTH_DP / (text.length * CHAR_PER_SP)).coerceIn(8f, 11f) else 12.5f
    val bg by animateColorAsState(fill, label = "optionFill")
    val fg by animateColorAsState(ink, label = "optionInk")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(bg)
            .clickable { if (revealed) onContinue() else onPick() }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = size.sp,
            lineHeight = (size * 1.27f).sp,
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
            fontWeight = if (mono) FontWeight.Medium else FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}
