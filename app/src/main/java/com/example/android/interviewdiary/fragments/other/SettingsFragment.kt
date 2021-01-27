package com.example.android.interviewdiary.fragments.other

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.databinding.FragmentSettingsBinding
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    // TODO Implement manually switch DayNight Theme functionality
    // TODO Implement manually SetLocale functionality

    lateinit var binding: FragmentSettingsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSettingsBinding.bind(view)


        binding.darkMode.root.setOnClickListener {
            binding.darkMode.switchDarkMode.isChecked = !binding.darkMode.switchDarkMode.isChecked
        }

        binding.darkMode.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                Snackbar.make(
                    requireContext(),
                    view,
                    getString(R.string.snack_bar_function_not_implemented),
                    Snackbar.LENGTH_SHORT
                ).show()
            else
                Snackbar.make(
                    requireContext(),
                    view,
                    getString(R.string.snack_bar_function_not_implemented),
                    Snackbar.LENGTH_SHORT
                ).show()
        }
    }
}
