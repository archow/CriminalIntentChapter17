package com.example.criminalintentchapter17

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.criminalintentchapter17.databinding.ListItemCrimeBinding
import java.util.UUID

class CrimeHolder(
    private val binding: ListItemCrimeBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(crime: Crime, onCrimeClicked: (crimeId: UUID) -> Unit) {
        binding.apply {
            crimeTitle.text = crime.title
            crimeDate.text = crime.date.toString()
            crimeSolved.visibility = if (crime.isSolved) View.VISIBLE else View.GONE
            root.setOnClickListener {
                onCrimeClicked(crime.id)
            }
        }
    }
}

class CrimeListAdapter(
    private val onCrimeClicked: (crimeId: UUID) -> Unit
) : ListAdapter<Crime, CrimeHolder>(CrimeDiffUtil){
    private lateinit var crimes: List<Crime>

    override fun submitList(list: List<Crime>?) {
        super.submitList(list)
        crimes = list ?: emptyList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemCrimeBinding.inflate(layoutInflater, parent, false)
        return CrimeHolder(binding)
    }

    override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
        val crime = crimes[position]
        holder.bind(crime, onCrimeClicked)
    }
}

object CrimeDiffUtil : DiffUtil.ItemCallback<Crime>() {
    override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean =
        oldItem == newItem && oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean =
        oldItem.id == newItem.id
}