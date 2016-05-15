package si.virag.promet

import android.app.Application

class PrometApplication : Application() {

    companion object {
        @JvmStatic lateinit var graph : ApplicationComponent
    }

    override fun onCreate() {
        super.onCreate();

        // Prepare Dagger graph
        graph = DaggerApplicationComponent.builder().prometModule(PrometModule(this)).build()
        graph.inject(this)
    }

}