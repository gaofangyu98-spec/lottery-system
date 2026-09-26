package com.example.lotterysystem.common.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * 日志级别过滤器（Logback）
 * <p>只放行 INFO 级别日志，其余级别（DEBUG/WARN/ERROR 等）一律拒绝，
 * 用于在 logback 配置中单独把 INFO 日志输出到指定文件。</p>
 */
public class InfoLevelFilter extends Filter<ILoggingEvent> {
    @Override
    public FilterReply decide(ILoggingEvent iLoggingEvent) {
        if (iLoggingEvent.getLevel().toInt() == Level.INFO.toInt()) {
            return FilterReply.ACCEPT;
        }
        return FilterReply.DENY;
    }
}