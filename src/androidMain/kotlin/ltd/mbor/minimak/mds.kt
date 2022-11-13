@file:JvmName("MDSAndroid")
package ltd.mbor.minimak

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import java.net.URLEncoder
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

actual fun encodeURIComponent(data: String): String = URLEncoder.encode(data, "UTF-8")

private const val TAG = "MDS"

/**
 * Log some data with a timestamp in a consistent manner to the console
 */
actual fun log(output: String){
  Log.w(TAG, "Minima @ ${Date().toLocaleString()} : $output")
}

actual fun createClient() = HttpClient(OkHttp) {
  install(HttpTimeout) {
    requestTimeoutMillis = 45000
    socketTimeoutMillis = 60000
  }
  engine {
    config {
      val trustManager = object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOf()
        override fun checkClientTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
        override fun checkServerTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
      }
      val sslContext = SSLContext.getInstance("TLS")
      sslContext.init(null, arrayOf(trustManager), null)
      sslSocketFactory(sslContext.socketFactory, trustManager)
      hostnameVerifier { hostname, session -> true }
    }
  }
}