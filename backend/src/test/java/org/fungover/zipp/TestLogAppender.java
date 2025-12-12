package org.fungover.zipp;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.ArrayList;
import java.util.List;

public class TestLogAppender extends AppenderBase<ILoggingEvent> {

    private final List<ILoggingEvent> logs = new ArrayList<ILoggingEvent>();
    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        logs.add(iLoggingEvent);
    }

    public List<ILoggingEvent> getLogs() {
        return logs;
    }
}
