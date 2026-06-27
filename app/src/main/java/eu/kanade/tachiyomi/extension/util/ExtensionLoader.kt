package eu.kanade.tachiyomi.extension.util

}            deferred.map { it.await() }        }    }        /**     * Attempts to load an extension from the given 
package name. It checks if the extension     * contains the required feature flag before trying to load it.     */    
fun loadAnimeExtensionFromPkgName(context: Context, pkgName: String): AnimeLoadResult {
    val pkgInfo = try {            context.packageManager.getPackageInfo(pkgName, PACKAGE_FLAGS)        } catch (error: PackageManager.NameNotFoundException) {            // Unlikely, but the 
package may have been uninstalled at this point            Logger.log(error)
return AnimeLoadResult.Error        }
if (!isPackageAnExtension(MediaType.ANIME, pkgInfo)) {     * @param pkgName The 
package name of the extension to load.     * @param pkgInfo The 
package info of the extension.     */    
private fun loadAnimeExtension(        context: Context,        pkgName: String,        pkgInfo: PackageInfo    ): AnimeLoadResult {
    val pkgManager = context.packageManager
val appInfo = try {            pkgManager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA)        } catch (error: PackageManager.NameNotFoundException) {            // Unlikely, but the 
package may have been uninstalled at this point            Logger.log(error)
return AnimeLoadResult.Error        }

val extName = pkgManager.getApplicationLabel(appInfo).toString().substringAfter("Aniyomi: ")        
val versionName = pkgInfo.versionName
val versionCode = PackageInfoCompat.getLongVersionCode(pkgInfo)
if (versionName.isNullOrEmpty()) {            Logger.log("Missing versionName for extension $extName")
return AnimeLoadResult.Error        }        // Validate lib version
val libVersion = versionName.substringBeforeLast('.').toDoubleOrNull()
if (libVersion == null || libVersion < ANIME_LIB_VERSION_MIN || libVersion > ANIME_LIB_VERSION_MAX) {            Logger.log(                "Lib version is $libVersion, while only versions " +                        "$ANIME_LIB_VERSION_MIN to $ANIME_LIB_VERSION_MAX are allowed"            )
return AnimeLoadResult.Error        }

val isNsfw = appInfo.metaData.getInt("$ANIME_PACKAGE$XX_METADATA_NSFW") == 1
if (!loadNsfwSource && isNsfw) {            Logger.log("NSFW extension $pkgName not allowed")
return AnimeLoadResult.Error        }

val hasReadme = appInfo.metaData.getInt("$ANIME_PACKAGE$XX_METADATA_HAS_README", 0) == 1
val hasChangelog =            appInfo.metaData.getInt("$ANIME_PACKAGE$XX_METADATA_HAS_CHANGELOG", 0) == 1
val classLoader = try{            PathClassLoader(appInfo.sourceDir, null, context.classLoader)        } catch (e: Throwable) {            Logger.log("Extension load error: $extName")            Injekt.get<CrashlyticsInterface>().logException(e)
return AnimeLoadResult.Error        }

val sources = appInfo.metaData.getString("$ANIME_PACKAGE$XX_METADATA_SOURCE_CLASS")!!            .split("
")            .map {
    val sourceClass = it.trim()
if (sourceClass.startsWith(".")) {                    pkgInfo.packageName + sourceClass
} else {                    sourceClass                }            }            .flatMap {
try {
when (
val obj = Class.forName(it, false, classLoader).getDeclaredConstructor()                        .newInstance()) {                        is AnimeSource -> listOf(obj)                        is AnimeSourceFactory -> obj.createSources()
else -> throw Exception("Unknown source 
class type! ${obj.javaClass}")                    }                } catch (e: Throwable) {                    Logger.log("Extension load error: $extName ($it)")
return AnimeLoadResult.Error                }            }

val langs = sources.filterIsInstance<AnimeCatalogueSource>()
