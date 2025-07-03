package org.meteorcat.fusion.configuration;

import java.io.Serial;
import java.io.Serializable;

public class Configuration implements Serializable, Cloneable, WritableConfig, ReadableConfig {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public Configuration clone() {
        try {
            return (Configuration) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public <T> WritableConfig set(ConfigOption<T> option, T value) {
        //final boolean canBePrefixMap = canBePrefixMap(option);
        //setValueInternal(option.key(), value, canBePrefixMap);
        return this;
    }
}
