package com.example.praticum.core.common;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.praticum.R;
import com.google.android.material.button.MaterialButton;

public class FilterSortFragment extends Fragment {

    private static final String ARG_SORT_OPTIONS = "sort_options";
    private static final String ARG_FILTER_OPTIONS = "filter_options";

    public interface OnFilterSortListener {
        void onApplyFilterSort(
                String sortBy,
                boolean ascending,
                String filterBy
        );
    }

    private OnFilterSortListener listener;

    private Spinner spinnerSortBy;
    private Spinner spinnerFilterBy;
    private RadioGroup radioSortOrder;

    public FilterSortFragment() {
        // Constructeur obligatoire
    }

    public static FilterSortFragment newInstance(
            String[] sortOptions,
            String[] filterOptions
    ) {
        FilterSortFragment fragment = new FilterSortFragment();

        Bundle args = new Bundle();
        args.putStringArray(ARG_SORT_OPTIONS, sortOptions);
        args.putStringArray(ARG_FILTER_OPTIONS, filterOptions);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        Fragment parent = getParentFragment();

        if (parent instanceof OnFilterSortListener) {
            listener = (OnFilterSortListener) parent;
        } else if (context instanceof OnFilterSortListener) {
            listener = (OnFilterSortListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_filter_sort,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        spinnerSortBy = view.findViewById(R.id.spinnerSortBy);
        spinnerFilterBy = view.findViewById(R.id.spinnerFilterBy);
        radioSortOrder = view.findViewById(R.id.radioSortOrder);

        MaterialButton btnReset =
                view.findViewById(R.id.btnResetFilterSort);

        MaterialButton btnApply =
                view.findViewById(R.id.btnApplyFilterSort);

        Bundle args = getArguments();

        String[] sortOptions = args != null
                ? args.getStringArray(ARG_SORT_OPTIONS)
                : null;

        String[] filterOptions = args != null
                ? args.getStringArray(ARG_FILTER_OPTIONS)
                : null;

        if (sortOptions == null || sortOptions.length == 0) {
            sortOptions = new String[]{"Nom"};
        }

        if (filterOptions == null || filterOptions.length == 0) {
            filterOptions = new String[]{"Tous"};
        }

        configurerSpinner(spinnerSortBy, sortOptions);
        configurerSpinner(spinnerFilterBy, filterOptions);

        btnReset.setOnClickListener(v -> {
            spinnerSortBy.setSelection(0);
            spinnerFilterBy.setSelection(0);
            radioSortOrder.check(R.id.radioAscending);
        });

        btnApply.setOnClickListener(v -> appliquer());
    }

    private void configurerSpinner(
            Spinner spinner,
            String[] options
    ) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                options
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);
    }

    private void appliquer() {
        if (listener == null) {
            return;
        }

        String sortBy =
                spinnerSortBy.getSelectedItem().toString();

        String filterBy =
                spinnerFilterBy.getSelectedItem().toString();

        boolean ascending =
                radioSortOrder.getCheckedRadioButtonId()
                        == R.id.radioAscending;

        listener.onApplyFilterSort(
                sortBy,
                ascending,
                filterBy
        );
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}