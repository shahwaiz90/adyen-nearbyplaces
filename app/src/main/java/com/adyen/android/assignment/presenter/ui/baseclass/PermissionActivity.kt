package com.adyen.android.assignment.presenter.ui.baseclass

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

open class PermissionActivity : AppCompatActivity() {

    fun requestPermission(vararg permissionsArray: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, permissionsArray, requestCode)
    }
    fun isPermissionGranted(permission: String): Boolean = ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}