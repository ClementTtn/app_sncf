package com.example.tp1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ListView
import raw.TypeTrain
import raw.station
import raw.train
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.content.Intent

class MainActivity : AppCompatActivity() {
    private var station: station? = null;                                                           // Station peu être nul
    private val stationListe : ArrayList<station> = ArrayList<station>()                            // Liste des stations

    private fun csvGares(){
        val inputStream = resources.openRawResource(R.raw.gares)                                    //récupère le fichier
        inputStream.bufferedReader().useLines { lines ->                                            // Lire les lignes 1 par 1
            lines.forEach {
                val lineArray = it.split(";")//                                          //Délimitation des lignes
                if (lineArray.size == 4 && lineArray[0] != "CODE_UIC"){                             // Vérifie que la première ligne est "CODE_UIC"
                    val station =
                        station(lineArray[0].toInt(),
                        lineArray[1],
                        lineArray[2].toDouble(),
                        lineArray[3].toDouble())
                    this.stationListe.add(station)                                                  // Ajoute la station à la liste
                }
            }
        }
    }

    private fun API() {
        val body = station!!.getStationBody();
        if (body != null) {
            val departs = body.getJSONArray("departures")                                     // Récupère la partie departures dans l'API
            val trainListe: ArrayList<train> = ArrayList<train>()                                   // Liste des trains
            for (i in 0 until departs.length()) {                                             // Exécute la boucle jusqu'au bout de departures
                val trainJson = departs.getJSONObject(i)                                            // Itération pour créer un nouvel objet
                val trainId = trainJson.getJSONObject("display_informations").getString("trip_short_name").toInt()
                val typeTrainString = trainJson.getJSONObject("display_informations").getString("commercial_mode").split(" ")[0]
                val typeTrain = TypeTrain.valueOf(typeTrainString)
                val dateDepart = trainJson.getJSONObject("stop_date_time").getString("departure_date_time")
                val format = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
                val heureFormat = LocalDateTime.parse(dateDepart, format)                           // Conversion de la date avec le format indiqué au-dessus
                val heureDepart = heureFormat.format(DateTimeFormatter.ofPattern("HH")).toString()
                val minutesDepart = heureFormat.format(DateTimeFormatter.ofPattern("mm")).toString()
                val vehicle_journey = trainJson.getJSONArray("links").getJSONObject(1).getString("id")
                val train = train(trainId, typeTrain, heureDepart, minutesDepart, vehicle_journey)                   // Création d'un train
                trainListe.add(train)                                                               // Ajout d'un train à la liste
                train.run()
            }
            val listView = findViewById<ListView>(R.id.listView);
            val adapterDepartures = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, trainListe) // Mettre la liste dans le listView
            listView.adapter = adapterDepartures
            listView.setOnItemClickListener{ parent, _, position, _ ->
                val intent = Intent(this, MapsActivity::class.java).apply{
                    putExtra("train", trainListe[position])
                }
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val listView = findViewById<ListView>(R.id.listView);
        val autoComplete = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView);
        this.csvGares()                                                                             // Récupère la fonction csvGares
        autoComplete.setAdapter(ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, stationListe)) // Mettre la liste dans l'autocomplete
        autoComplete.setOnItemClickListener { adapterView, view, position, id ->
            val selectedItem = adapterView.getItemAtPosition(position) as station
            this.station = station(selectedItem.getStationCodeUIC())                                // Récupère l'id de la station
            this.station!!.run()                                                                    // Station ne peut pas être nul
            this.API()
        }
    }
}