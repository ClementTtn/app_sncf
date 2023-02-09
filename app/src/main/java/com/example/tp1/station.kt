package raw

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.CountDownLatch
import okhttp3.*
import okhttp3.Credentials.basic

class station (private val codeUIC: Int): Parcelable{
    private val client = OkHttpClient()
    private var body :JSONObject? = null;
    private val latch = CountDownLatch(1)
    private var name: String? = null
    private var lon: Double = 0.0
    private var lat: Double = 0.0

    constructor(codeUIC: Int, name: String?, lon: Double, lat: Double): this(codeUIC){
        this.name = name
        this.lon = lon
        this.lat = lat
    }

    fun setName(name: String?){
        this.name = name
    }

    fun getName(): String?{
        return name
    }

    fun setLongitude(lon: Double){
        this.lon = lon
    }

    fun getLongitude(): Double{
        return lon
    }

    fun setLatitude(lat: Double){
        this.lat = lat
    }

    fun getLatitude(): Double{
        return lat
    }

    fun getStationCodeUIC(): Int{
        return codeUIC
    }

    fun getStationBody(): JSONObject?{
        return body
    }

    private val stationListe : ArrayList<station> = ArrayList<station>()

    constructor(parcel: Parcel) : this(parcel.readInt()) {
        name = parcel.readString()
        lon = parcel.readDouble()
        lat = parcel.readDouble()
    }

    fun run(){
        val credential = basic("","")
        val request = Request.Builder().url("https://api.sncf.com/v1/coverage/sncf/stop_areas/stop_area:SNCF:$codeUIC/departures/?count=8").header("Authorization", credential).build()
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
                    println("Request failed with code ${response.code}")                            // Code erreur
                }
                latch.countDown()
            }
        })
        latch.await()
    }

    override fun toString(): String{
        return "$name"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(codeUIC)
        parcel.writeString(name)
        parcel.writeDouble(lon)
        parcel.writeDouble(lat)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<station> {
        override fun createFromParcel(parcel: Parcel): station {
            return station(parcel)
        }

        override fun newArray(size: Int): Array<station?> {
            return arrayOfNulls(size)
        }
    }
}