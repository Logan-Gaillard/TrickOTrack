package octo.tricko.trickotrack.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PhoneUtils {
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun isLocalisationPermissionsGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermission(activity: Activity) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            AlertDialog.Builder(activity)
                .setTitle("Permission requise")
                .setMessage("L'application a besoin de la localisation précise pour fonctionner correctement.")
                .setPositiveButton("Autoriser") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", activity.packageName, null)
                    }
                    activity.startActivity(intent)
                }
                .setNegativeButton("Annuler", null)
                .show()
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), 1001)
        }
    }
}
