package com.marketcountry.psbuyandsell.ui.user.phonelogin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.marketcountry.psbuyandsell.Config;
import com.marketcountry.psbuyandsell.R;
import com.marketcountry.psbuyandsell.databinding.ActivityPhoneLoginBinding;
import com.marketcountry.psbuyandsell.ui.common.PSAppCompactActivity;
import com.marketcountry.psbuyandsell.utils.Constants;
import com.marketcountry.psbuyandsell.utils.MyContextWrapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.kakao.util.helper.Utility.getPackageInfo;

public class PhoneLoginActivity extends PSAppCompactActivity {


    //region Override Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityPhoneLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_phone_login);

        // Init all UI
        initUI(binding);
    }

    @Override
    protected void attachBaseContext(Context newBase) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String CURRENT_LANG_CODE = preferences.getString(Constants.LANGUAGE_CODE, Config.DEFAULT_LANGUAGE);
        String CURRENT_LANG_COUNTRY_CODE = preferences.getString(Constants.LANGUAGE_COUNTRY_CODE, Config.DEFAULT_LANGUAGE_COUNTRY_CODE);

        super.attachBaseContext(MyContextWrapper.wrap(newBase, CURRENT_LANG_CODE, CURRENT_LANG_COUNTRY_CODE, true));
    }

    //endregion


    //region Private Methods

    private void initUI(ActivityPhoneLoginBinding binding) {

        // Toolbar
        initToolbar(binding.toolbar, getResources().getString(R.string.login__login));

        // setup Fragment
        setupFragment(new PhoneLoginFragment());
        // Or you can call like this
        //setupFragment(new NewsListFragment(), R.id.content_frame);

    }

    //endregion

    //for google login on activity result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if(fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

    }
}