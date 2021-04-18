package group.bost.mockclasses

import android.util.Log
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)

        MockClass(Test1Mo::class.java).getWithNullable {
            Log.d("qweqweqwe", "onCreate: " + it.toJson())
        }

    }
}
