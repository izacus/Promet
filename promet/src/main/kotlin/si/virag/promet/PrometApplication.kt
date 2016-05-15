package si.virag.promet

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class PrometApplication : Application() {

    companion object {
        @JvmStatic lateinit var graph : ApplicationComponent
    }

    override fun onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);

        // Prepare Dagger graph
        graph = DaggerApplicationComponent.builder().prometModule(PrometModule(this)).build()
        graph.inject(this)
    }

}