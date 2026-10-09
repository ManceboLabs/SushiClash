package com.mancebolabs.sushiclash.screenshots

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Assert.assertTrue
import java.io.File

/**
 * Captures a PNG of the current device window for store/README screenshots.
 *
 * Primary write path (app-private, wiped by `clearPackageData`):
 * `targetContext.getExternalFilesDir("screenshots")/<locale>/<filename>.png`
 *
 * Durable copy for `adb pull` after the suite (survives orchestrator clear):
 * `/data/local/tmp/sushiclash-screenshots/<locale>/<filename>.png`
 */
object ScreenshotCapture {
    private const val DEFAULT_SETTLE_MILLIS = 400L
    private const val SCREENSHOTS_DIR_NAME = "screenshots"
    const val DURABLE_SCREENSHOTS_ROOT = "/data/local/tmp/sushiclash-screenshots"

    /**
     * Waits for Compose idle (via [waitForIdle]), settles briefly, then captures via UiAutomator.
     *
     * @param localeFolder locale directory name matching [com.mancebolabs.sushiclash.domain.model.AppLanguage.languageTag]
     *   (e.g. `en`, `zh-CN`)
     * @param filename base name with or without `.png` suffix
     * @param waitForIdle typically `{ composeTestRule.waitForIdle() }`
     */
    fun capture(
        localeFolder: String,
        filename: String,
        waitForIdle: () -> Unit,
        settleMillis: Long = DEFAULT_SETTLE_MILLIS,
    ): File {
        require(localeFolder.isNotBlank()) { "localeFolder must not be blank" }
        require(filename.isNotBlank()) { "filename must not be blank" }

        // Looping GIF / Choreographer frames can prevent Compose/Espresso idle forever.
        // Prefer a best-effort idle, then always settle before capturing.
        runCatching { waitForIdle() }
        if (settleMillis > 0L) {
            Thread.sleep(settleMillis)
        }

        val outputFile = screenshotFile(localeFolder, filename)
        outputFile.parentFile?.mkdirs()

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val wrote = device.takeScreenshot(outputFile)
        assertTrue("UiDevice.takeScreenshot returned false for ${outputFile.absolutePath}", wrote)
        assertTrue("Screenshot file missing: ${outputFile.absolutePath}", outputFile.exists())
        assertTrue(
            "Screenshot file is empty: ${outputFile.absolutePath}",
            outputFile.length() > 0L,
        )

        // Orchestrator clearPackageData wipes app external files after each test; mirror to tmp.
        copyToDurableLocation(device, outputFile, localeFolder)
        return outputFile
    }

    fun screenshotFile(localeFolder: String, filename: String): File {
        val pngName = normalizedPngName(filename)
        val root = InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getExternalFilesDir(SCREENSHOTS_DIR_NAME)
            ?: error("getExternalFilesDir(\"$SCREENSHOTS_DIR_NAME\") returned null")
        return File(File(root, localeFolder), pngName)
    }

    fun durableScreenshotFile(localeFolder: String, filename: String): File {
        return File(File(DURABLE_SCREENSHOTS_ROOT, localeFolder), normalizedPngName(filename))
    }

    private fun normalizedPngName(filename: String): String {
        return if (filename.endsWith(".png", ignoreCase = true)) {
            filename
        } else {
            "$filename.png"
        }
    }

    private fun copyToDurableLocation(device: UiDevice, source: File, localeFolder: String) {
        val durableDir = "$DURABLE_SCREENSHOTS_ROOT/$localeFolder"
        val durablePath = "$durableDir/${source.name}"
        device.executeShellCommand("mkdir -p $durableDir")
        device.executeShellCommand("cp ${source.absolutePath} $durablePath")
        // Best-effort readable for adb pull as the host user.
        device.executeShellCommand("chmod 644 $durablePath")
    }
}
