package com.valeria.criminalintent

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import java.io.File
import java.lang.IllegalStateException

private const val ARG_IMAGE = "image"

class DetailDialogPicture(): DialogFragment(){
    private lateinit var photoFile: File
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater

            val view = inflater.inflate(R.layout.detail_dialog_picture, null)

            builder.setView(view)

            val crimePicture = view.findViewById(R.id.crimePicture) as ImageView

            photoFile = arguments?.getSerializable(ARG_IMAGE) as File

            val bitmap = getScaledBitmap(photoFile.path, requireActivity())

            crimePicture.setImageBitmap(bitmap)

            builder.setTitle("Photo")
                .setNegativeButton("OK", DialogInterface.OnClickListener{_,_ ->dialog?.cancel()})

            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")

    }

    companion object{
        fun newInstance(photoFile: File): DetailDialogPicture{
            val args = Bundle().apply {
                putSerializable(ARG_IMAGE, photoFile)
            }
            return DetailDialogPicture().apply {
                arguments = args
            }
        }
    }

}