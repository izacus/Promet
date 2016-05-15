package si.virag.promet.model

import retrofit2.http.GET
import rx.Observable
import si.virag.promet.model.data.TrafficCounters
import si.virag.promet.model.data.TrafficEvents

interface OpenDataTrafficApi {

    @GET("/promet/events/")
    fun getTrafficEvents() : Observable<TrafficEvents>

    @GET("/promet/counters/")
    fun getTrafficCounters() : Observable<TrafficCounters>
}