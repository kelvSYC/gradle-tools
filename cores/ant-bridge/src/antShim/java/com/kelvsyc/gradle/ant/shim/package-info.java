/**
 * The Ant shim: the only code in this core that references Ant. It is plain Java, ships in the plugin jar as
 * {@code ant-bridge-shim/**.class.bin} resources and is defined at run time by {@code AntRuntimeClassLoader}, next to the
 * consumer's Ant. Only JDK types cross its boundary, and its entry points never throw. See the core's
 * {@code INTERNALS.md}.
 */
package com.kelvsyc.gradle.ant.shim;
