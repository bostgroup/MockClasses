package group.bost.mockclasses

import com.google.gson.Gson
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.math.pow
import kotlin.reflect.full.declaredMemberProperties

class MockClass<T : Any>(private val obj: Class<T>) {

    fun get(): T {
        return get(obj.newInstanceMock())
    }

    fun getWithNullable(cb: (T) -> Unit) {
        val instanceObj = obj.newInstanceMock()

        val countNullableFields = instanceObj.countNullableFields()

        val rows = 2.0.pow(countNullableFields).toInt()
        val isNullableList = mutableListOf<Boolean>()
        for (i in 0 until rows) {
            for (j in countNullableFields - 1 downTo 0) {
                val isNullable = ((i / 2.0.pow(j)) % 2).toInt() == 1
                isNullableList.add(isNullable)
            }
            cb.invoke(get(instanceObj, isNullableList))
            isNullableList.clear()
        }

    }

    private fun Class<T>.newInstanceMock() = if (this.constructors.isEmpty()) {
        get(this.newInstance())
    } else {
        val constructor = obj.getConstructor(*obj.constructors[0].parameterTypes)
        val constructorParams = obj.constructors[0].parameterTypes.map { getObj(it) }.toTypedArray()
        get(constructor.newInstance(*constructorParams))
    }

    private fun <T : Any> get(obj: T, isNullable: MutableList<Boolean>): T {
        val objClass = obj::class.java
        val fields = objClass.declaredFields
        var nullableIndex = 0
        fields.forEachIndexed { index, field ->
            field.isAccessible = true

            if (obj.isNullable(field.name)) {
                when (field.get(obj)) {
                    is String -> field.set(obj, if (isNullable[nullableIndex]) null else field.name)
                    is Int -> field.set(
                        obj,
                        if (isNullable[nullableIndex]) null else index
                    )
                    is Float -> field.set(
                        obj,
                        if (isNullable[nullableIndex]) null else index.toFloat()
                    )
                    is Double -> field.set(
                        obj,
                        if (isNullable[nullableIndex]) null else index.toDouble()
                    )
                    is Char -> field.set(
                        obj,
                        if (isNullable[nullableIndex]) null else index.toChar()
                    )
                    is List<*> -> field.set(
                        obj,
                        if (isNullable[nullableIndex]) null else getCollectionMock(field)
                    )
                    else -> if (!isNullable[nullableIndex]) get(field.get(obj))
                }
                nullableIndex++
            } else {
                when (field.get(obj)) {
                    is String -> field.set(obj, field.name)
                    is Int -> field.set(obj, index)
                    is Float -> field.set(obj, index.toFloat())
                    is Double -> field.set(obj, index.toDouble())
                    is Char -> field.set(obj, index.toChar())
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

        fields.forEachIndexed { index, field ->
            field.isAccessible = true
            when (field.get(obj)) {
                is String -> field.set(obj, field.name)
                is Int -> field.set(obj, index)
                is Float -> field.set(obj, index.toFloat())
                is Double -> field.set(obj, index.toDouble())
                is Char -> field.set(obj, index.toChar())
                is List<*> -> field.set(obj, getCollectionMock(field))
                else -> get(field.get(obj))
            }
        }
        return obj
    }

    private fun getObj(type: Type): Any? {
        return when (type) {
            String::class.java -> (type as Class<*>).simpleName
            Int::class.java, java.lang.Integer::class.java -> 1
            Float::class.java, java.lang.Float::class.java -> 1.toFloat()
            Double::class.java, java.lang.Double::class.java -> 1.toDouble()
            Char::class.java, java.lang.Character::class.java -> 1.toChar()
            List::class.java -> emptyList<Any>()
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
