package group.bost.mockclasses

import com.google.gson.Gson
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

class MockClass<T : Any>(val obj: T) {

    fun get(): T {
        return get(obj)
    }

    private fun <T : Any> get(obj: T): T {
        val objClass = obj::class.java
        val fields = try {
            (obj as Class<*>).declaredFields
        } catch (e: Throwable) {
            objClass.declaredFields
        }
//        val fields = objClass.declaredFields
        fields.forEachIndexed { index, field ->
            field.isAccessible = true
            when (field.get(obj)) {
                is String -> field.set(obj, field.name)
                is Int -> field.set(obj, Random(10).nextInt())
                is Float -> field.set(obj, Random(10).nextFloat())
                is Double -> field.set(obj, Random(10).nextDouble())
                is Char -> field.set(obj, index.toChar())
//                is List<*> -> field.set(obj, getCollectionMock(field))
                else -> get(field.get(obj))
            }
        }
        return obj
    }

    private fun getObj(type: Type): Any? {
        return when (type) {
            String::class.java -> ('a'..'z').random()
            Int::class.java -> Random(10).nextInt()
            Float::class.java -> Random(10).nextFloat()
            Double::class.java -> Random(10).nextDouble()
            Char::class.java -> Random(10).nextInt().toChar()
            else -> get(type as Class<*>)
        }
    }

    private fun getCollectionMock(obj: Field): List<*> {
        val type = obj.genericType
        val pType = type as ParameterizedType
        val classType = pType.actualTypeArguments[0]
        return listOf(get(classType as Class<Any>))
    }

    fun toJson(): String {
        return Gson().toJson(get())
    }

}