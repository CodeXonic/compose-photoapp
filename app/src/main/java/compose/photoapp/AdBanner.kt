/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package compose.photoapp

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.compose.Composable
import androidx.compose.Providers
import androidx.compose.remember
import androidx.compose.staticAmbientOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.ui.core.ContextAmbient
import androidx.ui.core.LifecycleOwnerAmbient
import androidx.ui.core.Modifier
import androidx.ui.layout.padding
import androidx.ui.unit.dp
import androidx.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

@Composable
fun AdBanner() {
    val adProvider = AdProviderAmbient.current
    val adView = remember(adProvider) { adProvider.getAdView() }
    AndroidView(adView, Modifier.padding(16.dp))
}

@Composable
fun ProvideAdProvider(children: @Composable() () -> Unit) {
    val context = ContextAmbient.current
    val lifecycle = LifecycleOwnerAmbient.current.lifecycle
    Providers(
        AdProviderAmbient provides remember { AdProvider(context, lifecycle) },
        children = children
    )
}

class AdProvider(private val context: Context, private val lifecycle: Lifecycle) {

    // In the real life we will have a pool of views instead of only one
    private val preloaded: AdView

    init {
        MobileAds.initialize(context)
        // we start preloading ad straight away so it will be ready once we need it
        preloaded = createAdView()
    }

    private fun createAdView() = AdView(context).apply {
        adSize = AdSize.MEDIUM_RECTANGLE
        adUnitId = "ca-app-pub-3940256099942544/6300978111"
        loadAd(AdRequest.Builder().build())
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_RESUME -> resume()
                    Lifecycle.Event.ON_PAUSE -> pause()
                    Lifecycle.Event.ON_DESTROY -> destroy()
                }
            }
        })
    }

    fun getAdView(): View = if (preloaded.parent == null) {
        preloaded
    } else {
        createAdView()
    }
}

val AdProviderAmbient = staticAmbientOf<AdProvider>()
