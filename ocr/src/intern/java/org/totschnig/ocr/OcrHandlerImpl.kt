package org.totschnig.ocr

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.totschnig.myexpenses.MyApplication
import org.totschnig.myexpenses.feature.Feature
import org.totschnig.myexpenses.feature.OcrResult
import org.totschnig.myexpenses.feature.getUserConfiguredOcrEngine
import org.totschnig.myexpenses.preference.PrefHandler
import javax.inject.Inject

class OcrHandlerImpl @Inject constructor(prefHandler: PrefHandler, application: MyApplication) : AbstractOcrHandlerImpl(prefHandler, application) {
    override suspend fun runTextRecognition(uri: Uri, context: Context) =
            getEngine(context, prefHandler)?.let { processTextRecognitionResult(it.run(uri, context, prefHandler), queryPayees()) } ?: throw java.lang.IllegalStateException("No engine loaded")

    override suspend fun handleData(intent: Intent): OcrResult {
        throw IllegalStateException()
    }

    override fun info(context: Context): CharSequence? {
       return getEngine(context, prefHandler)?.info(context, prefHandler)
    }

    companion object {

        fun availableEngines(): List<Engine> = listOfNotNull(getEngine(Feature.TESSERACT), getEngine(Feature.MLKIT))

        fun getEngine(context: Context, prefHandler: PrefHandler) =
                with (availableEngines()) {
                    when(size) {
                        0 -> null
                        1 -> get(0)
                        else -> find { it.javaClass.`package`?.name?.contains(getUserConfiguredOcrEngine(context, prefHandler).moduleName) == true  }
                    }
                }

        fun getEngine(engine: Feature) = try {
            Class.forName("org.totschnig.${engine.moduleName}.Engine").kotlin.objectInstance as Engine
        } catch (e: Exception) {
            null
        }
    }
}