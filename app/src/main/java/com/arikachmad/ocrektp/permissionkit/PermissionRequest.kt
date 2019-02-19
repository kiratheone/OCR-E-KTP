package id.co.mmksi.mitsubishimotors.base.util.permissionkit

import android.app.Activity
import androidx.core.app.ActivityCompat
import java.lang.ref.WeakReference

/**
 * Represents permission request to retry after rationale message is shown.
 */
class PermissionRequest internal constructor(
    activity: Activity,
    val permissions: List<String>,
    private val requestCode: Int
) {

    private val weakActivity: WeakReference<Activity> = WeakReference(activity)

    /**
     * Invoke this after rationale message is shown.
     */
    fun retry() {
        val activity = weakActivity.get()
        activity?.let { ActivityCompat.requestPermissions(it, permissions.toTypedArray(), requestCode) }
    }
}