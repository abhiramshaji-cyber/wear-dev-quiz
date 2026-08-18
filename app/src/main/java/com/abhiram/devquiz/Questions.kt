package com.abhiram.devquiz

import androidx.compose.ui.graphics.Color

enum class Lane { TS, FR }

enum class Topic(val label: String, val color: Color, val lane: Lane) {
    BASICS("BASICS", Color(0xFF60A5FA), Lane.TS),
    TYPES("TYPES", Color(0xFF61DAFB), Lane.TS),
    GENERIC("GENERIC", Color(0xFFA78BFA), Lane.TS),
    NARROW("NARROW", Color(0xFF2DD4BF), Lane.TS),
    ASYNC("ASYNC", Color(0xFFFBBF24), Lane.TS),
    ARRAY("ARRAY", Color(0xFF4ADE80), Lane.TS),
    OBJECT("OBJECT", Color(0xFFFB923C), Lane.TS),
    GOTCHA("GOTCHA", Color(0xFFF472B6), Lane.TS),

    VERBS("VERBES", Color(0xFF60A5FA), Lane.FR),
    PHRASE("PHRASES", Color(0xFF61DAFB), Lane.FR),
    WORK("TRAVAIL", Color(0xFFA78BFA), Lane.FR),
    DAILY("QUOTIDIEN", Color(0xFF2DD4BF), Lane.FR),
    TIME("TEMPS", Color(0xFFFBBF24), Lane.FR),
    SOCIAL("SOCIAL", Color(0xFF4ADE80), Lane.FR),
    PLACE("ENDROITS", Color(0xFFFB923C), Lane.FR),
    QUEBEC("QUEBEC", Color(0xFFF472B6), Lane.FR),
}

class Question(
    val topic: Topic,
    val code: String?,
    val prompt: String,
    val correct: String,
    val wrong: String,
    val why: String,
    val speak: String? = null,
) {
    val lane: Lane get() = topic.lane

    // Speaking it early would give the answer away when the French is the answer, not the prompt.
    val speakableUnanswered: Boolean get() = speak != null && speak == prompt
    val id: Int = (code.orEmpty() + prompt + correct).hashCode()
}

object Questions {

    val all: List<Question> = TsQuestions.all + FrQuestions.all

    val byLane: Map<Lane, List<Int>> = all.indices.groupBy { all[it].lane }
}
