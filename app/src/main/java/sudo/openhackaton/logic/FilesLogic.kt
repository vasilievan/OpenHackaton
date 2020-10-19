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
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import sudo.openhackaton.logic.Constants.DATA_FILE
import sudo.openhackaton.logic.Constants.IMAGE_DOCUMENT_TYPE
import sudo.openhackaton.logic.Constants.JPG
import sudo.openhackaton.logic.Constants.MAIN_DIR
import sudo.openhackaton.logic.Constants.MODE_READ
import sudo.openhackaton.logic.Constants.REQUEST_CODE_PERMISSIONS
import sudo.openhackaton.logic.Constants.REQUEST_CODE_PICTURE
import sudo.openhackaton.logic.Constants.ROOT
import sudo.openhackaton.logic.Constants.TESS_DIR
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class FilesLogic(val context: Context, private val activity: Activity, private val assets: AssetManager) {
    var currentPhotoPath: String? = null
    var currentCroppedPhotoPath: String? = null

    fun askForPermissions() {
        if ((ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    fun getBitmapFromUri(contentResolver: ContentResolver, uri: Uri?): Bitmap? {
        if (uri == null) return null
        val parcelFileDescriptor: ParcelFileDescriptor = contentResolver.openFileDescriptor(uri,
            MODE_READ
        )
            ?: return null
        val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    fun getBitmapFromAbsolutePath(path: String?): Bitmap? {
        try {
            val options = BitmapFactory.Options()
            return BitmapFactory.decodeFile(path, options)
        } catch (e: IOException) {
        }
        return null
    }

    fun beginning() {
        MAIN_DIR = context.getExternalFilesDir(null).toString()
        loadServiceFiles()
    }

    fun loadServiceFiles() {
        val myDir = File("$MAIN_DIR/$ROOT/$TESS_DIR")
        if (!myDir.exists()) {
            myDir.mkdirs()
        }

        val dataFile = File(myDir, DATA_FILE)
        if (!dataFile.exists()) {
            dataFile.createNewFile()
            fulFillDataFile(dataFile)
        }
    }

    private fun fulFillDataFile(dataFile: File) {
        val fous = FileOutputStream(dataFile)
        fous.write(assets.open("$TESS_DIR/$DATA_FILE").readBytes())
        fous.flush()
        fous.close()
    }

    fun deleteBrokenFile() {
        val brokenFile = File("$MAIN_DIR/$ROOT/$TESS_DIR/$DATA_FILE")
        if (brokenFile.exists()) {
            brokenFile.delete()
        }
    }

    fun performFileSearch() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = IMAGE_DOCUMENT_TYPE
        startActivityForResult(activity, intent, REQUEST_CODE_PICTURE, null)
    }

    fun createImageFile(suffix: String): File? {
        val time = Date().time.toString()
        val file = File("$MAIN_DIR/$ROOT/$time$suffix$JPG")
        return try {
            file.createNewFile()
            if (suffix.isEmpty()) {
                currentPhotoPath = file.absolutePath
            } else {
                currentCroppedPhotoPath = file.absolutePath
            }
            file
        } catch (e: IOException) {
            null
        }
    }
}