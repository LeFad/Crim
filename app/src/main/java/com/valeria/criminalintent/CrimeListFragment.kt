package com.valeria.criminalintent

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

private const val DATE_FORMAT = "dd.MM.yyyy(EEEE)  HH:mm"
private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {

    interface Callbacks{ //интерфейс обратного вызова - передает события на хост активити
        fun onCrimeSelected(crimeId: UUID)
    }
    private var callbacks: Callbacks? = null // св-во, чтобы удерживать объект коллбэкс

    private lateinit var crimeRecyclerView: RecyclerView
   // private var adapter: CrimeAdapter? = null
    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this).get(CrimeListViewModel::class.java)
    }

    //3//
    override fun onAttach(context: Context) { //фрагмент прикрепл к активити  //для установки св-ва коллбэкс
        super.onAttach(context)
        callbacks = context as Callbacks?
    }
    ////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView = view.findViewById(R.id.crime_recycling_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter

       // updateUI()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(//регистрация наблюдателя за экз LiveData и связи ннаблюдения с жизн циклом др компон
            viewLifecycleOwner,
            Observer { crimes ->                     // реакция на новые данные из LiveData
                crimes?.let {
                    Log.i(TAG, "Got crimes ${crimes.size}")
                    updateUI(crimes)
                }
            }
        )
    }

    //3//
    override fun onDetach() {//для удаления св-ва коллбэкс
        super.onDetach()
        callbacks = null
    }
    ////

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime ->{
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }

    private fun updateUI(crimes: List<Crime>) {
        //val crimes = crimeListViewModel.crimes
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter
    }

    private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title_item)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_data_item)
        private val solvedImgV: ImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener {
                crimeListViewModel.deleteCrime(crime)
                true
            }
        }

        fun bind(crime: Crime){
            this.crime = crime
           // val sdf = SimpleDateFormat(DATE_FORMAT)
            titleTextView.text = this.crime.title
//            dateTextView.text = this.crime.date.toString()
           // dateTextView.text = sdf.format(this.crime.date)
            dateTextView.text = DateFormat.format(DATE_FORMAT,this.crime.date).toString()
            solvedImgV.visibility = if (crime.isSolved){
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View) {
           // Toast.makeText(context, "${crime.title} pressed!", Toast.LENGTH_SHORT).show()
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    private inner class CrimeAdapter(var crimes: List<Crime>) :
        RecyclerView.Adapter<CrimeHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {

            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun getItemCount() = crimes.size

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }
    }
}

