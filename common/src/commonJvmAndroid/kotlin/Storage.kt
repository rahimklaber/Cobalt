import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValue
import com.russhwolf.settings.serialization.encodeValue
import com.russhwolf.settings.set
import kotlinx.serialization.Serializable

@Serializable
data class State(val XdbBalance : String, val customBalances : Map<Asset.Custom,String>)
object Storage {

    private val settings = Settings()
    var accountCreated: Boolean
        get() = settings.getBoolean("accountCreated",false)
        set(value) = settings.set("accountCreated", value)
    var balances: State
        get() = settings.decodeValue(State.serializer(),"balance",State("0", mapOf()))
        set(value) = settings.encodeValue(State.serializer(),"balance",value)

    var secret: String?
        get() = settings.getStringOrNull("secret")
        set(value) = settings.set("secret", value)
}