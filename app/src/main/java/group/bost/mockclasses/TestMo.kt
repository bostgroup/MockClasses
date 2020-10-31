package group.bost.mockclasses

data class TestMo(
    val str: String = "",
    val int: Int = 0,
    val dbl: Double = 0.0,
    val float: Float = 0f,
    val char: Char = 'a',
    var list: List<Test1Mo> = listOf()
)