package com.example.criminalintentchapter17

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.criminalintentchapter17.databinding.FragmentCrimeListBinding
import com.example.criminalintentchapter17.viewmodels.CrimeListViewModel
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class CrimeListFragment : Fragment() {
    private var _binding: FragmentCrimeListBinding? = null
    private val binding: FragmentCrimeListBinding
        get() = checkNotNull(_binding) {
            "CrimeListFragment should not be null. Is the view visible?"
        }
    private val crimeListViewModel : CrimeListViewModel by viewModels()
    private lateinit var crimeListAdapter: CrimeListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCrimeListBinding.inflate(inflater, container, false)
        binding.crimeRecyclerView.apply {
            layoutManager = LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL,
                false)
            crimeListAdapter = CrimeListAdapter {
                findNavController().navigate(
                    CrimeListFragmentDirections.showCrimes(it)
                )
            }
            adapter = crimeListAdapter
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                crimeListViewModel.crimes.collect {
                    crimeListAdapter.submitList(it)
                }
            }
        }

        //also add the menu here
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_crime_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.new_crime -> {
                        showNewCrime()
                        true
                    }
                    else -> true
                }
            }

        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showNewCrime() {
        viewLifecycleOwner.lifecycleScope.launch {
            val newCrime = Crime(UUID.randomUUID(), "", Date(), false)
            crimeListViewModel.addCrime(newCrime)
            findNavController().navigate(
                CrimeListFragmentDirections.showCrimes(newCrime.id)
            )
        }
    }
}