package com.example.sample

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.component1
import androidx.activity.result.component2
import androidx.lifecycle.lifecycleScope
import com.example.sample.databinding.ActivityMainBinding
import com.example.sample.db.DB
import com.example.sample.db.Test
import com.example.sample.db.TestTable
import com.nice.kotlins.app.NiceActivity
import com.nice.kotlins.app.launch
import com.nice.kotlins.event.MutableLiveEvent
import com.nice.kotlins.helper.*
import com.nice.kotlins.http.OkFaker
import com.nice.kotlins.http.asFlow
import com.nice.kotlins.sqlite.db.*
import com.nice.kotlins.widget.ProgressView
import com.nice.kotlins.widget.TipView
import com.nice.kotlins.widget.progressViews
import com.nice.kotlins.widget.tipViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.X509TrustManager

class MainActivity : NiceActivity() {

    private val progressView: ProgressView by progressViews()

    private val binding: ActivityMainBinding by viewBindings()

    private val tipView: TipView by tipViews()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding)

        title = "Home"

        val titleBar = binding.titleBar
        val fab = binding.fab

        fab.doOnClick {
            activityForResultLauncher.launch<SecondActivity, ActivityResult>(
                    this,
                    "key" to "value"
            ) {
                Log.e(TAG, "" + it.component1() + " " + it.component2())
            }
        }

        fab.doOnLongClick {
            true
        }
        val liveEvent = MutableLiveEvent<String>()
        liveEvent.observe(this) {
            Log.e(TAG, "event: $it")
        }

        liveEvent += "event1"
        liveEvent += "event2"

        lifecycleScope.launch(Dispatchers.IO) {
            DB.use(true) {
                var start = System.currentTimeMillis()
                for (index in 0..10000) {
                    val test = Test(index.toLong(),
                            "jack$index",
                            20,
                            index,
                            listOf("A", "B", "C", "D"),
                            "lalalalal",
                            "")

                    insert(TestTable.TABLE_NAME,
                            SQLiteDatabase.CONFLICT_REPLACE,
                            test.toColumnElements())
                }

                Log.e(TAG, "insert: ${System.currentTimeMillis() - start}")
                start = System.currentTimeMillis()

                updateBuilder(TestTable.TABLE_NAME)
                        .values(TestTable.NAME + "jack100")
                        .where(TestTable.NAME.equal("jack1") or TestTable.NAME.equal("jack2"))
                        .execute()

                updateBuilder(TestTable.TABLE_NAME)
                        .values(TestTable.NAME + "jack101")
                        .where(TestTable.NAME.equal("jack3") or TestTable.NAME.equal("jack4"))
                        .execute()

                Log.e(TAG, "update: ${System.currentTimeMillis() - start}")
                start = System.currentTimeMillis()

                val result = queryBuilder(TestTable.TABLE_NAME)
                        .selection(TestTable.ID.notBetween(3, 6))
                        .groupBy(TestTable.NAME, TestTable.JJ)
                        .parseList<Test>()

                Log.e(TAG, "query: ${System.currentTimeMillis() - start}")

                Log.e(TAG, "result: ${result.size}  " + result.toString())
            }
        }

        lifecycleScope.launch {

            OkFaker.get<String>().client(OkHttpClient()).url("https://www.baidu.com")
                    .mapResponse {
                        throw IllegalStateException("hhhhhhhhhhhhhhhhhhhhhh")
                    }.build().asFlow().catch {
                        Log.e(TAG, "error: $it")
                    }.collect {
                        Log.e(TAG, "result1: $it")
                    }

        }


        val job = step {
            add(Dispatchers.IO) {
                val result =
                        OkFaker.get<String>().client(OkHttpClient()).url("https://www.baidu.com")
                                .mapResponse {
                                    it.body!!.string()
                                }.execute()


                Log.e(TAG, "result2: $result")
            }

            add {
                delay(3000)
                Log.e(TAG, "step2")
            }

            add {
                Log.e(TAG, "step3")
            }
        }.launchIn(lifecycleScope)

        lifecycleScope.launch {
            suspendBlocking {
                Log.e(TAG, Thread.currentThread().name)
            }

            suspendBlocking(ExecutorDispatchers.Default) {
                Log.e(TAG, Thread.currentThread().name)
            }

            suspendBlocking(ExecutorDispatchers.IO) {
                val result = OkFaker.get<String>().url("https://www.biquge.com").client {
                    val trustManager = TrustAllX509TrustManager()

                    val sslContext = SSLContext.getInstance("TLS")
                    sslContext.init(null, arrayOf(trustManager), null)
                    val sslSocketFactory = sslContext.socketFactory

                    OkHttpClient.Builder()
                            .sslSocketFactory(sslSocketFactory, trustManager)
                            .hostnameVerifier(TrustAllHostnameVerifier())
                            .build()
                }.execute()
                Log.e(TAG, "${Thread.currentThread().name}:  $result")
            }
        }

    }

    @SuppressLint("TrustAllX509TrustManager")
    private class TrustAllX509TrustManager : X509TrustManager {

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate?>, authType: String) {
        }

        @Throws(CertificateException::class)
        override fun checkClientTrusted(x509Certificates: Array<X509Certificate?>, s: String) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return emptyArray()
        }

    }

    @SuppressLint("BadHostnameVerifier")
    private class TrustAllHostnameVerifier : HostnameVerifier {
        override fun verify(hostname: String, session: SSLSession): Boolean {
            return true
        }
    }

    companion object {
        private val TAG = MainActivity::class.simpleName
    }
}


