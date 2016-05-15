package si.virag.promet

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import si.virag.promet.model.TrafficData
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var trafficData : TrafficData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PrometApplication.graph.inject(this)

        trafficData.getTrafficEvents()
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe()
    }

}