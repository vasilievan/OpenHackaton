package sudo.openhackaton.logic

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.googlecode.tesseract.android.TessBaseAPI
import sudo.openhackaton.logic.Constants.ALLOWED_SYMBOLS
import sudo.openhackaton.logic.Constants.DATA_FILE
import sudo.openhackaton.logic.Constants.IMAGE_DOCUMENT_TYPE
import sudo.openhackaton.logic.Constants.LANGUAGE
import sudo.openhackaton.logic.Constants.MAIN_DIR
import sudo.openhackaton.logic.Constants.REQUEST_CODE_PERMISSIONS
import sudo.openhackaton.logic.Constants.REQUEST_CODE_PICTURE
import sudo.openhackaton.logic.Constants.TESS_DIR
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream


class Logic {
    fun askForPermissions(context: Context, activity: Activity) {
        if ((ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    fun beginning(assets: AssetManager) {
        val myDir = File("$MAIN_DIR/$TESS_DIR")
        if (!myDir.exists()) {
            myDir.mkdirs()
        }
        val dataFile = File("$MAIN_DIR/$TESS_DIR/")
        if (!dataFile.exists()) {
            dataFile.createNewFile()
            fulFillDataFile(assets, dataFile)
        }
    }

    private fun fulFillDataFile(assets: AssetManager, dataFile: File) {
        val fous = FileOutputStream(dataFile)
        fous.write(assets.open("$TESS_DIR/$DATA_FILE").readBytes())
        fous.flush()
        fous.close()
    }

    private fun deleteBrokenFile() {
        val brokenFile = File("$MAIN_DIR/$TESS_DIR/$DATA_FILE")
        if (brokenFile.exists()) {
            brokenFile.delete()
        }
    }

    fun recognizeText(assets: AssetManager, bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        val tessBaseAPI = TessBaseAPI()
        try {
            tessBaseAPI.init(MAIN_DIR, LANGUAGE)
        } catch (e: RuntimeException) {
            deleteBrokenFile()
            beginning(assets)
            tessBaseAPI.init(MAIN_DIR, LANGUAGE)
        }
        //tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, ALLOWED_SYMBOLS)
        tessBaseAPI.setImage(bitmap)
        val result = tessBaseAPI.utF8Text
        tessBaseAPI.end()
        return result
    }

    fun performFileSearch(activity: Activity) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = IMAGE_DOCUMENT_TYPE
        ActivityCompat.startActivityForResult(activity, intent, REQUEST_CODE_PICTURE, null)
    }

    fun getBitmapFromUri(contentResolver: ContentResolver, uri: Uri?): Bitmap? {
        if (uri == null) return null
        val parcelFileDescriptor: ParcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r") ?: return null
        val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }
}