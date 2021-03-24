package com.valeria.criminalintent

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import androidx.lifecycle.Observer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.io.File

import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val DETAIL_PICTURE = "detail picture"

private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1
private const val REQUEST_CONTACT = 2
private const val REQUEST_PHOTO = 3
private const val TAG = "CrimeFragments"
private const val DATE_FORMAT = "EEE, MMM, dd"


class CrimeFragment : Fragment(), DatePickerFragment.Callbacks {
    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView

    private val crimeDetailListViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID =
            arguments?.getSerializable(ARG_CRIME_ID) as UUID //Когда фрагменту требуется получить доступ к его аргументам, он ссылается на свойство arguments класса Fragment, а затем один из геттеров Bundle для конкретного типа. В функции  CrimeFragment.onCreate(...) нужно извлечь UUID из аргументов фрагмента. Запишем идентификатор в журнал, чтобы убедиться, что аргумент прикрепился правильно.
        // Log.d(TAG,"args bundle crime ID: $crimeId")
        //загрузка преступления с БД
        crimeDetailListViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_data) as Button
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        photoView = view.findViewById(R.id.crime_photo) as ImageView
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        return view
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) { // набл за значением и обновление польз интер при публик нов данных
        super.onViewCreated(view, savedInstanceState)
        crimeDetailListViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    this.crime = crime
                    photoFile = crimeDetailListViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(requireActivity(),"com.valeria.criminalintent.fileprovider",photoFile)
                    updateUI()
                }
            })
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {

            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {

            }

        }
        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject)
                )
            }.also { intent ->
                //startActivity(intent)
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        suspectButton.apply {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
                val packageManager: PackageManager = requireActivity().packageManager
                val resolvedActivity: ResolveInfo? =
                    packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
                if (resolvedActivity ==null){
                    isEnabled = false
                }
            }
        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null){
                isEnabled = false
            }

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(captureImage,PackageManager.MATCH_DEFAULT_ONLY)

                for (cameraActivity in cameraActivities){
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }

        photoView.setOnClickListener{
            if(photoFile.exists()){
                DetailDialogPicture.newInstance(photoFile)
                    .apply { show(this@CrimeFragment.parentFragmentManager, DETAIL_PICTURE) }
            }
        }
       
    }

    override fun onStop() {
        super.onStop()
        crimeDetailListViewModel.saveCrime(crime)
    }



    override fun onDetach() {
        super.onDetach()
        // скрыть вероятность неверного отклика
        requireActivity().revokeUriPermission(photoUri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()
        // solvedCheckBox.isChecked = crime.isSolved
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }

        if (crime.suspect.isNotBlank()){
            suspectButton.text = crime.suspect
        }

        updatePhotoView()
    }

    private fun updatePhotoView(){
        if (photoFile.exists()){
            photoView.visibility = View.VISIBLE
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //вызывается перед ViewCreated
        when {
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                //Указать, для каких полей запрос должен возвращать значения
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                //Выполняемый здесь запрос - contactUri похож на предложение "where"
                val cursor = requireActivity().contentResolver
                    .query(contactUri!!, queryFields, null, null,null)
                cursor?.use {
                    //Verify cursor contains at least one result
                    if (it.count == 0){
                        return
                    }

                    // Первый столбец первой стороки данных - это имя вашего подозреваемого
                    it.moveToFirst()
                    val suspect = it.getString(0) // для перемещения содеримого первого столбца в виде строки; это строка - имя подозреваемого
                    crime.suspect = suspect
                    crimeDetailListViewModel.saveCrime(crime)// сохранение в БД
                    suspectButton.text = suspect
                }
            }

            requestCode == REQUEST_PHOTO -> {
                //отозвать разрешение в случае действительного результата
                requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()}
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    companion object {  // Включите в CrimeFragment функцию newInstance(UUID), который получает UUID, создает пакет аргументов, создает экземпляр фрагмента, а затем присоединяет аргументы к фрагменту
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }

}