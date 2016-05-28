package si.virag.promet

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import si.virag.promet.model.TrafficData
import si.virag.promet.settings.PrometSettings
import javax.inject.Singleton

@Module
class PrometModule(private val application: Application) {

    @Provides
    @Singleton
    fun provideApplicationContext() : Context {
        return application
    }

    @Provides
    @Singleton
    fun provideTrafficeData() : TrafficData {
        return TrafficData(application)
    }

    @Provides
    @Singleton
    fun provideSettings() : PrometSettings {
        return PrometSettings(application)
    }

}
