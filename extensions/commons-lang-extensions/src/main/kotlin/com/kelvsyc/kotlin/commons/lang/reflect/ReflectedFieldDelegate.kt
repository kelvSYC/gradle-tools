package com.kelvsyc.kotlin.commons.lang.reflect

import org.apache.commons.lang3.reflect.FieldUtils
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Implementation of [ReadWriteProperty], backed a field of a target object, accessed by reflection. The name of the
 * field is taken by the name of the property.
 *
 * @param target the target object
 * @param declared `true` if only the declared class of the target object should be considered, or `false` to consider
 *                  fields declared in supertypes.
 * @param forceAccess `true` if scope restrictions need to be broken, or `false` to limit to public fields.
 */
class ReflectedFieldDelegate<T>(private val target: Any, private val declared: Boolean = false, private val forceAccess: Boolean = false) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return if (declared) {
            FieldUtils.readDeclaredField(target, property.name, forceAccess) as T
        } else {
            FieldUtils.readField(target, property.name, forceAccess) as T
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (declared) {
            FieldUtils.writeDeclaredField(target, property.name, value)
        } else {
            FieldUtils.writeField(target, property.name, value)
        }
    }
}
