package com.example.tp1

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tp1.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import raw.stop
import raw.train


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var train: train

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        train = intent.getParcelableExtra<train>("train")!!

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val stop = train.getStops();

        val points = mutableListOf<LatLng>()
        addStop(mMap, train.getFrom(), points)

        for (i in 0 until stop.size) {
            addStop(mMap, stop[i], points)
        }

        addStop(mMap, train.getTo(), points)

        mMap.addPolyline(PolylineOptions()
            .clickable(false)
            .addAll(points)
        )

        val fromStation = train.getFrom()!!.getStation()
        val from = LatLng(fromStation!!.getLatitude(), fromStation.getLongitude())

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(from))

        addPolylines(mMap, train.getStops())

        val textViewMap = findViewById<TextView>(R.id.textViewMap)
        textViewMap.text = toString()
    }

    private fun addStop(mMap: GoogleMap, stop: stop?, points: MutableList<LatLng>) {
        val station = stop!!.getStation()
        val coord = LatLng(station!!.getLatitude(), station.getLongitude())
        points.add(coord)
        val marker = MarkerOptions()
            .position(coord)
            .title(station.getName())
            .snippet(stop.getHourArrival() + "h" + stop.getMinuteArrival())
        mMap.addMarker(marker)
    }

    private fun addPolylines(map: GoogleMap, stops: List<stop>) {
        for (i in 0 until stops.size - 1) {
            val from = LatLng(stops[i].getStation()!!.getLatitude(), stops[i].getStation()!!.getLongitude())
            val to = LatLng(stops[i + 1].getStation()!!.getLatitude(), stops[i + 1].getStation()!!.getLongitude())

            map.addPolyline(PolylineOptions()
                .clickable(false)
                .add(from, to))
        }
    }

    override fun toString(): String{
        return "${train.getType()} n°${train.getNum()} -> ${train.getFrom()} - ${train.getTo()}"
    }
}