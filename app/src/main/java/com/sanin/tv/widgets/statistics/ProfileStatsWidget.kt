package com.sanin.tv.widgets.statistics

appWidgetIds.forEach { appWidgetId ->            context.getSharedPreferences(getPrefsName(appWidgetId), Context.MODE_PRIVATE).edit().clear().apply()        }        super.onDeleted(context, appWidgetIds)    }

override fun onEnabled(context: Context) {        super.onEnabled(context)    }

override fun onDisabled(context: Context) {        super.onDisabled(context)    }

override fun onAppWidgetOptionsChanged(        context: Context,        appWidgetManager: AppWidgetManager,        appWidgetId: Int,        newOptions: android.os.Bundle?    ) {        updateAppWidget(context, appWidgetManager, appWidgetId)        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)    }

companion object {        
@OptIn(DelicateCoroutinesApi::class)        
fun updateAppWidget(            context: Context,            appWidgetManager: AppWidgetManager,            appWidgetId: Int        ) {
    val prefs = context.getSharedPreferences(getPrefsName(appWidgetId), Context.MODE_PRIVATE)            
val backgroundColor = prefs.getInt(PREF_BACKGROUND_COLOR, Color.parseColor("#80000000"))            
val backgroundFade = prefs.getInt(PREF_BACKGROUND_FADE, Color.parseColor("#00000000"))            
val titleTextColor = prefs.getInt(PREF_TITLE_TEXT_COLOR, Color.WHITE)            
val statsTextColor = prefs.getInt(PREF_STATS_TEXT_COLOR, Color.WHITE)            
val gradientDrawable = ResourcesCompat.getDrawable(                context.resources,                R.drawable.linear_gradient_black,        ) {            withContext(Dispatchers.Main) {
    val views = RemoteViews(context.packageName, R.layout.statistics_widget).apply {                    setImageViewBitmap(R.id.backgroundView, backgroundBitmap)                    setTextViewText(R.id.topLeftItem, "")                    setTextViewText(R.id.topLeftLabel, context.getString(R.string.please))                    setTextViewText(R.id.topRightItem, "")                    setTextViewText(R.id.topRightLabel, context.getString(R.string.log_in))                    setTextViewText(R.id.bottomLeftItem, context.getString(R.string.or_join))                    setTextViewText(R.id.bottomLeftLabel, "")                    setTextViewText(R.id.bottomRightItem, context.getString(R.string.anilist))                    setTextViewText(R.id.bottomRightLabel, "")                    
val intent = Intent(context, MainActivity::class.java)                    
val pendingIntent = PendingIntent.getActivity(                        context, 0, intent, PendingIntent.FLAG_IMMUTABLE                    )                    setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)                }                appWidgetManager.updateAppWidget(appWidgetId, views)            }        }

fun getPrefsName(appWidgetId: Int) = "com.sanin.tv.widgets.Statistics.${appWidgetId}"        const val PREF_BACKGROUND_COLOR = "background_color"        const val PREF_BACKGROUND_FADE = "background_fade"        const val PREF_TITLE_TEXT_COLOR = "title_text_color"        const val PREF_STATS_TEXT_COLOR = "stats_text_color"    }
