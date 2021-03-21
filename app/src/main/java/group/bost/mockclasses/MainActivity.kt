package group.bost.mockclasses

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRun.setOnClickListener {
//            tvLog.text = MockClass(TestMo::class.java).get().toJson()
//            MockClass(Test1Mo::class.java).getWithNullable {
//                Log.d("qweqweqwe", "onCreate: "+it.toJson())
//            }

            tvLog.text = MockClass(TestMo::class.java).get().toJson()

        }
    }

}
