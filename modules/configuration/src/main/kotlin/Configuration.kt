package fr.valentinjdt.lib.configuration

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.nio.file.Path


class Configuration<T>(val file: Path): Serializable {

    private var obj: T? = null
    val content: T?
        get() = obj

    fun save(): Boolean {
        return obj?.let { erase(it) } == true
    }

    fun erase(content: T): Boolean = try {
        val fileOutputStream = FileOutputStream(file.toString())
        val objectOutputStream = ObjectOutputStream(fileOutputStream)
        objectOutputStream.writeObject(content)
        objectOutputStream.flush()
        objectOutputStream.close()
        true
    } catch (_: Exception) {
        false
    }

    fun load(): T? {
        val fileInputStream = FileInputStream(file.toString())
        val objectInputStream = ObjectInputStream(fileInputStream)
        obj = objectInputStream.readObject() as T
        objectInputStream.close()
        return obj
    }

}