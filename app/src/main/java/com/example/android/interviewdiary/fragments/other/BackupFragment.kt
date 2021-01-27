package com.example.android.interviewdiary.fragments.other

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.databinding.FragmentBackupBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

// TODO Implement Database Export Restore Function

@AndroidEntryPoint
class BackupFragment : Fragment(R.layout.fragment_backup) {

    private val viewModel: BackupViewModel by viewModels(ownerProducer = { this })

    lateinit var binding: FragmentBackupBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding = FragmentBackupBinding.bind(view)

        binding.btnBackup.setOnClickListener {
//            viewModel.checkpoint(
//                (SimpleSQLiteQuery("pragma wal_checkpoint(full)")),
//                requireContext()
//            )
            Snackbar.make(
                requireContext(),
                view,
                getString(R.string.snack_bar_function_not_implemented),
                Snackbar.LENGTH_SHORT
            ).show()
        }

        binding.btnRestore.setOnClickListener {
            Snackbar.make(
                requireContext(),
                view,
                getString(R.string.snack_bar_function_not_implemented),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
}