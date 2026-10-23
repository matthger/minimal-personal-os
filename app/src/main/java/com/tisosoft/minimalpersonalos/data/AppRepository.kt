package com.tisosoft.minimalpersonalos.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.edit
import com.tisosoft.minimalpersonalos.model.AppInfo

class AppRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }

        return resolveInfos.mapNotNull { resolveInfo ->
            val activityInfo = resolveInfo.activityInfo
            activityInfo?.applicationInfo?.let { appInfo ->
                AppInfo(
                    packageName = appInfo.packageName,
                    name = appInfo.loadLabel(pm).toString(),
                    icon = appInfo.loadIcon(pm)
                )
            }
        }.filter {
            it.packageName != context.packageName // exclude own launcher
        }.sortedBy { it.name }
    }

    fun launchApp(packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
        }
    }

    fun saveSelectedApps(apps: List<AppInfo>) {
        val packageNames = apps.map { it.packageName }.toSet()
        prefs.edit {
            putStringSet(PREF_KEY_SELECTED_APPS, packageNames)
        }
    }

    fun loadSelectedApps(): List<AppInfo> {
        val selectedPackageNames = prefs.getStringSet(PREF_KEY_SELECTED_APPS, emptySet()) ?: emptySet()
        if (selectedPackageNames.isEmpty()) return emptyList()

        return getInstalledApps()
            .filter { selectedPackageNames.contains(it.packageName) }
            .map { it.copy(isSelected = true) }
    }

    fun clearSelectedApps() {
        prefs.edit {
            remove(PREF_KEY_SELECTED_APPS)
        }
    }
}