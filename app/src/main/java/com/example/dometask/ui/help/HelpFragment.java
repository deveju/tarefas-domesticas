package com.example.dometask.ui.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dometask.R;
import com.example.dometask.databinding.FragmentHelpBinding;
import com.example.dometask.databinding.FragmentHomeBinding;
import com.example.dometask.ui.home.HomeViewModel;

public class HelpFragment extends Fragment {

private FragmentHelpBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel helpViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHelpBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHelp;

        // textView.setText(R.string.fragment_help_text);

        return root;
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}