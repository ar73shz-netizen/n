package com.rasahamrah.installment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(AndroidShareBridge(), "AndroidShare")
        webView.loadUrl("file:///android_asset/index.html")
    }

    inner class AndroidShareBridge {
        @JavascriptInterface
        fun shareText(text: String) {
            runOnUiThread {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    putExtra(Intent.EXTRA_SUBJECT, "نتیجه محاسبه اقساط")
                }
                startActivity(Intent.createChooser(intent, "اشتراک‌گذاری محاسبه اقساط"))
            }
        }

        @JavascriptInterface
        fun shareImage(dataUrl: String) {
            try {
                val encoded = dataUrl.substringAfter(",", "")
                if (encoded.isBlank()) return
                val bytes = Base64.decode(encoded, Base64.DEFAULT)
                val file = File(cacheDir, "shared_installment_result.png")
                file.writeBytes(bytes)
                val uri: Uri = FileProvider.getUriForFile(
                    this@MainActivity,
                    "$packageName.fileprovider",
                    file
                )
                runOnUiThread {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TEXT, "نتیجه محاسبه اقساط - رسا همراه")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(intent, "اشتراک‌گذاری تصویر محاسبه اقساط"))
                }
            } catch (_: Exception) {
                runOnUiThread {
                    android.widget.Toast.makeText(this@MainActivity, "ساخت یا اشتراک‌گذاری تصویر ناموفق بود", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
