package com.lb.common_utils.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout

/**workaround for this: https://issuetracker.google.com/issues/133390434 */
class WebViewContainer : FrameLayout {
    @Suppress("MemberVisibilityCanBePrivate")
    val webView: WebView = try {
        WebView(context)
    } catch (e: Exception) {
        e.printStackTrace()
        WebView(context.applicationContext)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        addView(webView, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        //TODO in case of dark theme requirement, consider telling the WebView to also support it:
        //IMPORTANT: This seems to force dark theme, even if the webpage isn't ready.
        //            https://joebirch.co/2020/01/24/enabling-dark-theme-in-android-webviews/
        //        if (context.isCurrentlyOnDarkTheme() && WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK))
        //            WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_ON)
    }
}
