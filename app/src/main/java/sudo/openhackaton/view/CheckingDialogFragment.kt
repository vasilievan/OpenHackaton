package sudo.openhackaton.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import sudo.openhackaton.R

import sudo.openhackaton.logic.Constants.TEXT_FROM_RECOGNITION


class CheckingDialogFragment : DialogFragment() {

    private var textFromRecognition: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textFromRecognition = arguments!!.getString(TEXT_FROM_RECOGNITION)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.alert_background)
        val view = inflater.inflate(R.layout.dialog_check_if_text_correct, container, false)
        val textView = view.findViewById<TextView>(R.id.text_from_recognition1)

        textView.text = textFromRecognition
        return view
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.30).toInt()
        dialog!!.window?.setLayout(width, height)
    }


    companion object {
        private lateinit var currentDialog: CheckingDialogFragment
        fun newInstance(textFromRecognition: String?): DialogFragment {
            val fragment = CheckingDialogFragment()
            currentDialog = fragment
            val args = Bundle()
            args.putString(TEXT_FROM_RECOGNITION, textFromRecognition)
            fragment.arguments = args
            return fragment
        }

        fun close() {
            if (this::currentDialog.isInitialized) {
                currentDialog.dismiss()
            }
        }
    }
}