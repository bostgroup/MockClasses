package group.bost.mockclasses

import android.util.Log
import com.google.gson.Gson
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*
import kotlin.math.pow
import kotlin.reflect.full.declaredMemberProperties

class MockClass<T : Any>(private val obj: Class<T>) {

    private val random = Random()

    fun get(): T {
        return get(obj.newInstance())
    }

    fun getWithNullable(cb: (T) -> Unit) {
        val countNullableFields = obj.newInstance().countNullableFields()

        val rows = 2.0.pow(countNullableFields).toInt()
        val isNullableList = mutableListOf<Boolean>()
        for (i in 0 until rows) {
            for (j in countNullableFields - 1 downTo 0) {
                val isNullable = ((i / 2.0.pow(j)) % 2).toInt() == 1
                isNullableList.add(isNullable)
            }
            cb.invoke(get(obj.newInstance(), isNullableList))
            isNullableList.clear()
        }

    }

    private fun <T : Any> get(obj: T, isNullable: MutableList<Boolean>): T {
        val objClass = obj::class.java
        val fields = objClass.declaredFields
        var nullableIndex = 0
        fields.forEachIndexed { _, field ->
            field.isAccessible = true

            if (obj.isNullable(field.name)) {
                when (field.get(obj)) {
                    is String -> field.set(obj, if (isNullable[nullableIndex]) null else field.name)
                    is Int -> field.set(obj, if (isNullable[nullableIndex]) null else random.nextInt())
                    is Float -> field.set(obj, if (isNullable[nullableIndex]) null else random.nextFloat())
                    is Double -> field.set(obj, if (isNullable[nullableIndex]) null else random.nextDouble())
                    is Char -> field.set(obj, if (isNullable[nullableIndex]) null else random.nextInt().toChar())
                    is List<*> -> field.set(obj, if (isNullable[nullableIndex]) null else getCollectionMock(field))
                    else -> if (!isNullable[nullableIndex]) get(field.get(obj))
                }
                nullableIndex++
            } else {
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
        }
        return obj
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

    private fun Any.isNullable(fieldName: String) =
        this::class.declaredMemberProperties.find { it.name == fieldName }!!.returnType.isMarkedNullable


    private fun Any.countNullableFields() =
        this::class.declaredMemberProperties.count { it.returnType.isMarkedNullable }

}

fun Any.toJson() = Gson().toJson(this)!!