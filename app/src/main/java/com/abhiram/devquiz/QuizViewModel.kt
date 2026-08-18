package com.abhiram.devquiz

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class Verdict { NONE, RIGHT, WRONG }

const val REQUEUE_WITHIN = 3

class QuizViewModel(app: Application) : AndroidViewModel(app) {

    private val store = QuizStore(app)
    private val haptics = Haptics(app)
    private val rng = Random(SystemClock.elapsedRealtimeNanos())
    private val queues = Lane.entries.associateWith { ArrayDeque<Int>() }

    var question by mutableStateOf(Questions.all.first())
        private set

    var correctOnTop by mutableStateOf(true)
        private set

    var verdict by mutableStateOf(Verdict.NONE)
        private set

    var right by mutableIntStateOf(0)
        private set

    var asked by mutableIntStateOf(0)
        private set

    var turn by mutableIntStateOf(0)
        private set

    private var autoAdvance: Job? = null
    private var currentIndex = 0
    private var nextLane = Lane.TS

    init {
        restart()
    }

    fun answer(chosenCorrect: Boolean) {
        if (verdict != Verdict.NONE) return
        asked++
        store.lifetimeAsked = store.lifetimeAsked + 1
        if (chosenCorrect) {
            right++
            store.lifetimeRight = store.lifetimeRight + 1
            store.markMastered(question.id)
            verdict = Verdict.RIGHT
            haptics.right()
            autoAdvance = viewModelScope.launch {
                delay(RIGHT_HOLD_MS)
                advance()
            }
        } else {
            store.markMissed(question.id)
            verdict = Verdict.WRONG
            haptics.wrong()
            requeue(currentIndex)
        }
    }

    fun advance() {
        autoAdvance?.cancel()
        val lane = nextLane
        nextLane = if (lane == Lane.TS) Lane.FR else Lane.TS
        val queue = queues.getValue(lane)
        if (queue.isEmpty()) fill(lane)
        currentIndex = queue.removeFirst()
        question = Questions.all[currentIndex]
        correctOnTop = rng.nextBoolean()
        verdict = Verdict.NONE
        turn++
    }

    fun restart() {
        right = 0
        asked = 0
        nextLane = if (rng.nextBoolean()) Lane.TS else Lane.FR
        Lane.entries.forEach {
            queues.getValue(it).clear()
            fill(it)
        }
        advance()
    }

    val accuracy: Int get() = if (asked == 0) 0 else right * 100 / asked

    val lifetimeAccuracy: Int
        get() = if (store.lifetimeAsked == 0) 0 else store.lifetimeRight * 100 / store.lifetimeAsked

    // A missed question is reinserted within the next few turns of its own lane, so it is retried
    // while the reason is still fresh. Never appended at the end, or a miss costs a full round.
    private fun requeue(index: Int) {
        val queue = queues.getValue(Questions.all[index].lane)
        queue.add(minOf(rng.nextInt(REQUEUE_WITHIN), queue.size), index)
    }

    private fun fill(lane: Lane) {
        val order = Questions.byLane.getValue(lane).shuffled(rng).toMutableList()
        val missed = store.missed()
        order.toList()
            .filter { Questions.all[it].id in missed }
            .forEach { index ->
                if (order.remove(index)) order.add(rng.nextInt(minOf(LEECH_WINDOW, order.size + 1)), index)
            }
        queues.getValue(lane).addAll(order)
    }

    private companion object {
        const val RIGHT_HOLD_MS = 520L
        const val LEECH_WINDOW = 8
    }
}
