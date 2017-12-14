package io.github.cvrunmin.toolbox

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.Appcompat
import org.jetbrains.anko.sdk25.coroutines.onSeekBarChangeListener

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SeekBarDialogFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SeekBarDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SeekBarDialogFragment() : DialogFragment() {
    private var _min : Int = 0
    private var _max : Int = 0
    private var _current : Int = 0
    private var _titleId : Int = 0
    private var _leftId : Int = 0
    private var _middleId : Int = 0
    private var _rightId : Int = 0

    var eventOkListener : OnDialogOkListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            _min = arguments.getInt(ARG_MIN)
            _max = arguments.getInt(ARG_MAX)
            _current = arguments.getInt(ARG_CURRENT)
            _titleId = arguments.getInt(ARG_TITLE_ID)
            _leftId = arguments.getInt(ARG_LEFT_ID)
            _middleId = arguments.getInt(ARG_MIDDLE_ID)
            _rightId = arguments.getInt(ARG_RIGHT_ID)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return with(activity){
            alert(Appcompat,"",title = getText(_titleId).toString()){
                var seekBar1 : SeekBar? = null
                customView {
                    verticalLayout {
                        seekBar1 = seekBar {
                            progress = _current + Math.abs(_min)
                            max = Math.abs(_min) + _max
                        }.lparams(width = matchParent)
                        var text = textView {
                            textAppearance = android.R.attr.textAppearanceMedium
                            text = getString(_middleId)
                        }
                        seekBar1!!.onSeekBarChangeListener {
                            onProgressChanged { seekBar, i, b ->
                                val processed = seekBar1!!.progress + _min
                                if (processed > 0) {
                                    text.text = String.format(getText(_rightId).toString(), Math.abs(processed))
                                } else if (processed < 0) {
                                    text.text = String.format(getText(_leftId).toString(), Math.abs(processed))
                                } else {
                                    text.text = String.format(getText(_middleId).toString(), Math.abs(processed))
                                }
                            }
                        }
                        seekBar1!!.progress = _current + Math.abs(_min)
                    }
                }
                cancelButton {  }
                okButton {
                    eventOkListener?.onDialogOk(seekBar1!!.progress + _min)
                }
            }.build()
        }
    }

    override fun onDetach() {
        super.onDetach()
        eventOkListener = null
    }

    interface OnDialogOkListener{
        fun onDialogOk(finProgress : Int)
    }

    companion object {
        private val ARG_CONTEXT = "context"
        private val ARG_MIN = "min"
        private val ARG_MAX = "max"
        private val ARG_CURRENT = "currnet"
        private val ARG_TITLE_ID = "title_id"
        private val ARG_LEFT_ID = "left_id"
        private val ARG_MIDDLE_ID = "middle_id"
        private val ARG_RIGHT_ID = "right_id"

        fun newInstance(context : Context, min : Int, max : Int, current : Int, titleid:Int, leftid:Int, middleid : Int, rightid : Int): SeekBarDialogFragment {
            val fragment = SeekBarDialogFragment()
            val args = Bundle()
            args.putInt(ARG_MIN, min)
            args.putInt(ARG_MAX, max)
            args.putInt(ARG_CURRENT, current)
            args.putInt(ARG_TITLE_ID, titleid)
            args.putInt(ARG_LEFT_ID, leftid)
            args.putInt(ARG_MIDDLE_ID, middleid)
            args.putInt(ARG_RIGHT_ID, rightid)
            fragment.arguments = args
            return fragment
        }
    }
}
