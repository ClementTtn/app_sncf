package raw

import android.os.Parcel
import android.os.Parcelable

class stop(
    private val hourArrival: String,
    private val minuteArrival: String,
    private val hourDeparture: String,
    private val minuteDeparture: String,
    private val station: station?): Parcelable{

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(raw.station::class.java.classLoader)
    ) {
    }

    fun getHourArrival(): String{
        return hourArrival
    }

    fun getMinuteArrival(): String{
        return minuteArrival
    }

    fun getHourDeparture(): String{
        return hourDeparture
    }

    fun getMinuteDeparture(): String{
        return minuteDeparture
    }

    fun getStation(): station?{
        return station
    }

    override fun toString(): String {
        return station!!.getName().toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(hourArrival)
        parcel.writeString(minuteArrival)
        parcel.writeString(hourDeparture)
        parcel.writeString(minuteDeparture)
        parcel.writeParcelable(station, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<stop> {
        override fun createFromParcel(parcel: Parcel): stop {
            return stop(parcel)
        }

        override fun newArray(size: Int): Array<stop?> {
            return arrayOfNulls(size)
        }
    }
}