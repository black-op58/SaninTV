package tachiyomi.core.metadata.comicinfo

val coverArtist: CoverArtist?,    
val translator: Translator?,    
val genre: Genre?,    
val tags: Tags?,    
val web: Web?,    
val publishingStatus: PublishingStatusTachiyomi?,    
val categories: CategoriesTachiyomi?,    
val source: SourceAniyomi?,) {    
@XmlElement(false)    
@XmlSerialName("xmlns:xsd", "", "")    
val xmlSchema: String = "http://www.w3.org/2001/XMLSchema"    
@XmlElement(false)    
@XmlSerialName("xmlns:xsi", "", "")    
val xmlSchemaInstance: String = "http://www.w3.org/2001/XMLSchema-instance"    
@Serializable    
@XmlSerialName("Title", "", "")    
data class Title(
@XmlValue(true) 
val value: String = "")    
@Serializable    
@XmlSerialName("Series", "", "")    
data class Series(
@XmlValue(true) 
val value: String = "")    
@Serializable    
@XmlSerialName("Number", "", "")    
data class Number(
@XmlValue(true) 
val value: String = "")    
@Serializable    
@XmlSerialName("Summary", "", "")    
data class Summary(
@XmlValue(true) 
val value: String = "")    
@Serializable    
@XmlSerialName("Writer", "", "")    
data class Writer(
@XmlValue(true) 
val value: String = "")    
@Serializable    
@XmlSerialName("Penciller", "", "")    
data class Penciller(
@XmlValue(true) 
val value: String = "")    
@Serializable    
@XmlSerialName("Inker", "", "")    
data class Inker(
@XmlValue(true) 
val value: String = "")    
@Serializable    
@XmlSerialName("Colorist", "", "")    
data class Colorist(
@XmlValue(true) 
val value: String = "")    
@Serializable    
@XmlSerialName("Letterer", "", "")    
data class Letterer(
@XmlValue(true) 
val value: String = "")    
@Serializable    
@XmlSerialName("CoverArtist", "", "")    
data class CoverArtist(
@XmlValue(true) 
val value: String = "")    
@Serializable    
@XmlSerialName("Translator", "", "")    
data class Translator(
@XmlValue(true) 
val value: String = "")    
@Serializable    
@XmlSerialName("Genre", "", "")    
data class Genre(
@XmlValue(true) 
val value: String = "")    
@Serializable    
@XmlSerialName("Tags", "", "")    
data class Tags(
@XmlValue(true) 
val value: String = "")    
@Serializable    
@XmlSerialName("Web", "", "")    
data class Web(
@XmlValue(true) 
val value: String = "")    
@Serializable    
@XmlSerialName("PublishingStatusTachiyomi", "http://www.w3.org/2001/XMLSchema", "ty")    
data class PublishingStatusTachiyomi(
@XmlValue(true) 
val value: String = "")    
@Serializable    
@XmlSerialName("Categories", "http://www.w3.org/2001/XMLSchema", "ty")    
data class CategoriesTachiyomi(
@XmlValue(true) 
val value: String = "")
@Serializable
class EpisodeDetails(    
val episode_number: Float,    
val name: String? = null,    
val date_upload: String? = null,    
val scanlator: String? = null,
}
