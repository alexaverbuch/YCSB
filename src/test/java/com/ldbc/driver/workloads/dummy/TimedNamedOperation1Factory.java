package com.ldbc.driver.workloads.dummy;

import com.ldbc.driver.Operation;
import com.ldbc.driver.temporal.Time;

import java.util.Iterator;

public class TimedNamedOperation1Factory implements Iterator<Operation<?>> {
    private final Iterator<Time> startTimes;
    private final Iterator<Time> dependencyTimes;
    private final Iterator<String> names;

    public TimedNamedOperation1Factory(Iterator<Time> startTimes,
                                       Iterator<Time> dependencyTimes,
                                       Iterator<String> names) {
        this.startTimes = startTimes;
        this.dependencyTimes = dependencyTimes;
        this.names = names;
    }

    @Override
    public boolean hasNext() {
        return startTimes.hasNext() & dependencyTimes.hasNext();
    }

    @Override
    public TimedNamedOperation1 next() {
        return new TimedNamedOperation1(startTimes.next(), dependencyTimes.next(), names.next());
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}