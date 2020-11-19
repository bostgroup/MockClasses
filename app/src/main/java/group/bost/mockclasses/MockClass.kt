package group.bost.mockclasses

import com.google.gson.Gson
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

class MockClass<T : Any>(private val obj: Class<T>) {

    private val random = Random()

    fun get(): T {
        return get(obj.newInstance())
    }

    private fun <T : Any> get(obj: T): T {
        val objClass = obj::class.java
        val fields = objClass.declaredFields

        fields.forEachIndexed { _, field ->
            field.isAccessible = true
            when (field.get(obj)) {
                is String -> field.set(obj, field.name)
                is Int -> field.set(obj, random.nextInt())
                is Float -> field.set(obj, random.nextFloat())
                is Double -> field.set(obj, random.nextDouble())
                is Char -> field.set(obj, random.nextInt().toChar())
                is List<*> -> field.set(obj, getCollectionMock(field))
                else -> get(field.get(obj))
            }
        }
        return obj
    }

    private fun getObj(type: Type): Any? {
        return when (type) {
            String::class.java -> ('a'..'z').random()
            Int::class.java -> random.nextInt()
            Float::class.java -> random.nextFloat()
            Double::class.java -> random.nextDouble()
            Char::class.java -> random.nextInt().toChar()
            else -> get((type as Class<*>).newInstance())
        }
    }

    private fun getCollectionMock(obj: Field): List<*> {
        val type = obj.genericType
        val pType = type as ParameterizedType
        val classType = pType.actualTypeArguments[0]
        return listOf(getObj(classType as Class<*>))
    }

}

fun Any.toJson() = Gson().toJson(this)!!