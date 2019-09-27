/*
 * Created by Muhammad Utsman on 9/28/19 6:25 AM
 * Copyright (c) 2019 . All rights reserved.
 * Last modified 9/28/19 6:21 AM
 */

package com.utsman.googlemapskece

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil.computeHeading
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider


class MainActivity : AppCompatActivity() {

    private val disposable = CompositeDisposable()

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        withPermission(this) {

            // start map function after permission granted
            (my_map as SupportMapFragment).apply {
                getMapAsync { map ->

                    // disable map rotate
                    map.uiSettings.isRotateGesturesEnabled = false

                    // get location one update for first time
                    getLocation { loc ->

                        // for first time app opened, setup your marker
                        val marker = loc.toLatlng()
                        val markerOption = MarkerOptions()
                            .position(marker)
                            .title("Jakarta")
                            .icon(bitmapFromVector())

                        val markerPosition = map.addMarker(markerOption)

                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 16f))

                        // after setup your marker, get location every changes for update location
                        getLocationUpdate {  locUpdate ->

                            // delay 30 millisecond for getting old and new location
                            Handler().postDelayed({
                                logi("${locUpdate.toLatlng()}")
                                changePositionSmoothly(markerPosition, locUpdate.toLatlng())
                            }, 30)

                            logi("${locUpdate.toLatlng()}")
                        }
                    }
                }
            }
        }

    }

    // get permission with kotlin DSL
    private fun withPermission(activity: AppCompatActivity, listener: Context.() -> Unit) {
        Dexter.withActivity(activity)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    toast("permission granted")

                    // setup your listener
                    listener(activity)
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {

                    // if permission denied, application will be close on 3 detik
                    logi("permission denied")
                    toast("permission denied, application will be close")
                    Handler().postDelayed({
                        finish()
                    }, 3000)
                }

            })
            .check()
    }

    // animation marker change position for smoothly
    private fun changePositionSmoothly( marker: Marker, newLatLng: LatLng){

        // setup your value animator
        val animation = ValueAnimator.ofFloat(0f, 100f)
        var previousStep = 0f
        val deltaLatitude = newLatLng.latitude - marker.position.latitude
        val deltaLongitude = newLatLng.longitude - marker.position.longitude
        animation.duration = 1500

        // animation can be start for every update location
        animation.addUpdateListener { updatedAnimation ->
            val deltaStep = updatedAnimation.animatedValue as Float - previousStep
            previousStep = updatedAnimation.animatedValue as Float
            marker.position = LatLng(marker.position.latitude + deltaLatitude * deltaStep * 1/100, marker.position.longitude + deltaStep * deltaLongitude * 1/100)
        }
        animation.start()

        // and rotate marker smoothly
        rotateMarker(marker, getAngle(LatLng(marker.position.latitude, marker.position.longitude), newLatLng).toFloat())

    }

    // Get location using rx location once update with kotlin DSL
    private fun getLocation(locationListener: Context.(loc: Location) -> Unit) {
        val request = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setNumUpdates(1)
            .setInterval(100)

        val provider = ReactiveLocationProvider(this)
        val subscription = provider.getUpdatedLocation(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ updateLocation ->
                locationListener(this, updateLocation)

            }, {  thr ->
                loge(thr.localizedMessage)
            })

        disposable.add(subscription)
    }

    // Get location using rx every updates with kotlin DSL
    private fun getLocationUpdate(locationListener: Context.(loc: Location) -> Unit) {
        val request = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(100)

        val provider = ReactiveLocationProvider(this)
        val subscription = provider.getUpdatedLocation(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ updateLocation ->
                locationListener(this, updateLocation)

            }, {  thr ->
                loge(thr.localizedMessage)
            })

        disposable.add(subscription)
    }

    // set angle
    private fun getAngle(fromLatLng: LatLng, toLatLng: LatLng) : Double {

        // default angle is 0.0
        var heading = 0.0

        // if marker different, update heading
        if (fromLatLng != toLatLng) {
            heading = computeHeading(fromLatLng, toLatLng)
        }

        return heading
    }

    // set bitmap using vector xml for high resolution marker
    private fun bitmapFromVector(): BitmapDescriptor {

        val background = ContextCompat.getDrawable(this, R.drawable.ic_marker_direction_2)
        background!!.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)

        val bitmap = Bitmap.createBitmap(background.intrinsicWidth, background.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        background.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    // animation rotation marker
    private fun rotateMarker(marker: Marker, toRotation: Float) {
        val handler = Handler()
        val start = SystemClock.uptimeMillis()
        val startRotation = marker.rotation
        val duration: Long = 300

        handler.post(object : Runnable {
            override fun run() {
                val elapsed = SystemClock.uptimeMillis() - start
                val t = LinearInterpolator().getInterpolation(elapsed.toFloat() / duration)

                val rot = t * toRotation + (1 - t) * startRotation

                marker.rotation = if (-rot > 180) rot / 2 else rot
                if (t < 1.0) {
                    handler.postDelayed(this, 16)
                }
            }
        })
    }
}
