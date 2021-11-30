import androidx.compose.ui.graphics.painter.Painter
import io.digitalbits.sdk.AssetTypeCreditAlphaNum
import io.digitalbits.sdk.AssetTypeNative
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.rahim.common.*

sealed class Asset {
    abstract var iconLink: String?
    abstract val name: String?
    abstract val code : String
    abstract val tomlString: String?
    abstract val image: Painter?
    abstract fun toDigitalBitsAsset(): io.digitalbits.sdk.Asset


    object Native : Asset() {
        override var iconLink: String? = "todo"
        override val name: String
            get() = "digitalbits"
        override val code: String
            get() = "XDB"
        override val tomlString: String? = null
        var _image : Painter? = null
        override val image: Painter?
        get() = _image

        override fun toDigitalBitsAsset(): AssetTypeNative {
            return AssetTypeNative()
        }
    }

    /**
     * Todo: Coda and name should be the opposite
     */
    @Serializable
    data class Custom(
        override val code: String,
        val issuer: String,
        override val name: String? = null,
        override var iconLink: String? = null,
        override val tomlString: String? = null,
        @Transient val imageLoader : () -> Painter? = {null}
    ) : Asset() {

        override val image by lazy {
            imageLoader()
        }

        // Doing this to make serialization easier.
//        @Json(ignored=true)
//        val toml : Toml by lazy {Toml().read(tomlString)}
        override fun toDigitalBitsAsset(): io.digitalbits.sdk.Asset {
            return io.digitalbits.sdk.Asset.createNonNativeAsset(code, issuer)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Custom

            if (code != other.code) return false
            if (issuer != other.issuer) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + issuer.hashCode()
            return result
        }

    }

    companion object {

        fun fromSdkAsset(sdkAsset: io.digitalbits.sdk.Asset): Asset {
            return when (sdkAsset) {
                is AssetTypeNative -> Native
                is AssetTypeCreditAlphaNum -> Custom(sdkAsset.code, sdkAsset.issuer, null, null,imageLoader = {null})
                else -> throw Exception("Asset type not supported")
            }
        }
    }
}