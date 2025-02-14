package opg.p2vman.moduleloader.profiling;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ExempleProfiler implements Profiler {
    private final Map<String, Long> totalTimes = new HashMap<>();
    private final Stack<String> stack = new Stack<>();
    private final Map<String, Long> startTimes = new HashMap<>();

    public void push(String name) {
        stack.push(name);
        startTimes.put(name, System.nanoTime());
    }

    public String pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Нет активных блоков для остановки.");
        }
        String name = stack.pop();
        Long startTime = startTimes.remove(name);
        if (startTime == null) {
            throw new IllegalStateException("Блок " + name + " не был запущен.");
        }
        long elapsedTime = System.nanoTime() - startTime;
        totalTimes.put(name, totalTimes.getOrDefault(name, 0L) + elapsedTime);
        return name;
    }

    public String peek() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Нет активных блоков.");
        }
        return stack.peek();
    }

    public long getElapsedTimeAndRemove(String name) {
        Long elapsedTime = totalTimes.remove(name);
        if (elapsedTime == null) {
            throw new IllegalStateException("Блок " + name + " не найден.");
        }
        return elapsedTime / 1_000_000;
    }

    public long getElapsedTime(String name) {
        Long elapsedTime = totalTimes.get(name);
        if (elapsedTime == null) {
            throw new IllegalStateException("Блок " + name + " не найден.");
        }
        return elapsedTime / 1_000_000;
    }
}