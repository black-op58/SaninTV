package eu.kanade.tachiyomi.source.model
import android.net.Uri
import eu.kanade.tachiyomi.network.ProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient as KotlinxTransient
@Serializable
open class Page(    
val index: Int,    
val url: String = "",    
var imageUrl: String? = null,
@kotlin.jvm.Transient    
@KotlinxTransient
var uri: Uri? = null, // Deprecated but can't be deleted due to extensions) : ProgressListener {
    val number: Int        get() = index + 1
@kotlin.jvm.Transient    
@KotlinxTransient    
private val _statusFlow = MutableStateFlow(State.QUEUE)
@kotlin.jvm.Transient    
@KotlinxTransient
val statusFlow = _statusFlow.asStateFlow()    
var status: State        get() = _statusFlow.value        set(value) {            _statusFlow.value = value        }
@kotlin.jvm.Transient    
@KotlinxTransient    
private val _progressFlow = MutableStateFlow(0)
@kotlin.jvm.Transient)
