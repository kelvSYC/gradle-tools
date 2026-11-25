package com.kelvsyc.gradle.ant.shim;

import java.util.Set;

import org.apache.tools.ant.taskdefs.Property;

/**
 * Ant's {@code property} task, recording the name of every property it defines, whether or not the property was
 * already set (Ant keeps the earlier value).
 */
final class RecordingProperty extends Property {
    private final Set<String> keys;

    RecordingProperty(Set<String> keys) {
        this.keys = keys;
    }

    @Override
    protected void addProperty(String n, Object v) {
        keys.add(n);
        super.addProperty(n, v);
    }
}
