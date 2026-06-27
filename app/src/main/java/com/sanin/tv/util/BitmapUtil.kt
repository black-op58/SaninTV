package com.sanin.tv.util
object BitmapUtil {

private fun process(bitmap: android.graphics.Bitmap?): android.graphics.Bitmap? {
if (bitmap == null) return null
return try {

var urlConnection: HttpURLConnection? = null
try {
    val url = URL(imageUrl)                urlConnection = url.openConnection() as HttpURLConnection                urlConnection.requestMethod = "GET"                urlConnection.connect()
if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {                    inputStream = urlConnection.inputStream                    bitmap = BitmapFactory.decodeStream(inputStream)                    bitmap?.let { bitmapCache.put(cacheName, it) }                }            } catch (e: Exception) {                e.printStackTrace()            } finally {                inputStream?.close()                urlConnection?.disconnect()            }        }
return bitmap?.let { roundCorners(it) }    }}