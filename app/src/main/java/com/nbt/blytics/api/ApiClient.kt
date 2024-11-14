package com.nbt.blytics.api


import com.nbt.blytics.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*


object RetrofitFactory {
    fun getApiService(): ApiService {
        val apiPath = BuildConfig.BASE_URL

        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(apiPath)

            .client(provideOkHttpClient(provideLoggingInterceptor()))
            .addConverterFactory(GsonConverterFactory.create())

//            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build().create(ApiService::class.java)
    }
}


/**
 * @method use for provide ok Http client
 */
//private fun provideOkHttpClient(interceptor: HttpLoggingInterceptor): OkHttpClient {
//    val okHttpClient = OkHttpClient.Builder()
//    okHttpClient.addInterceptor(interceptor)
//    okHttpClient.connectTimeout(60, TimeUnit.SECONDS)
//    okHttpClient.readTimeout(60, TimeUnit.SECONDS)
//    return okHttpClient.build()
//}

private fun provideOkHttpClient(interceptor: HttpLoggingInterceptor): OkHttpClient {
/*    val okHttpClient = OkHttpClient.Builder()
    okHttpClient.addInterceptor(interceptor)
    okHttpClient.connectTimeout(60, TimeUnit.SECONDS)
    okHttpClient.readTimeout(60, TimeUnit.SECONDS)
    return okHttpClient.build()*/

    // Create a trust manager that does not validate certificate chains


    // Install the all-trusting trust manager

    // Create an ssl socket factory with our all-trusting manager
    val builder = OkHttpClient.Builder()
    try {
        builder.connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

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
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory = sslContext.socketFactory

        builder.addInterceptor(interceptor)
        builder.sslSocketFactory(
            sslSocketFactory,
            trustAllCerts[0] as X509TrustManager
        )
        builder.hostnameVerifier(HostnameVerifier { hostname, session -> true })
    } catch (e: Exception) {
        throw RuntimeException(e)
    }


    return builder.build()

}
private fun provideLoggingInterceptor(): HttpLoggingInterceptor {
    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY
    return interceptor
}
