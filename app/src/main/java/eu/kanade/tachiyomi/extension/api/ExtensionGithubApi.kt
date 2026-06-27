package eu.kanade.tachiyomi.extension.api

val libVersion = it.extractLibVersion()                libVersion >= ExtensionLoader.ANIME_LIB_VERSION_MIN && libVersion <= ExtensionLoader.ANIME_LIB_VERSION_MAX            }            .map {                AnimeExtension.Available(                    name = it.name.substringAfter("Aniyomi: "),                    pkgName = it.pkg,                    versionName = it.version,                    versionCode = it.code,                    libVersion = it.extractLibVersion(),                    lang = it.lang,                    isNsfw = it.nsfw == 1,                    hasReadme = it.hasReadme == 1,                    hasChangelog = it.hasChangelog == 1,                    sources = it.sources?.toAnimeExtensionSources().orEmpty(),                    apkName = it.apk,                    repository = repository,                    iconUrl = "${repository.removeSuffix("/index.min.json")}/icon/${it.pkg}.png",                )            }    }    suspend 
fun findAnimeExtensions(): List<AnimeExtension.Available> {
return withIOContext {
    val extensions: ArrayList<AnimeExtension.Available> = arrayListOf()            
val repos =                PrefManager.getVal<Set<String>>(PrefName.AnimeExtensionRepos).toMutableList()            repos.asyncMap {
    val repoUrl = if (it.contains("index.min.json")) {                    it
} else {                    "$it${if (it.endsWith('/')) "" else "/"}index.min.json"                }
try {
    val githubResponse = try {                        networkService.client                            .newCall(GET(repoUrl))    }

private fun fallbackRepoUrl(repoUrl: String): String? {
    var fallbackRepoUrl = "https://gcore.jsdelivr.net/gh/"        
val strippedRepoUrl = repoUrl            .removePrefix("https://")            .removePrefix("http://")            .removeSuffix("/")            .removeSuffix("/index.min.json")        
val repoUrlParts = strippedRepoUrl.split("/")
if (repoUrlParts.size < 3) {
return null        }

val repoOwner = repoUrlParts[1]        
val repoName = repoUrlParts[2]        fallbackRepoUrl += "$repoOwner/$repoName"        
val repoBranch = if (repoUrlParts.size > 3) {            repoUrlParts[3]
} else {            "main"        }        fallbackRepoUrl += "@$repoBranch"
return fallbackRepoUrl    }}

@Serializable
private data 
class ExtensionJsonObject(    
val name: String,    
val pkg: String,    
val apk: String,    
val lang: String,    
val code: Long,    
val version: String,    
val nsfw: Int,    
val hasReadme: Int = 0,    
val hasChangelog: Int = 0,    
val sources: List<ExtensionSourceJsonObject>?,)
@Serializable
private data 
class ExtensionSourceJsonObject(    
val id: Long,    
val lang: String,    
val name: String,    
val baseUrl: String,)
private fun ExtensionJsonObject.extractLibVersion(): Double {
return version.substringBeforeLast('.').toDouble()}
}
