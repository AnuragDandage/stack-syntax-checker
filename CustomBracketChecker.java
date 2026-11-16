import java.util.ArrayList;
import java.util.List;

/** Validates a user-entered bracket sequence with the application's array Stack. */
public final class CustomBracketChecker {
    // One recorded character action; the stack snapshot lets the UI replay the algorithm.
    public static final class Step {
        public final int index;
        public final char character;
        public final String operation;
        public final String reason;
        public final char[] stack;
        public final boolean error;

        Step(int index, char character, String operation, String reason, char[] stack, boolean error) {
            this.index = index;
            this.character = character;
            this.operation = operation;
            this.reason = reason;
            this.stack = stack;
            this.error = error;
        }
    }

    // Final status distinguishes invalid input from valid-but-unbalanced brackets.
    public static final class Result {
        public final boolean valid;
        public final boolean balanced;
        public final String message;
        public final String explanation;
        public final char[] finalStack;

        Result(boolean valid, boolean balanced, String message, String explanation, char[] finalStack) {
            this.valid = valid;
            this.balanced = balanced;
            this.message = message;
            this.explanation = explanation;
            this.finalStack = finalStack;
        }
    }

    // Bundles the trace and result returned by one custom sequence analysis.
    public static final class Analysis {
        public final List<Step> steps;
        public final Result result;

        Analysis(List<Step> steps, Result result) {
            this.steps = steps;
            this.result = result;
        }
    }

    // Validate and trace a bracket-only sequence using the shared manual Stack class.
    public Analysis analyze(String input) {
        List<Step> steps = new ArrayList<>();
        String sequence = input == null ? "" : input;
        Stack stack = new Stack(Math.max(4, sequence.length()));

        // Reject blank input so it cannot be mistaken for a successful empty sequence.
        if (sequence.trim().isEmpty()) {
            return new Analysis(steps, new Result(false, false, "INPUT REQUIRED",
                    "Enter at least one bracket before checking.", stack.display()));
        }

        // Check the whole sequence first; unsupported characters stop the check safely.
        for (int i = 0; i < sequence.length(); i++) {
            char c = sequence.charAt(i);
            if (!SyntaxChecker.open(c) && !SyntaxChecker.close(c)) {
                String message = "Unsupported character '" + c + "' at position " + (i + 1)
                        + ". Use only (, ), {, }, [, and ].";
                return new Analysis(steps, new Result(false, false, "INVALID INPUT", message, stack.display()));
            }
        }

        // Process each supported bracket and record the resulting stack after the operation.
        for (int i = 0; i < sequence.length(); i++) {
            char c = sequence.charAt(i);
            String operation;
            String reason;
            boolean error = false;
            if (SyntaxChecker.open(c)) {
                stack.push(c, i);
                operation = "PUSH";
                reason = "Opening bracket pushed onto the stack.";
            } else if (stack.isEmpty()) {
                operation = "ERROR";
                reason = "Unmatched closing bracket; the stack is empty.";
                error = true;
            } else {
                char opening = stack.pop();
                char expected = SyntaxChecker.matching(opening);
                if (c == expected) {
                    operation = "POP";
                    reason = "Matching close; popped " + opening + ".";
                } else {
                    operation = "ERROR";
                    reason = "Mismatched closing bracket; expected '" + expected
                            + "' to match the most recently opened '" + opening + "'.";
                    error = true;
                }
            }
            steps.add(new Step(i, c, operation, reason, stack.display(), error));
            // An empty stack or mismatched pair ends the trace at the first decisive error.
            if (error) {
                return new Analysis(steps, new Result(true, false, "NOT BALANCED", reason, stack.display()));
            }
        }

        // Any opener still present has no matching closer; report the remaining stack values.
        if (!stack.isEmpty()) {
            char[] remaining = stack.display();
            StringBuilder unmatched = new StringBuilder();
            for (char c : remaining) unmatched.append(c);
            String explanation = "Unmatched opening bracket(s) remain on the stack: " + unmatched + ".";
            return new Analysis(steps, new Result(true, false, "NOT BALANCED", explanation, remaining));
        }
        // No errors and no leftover openers means every pair matched in nesting order.
        return new Analysis(steps, new Result(true, true, "BALANCED",
                "All opening brackets have matching closing brackets in the correct order. The stack is empty.",
                stack.display()));
    }
}
