package com.app.toolbox.fragment.timer;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

class TimerManager implements Iterable<Timer> {
    static final TimerManager instance=new TimerManager();
    private final List<Timer> timers=new ArrayList<>();

    private TimerManager(){}

    public void add(Timer timer) {
        timers.add(timer);
    }

    @NonNull
    @Override
    public Iterator<Timer> iterator() {
        return timers.iterator();
    }

    @Override
    public void forEach(@NonNull Consumer<? super Timer> action) {
        timers.forEach(action);
    }

    @NonNull
    @Override
    public Spliterator<Timer> spliterator() {
        return timers.spliterator();
    }
}
