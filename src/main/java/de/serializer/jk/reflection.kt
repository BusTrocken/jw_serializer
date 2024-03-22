package de.serializer.jk

import java.lang.reflect.Field

val Class<*>.allFields: List<Field>
    get() {
        val fields = mutableListOf<Field>()
        collectFields(this, fields)
        fields.forEach { it.isAccessible = true }
        return fields
    }

private fun collectFields(clazz: Class<*>, fields: MutableList<Field>) {
    fields.addAll(clazz.declaredFields)
    val superclass = clazz.superclass
    if (superclass != null) collectFields(superclass, fields)
}
