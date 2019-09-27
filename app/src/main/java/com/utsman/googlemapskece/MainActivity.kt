package com.utsman.googlemapskece

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (my_map as SupportMapFragment).apply {
            getMapAsync { map ->

                val marker = LatLng(-6.117664, 106.906349)
                val markerOption = MarkerOptions()
                    .position(marker)
                    .title("Jakarta")
                map.addMarker(markerOption)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 14f))

            }
        }
    }
}
