package com.kelvsyc.kotlin.commons.lang

import org.apache.commons.lang3.EnumUtils

/**
 * Returns `true` if there is an enum constant with the specified name.
 */
inline fun <reified E : Enum<E>> isValidEnum(value: String, ignoreCase: Boolean = false): Boolean = if (ignoreCase) {
    EnumUtils.isValidEnumIgnoreCase(E::class.java, value)
} else {
    EnumUtils.isValidEnum(E::class.java, value)
}

/**
 * Gets an enum value for the specified name.
 *
 * @return The enum value with the specified name, or `null` if no such value exists.
 */
inline fun <reified E : Enum<E>> enumValueOfOrNull(value: String, ignoreCase: Boolean = false): E? = if (ignoreCase) {
    EnumUtils.getEnumIgnoreCase(E::class.java, value)
} else {
    EnumUtils.getEnum(E::class.java, value)
}

/**
 * Gets an enum value for the specified name, or a default value if no such value exists.
 */
inline fun <reified E : Enum<E>> enumValueOfOrDefault(value: String, ignoreCase: Boolean = false, defaultValue: E): E = if (ignoreCase) {
    EnumUtils.getEnumIgnoreCase(E::class.java, value, defaultValue)
} else {
    EnumUtils.getEnum(E::class.java, value, defaultValue)
}
