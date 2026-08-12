# Drill

Two choice senior frontend interview drill for Wear OS. Built for a OnePlus Watch 2R, works on any round Wear OS 4 watch.

176 questions on the things interviewers actually probe at mid and senior level: React internals, caching and state architecture, performance diagnosis, the TypeScript type system, the JS runtime, frontend architecture, debugging process, and the live coding tasks that replace trivia. No syntax recall, no "what does HTML stand for".

## How it works

- One question, two answers. Tap one.
- Right: brief green confirm, short buzz, auto advance.
- Wrong: the correct answer turns green, yours turns red, and a one line reason explains the mechanism. Double buzz. Tap anywhere to continue.
- **A question you miss comes back within the next 5 turns, and keeps coming back until you get it right.**
- Which side the correct answer sits on is randomized every time, so you cannot memorize positions.
- Question order is randomized per round. Questions you have ever missed are seeded early in the next round and stay flagged across launches.
- Long press anywhere resets the session score.

Header shows the topic, this session's right over asked, and accuracy.

## Topics

| Topic | Questions |
| --- | --- |
| RENDER | 28 |
| TYPES | 28 |
| RUNTIME | 28 |
| STATE | 22 |
| DESIGN | 22 |
| PERF | 20 |
| CODE | 16 |
| DEBUG | 12 |

## Build

Push to `main` and grab `drill-debug-apk` from the Actions run, or locally:

```
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Adding questions

Everything lives in `app/src/main/java/com/abhiram/devquiz/Questions.kt`. One entry per question:

```kotlin
q(
    Topic.RENDER,
    "prompt, keep it under 60 characters",
    "the correct answer",
    "the plausible wrong answer",
    "one line on why, shown only when you miss it",
)
```

Prompts read fine up to about 78 characters and options up to about 70 on a 192dp screen, which is the tightest round Wear display. Longer than that and the screen scrolls instead of fitting.
