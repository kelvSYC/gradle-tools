package com.kelvsyc.kotlin.commons.lang.reflect

import org.apache.commons.lang3.reflect.FieldUtils
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Implementation of [ReadWriteProperty], backed a static field of a target class, accessed by reflection. The name of
 * the field is taken by the name of the property.
 *
 * @param targetClass the target class
 * @param declared `true` if only the target class should be considered, or `false` to consider fields declared in
 *                  supertypes.
 * @param forceAccess `true` if scope restrictions need to be broken, or `false` to limit to public fields.
 */
class ReflectedStaticFieldDelegate<T>(private val targetClass: Class<*>, private val declared: Boolean = false, private val forceAccess: Boolean = false) : ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return if (declared) {
            FieldUtils.readDeclaredStaticField(targetClass, property.name, forceAccess) as T
        } else {
            FieldUtils.readStaticField(targetClass, property.name, forceAccess) as T
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        if (declared) {
            FieldUtils.writeDeclaredStaticField(targetClass, property.name, value, forceAccess)
        } else {
            FieldUtils.writeStaticField(targetClass, property.name, value, forceAccess)
        }
    }
}
