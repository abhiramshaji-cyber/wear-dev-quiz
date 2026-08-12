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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Header(q.topic, vm.right, vm.asked, vm.accuracy)
            Spacer(Modifier.height(8.dp))
            Text(
                text = q.prompt,
                color = Color(0xFFF2F2F4),
                fontSize = 14.5.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(9.dp))

            val top = if (vm.correctOnTop) q.correct else q.wrong
            val bottom = if (vm.correctOnTop) q.wrong else q.correct
            Option(top, vm.correctOnTop, vm.verdict) { vm.answer(vm.correctOnTop) }
            Spacer(Modifier.height(6.dp))
            Option(bottom, !vm.correctOnTop, vm.verdict) { vm.answer(!vm.correctOnTop) }

            if (vm.verdict == Verdict.WRONG) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = q.why,
                    color = Color(0xFFB9B9C4),
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp,
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
private fun Option(text: String, isCorrect: Boolean, verdict: Verdict, onClick: () -> Unit) {
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
            .clickable(enabled = !revealed, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}
