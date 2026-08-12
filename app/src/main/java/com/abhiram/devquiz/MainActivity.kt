package com.abhiram.devquiz

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
                .verticalScroll(scroll),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Header(q.topic, vm.right, vm.asked, vm.accuracy)
            Spacer(Modifier.height(6.dp))
            Text(
                text = q.prompt,
                color = Color(0xFFF2F2F4),
                fontSize = 13.5.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))

            val top = if (vm.correctOnTop) q.correct else q.wrong
            val bottom = if (vm.correctOnTop) q.wrong else q.correct
            Option(top, vm.correctOnTop, vm.verdict, { vm.answer(vm.correctOnTop) }, vm::advance)
            Spacer(Modifier.height(6.dp))
            Option(bottom, !vm.correctOnTop, vm.verdict, { vm.answer(!vm.correctOnTop) }, vm::advance)

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
private fun Option(
    text: String,
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
    val bg by animateColorAsState(fill, label = "optionFill")
    val fg by animateColorAsState(ink, label = "optionInk")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(bg)
            .clickable { if (revealed) onContinue() else onPick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}
