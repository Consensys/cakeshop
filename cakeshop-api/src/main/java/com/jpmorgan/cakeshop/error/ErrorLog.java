package com.jpmorgan.cakeshop.error;

import com.jpmorgan.cakeshop.util.StringUtils;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.FastDateFormat;

public class ErrorLog {

    public static final FastDateFormat tsFormatter = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss,S");

    public final long ts;
    public final long nanos;
    public final Object err;

    public ErrorLog(Object err) {
        this.nanos = System.nanoTime();
        this.ts = System.currentTimeMillis();
        this.err = err;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        out.append("[" + tsFormatter.format(ts) + "] ");
        if (err instanceof String) {
            out.append(err);
        } else if (err instanceof Throwable) {
            out.append(ExceptionUtils.getStackTrace((Throwable) err));
        } else {
            out.append(StringUtils.toString(err));
        }
        return out.toString();
    }

}