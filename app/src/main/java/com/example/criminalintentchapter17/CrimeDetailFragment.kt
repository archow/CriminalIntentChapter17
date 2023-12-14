package com.example.criminalintentchapter17

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.criminalintentchapter17.databinding.FragmentCrimeDetailBinding
import com.example.criminalintentchapter17.viewmodels.CrimeDetailViewModel
import com.example.criminalintentchapter17.viewmodels.CrimeDetailViewModelFactory
import kotlinx.coroutines.launch
import java.util.Date

private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeDetailFragment : Fragment() {
    private val args: CrimeDetailFragmentArgs by navArgs()
    private val crimeDetailViewModel : CrimeDetailViewModel by viewModels {
        CrimeDetailViewModelFactory(args.crimeId)
    }
    private var _binding: FragmentCrimeDetailBinding? = null
    private val binding: FragmentCrimeDetailBinding
        get() = checkNotNull(_binding) {
            "CrimeDetailFragment binding should not be null. Is the view visible?"
        }
    private val selectSuspect = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let {
            parseContact(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCrimeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            crimeTitle.doOnTextChanged { text, _, _, _ ->
                crimeDetailViewModel.updateCrime { it.copy(title=text.toString()) }
            }
            crimeSolved.setOnCheckedChangeListener { _, isChecked ->
                crimeDetailViewModel.updateCrime { it.copy(isSolved = isChecked) }
            }
            crimeSuspect.apply {
                setOnClickListener {
                    selectSuspect.launch(null)
                    val suspectIntent = selectSuspect.contract.createIntent(
                        requireContext(),
                        null
                    )
                    isEnabled = canResolveIntent(suspectIntent)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                crimeDetailViewModel.crime.collect {
                    it?.let { updateUI(it) }
                }
            }
        }

        setFragmentResultListener(DatePickerFragment.REQUEST_KEY_DATE) { _, bundle ->
            val newDate = bundle.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE) as Date
            crimeDetailViewModel.updateCrime { it.copy(date=newDate) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI(crime: Crime) {
        binding.apply {
            if (crime.title != crimeTitle.text.toString()) {
                crimeTitle.setText(crime.title)
            }
            crimeSolved.isChecked = crime.isSolved
            crimeDate.apply {
                text = crime.date.toString()
                setOnClickListener {
                    findNavController().navigate(
                        CrimeDetailFragmentDirections.selectDate(crime.date)
                    )
                }
            }
            crimeSuspect.text = crime.suspect.ifEmpty { getString(R.string.crime_suspect_text) }
            crimeReport.setOnClickListener {
                val reportIntent = Intent(Intent.ACTION_SEND).apply {
                    type="text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
                    putExtra(Intent.EXTRA_TEXT, getCrimeReport(crime))
                }
                val chooserIntent = Intent.createChooser(
                    reportIntent,
                    getString(R.string.send_report)
                )
                startActivity(chooserIntent)
            }
        }
    }

    private fun getCrimeReport(crime: Crime): String {
        val solvedString =
            if (crime.isSolved)
                getString(R.string.crime_report_solved)
            else getString(R.string.crime_report_unsolved)

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspectText = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(
            R.string.crime_report,
            crime.title,
            dateString,
            solvedString,
            suspectText
        )
    }

    private fun parseContact(contactUri: Uri) {
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
        val queryCursor = requireActivity().contentResolver
            .query(contactUri, queryFields, null, null, null)
        queryCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val selection = cursor.getString(0)
                crimeDetailViewModel.updateCrime { it.copy(suspect = selection) }
            }
        }
    }
    private fun canResolveIntent(intent: Intent): Boolean {
        val packageManager = requireActivity().packageManager
        val resolveInfo: ResolveInfo? = packageManager
            .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo != null
    }
}