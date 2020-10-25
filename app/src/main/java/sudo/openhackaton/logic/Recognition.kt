package sudo.openhackaton.logic

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import sudo.openhackaton.logic.Constants.DIGITS
import sudo.openhackaton.logic.Constants.FROM_WHERE
import sudo.openhackaton.logic.Constants.RECOGNITION_DIDNT_SUCCEED
import sudo.openhackaton.logic.Constants.RECOGNITION_IN_PROGRESS
import sudo.openhackaton.logic.Constants.RECOGNITION_RESULT
import sudo.openhackaton.view.CheckingDialogFragment
import java.io.FileOutputStream
import java.io.IOException

class Recognition(private val filesLogic: FilesLogic) {
    private var inputImage: InputImage? = null
    private val recognizer = TextRecognition.getClient()
    private lateinit var context: AppCompatActivity

    private fun serialNumberAndIndication(strings: MutableList<String>): Pair<String?, String?> {
        if (strings.size == 0) return null to null
        if (strings.size == 1) return strings[0] to null
        val newStr = mutableListOf<String>()
        strings.forEach { if (it.count { char -> Regex("""[A-Za-z]""").matches(char.toString()) } < 2) newStr.add(Regex("""[ ,]""").replace(it, "")) }
        newStr.sortByDescending { it.count { char -> DIGITS.contains(char) } }
        val newNewStr = mutableListOf<String>()
        newStr.forEach { newNewStr.add(Regex("""[A-Za-z]""").replace(it, "")) }
        return newNewStr[0] to newNewStr.firstOrNull { it.matches(Regex("""\d{3,8}""")) }
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun prepareData(contentResolver: ContentResolver, uri: Uri?) {
        try {
            val bm = filesLogic.getBitmapFromUri(contentResolver, uri) ?: return
            filesLogic.createImageFile()
            val fos = FileOutputStream(filesLogic.lastCreated)
            val rotated = rotateBitmap(bm, 90f) ?: return
            rotated.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            return
        }
    }

    fun serverDoTask(path: String?): Pair<String?, String?>? {
        var serial: String? = null
        var indication: String? = null
        if (path == null) return null
        val bm = filesLogic.getBitmapFromAbsolutePath(path) ?: return null
        val rotated = rotateBitmap(bm, 90f) ?: return null
        val inIm = InputImage.fromBitmap(rotated, 0)
        val process = recognizer.process(inIm)
        // Kostyl' ;)
        while (!process.isComplete) {
        }
        if (process.result != null && process.result!!.text.isNotEmpty()) {
            val recognizedStrings = mutableListOf<String>()
            process.result!!.textBlocks.forEach { textBlock ->
                recognizedStrings.add(textBlock.text)
            }
            val result = serialNumberAndIndication(recognizedStrings)
            serial = result.first
            indication = result.second
        }
        return serial to indication
    }

    fun doTask(
            context: AppCompatActivity,
            contentResolver: ContentResolver,
            resultCode: Int,
            resultData: Intent?
    ) {
        this.context = context
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(filesLogic.context, RECOGNITION_IN_PROGRESS, Toast.LENGTH_LONG).show()
            inputImage = if (resultData == null) {
                FROM_WHERE = true
                InputImage.fromFilePath(filesLogic.context, Uri.fromFile(filesLogic.lastCreated))
            } else {
                FROM_WHERE = false
                prepareData(contentResolver, resultData.data)
                val bm = filesLogic.getBitmapFromAbsolutePath(filesLogic.lastCreated!!.absolutePath) ?: return
                InputImage.fromBitmap(bm, 0)
            }
            if (inputImage == null) {
                Toast.makeText(context, RECOGNITION_DIDNT_SUCCEED, Toast.LENGTH_LONG).show()
                return
            }
            recognizer.process(inputImage!!).addOnCompleteListener {
                if (it.result == null || it.result!!.text.isEmpty()) {
                    Toast.makeText(context, RECOGNITION_DIDNT_SUCCEED, Toast.LENGTH_LONG).show()
                } else {
                    if (filesLogic.lastCreated != null) {
                        filesLogic.lastCreated!!.delete()
                    }
                    val recognizedStrings = mutableListOf<String>()
                    it.result!!.textBlocks.forEach { textBlock ->
                        recognizedStrings.add(textBlock.text)
                    }
                    val serialNumberAndIndication = serialNumberAndIndication(recognizedStrings)
                    CheckingDialogFragment.newInstance(serialNumberAndIndication.first,
                            serialNumberAndIndication.second)
                            .show(context.supportFragmentManager, RECOGNITION_RESULT)
                }
            }
        }
    }
}