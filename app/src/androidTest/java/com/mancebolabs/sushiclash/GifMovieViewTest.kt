package com.mancebolabs.sushiclash

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mancebolabs.sushiclash.ui.components.character.GifMovieView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GifMovieViewTest {

    @Test
    fun givenSameRawResource_whenSetGifResourceCalledTwice_thenPlaybackStartIsNotReset() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val view = GifMovieView(context)

        view.setGifResource(R.raw.chef_saludo)
        val startAfterFirstLoad = view.readMovieStartMillis()
        Thread.sleep(50)

        view.setGifResource(R.raw.chef_saludo)

        assertEquals(startAfterFirstLoad, view.readMovieStartMillis())
    }

    @Test
    fun givenDifferentRawResource_whenSetGifResourceCalled_thenGifIsReloaded() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val view = GifMovieView(context)

        view.setGifResource(R.raw.chef_saludo)
        val startAfterFirstLoad = view.readMovieStartMillis()
        Thread.sleep(50)

        view.setGifResource(R.raw.chef_inicio)

        assertNotEquals(startAfterFirstLoad, view.readMovieStartMillis())
        assertNotNull(view.readMovie())
    }

    @Test
    fun givenStoppedView_whenSetGifResourceCalledWithSameId_thenGifIsReloaded() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val view = GifMovieView(context)

        view.setGifResource(R.raw.chef_saludo)
        view.stop()

        view.setGifResource(R.raw.chef_saludo)

        assertNotNull(view.readMovie())
        assertTrue(view.readIsPlaying())
    }

    private fun GifMovieView.readMovieStartMillis(): Long {
        val field = GifMovieView::class.java.getDeclaredField("movieStartMillis")
        field.isAccessible = true
        return field.getLong(this)
    }

    private fun GifMovieView.readMovie(): Any? {
        val field = GifMovieView::class.java.getDeclaredField("movie")
        field.isAccessible = true
        return field.get(this)
    }

    private fun GifMovieView.readIsPlaying(): Boolean {
        val field = GifMovieView::class.java.getDeclaredField("isPlaying")
        field.isAccessible = true
        return field.getBoolean(this)
    }
}
