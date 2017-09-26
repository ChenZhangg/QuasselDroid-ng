package de.kuschku.quasseldroid_ng.service

import android.arch.lifecycle.LifecycleService
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import de.kuschku.libquassel.protocol.*
import de.kuschku.libquassel.session.Backend
import de.kuschku.libquassel.session.ISession
import de.kuschku.libquassel.session.SessionManager
import de.kuschku.libquassel.session.SocketAddress
import de.kuschku.quasseldroid_ng.BuildConfig
import de.kuschku.quasseldroid_ng.R
import de.kuschku.quasseldroid_ng.persistence.QuasselDatabase
import de.kuschku.quasseldroid_ng.util.AndroidHandlerService
import org.threeten.bp.Instant
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class QuasselService : LifecycleService() {
  private lateinit var sessionManager: SessionManager

  private lateinit var clientData: ClientData

  private val trustManager = object : X509TrustManager {
    override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {
    }

    override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
  }

  private val backendImplementation = object : Backend {
    override fun sessionManager() = sessionManager

    override fun connect(address: SocketAddress, user: String, pass: String) {
      disconnect()
      val handlerService = AndroidHandlerService()
      sessionManager.connect(clientData, trustManager, address, handlerService, user to pass)
    }

    override fun disconnect() {
      sessionManager.disconnect()
    }
  }

  private val asyncBackend = object : Backend {
    private val thread = HandlerThread("BackendHandler")
    private val handler: Handler

    init {
      thread.start()
      handler = Handler(thread.looper)
    }

    override fun connect(address: SocketAddress, user: String, pass: String) {
      handler.post {
        backendImplementation.connect(address, user, pass)
      }
    }

    override fun disconnect() {
      handler.post {
        backendImplementation.disconnect()
      }
    }

    override fun sessionManager() = backendImplementation.sessionManager()
  }

  private lateinit var database: QuasselDatabase

  override fun onCreate() {
    super.onCreate()
    database = QuasselDatabase.Creator.init(application)
    sessionManager = SessionManager(ISession.NULL)
    clientData = ClientData(
      identifier = "${resources.getString(R.string.app_name)} ${BuildConfig.VERSION_NAME}",
      buildDate = Instant.ofEpochSecond(BuildConfig.GIT_COMMIT_DATE),
      clientFeatures = Quassel_Features.of(*Quassel_Feature.values()),
      protocolFeatures = Protocol_Features.of(
        Protocol_Feature.Compression,
        Protocol_Feature.TLS
      ),
      supportedProtocols = byteArrayOf(0x02)
    )
  }

  override fun onBind(intent: Intent?): QuasselBinder {
    super.onBind(intent)
    return QuasselBinder(asyncBackend)
  }

  class QuasselBinder(val backend: Backend) : Binder()
}
