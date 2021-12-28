package group.bost.mockclasses

import com.google.gson.Gson
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.math.pow
import kotlin.reflect.full.declaredMemberProperties

class MockClass<T : Any>(private val obj: Class<T>) {

    private val customValues = mutableMapOf<String, Any>()
    private val exceptionFields = mutableListOf<String>()

    fun <FN : Any> setCustomParam(fieldName: String, value: FN): MockClass<T> {
        customValues[fieldName] = value
        return this
    }

    fun setExceptionField(fieldName: String): MockClass<T> {
        exceptionFields.add(fieldName)
        return this
    }

    fun setExceptionClassFields(fieldName: Class<*>): MockClass<T> {
        exceptionFields.addAll(
            fieldName.declaredFields.map {
                it.name
            }
        )
        return this
    }

    fun build(): T {
        return build(obj.newInstanceMock())
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
            cb.invoke(build(instanceObj, isNullableList))
            isNullableList.clear()
        }

    }

    private fun Class<T>.newInstanceMock() = if (this.constructors.isEmpty()) {
        build(newInstance())
    } else {
        val constructor = obj.getConstructor(*obj.constructors[0].parameterTypes)
        val constructorParams = obj.constructors[0].parameterTypes.map { getObj(it) }.toTypedArray()
        build(constructor.newInstance(*constructorParams))
    }

    private fun <T : Any> build(obj: T, isNullable: MutableList<Boolean>): T {
        val objClass = obj::class.java
        val fields = objClass.declaredFields
        var nullableIndex = 0
        fields.forEachIndexed { index, field ->
            field.isAccessible = true

            if (obj.isNullable(field.name)) {
                when (field.type) {
                    String::class.java -> field.set(
                        obj,
                        if (isNullable[nullableIndex]) null else field.name
                    )
                    Int::class.java, java.lang.Integer::class.java -> field.set(
                        obj,
                        if (isNullable[nullableIndex]) null else index
                    )
                    Float::class.java, java.lang.Float::class.java -> field.set(
                        obj,
                        if (isNullable[nullableIndex]) null else index.toFloat()
                    )
                    Double::class.java, java.lang.Double::class.java -> field.set(
                        obj,
                        if (isNullable[nullableIndex]) null else index.toDouble()
                    )
                    Char::class.java, java.lang.Character::class.java -> field.set(
                        obj,
                        if (isNullable[nullableIndex]) null else index.toChar()
                    )
                    List::class.java -> field.set(
                        obj,
                        if (isNullable[nullableIndex]) null else getCollectionMock(field)
                    )
                    else -> if (!isNullable[nullableIndex]) build(field.get(obj))
                }
                nullableIndex++
            } else {
                when (field.type) {
                    String::class.java -> field.set(
                        obj,
                        customValues.getOrDefault<String>(field.name, field.name)
                    )
                    Int::class.java, java.lang.Integer::class.java -> field.set(obj, index)
                    Float::class.java, java.lang.Float::class.java -> field.set(
                        obj,
                        index.toFloat()
                    )
                    Double::class.java, java.lang.Double::class.java -> field.set(
                        obj,
                        index.toDouble()
                    )
                    Char::class.java, java.lang.Character::class.java -> field.set(
                        obj,
                        index.toChar()
                    )
                    List::class.java -> field.set(obj, getCollectionMock(field))
                    else -> build(field.get(obj))
                }
            }
        }
        return obj
    }

    private fun <T : Any> build(obj: T): T {
        val objClass = obj::class.java
        val fields = objClass.declaredFields

        fields.forEachIndexed fields@{ index, field ->
            field.isAccessible = true
            when (field.type) {
                String::class.java -> {
                    field.setParam(obj, "")
                }
                Int::class.java, java.lang.Integer::class.java -> {
                    field.setParam(obj, index)
                }
                Float::class.java, java.lang.Float::class.java -> {
                    field.setParam(obj, index.toFloat())
                }
                Double::class.java, java.lang.Double::class.java -> {
                    field.setParam(obj, index.toDouble())
                }
                Char::class.java, java.lang.Character::class.java -> {
                    field.setParam(obj, index.toChar())
                }
                List::class.java -> {
                    field.setParam(obj, getCollectionMock(field))
                }
                else -> build(field.get(obj))
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
            else -> build((type as Class<*>).newInstance())
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

    private fun <DV> Map<String, Any>.getOrDefault(fieldName: String, defaultValue: DV?) =
        this[fieldName] ?: defaultValue

    private fun List<String>.hasValue(fieldName: String) =
        this.find { it == fieldName } != null


    private fun <T, V> Field.setParam(obj: T, value: V) {
        set(
            obj,
            if (exceptionFields.hasValue(name)) {
                null
            } else {
                customValues.getOrDefault(name, value)
            }
        )
    }

}

fun Any.toJson() = Gson().toJson(this)!!
