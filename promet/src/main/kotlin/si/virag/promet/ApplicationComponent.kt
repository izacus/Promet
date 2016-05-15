package si.virag.promet

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(PrometModule::class))
interface ApplicationComponent {

    fun inject(application: PrometApplication)
    fun inject(mainActivity: MainActivity)
}