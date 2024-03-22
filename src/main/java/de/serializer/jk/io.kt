package de.serializer.jk

import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.*

class DataWriter(private val outputStream: OutputStream) {

    fun write(byte: Int) {
        outputStream.write(byte)
    }

    fun write(bytes: ByteArray) {
        outputStream.write(bytes, 0, bytes.size)
    }

    fun write(bytes: ByteArray, offset: Int, length: Int) {
        outputStream.write(bytes, offset, length)
    }

    fun writeByte(value: Int) {
        write(value)
    }

    fun writeBoolean(value: Boolean) {
        write(if (value) 1 else 0)
    }

    fun writeInt(value: Int) {
        write(value ushr 24)
        write((value shr 16) and 0xFF)
        write((value shr 8) and 0xFF)
        write(value and 0xFF)
    }

    fun writeVarInt(value: Int) {
        var i = value
        while (i and -0x80 != 0) {
            write(i and 0x7F or 0x80)
            i = i ushr 7
        }

        write(i)
    }

    fun writeLong(value: Long) {
        write((value ushr 56).toInt())
        write(((value shr 48) and 0xFF).toInt())
        write(((value shr 40) and 0xFF).toInt())
        write(((value shr 32) and 0xFF).toInt())
        write(((value shr 24) and 0xFF).toInt())
        write(((value shr 16) and 0xFF).toInt())
        write(((value shr 8) and 0xFF).toInt())
        write((value and 0xFF).toInt())
    }

    fun writeFloat(value: Float) {
        writeInt(value.toRawBits())
    }

    fun writeDouble(value: Double) {
        writeLong(value.toRawBits())
    }

    fun writeUUID(value: UUID) {
        writeLong(value.mostSignificantBits)
        writeLong(value.leastSignificantBits)
    }

    fun writeString(value: String) {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        writeByteArray(bytes)
    }

    fun writeByteArray(bytes: ByteArray) {
        writeVarInt(bytes.size)
        write(bytes)
    }
}

class DataReader(private val inputStream: InputStream) {

    fun read(): Int {
        val byte = inputStream.read()
        if (byte == -1) throw EOFException()
        return byte
    }

    fun read(count: Int): ByteArray {
        val bytes = ByteArray(count)
        read(bytes, 0, bytes.size)
        return bytes
    }

    fun read(bytes: ByteArray) {
        read(bytes, 0, bytes.size)
    }

    fun read(bytes: ByteArray, offset: Int, length: Int) {
        var read = 0
        while (read < length) {
            val remaining = length - read
            val n = inputStream.read(bytes, offset + read, remaining)
            if (n == -1) throw EOFException()
            read += n
        }
    }

    fun readByte(): Int {
        return read()
    }

    fun readBoolean(): Boolean {
        return read() != 0
    }

    fun readInt(): Int {
        return (read() shl 24) or
                (read() shl 16) or
                (read() shl 8) or
                (read())
    }

    fun readVarInt(): Int {
        var result = 0
        var readBytes = 0

        var b: Int
        do {
            b = read()
            result = result or ((b and 0x7F) shl (readBytes++ * 7))

            if (readBytes > 5) {
                throw IllegalStateException("VarInt too big")
            }
        } while (b and 0x80 == 0x80)

        return result
    }

    fun readLong(): Long {
        return (read().toLong() shl 56) or
                (read().toLong() shl 48) or
                (read().toLong() shl 40) or
                (read().toLong() shl 32) or
                (read().toLong() shl 24) or
                (read().toLong() shl 16) or
                (read().toLong() shl 8) or
                (read().toLong())
    }

    fun readFloat(): Float {
        val value = readInt()
        return Float.fromBits(value)
    }

    fun readDouble(): Double {
        val value = readLong()
        return Double.fromBits(value)
    }

    fun readString(): String {
        val bytes = readByteArray()
        return String(bytes, StandardCharsets.UTF_8)
    }

    fun readUUID(): UUID {
        return UUID(readLong(), readLong())
    }

    fun readByteArray(): ByteArray {
        val length = readVarInt()
        val bytes = ByteArray(length)
        read(bytes)
        return bytes
    }
}

