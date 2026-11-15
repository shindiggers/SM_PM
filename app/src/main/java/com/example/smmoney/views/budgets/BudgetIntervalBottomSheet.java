package com.example.smmoney.views.budgets;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.smmoney.R;
import com.example.smmoney.misc.CalExt;
import com.example.smmoney.misc.CurrencyExt;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.example.smmoney.views.CheckBoxTint;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class BudgetIntervalBottomSheet extends BottomSheetDialogFragment {

    public interface OnIntervalSavedListener {
        void onIntervalSaved(GregorianCalendar fromDate, GregorianCalendar toDate, boolean isOngoing, double amount, boolean resetRollover);
    }

    private static final String ARG_FROM_MILLIS = "from_millis";
    private static final String ARG_TO_MILLIS = "to_millis";
    private static final String ARG_IS_ONGOING = "is_ongoing";
    private static final String ARG_AMOUNT = "amount";
    private static final String ARG_RESET_ROLLOVER = "reset_rollover";
    private static final String ARG_IS_EDIT = "is_edit";

    private GregorianCalendar fromDate;
    private GregorianCalendar toDate;
    private boolean isOngoing;
    private double amount;
    private boolean resetRollover;
    private boolean isEdit;

    private OnIntervalSavedListener listener;

    private TextView sheetTitleTextView;
    private TextView fromDateTextView;
    private TextView toDateTextView;
    private View toDateContainer;
    private CheckBox ongoingCheckBox;
    private EditText amountEditText;
    private CheckBox resetRolloverCheckBox;

    public static BudgetIntervalBottomSheet newInstance(
            @NonNull GregorianCalendar fromDate,
            @Nullable GregorianCalendar toDate,
            boolean isOngoing,
            double amount,
            boolean resetRollover,
            boolean isEdit) {
        BudgetIntervalBottomSheet fragment = new BudgetIntervalBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_FROM_MILLIS, fromDate.getTimeInMillis());
        if (toDate != null) {
            args.putLong(ARG_TO_MILLIS, toDate.getTimeInMillis());
        } else {
            args.putLong(ARG_TO_MILLIS, -1L);
        }
        args.putBoolean(ARG_IS_ONGOING, isOngoing);
        args.putDouble(ARG_AMOUNT, amount);
        args.putBoolean(ARG_RESET_ROLLOVER, resetRollover);
        args.putBoolean(ARG_IS_EDIT, isEdit);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnIntervalSavedListener(OnIntervalSavedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            long fromMillis = getArguments().getLong(ARG_FROM_MILLIS, System.currentTimeMillis());
            this.fromDate = new GregorianCalendar();
            this.fromDate.setTimeInMillis(fromMillis);

            long toMillis = getArguments().getLong(ARG_TO_MILLIS, -1L);
            if (toMillis > 0) {
                this.toDate = new GregorianCalendar();
                this.toDate.setTimeInMillis(toMillis);
            } else {
                this.toDate = (GregorianCalendar) this.fromDate.clone();
                this.toDate.add(Calendar.MONTH, 1);
                this.toDate.add(Calendar.DAY_OF_MONTH, -1);
            }

            this.isOngoing = getArguments().getBoolean(ARG_IS_ONGOING, true);
            this.amount = getArguments().getDouble(ARG_AMOUNT, 0.0d);
            this.resetRollover = getArguments().getBoolean(ARG_RESET_ROLLOVER, false);
            this.isEdit = getArguments().getBoolean(ARG_IS_EDIT, false);
        } else {
            this.fromDate = new GregorianCalendar();
            this.toDate = (GregorianCalendar) this.fromDate.clone();
            this.toDate.add(Calendar.MONTH, 1);
            this.isOngoing = true;
            this.amount = 0.0d;
            this.resetRollover = false;
            this.isEdit = false;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_budget_interval, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.sheetTitleTextView = view.findViewById(R.id.sheet_title);
        this.fromDateTextView = view.findViewById(R.id.sheet_from_date_text);
        this.toDateTextView = view.findViewById(R.id.sheet_to_date_text);
        this.toDateContainer = view.findViewById(R.id.sheet_to_date_container);
        this.ongoingCheckBox = view.findViewById(R.id.sheet_ongoing_checkbox);
        this.amountEditText = view.findViewById(R.id.sheet_amount_edittext);
        this.resetRolloverCheckBox = view.findViewById(R.id.sheet_reset_rollover_checkbox);
        Button cancelButton = view.findViewById(R.id.sheet_cancel_button);
        Button applyButton = view.findViewById(R.id.sheet_apply_button);

        // Apply theme colors
        applyTheme(view, cancelButton, applyButton);

        this.sheetTitleTextView.setText(this.isEdit ? R.string.BUDGETS_EDIT_INTERVAL : R.string.BUDGETS_ADD_INTERVAL);
        updateDateDisplays();

        this.ongoingCheckBox.setChecked(this.isOngoing);
        updateOngoingState();
        this.ongoingCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BudgetIntervalBottomSheet.this.isOngoing = isChecked;
            updateOngoingState();
        });

        if (this.amount > 0.0d) {
            this.amountEditText.setText(CurrencyExt.amountAsString(this.amount));
        }

        this.resetRolloverCheckBox.setChecked(this.resetRollover);

        view.findViewById(R.id.sheet_from_date_container).setOnClickListener(v -> showDatePicker(true));
        this.toDateContainer.setOnClickListener(v -> {
            if (!this.isOngoing) {
                showDatePicker(false);
            }
        });

        cancelButton.setOnClickListener(v -> dismiss());
        applyButton.setOnClickListener(v -> handleApply());
    }

    private void applyTheme(View root, Button cancelButton, Button applyButton) {
        int primaryText = PocketMoneyThemes.primaryCellTextColor();
        int secondaryText = PocketMoneyThemes.fieldLabelColor();
        int rowSelector = PocketMoneyThemes.primaryRowSelector();

        this.sheetTitleTextView.setTextColor(primaryText);
        this.fromDateTextView.setTextColor(primaryText);
        this.toDateTextView.setTextColor(primaryText);
        this.ongoingCheckBox.setTextColor(primaryText);
        this.amountEditText.setTextColor(PocketMoneyThemes.primaryEditTextColor());
        this.amountEditText.setHintTextColor(PocketMoneyThemes.primaryHintTextColor());
        this.resetRolloverCheckBox.setTextColor(primaryText);

        CheckBoxTint.colorCheckBox(this.ongoingCheckBox);
        CheckBoxTint.colorCheckBox(this.resetRolloverCheckBox);

        root.findViewById(R.id.sheet_from_date_container).setBackgroundResource(rowSelector);
        this.toDateContainer.setBackgroundResource(rowSelector);
        this.amountEditText.setBackgroundResource(rowSelector);

        int tintColor = PocketMoneyThemes.currentTintColor();
        if (applyButton instanceof MaterialButton mb) {
            mb.setBackgroundColor(tintColor);
            mb.setTextColor(PocketMoneyThemes.isDarkTheme() ? 0xFF000000 : 0xFFFFFFFF);
        } else {
            applyButton.setBackgroundColor(tintColor);
        }

        if (cancelButton instanceof MaterialButton mb) {
            mb.setStrokeColor(ColorStateList.valueOf(secondaryText));
            mb.setStrokeWidth((int) (1.5f * getResources().getDisplayMetrics().density));
            mb.setTextColor(primaryText);
        }
    }

    private void updateOngoingState() {
        if (this.isOngoing) {
            this.toDateTextView.setText(R.string.BUDGETS_ONGOING);
            this.toDateTextView.setAlpha(0.5f);
            this.toDateContainer.setClickable(false);
        } else {
            this.toDateTextView.setAlpha(1.0f);
            this.toDateContainer.setClickable(true);
            this.toDateTextView.setText(CalExt.descriptionWithMediumDate(this.toDate));
        }
    }

    private void updateDateDisplays() {
        this.fromDateTextView.setText(CalExt.descriptionWithMediumDate(this.fromDate));
        if (!this.isOngoing) {
            this.toDateTextView.setText(CalExt.descriptionWithMediumDate(this.toDate));
        }
    }

    private void showDatePicker(boolean isFromDate) {
        if (getContext() == null) return;
        GregorianCalendar initial = isFromDate ? this.fromDate : this.toDate;
        new DatePickerDialog(
                getContext(),
                PocketMoneyThemes.datePickerTheme(),
                (view, year, month, dayOfMonth) -> {
                    GregorianCalendar picked = new GregorianCalendar(year, month, dayOfMonth);
                    if (isFromDate) {
                        BudgetIntervalBottomSheet.this.fromDate = picked;
                        if (!BudgetIntervalBottomSheet.this.isOngoing && BudgetIntervalBottomSheet.this.toDate.before(BudgetIntervalBottomSheet.this.fromDate)) {
                            BudgetIntervalBottomSheet.this.toDate = (GregorianCalendar) BudgetIntervalBottomSheet.this.fromDate.clone();
                        }
                    } else {
                        BudgetIntervalBottomSheet.this.toDate = picked;
                        if (BudgetIntervalBottomSheet.this.toDate.before(BudgetIntervalBottomSheet.this.fromDate)) {
                            BudgetIntervalBottomSheet.this.fromDate = (GregorianCalendar) BudgetIntervalBottomSheet.this.toDate.clone();
                        }
                    }
                    updateDateDisplays();
                },
                initial.get(Calendar.YEAR),
                initial.get(Calendar.MONTH),
                initial.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void handleApply() {
        if (!this.isOngoing && this.toDate.before(this.fromDate)) {
            Toast.makeText(getContext(), "End date cannot be earlier than start date", Toast.LENGTH_SHORT).show();
            return;
        }

        double enteredAmount = CurrencyExt.amountFromString(this.amountEditText.getText().toString());
        boolean isReset = this.resetRolloverCheckBox.isChecked();

        if (this.listener != null) {
            this.listener.onIntervalSaved(this.fromDate, this.toDate, this.isOngoing, enteredAmount, isReset);
        }
        dismiss();
    }
}
