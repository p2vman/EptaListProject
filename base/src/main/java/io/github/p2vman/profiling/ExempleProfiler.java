package io.github.p2vman.profiling;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ExempleProfiler implements Profiler {
    private static final String $file = "https://github.com/p2vman/EptaListProject/blob/master/base/src/main/java/io/github/p2vman/profiling/ExempleProfiler.java";
    private final Map<String, Long> totalTimes = new HashMap<>();
    private final Stack<String> stack = new Stack<>();
    private final Map<String, Long> startTimes = new HashMap<>();

    public void push(String name) {
        stack.push(name);
        startTimes.put(name, System.nanoTime());
    }

    public String pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException($file+"#L20");
        }
        String name = stack.pop();
        Long startTime = startTimes.remove(name);
        if (startTime == null) {
            System.out.println($file+"#L26");
            throw new IllegalStateException(name);
        }
        long elapsedTime = System.nanoTime() - startTime;
        totalTimes.put(name, totalTimes.getOrDefault(name, 0L) + elapsedTime);
        return name;
    }

    public String peek() {
        if (stack.isEmpty()) {
            throw new IllegalStateException($file+"#L35");
        }
        return stack.peek();
    }

    public long getElapsedTimeAndRemove(String name) {
        Long elapsedTime = totalTimes.remove(name);
        if (elapsedTime == null) {
            System.out.println($file+"#L44");
            throw new IllegalStateException(name);
        }
        return elapsedTime / 1_000_000;
    }

    public long getElapsedTime(String name) {
        Long elapsedTime = totalTimes.get(name);
        if (elapsedTime == null) {
            System.out.println($file+"#L53");
            throw new IllegalStateException(name);
        }
        return elapsedTime / 1_000_000;
    }
}
