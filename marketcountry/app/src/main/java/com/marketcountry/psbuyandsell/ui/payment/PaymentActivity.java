package com.marketcountry.psbuyandsell.ui.payment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.marketcountry.psbuyandsell.Config;
import com.marketcountry.psbuyandsell.R;
import com.marketcountry.psbuyandsell.databinding.ActivityItemEntryBinding;
import com.marketcountry.psbuyandsell.ui.common.PSAppCompactActivity;
import com.marketcountry.psbuyandsell.ui.item.entry.ItemEntryFragment;
import com.marketcountry.psbuyandsell.utils.Constants;
import com.marketcountry.psbuyandsell.utils.MyContextWrapper;

public class PaymentActivity extends PSAppCompactActivity {


    //region Override Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityItemEntryBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_payment);

        // Init all UI
        initUI(binding);

    }

    private void initUI(ActivityItemEntryBinding binding) {

        initToolbar(binding.toolbar, "결제");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}

