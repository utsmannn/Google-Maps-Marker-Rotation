/*
 * Created by Muhammad Utsman on 9/28/19 6:25 AM
 * Copyright (c) 2019 . All rights reserved.
 * Last modified 9/28/19 6:16 AM
 */

@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.utsman.googlemapskece

import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng

fun logi(msg: String?) = Log.i("anjay", msg)
fun loge(msg: String?) = Log.e("anjay", msg)

fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun Location.toLatlng() = LatLng(latitude, longitude)