package com.sm.orbitlauncher.widget

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.sm.orbitlauncher.data.LauncherRepository

/** Hosts the user-selected Android widget inside Orbit's central surface. */
class OrbitWidgetHost(
    private val context: Context,
    private val repository: LauncherRepository
) : AppWidgetHost(context, HOST_ID) {
    private val manager = AppWidgetManager.getInstance(context)

    fun createPickerIntent(): Intent {
        val widgetId = allocateAppWidgetId()
        return Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
    }

    fun selectedWidgetId(data: Intent?): Int = data?.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

    fun providerFor(widgetId: Int): ComponentName? = manager.getAppWidgetInfo(widgetId)?.provider

    fun bindingIntent(widgetId: Int, provider: ComponentName): Intent = Intent(
        AppWidgetManager.ACTION_APPWIDGET_BIND
    ).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
    }

    fun needsConfiguration(widgetId: Int): Boolean = manager.getAppWidgetInfo(widgetId)?.configure != null

    fun configurationIntent(widgetId: Int): Intent? {
        val config = manager.getAppWidgetInfo(widgetId)?.configure ?: return null
        return Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
            component = config
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
    }

    fun saveConfiguredWidget(widgetId: Int) {
        repository.saveWidgetId(widgetId)
    }

    fun discardWidget(widgetId: Int) {
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID && widgetId >= 0) {
            deleteAppWidgetId(widgetId)
        }
    }

    fun removeWidget(widgetId: Int = repository.widgetId()) {
        discardWidget(widgetId)
        if (widgetId == repository.widgetId()) repository.clearWidgetId()
    }

    fun viewFor(widgetId: Int): AppWidgetHostView? {
        val info = manager.getAppWidgetInfo(widgetId) ?: return null
        return createView(context, widgetId, info)
    }

    fun onActivityResumed() = startListening()

    fun onActivityPaused() = stopListening()

    companion object {
        const val REQUEST_PICK_WIDGET = 1001
        const val REQUEST_BIND_WIDGET = 1002
        const val REQUEST_CONFIGURE_WIDGET = 1003
        private const val HOST_ID = 8042

        fun cleanUpAbandonedSelection(host: OrbitWidgetHost, resultCode: Int, data: Intent?) {
            val widgetId = host.selectedWidgetId(data)
            if (resultCode != Activity.RESULT_OK && widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                host.discardWidget(widgetId)
            }
        }
    }
}
