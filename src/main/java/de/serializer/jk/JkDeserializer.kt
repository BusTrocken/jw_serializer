package de.serializer.jk

import java.io.File
import java.io.InputStream
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.math.sin

class JkDeserializer {

    private val fieldDeserializers = listOf(
        BooleanFieldDeserializer,
        IntFieldDeserializer,
        StringFieldDeserializer,
        CollectionFieldDeserializer,
        ObjectFieldDeserializer,
    )

    fun <T> deserialize(file: File, type: Type): T {
        return file.inputStream().use { inputStream ->
            deserialize(inputStream, type)
        }
    }

    fun <T> deserialize(inputStream: InputStream, type: Class<T>): T {
        return deserialize(inputStream, type as Type)
    }

    fun <T> deserialize(inputStream: InputStream, type: Type): T {
        val reader = DataReader(inputStream)

        val magicNumber = reader.read(MAGIC_NUMBER.size)
        require(magicNumber.contentEquals(MAGIC_NUMBER)) { "malformed format" }

        @Suppress("UNCHECKED_CAST")
        return read(type, reader) as T
    }

    private val sink = FieldDeserializer.Sink { type, reader -> read(type, reader) }

    private fun read(type: Type, reader: DataReader): Any? {
        val tag = reader.readByte()
        if (tag == TypeTags.NULL) {
            return null
        }

        for (fieldDeserializer in fieldDeserializers) {
            if (fieldDeserializer.supports(type)) {
                return fieldDeserializer.read(type, tag, reader, sink)
            }
        }

        error("no field deserializer found for type $type and tag $tag")
    }
}

interface FieldDeserializer {

    fun supports(type: Type): Boolean
    fun read(type: Type, tag: Int, reader: DataReader, sink: Sink): Any

    fun interface Sink {
        fun read(type: Type, reader: DataReader): Any?
    }
}

object BooleanFieldDeserializer : FieldDeserializer {
    override fun supports(type: Type): Boolean = type == Boolean::class.javaPrimitiveType || type == Boolean::class.javaObjectType

    override fun read(type: Type, tag: Int, reader: DataReader, sink: FieldDeserializer.Sink): Any {
        check(tag == TypeTags.BOOLEAN)

        return reader.readBoolean()
    }
}

object IntFieldDeserializer : FieldDeserializer {
    override fun supports(type: Type): Boolean = type == Int::class.javaPrimitiveType || type == Int::class.javaObjectType

    override fun read(type: Type, tag: Int, reader: DataReader, sink: FieldDeserializer.Sink): Any {
        check(tag == TypeTags.INT)

        return reader.readInt()
    }
}

object StringFieldDeserializer : FieldDeserializer {
    override fun supports(type: Type): Boolean = type == String::class.java

    override fun read(type: Type, tag: Int, reader: DataReader, sink: FieldDeserializer.Sink): Any {
        check(tag == TypeTags.STRING)

        return reader.readString()
    }
}

object ObjectFieldDeserializer : FieldDeserializer {
    override fun supports(type: Type): Boolean = type is Class<*>

    override fun read(type: Type, tag: Int, reader: DataReader, sink: FieldDeserializer.Sink): Any {
        check(tag == TypeTags.OBJECT)
        type as Class<*>

        val constructor = type.getDeclaredConstructor()
        constructor.isAccessible = true
        val value = constructor.newInstance()

        val fieldsLookup = type.allFields.associateBy { it.name }

        repeat(reader.readVarInt()) {
            val fieldName = reader.readString()
            val field = fieldsLookup[fieldName] ?: error("field $fieldName does not exist in class $type")

            val fieldType = field.genericType
            val fieldValue = sink.read(fieldType, reader)

            field.set(value, fieldValue)
        }


        return value
    }
}

object CollectionFieldDeserializer : FieldDeserializer {

    override fun supports(type: Type): Boolean = type is ParameterizedType && Collection::class.java.isAssignableFrom(type.rawType as Class<*>)

    override fun read(type: Type, tag: Int, reader: DataReader, sink: FieldDeserializer.Sink): Any {
        check(tag == TypeTags.ARRAY)
        type as ParameterizedType

        val collection = createCollection(type.rawType as Class<*>)

        repeat(reader.readVarInt()) {
            val element = sink.read(type.actualTypeArguments[0], reader)

            collection.add(element)
        }

        return collection
    }

    private fun createCollection(type: Class<*>): MutableCollection<Any?> {
        return when {
            List::class.java.isAssignableFrom(type) -> mutableListOf()
            Set::class.java.isAssignableFrom(type) -> mutableListOf()
            else -> error("cannot deserialize collection $type")
        }
    }
}



