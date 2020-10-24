package sudo.openhackaton.logic

object Constants {
    const val ROOT = "sudo"
    const val IMAGE_DOCUMENT_TYPE = "image/*"
    const val RECOGNITION_DIDNT_SUCCEED = "Text wasn't recognized. :( Try again."
    const val MODE_READ = "r"
    const val RECOGNITION_IN_PROGRESS = "Recognition is in process..."
    const val REQUEST_CODE_PERMISSIONS = 314
    const val REQUEST_CODE_PICTURE = 44
    const val REQUEST_TAKE_A_PHOTO = 45
    const val JPG = ".jpg"
    const val EMPTY_STRING = ""
    const val CAPTURE_FAILED = "Photo capture failed: "
    const val TAG = "TAG"
    const val BINDING_FAILED = "Use case binding failed"
    const val TEXT_FROM_RECOGNITION = "textFromRecognition"
    lateinit var cameraLogic: CameraLogic
    lateinit var MAIN_DIR: String
}