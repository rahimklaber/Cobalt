import io.digitalbits.sdk.Server

fun doStuff() {
    Server("https://frontier.testnet.digitalbits.io")
        .transactions().execute()
        .records
        .forEach {
            println(it.hash)
        }
}