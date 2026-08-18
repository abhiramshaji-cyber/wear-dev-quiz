# Drill

Two choice drill for Wear OS. Built for a OnePlus Watch 2R, works on any round Wear OS 4 watch.

433 questions in two alternating lanes: **read the code, pick what it does** in TypeScript and JavaScript, then **Québec French vocabulary** as connected chunks you can actually build a sentence from.

## How it works

- One question, two answers. Tap one.
- The lanes alternate: TypeScript, French, TypeScript, French. Order inside each lane is random every round.
- Right: brief green confirm, short buzz, auto advance.
- Wrong: the correct answer turns green, yours turns red, and a one line reason explains it. Double buzz. Tap anywhere to continue.
- **A question you miss comes back within the next few turns of its lane, and keeps coming back until you get it right.**
- French questions carry a speaker icon pinned at the top of the screen. Tap it and the French is read out loud in a Québec voice at 0.6 speed, and it is spoken once on its own the moment an answer is revealed. When the French is the answer rather than the question, the icon stays dim until you have answered, so it never gives the answer away.
- Which side the correct answer sits on is randomized every time.
- Questions you have ever missed are seeded early in the next round and stay flagged across launches.
- Long press anywhere resets the session score.

## TypeScript, 198 questions

A snippet on screen, two possible results. Language behaviour, not trivia: the things you use daily and still cannot answer cold.

| Topic | Questions |
| --- | --- |
| TYPES | 46 |
| BASICS | 28 |
| ASYNC | 22 |
| GOTCHA | 22 |
| ARRAY | 25 |
| OBJECT | 20 |
| NARROW | 20 |
| GENERIC | 15 |

## French, 235 questions

Québec French, not Paris French. Every entry is a **chunk** — `il faut que je`, `j'ai de la misère à`, `ça me tente pas` — because single words do not help you build a sentence. Prompts run both ways: French to English and English to French, so you drill recognition and production.

| Topic | Questions |
| --- | --- |
| PHRASES | 40 |
| VERBES | 30 |
| TRAVAIL | 30 |
| QUOTIDIEN | 30 |
| QUEBEC | 30 |
| ENDROITS | 25 |
| TEMPS | 25 |
| SOCIAL | 25 |

## Build

Push to `main` and grab `drill-debug-apk` from the Actions run, or locally:

```
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
