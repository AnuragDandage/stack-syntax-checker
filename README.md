# Stack-Based Parenthesis and Code Syntax Checker

Java 17 Swing application demonstrating how a manually implemented array stack validates (), {}, and [] in Java source.

## Compile and run
Requires JDK 17 or later. From this folder run:

```sh
javac -d build *.java
java -cp build Main
```

Windows: run `run.bat`.

## Use
Load a sample or paste code, then select PLAY, PAUSE, NEXT STEP, or RESET. The speed slider adjusts playback. The visualization and log show the stack after each character. Results identify the first delimiter error, line, and expected closing delimiter when available.

The **Custom Bracket Checker** accepts only `()`, `{}`, and `[]`. Enter a sequence and choose CHECK to watch the existing array-backed stack process it. PLAY, PAUSE, NEXT STEP, and RESET control the custom trace; CLEAR removes that sequence and its result. Empty input and unsupported characters receive validation messages. The custom checker keeps its own analysis and result separate from the Java code checker.

Delimiters inside strings, character literals, line comments, and block comments are ignored. This validates delimiter matching only, not full Java grammar.

## Algorithm
Scan left to right. Push each opener. For each closer, report an error if the stack is empty; otherwise compare with the top and pop on a match. Any opener left at the end is unmatched. Time O(n); worst-case stack space O(n).
