package sudo.openhackaton.logic

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import sudo.openhackaton.logic.Constants.IMAGE_DOCUMENT_TYPE
import sudo.openhackaton.logic.Constants.JPG
import sudo.openhackaton.logic.Constants.MAIN_DIR
import sudo.openhackaton.logic.Constants.MODE_READ
import sudo.openhackaton.logic.Constants.REQUEST_CODE_PERMISSIONS
import sudo.openhackaton.logic.Constants.REQUEST_CODE_PICTURE
import sudo.openhackaton.logic.Constants.ROOT
import java.io.File
import java.io.FileDescriptor
import java.io.IOException
import java.util.*

class FilesLogic(val context: Context, private val activity: Activity) {
    var lastCreated: File? = null
    var files: Queue<File> = LinkedList()

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

    fun getBitmapFromUri(contentResolver: ContentResolver, uri: Uri?): Bitmap? {
        if (uri == null) return null
        val parcelFileDescriptor: ParcelFileDescriptor =
            contentResolver.openFileDescriptor(uri, MODE_READ) ?: return null
        val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    fun performFileSearch() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = IMAGE_DOCUMENT_TYPE
        startActivityForResult(activity, intent, REQUEST_CODE_PICTURE, null)
    }

    fun createImageFile(): File? {
        val time = Date().time.toString()
        val file = File("$MAIN_DIR/$ROOT/$time$JPG")
        return try {
            file.createNewFile()
            lastCreated = file
            files.add(file)
            lastCreated
        } catch (e: IOException) {
            lastCreated = null
            lastCreated
        }
    }
}