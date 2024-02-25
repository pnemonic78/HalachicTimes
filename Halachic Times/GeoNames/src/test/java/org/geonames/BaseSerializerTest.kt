package org.geonames

import java.io.IOException
import java.io.InputStream

abstract class BaseSerializerTest {
    @Throws(IOException::class)
    fun openFile(name: String): InputStream? {
        val clazz = this.javaClass
        return clazz.getResourceAsStream(name)
    }
}