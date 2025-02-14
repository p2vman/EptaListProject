package opg.p2vman.moduleloader.profiling;

public interface Profiler {
    void push(String name);
    String pop();
    String peek();
    long getElapsedTimeAndRemove(String name);
    long getElapsedTime(String name);
}