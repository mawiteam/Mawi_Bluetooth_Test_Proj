package com.example.test.project

import android.Manifest
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun <T> Observable<T>.toMainThread() = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
fun Completable.toMainThread() = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun AppCompatActivity.isPermissionGranted(permission: String) =
    ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun AppCompatActivity.shouldShowPermissionRationale(permission: String) =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

fun AppCompatActivity.requestPermission(permission: String, requestId: Int) =
    ActivityCompat.requestPermissions(this, arrayOf(permission), requestId)

fun AppCompatActivity.batchRequestPermissions(permissions: Array<String>, requestId: Int) =
    ActivityCompat.requestPermissions(this, permissions, requestId)