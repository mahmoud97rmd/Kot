package com.tradingapp.metatrader.app.features.drawingui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tradingapp.metatrader.app.databinding.BottomsheetDrawingStyleBinding

class DrawingStyleBottomSheet : BottomSheetDialogFragment() {

    interface Listener {
        fun onApplyStyle(colorHex: String?, width: Float?)
        fun onToggleLock()
        fun onDelete()
    }

    private var _binding: BottomsheetDrawingStyleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomsheetDrawingStyleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = requireArguments()
        val type = args.getString(ARG_TYPE, "--")
        val id = args.getString(ARG_ID, "--")
        val color = args.getString(ARG_COLOR, "#FFFFFF")
        val width = args.getFloat(ARG_WIDTH, 2f)
        val locked = args.getBoolean(ARG_LOCKED, false)

        binding.infoText.text = "Type: $type | ID: $id | Locked: $locked"
        binding.colorHexInput.setText(color)
        binding.widthInput.setText(width.toString())

        val listener = (parentFragment as? Listener) ?: (activity as? Listener)

        binding.applyBtn.setOnClickListener {
            val c = binding.colorHexInput.text?.toString()?.trim()
            val w = binding.widthInput.text?.toString()?.trim()?.toFloatOrNull()
            listener?.onApplyStyle(
                colorHex = c?.takeIf { it.isNotEmpty() },
                width = w
            )
            dismissAllowingStateLoss()
        }

        binding.lockBtn.setOnClickListener {
            listener?.onToggleLock()
            dismissAllowingStateLoss()
        }

        binding.deleteBtn.setOnClickListener {
            listener?.onDelete()
            dismissAllowingStateLoss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_TYPE = "type"
        private const val ARG_COLOR = "color"
        private const val ARG_WIDTH = "width"
        private const val ARG_LOCKED = "locked"

        fun newInstance(id: String, type: String, color: String, width: Float, locked: Boolean): DrawingStyleBottomSheet {
            return DrawingStyleBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID, id)
                    putString(ARG_TYPE, type)
                    putString(ARG_COLOR, color)
                    putFloat(ARG_WIDTH, width)
                    putBoolean(ARG_LOCKED, locked)
                }
            }
        }
    }
}
