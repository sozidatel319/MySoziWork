package otus.homework.coroutines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    //lateinit var catsPresenter: CatsPresenter
    lateinit var catsViewModel: CatsViewModel

    private val diContainer = DiContainer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = layoutInflater.inflate(R.layout.activity_main, null) as CatsView
        setContentView(view)

        catsViewModel = CatsViewModel(diContainer.service, diContainer.avatarService)

        /*  val imageView:ImageView = view.findViewById(R.id.catAvatar)
          val textView:TextView = view.findViewById(R.id.fact_textView)*/

        catsViewModel.finalDataCats.observe(this) {
            if (it is ApiSuccess) {
                view.populate(it.data.first, it.data.second)
            } else {
                Toast.makeText(this, (it as ApiError).message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onStop() {
        if (isFinishing) {
            //  catsPresenter.detachView()
        }
        super.onStop()
    }
}