package com.jpmorgan.cakeshop.util;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ShortToStringStyle extends ToStringStyle {

    public static final ToStringStyle INSTANCE = new ShortToStringStyle();

    private static final long serialVersionUID = 1L;

    ShortToStringStyle() {
        super();
        this.setContentStart("[");
        this.setFieldSeparator(SystemUtils.LINE_SEPARATOR + "  ");
        this.setFieldSeparatorAtStart(true);
        this.setContentEnd(SystemUtils.LINE_SEPARATOR + "]");
        this.setUseShortClassName(true);
    }

    /**
     * <p>Ensure <code>Singleton</code> after serialization.</p>
     *
     * @return the singleton
     */
    private Object readResolve() {
        return ToStringStyle.MULTI_LINE_STYLE;
    }

}
