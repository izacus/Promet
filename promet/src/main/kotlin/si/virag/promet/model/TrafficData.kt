package si.virag.promet.model

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.threeten.bp.LocalDateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import rx.schedulers.Schedulers
import si.virag.promet.model.data.*

class TrafficData(val context: Context) {

    inline fun <reified T> genericType() = object: TypeToken<T>() {}.type

    /** Opendata Promet API **/
    private val trafficApi : OpenDataTrafficApi;

    init {
        val gson = GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapter(genericType<EventGroup>(), EventGroupTypeAdapter())
                    .registerTypeAdapter(genericType<LocalDateTime>(), EpochDateTypeAdapter())
                    .registerTypeAdapter(genericType<FunnyDouble>(), FunnyDoubleAdapter())
                    .registerTypeAdapter(genericType<TrafficStatus>(), TrafficStatusTypeAdapter())
                    .create()

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

        val retrofit = Retrofit.Builder()
                        .baseUrl("http://www.opendata.si")
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                        .client(client)
                        .build()


        trafficApi = retrofit.create(OpenDataTrafficApi::class.java)
    }


    fun getTrafficEvents() : Observable<TrafficEvent> {
        return trafficApi.getTrafficEvents().flatMap { Observable.from(it.events()) }
    }

    fun getTrafficCounters() : Observable<TrafficCounter> {
        return trafficApi.getTrafficCounters().flatMap { Observable.from(it.counters()) }
    }

}