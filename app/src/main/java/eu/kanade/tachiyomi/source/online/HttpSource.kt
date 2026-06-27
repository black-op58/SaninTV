package eu.kanade.tachiyomi.source.online

@Deprecated("Use the non-RxJava API instead", replaceWith = ReplaceWith("getImageUrl"))    
open fun fetchImageUrl(page: Page): Observable<String> {
return client.newCall(imageUrlRequest(page))            .asObservableSuccess()            .map { imageUrlParse(it) }    }    /**     * Returns the request for getting the url to the source image. Override only if it's needed to     * 
override the url, send different headers or request method like POST.     *     * @param page the chapter whose page list has to be fetched     */    
protected open 
fun imageUrlRequest(page: Page): Request {
return GET(page.url, headers)    }    /**     * Parses the response from the site and returns the absolute url to the source image.     *     * @param response the response from the site.
*/