package xyz.savvamirzoyan.trueithubtalks.repository.websocket

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import org.json.JSONObject
import timber.log.Timber
import xyz.savvamirzoyan.trueithubtalks.repository.model.ChatMessage
import xyz.savvamirzoyan.trueithubtalks.repository.model.jsonconvertable.Chat
import xyz.savvamirzoyan.trueithubtalks.repository.model.jsonconvertable.MessageFactory
import xyz.savvamirzoyan.trueithubtalks.repository.model.jsonconvertable.Wrapper
import xyz.savvamirzoyan.trueithubtalks.repository.model.jsonconvertable.income.TextMessageIncome
import java.net.SocketException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private fun getUnsafeClient(): OkHttpClient {
    return try {
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }
        )

        // Install the all-trusting trust manager
        val sslContext: SSLContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
        val builder = OkHttpClient.Builder()
        builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
        builder.hostnameVerifier { _, _ -> true }
        builder.pingInterval(30, TimeUnit.SECONDS)
        builder.build()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}

class WebSocketController {

    class ChatFeedController(
        val token: String,
        val chats: MutableLiveData<ArrayList<Chat>>
    ) {

        private val socketClient = getUnsafeClient()
        private var serverSocket: WebSocket? = null

        private val serverWebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)

                val json = Json.encodeToString(MessageFactory.connectToChatsFeed(token))

                serverSocket = webSocket
                serverSocket!!.send(json)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)

                val type = JSONObject(text).get("type")

                if (type == "chat-feed-download") {
                    val chatsDownloaded = Json.decodeFromString<Wrapper<ArrayList<Chat>>>(text).data
                    chats.postValue(chatsDownloaded)

                } else if (type == "chat-feed-update") {
                    val chatFeedUpdate = Json.decodeFromString<Wrapper<Chat>>(text).data

                    val newChatsFeed = arrayListOf<Chat>()
                    chats.value?.filter {
                        it.username != chatFeedUpdate.username
                    }?.map { it }?.let { newChatsFeed.addAll(it) }
                    newChatsFeed.add(0, chatFeedUpdate)

                    chats.postValue(newChatsFeed)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)

                webSocket.close(1000, null)
                webSocket.cancel()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Timber.i("onFailure() called | t: $t ${t.localizedMessage} | response: $response")
                if (t is SocketException) {
                    serverSocket = null
                    webSocket.close(1000, null)
                    webSocket.cancel()
                } else if (t is java.io.EOFException) {
                    connectToChats()
                }
            }
        }

        fun connectToChats() {
            Timber.i("connectToChats() called")
            socketClient.newWebSocket(buildInitRequest().build(), serverWebSocketListener)
        }

        private fun buildInitRequest(): Request.Builder {
            Timber.i("buildInitRequest() called")
            return Request.Builder().url("wss://192.168.0.105:8083/chats-feed")
        }

        fun disconnect() {
            Timber.i("disconnect() called")
            val json = Json.encodeToString(MessageFactory.disconnectFromChatsFeedAction(token))

            serverSocket?.send(json)
        }

    }

    class SingleChatController(
        private val token: String,
        private val username: String,
        private val lastMessage: MutableLiveData<ChatMessage>,
        private val messageHistory: MutableLiveData<ArrayList<ChatMessage>>
    ) {
        private val socketClient = getUnsafeClient()
        private var serverSocket: WebSocket? = null

        private val serverWebSocketListener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Timber.i("onOpen() called | response: $response")

                val json = Json.encodeToString(MessageFactory.openChatAction(username, token))

                serverSocket = webSocket
                serverSocket!!.send(json)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Timber.i("onMessage() called | text: $text")

                val type = JSONObject(text).get("type")
                if (type == "new-message") {
                    val textMessage = Json.decodeFromString<Wrapper<TextMessageIncome>>(text).data
                    lastMessage.postValue(ChatMessage(textMessage.message, false))
                } else if (type == "message-history") {
                    val messagesRaw =
                        Json.decodeFromString<Wrapper<ArrayList<TextMessageIncome>>>(text).data

                    val messagesPrepared = arrayListOf<ChatMessage>()
                    messagesPrepared.addAll(messagesRaw.map {
                        ChatMessage(
                            it.message,
                            it.username == username
                        )
                    })
                    messageHistory.postValue(messagesPrepared)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Timber.i("onClosing() called | code: $code | reason: $reason")

                webSocket.close(1000, null)
                webSocket.cancel()
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?
            ) {
                Timber.i("onFailure() called | t: $t ${t.localizedMessage} | response: $response")
                if (t is SocketException) {
                    serverSocket = null
                    webSocket.close(1000, null)
                    webSocket.cancel()
                } else if (t is java.io.EOFException) {
                    connectToChat()
                }
            }
        }

        private fun buildInitRequest(): Request.Builder {
            Timber.i("buildInitRequest() called")
            return Request.Builder().url("wss://192.168.0.105:8083/chat")
        }

        fun connectToChat() {
            Timber.i("connect() called")
            socketClient.newWebSocket(buildInitRequest().build(), serverWebSocketListener)
        }

        fun disconnect() {
            Timber.i("disconnect() called")

            val json = Json.encodeToString(MessageFactory.disconnectAction(username, token))

            serverSocket?.send(json)
        }

        fun sendText(text: String) {
            Timber.i("sendText($text) called")

            val json = Json.encodeToString(MessageFactory.textMessage(username, token, text))
            serverSocket?.send(json)
        }
    }
}