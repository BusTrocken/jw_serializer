package de.serializer.jk

import java.io.File
import java.io.OutputStream
import java.lang.reflect.Type

class JkSerializer {

    private val fieldSerializers = listOf(
        BooleanFieldSerializer,
        IntFieldSerializer,
        StringFieldSerializer,
        ObjectFieldSerializer,
        ListFieldSerializer,
    )

    fun serialize(file: File, value: Any) {
        file.outputStream().use { outputStream ->
            serialize(outputStream, value)
        }
    }

    fun serialize(outputStream: OutputStream, value: Any?) {
        val writer = DataWriter(outputStream)

        writer.write(MAGIC_NUMBER)

        val type = if (value != null) value::class.java else Nothing::class.java
        write(type, value, writer)
    }

    private val sink = FieldSerializer.Sink { type, value, writer -> write(type, value, writer) }

    private fun write(type: Type, value: Any?, writer: DataWriter) {
        if (value == null) {
            writer.writeByte(TypeTags.NULL)
            return
        }

        for (fieldSerializer in fieldSerializers) {
            if (fieldSerializer.supports(type)) {
                fieldSerializer.write(type, value, writer, sink)
                return
            }
        }

        error("no field serializer found for type $type and value $value")
    }
}

interface FieldSerializer {

    fun supports(type: Type): Boolean
    fun write(type: Type, value: Any, writer: DataWriter, sink: Sink)

    fun interface Sink {
        fun write(type: Type, value: Any?, writer: DataWriter)
    }
}

object BooleanFieldSerializer : FieldSerializer {

    override fun supports(type: Type): Boolean = type == Boolean::class.javaPrimitiveType || type == Boolean::class.javaObjectType

    override fun write(type: Type, value: Any, writer: DataWriter, sink: FieldSerializer.Sink) {
        value as Boolean

        writer.writeByte(TypeTags.BOOLEAN)
        writer.writeBoolean(value)
    }
}

object IntFieldSerializer : FieldSerializer {

    override fun supports(type: Type): Boolean = type == Int::class.javaPrimitiveType || type == Int::class.javaObjectType

    override fun write(type: Type, value: Any, writer: DataWriter, sink: FieldSerializer.Sink) {
        value as Int

        writer.writeByte(TypeTags.INT)
        writer.writeInt(value)
    }
}

object StringFieldSerializer : FieldSerializer {

    override fun supports(type: Type): Boolean = type == String::class.java

    override fun write(type: Type, value: Any, writer: DataWriter, sink: FieldSerializer.Sink) {
        value as String

        writer.writeByte(TypeTags.STRING)
        writer.writeString(value)
    }
}

object ObjectFieldSerializer : FieldSerializer {

    override fun supports(type: Type): Boolean = type is Class<*>

    override fun write(type: Type, value: Any, writer: DataWriter, sink: FieldSerializer.Sink) {
        type as Class<*>

        val fields = type.allFields

        writer.writeByte(TypeTags.OBJECT)
        writer.writeVarInt(fields.size)
        for (field in fields) {
            val fieldClazz = field.type
            val fieldValue = field.get(value)

            writer.writeString(field.name)
            sink.write(fieldClazz, fieldValue, writer)
        }
    }
}

object ListFieldSerializer : FieldSerializer {

    override fun supports(type: Type): Boolean = type is Class<*> && Collection::class.java.isAssignableFrom(type)

    override fun write(type: Type, value: Any, writer: DataWriter, sink: FieldSerializer.Sink) {
        type as Class<*>

        val collection = value as Collection<*>

        writer.writeByte(TypeTags.ARRAY)
        writer.writeVarInt(collection.size)
        for (element in collection) {
            val elementType = if (element != null) element::class.java else Nothing::class.java
            sink.write(elementType, element, writer)
        }
    }
}
