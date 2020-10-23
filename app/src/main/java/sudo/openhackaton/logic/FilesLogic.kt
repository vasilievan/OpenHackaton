package sudo.openhackaton.logic

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import sudo.openhackaton.logic.Constants.IMAGE_DOCUMENT_TYPE
import sudo.openhackaton.logic.Constants.JPG
import sudo.openhackaton.logic.Constants.MAIN_DIR
import sudo.openhackaton.logic.Constants.REQUEST_CODE_PERMISSIONS
import sudo.openhackaton.logic.Constants.REQUEST_CODE_PICTURE
import sudo.openhackaton.logic.Constants.ROOT
import java.io.File
import java.io.IOException
import java.util.*

class FilesLogic(val context: Context, private val activity: Activity) {
    var bitmap: Bitmap? = null
    var lastCreated: File? = null
    private set

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

    fun beginning() {
        MAIN_DIR = context.getExternalFilesDir(null).toString()
        val myDir = File("$MAIN_DIR/$ROOT")
        if (!myDir.exists()) {
            myDir.mkdirs()
        }
    }

    fun getBitmapFromAbsolutePath(path: String?): Bitmap? {
        try {
            val options = BitmapFactory.Options()
            return BitmapFactory.decodeFile(path, options)
        } catch (e: IOException) {
        }
        return null
    }

    fun performFileSearch() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = IMAGE_DOCUMENT_TYPE
        startActivityForResult(activity, intent, REQUEST_CODE_PICTURE, null)
    }

    fun createImageFile(): File? {
        val time = Date().time.toString()
        val file = File("$MAIN_DIR/$ROOT/$time$JPG")
        return try {
            file.createNewFile()
            lastCreated = file
            lastCreated
        } catch (e: IOException) {
            lastCreated = null
            lastCreated
        }
    }
}