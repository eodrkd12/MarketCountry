package com.marketcountry.psbuyandsell.ui.chathistory;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.marketcountry.psbuyandsell.MainActivity;
import com.marketcountry.psbuyandsell.R;
import com.marketcountry.psbuyandsell.binding.FragmentDataBindingComponent;
import com.marketcountry.psbuyandsell.databinding.FragmentMessageBinding;
import com.marketcountry.psbuyandsell.ui.chathistory.adapter.PagerAdapter;
import com.marketcountry.psbuyandsell.ui.common.PSFragment;
import com.marketcountry.psbuyandsell.utils.AutoClearedValue;
import com.marketcountry.psbuyandsell.utils.Constants;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends PSFragment {

    //region Variables
    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    @VisibleForTesting
    private AutoClearedValue<FragmentMessageBinding> binding;

    private PagerAdapter pagerAdapter;
    //endregion

    public static String userId;

    //region Override Methods
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        FragmentMessageBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_message, container, false, dataBindingComponent);
        binding = new AutoClearedValue<>(this, dataBinding);

        userId = pref.getString(Constants.USER_ID, Constants.EMPTY_STRING);

        return binding.get().getRoot();
    }

    @Override
    protected void initUIAndActions() {

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbarText(((MainActivity) getActivity()).binding.toolbar, getString(R.string.menu__message));
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            ((MainActivity) this.getActivity()).binding.toolbar.setBackgroundColor(getResources().getColor(R.color.global__primary));
            ((MainActivity) getActivity()).updateToolbarIconColor(Color.WHITE);
            ((MainActivity) getActivity()).updateMenuIconWhite();
            ((MainActivity) getActivity()).refreshPSCount();
        }

        if (getActivity() != null) {
            pagerAdapter = new PagerAdapter(getActivity().getSupportFragmentManager(), binding.get().tabLayout.getTabCount());
            binding.get().tabViewPager.setAdapter(pagerAdapter);
            binding.get().tabViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.get().tabLayout));
        }

        binding.get().tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.get().tabViewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    protected void initViewModels() {

    }

    @Override
    protected void initAdapters() {

    }

    @Override
    protected void initData() {

        getIntentData();

        bindingData();

    }

    private void bindingData() {

    }

    private void getIntentData() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE__BUYER_CHAT_FRAGMENT) {
            pagerAdapter.buyerFragment.onActivityResult(requestCode, resultCode, data);
        } else {
            pagerAdapter.sellerFragment.onActivityResult(requestCode, resultCode, data);
        }

    }


}
