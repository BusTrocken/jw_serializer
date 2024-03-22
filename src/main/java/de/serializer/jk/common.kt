package de.serializer.jk

internal val MAGIC_NUMBER = byteArrayOf(0x12, 0x12, 0x12, 0x12)

internal object TypeTags {
    const val NULL = 0
    const val BOOLEAN = 1
    const val BYTE = 2
    const val INT = 3
    const val LONG = 4
    const val STRING = 5
    const val OBJECT = 6
    const val ARRAY = 7
}
