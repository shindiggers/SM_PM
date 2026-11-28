package com.example.smmoney.views.filters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smmoney.R;
import com.example.smmoney.misc.PocketMoneyThemes;
import com.google.android.material.chip.ChipGroup;

public class FilterRowHolder extends RecyclerView.ViewHolder {
    public final View headerRow;
    public final TextView title;
    public final TextView subtitle;
    public final ImageView editImage;

    // Quick View Card components
    public final CardView quickViewCard;
    public final TextView generalLabel;
    public final ChipGroup generalChips;
    public final TextView accountsLabel;
    public final ChipGroup accountsChips;
    public final TextView categoriesLabel;
    public final ChipGroup categoriesChips;
    public final TextView payeesLabel;
    public final ChipGroup payeesChips;
    public final TextView classesLabel;
    public final ChipGroup classesChips;
    public final TextView idsLabel;
    public final ChipGroup idsChips;

    public FilterRowHolder(@NonNull View itemView) {
        super(itemView);
        this.headerRow = itemView.findViewById(R.id.filter_row_header);
        this.title = itemView.findViewById(R.id.filterfiltername);
        this.subtitle = itemView.findViewById(R.id.filter_summary_subtitle);
        this.editImage = itemView.findViewById(R.id.filterroweditimage);

        this.quickViewCard = itemView.findViewById(R.id.filter_quick_view_card);
        this.generalLabel = itemView.findViewById(R.id.quick_view_general_label);
        this.generalChips = itemView.findViewById(R.id.quick_view_general_chips);
        this.accountsLabel = itemView.findViewById(R.id.quick_view_accounts_label);
        this.accountsChips = itemView.findViewById(R.id.quick_view_accounts_chips);
        this.categoriesLabel = itemView.findViewById(R.id.quick_view_categories_label);
        this.categoriesChips = itemView.findViewById(R.id.quick_view_categories_chips);
        this.payeesLabel = itemView.findViewById(R.id.quick_view_payees_label);
        this.payeesChips = itemView.findViewById(R.id.quick_view_payees_chips);
        this.classesLabel = itemView.findViewById(R.id.quick_view_classes_label);
        this.classesChips = itemView.findViewById(R.id.quick_view_classes_chips);
        this.idsLabel = itemView.findViewById(R.id.quick_view_ids_label);
        this.idsChips = itemView.findViewById(R.id.quick_view_ids_chips);
    }

    public void applyTheme() {
        if (this.title != null) {
            this.title.setTextColor(PocketMoneyThemes.primaryCellTextColor());
        }
        if (this.subtitle != null) {
            this.subtitle.setTextColor(PocketMoneyThemes.alternateCellTextColor());
        }
    }
}
