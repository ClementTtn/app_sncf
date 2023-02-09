package raw

import android.os.Parcel
import android.os.Parcelable
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.CountDownLatch

class train(
    private val num: Int,
    private val type: TypeTrain,
    private val localHour: String,
    private val localMinute: String,
    private val vehicle_journey: String): Parcelable{

    private var from: stop? = null;
    private var to: stop? = null;
    private var body :JSONObject? = null;
    private val client = OkHttpClient()
    private val latch = CountDownLatch(1)
    private var stopListe: ArrayList<stop> = ArrayList<stop>()

    fun getNum(): Int{
        return num
    }

    fun getType(): TypeTrain{
        return type
    }

    fun getFrom(): stop?{
        return from
    }

    fun getTo(): stop?{
        return to
    }

    fun getLocalHour(): String{
        return localHour
    }

    fun getLocalMinute(): String{
        return localMinute
    }

    fun getStopBody(): JSONObject?{
        return body
    }

    fun getStops(): ArrayList<stop>{
        return stopListe
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        TypeTrain.valueOf(parcel.readString()!!),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
        from = parcel.readParcelable(stop::class.java.classLoader)
        to = parcel.readParcelable(stop::class.java.classLoader)
        parcel.readList(stopListe, stop::class.java.classLoader)
    }

    fun addStop(stop: stop, departureStation: Boolean, arrivalStation: Boolean){
        if (departureStation){
            from = stop
        }
        else if(arrivalStation){
            to = stop
        }
        else{
            stopListe.add(stop)
        }
    }

    fun run(){
        val credential = Credentials.basic("", "")
        val request = Request.Builder().url("https://api.sncf.com/v1/coverage/sncf/vehicle_journeys/$vehicle_journey/?count=8").header("Authorization", credential).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                latch.countDown()
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    body = JSONObject(response.body!!.string())
                }
                else {
                    println("Request failed with code ${response.code}")
                }
                latch.countDown()
            }
        })
        latch.await()
        this.API_stop()
    }

    private fun API_stop() {
        if (body != null) {
            val vehicle_journeys = body!!.getJSONArray("vehicle_journeys").getJSONObject(0).getJSONArray("stop_times")
            for (i in 0 until vehicle_journeys.length()) {
                val stopJson = vehicle_journeys.getJSONObject(i)
                val heureArrivee = stopJson.getString("arrival_time").substring(0,2)
                val minuteArrivee = stopJson.getString("arrival_time").substring(2,4)
                val heureDepart = stopJson.getString("departure_time").substring(0,2)
                val minuteDepart = stopJson.getString("departure_time").substring(2,4)
                val codeUIC = stopJson.getJSONObject("stop_point").getString("id").split(":")[2].toInt()
                val nomStop = stopJson.getJSONObject("stop_point").getString("name")
                val lon = stopJson.getJSONObject("stop_point").getJSONObject("coord").getDouble("lon")
                val lat = stopJson.getJSONObject("stop_point").getJSONObject("coord").getDouble("lat")
                val station = station(codeUIC, nomStop, lon, lat)
                val stop = stop(heureArrivee, minuteArrivee, heureDepart, minuteDepart, station)

                if (i == 0){
                    addStop(stop, true, false);
                }
                else if (i == vehicle_journeys.length() -1 ){
                    addStop(stop, false, true);
                }
                else{
                    addStop(stop, false, false);
                }

            }
        }
    }

    override fun toString(): String{
        return "${localHour}h${localMinute} - $to \n$type $num"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(num)
        parcel.writeString(type.name)
        parcel.writeString(localHour)
        parcel.writeString(localMinute)
        parcel.writeString(vehicle_journey)
        parcel.writeParcelable(from, flags)
        parcel.writeParcelable(to, flags)
        parcel.writeList(stopListe)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<train> {
        override fun createFromParcel(parcel: Parcel): train {
            return train(parcel)
        }

        override fun newArray(size: Int): Array<train?> {
            return arrayOfNulls(size)
        }
    }
}