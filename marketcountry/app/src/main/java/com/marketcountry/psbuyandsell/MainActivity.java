package com.marketcountry.psbuyandsell;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.facebook.login.LoginManager;
import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.consent.DebugGeography;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.marketcountry.psbuyandsell.databinding.ActivityMainBinding;
import com.marketcountry.psbuyandsell.ui.common.NavigationController;
import com.marketcountry.psbuyandsell.ui.common.PSAppCompactActivity;
import com.marketcountry.psbuyandsell.utils.AppLanguage;
import com.marketcountry.psbuyandsell.utils.Constants;
import com.marketcountry.psbuyandsell.utils.MyContextWrapper;
import com.marketcountry.psbuyandsell.utils.PSDialogMsg;
import com.marketcountry.psbuyandsell.utils.Utils;
import com.marketcountry.psbuyandsell.viewmodel.common.NotificationViewModel;
import com.marketcountry.psbuyandsell.viewmodel.pscount.PSCountViewModel;
import com.marketcountry.psbuyandsell.viewmodel.user.UserViewModel;
import com.marketcountry.psbuyandsell.viewobject.PSCount;
import com.marketcountry.psbuyandsell.viewobject.User;
import com.marketcountry.psbuyandsell.viewobject.common.Resource;
import com.marketcountry.psbuyandsell.viewobject.holder.ItemParameterHolder;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.util.List;
import java.util.Locale;
/**
 * MainActivity of Panacea-Soft
 * Contact Email : teamps.is.cool@gmail.com
 *
 * @author Panacea-soft
 * @version 1.0
 */

public class MainActivity extends PSAppCompactActivity {


    //region Variables

    @Inject
    SharedPreferences pref;

    @Inject
    AppLanguage appLanguage;
    private Boolean notificationSetting = false;
    private String token = "";
    private UserViewModel userViewModel;
    private NotificationViewModel notificationViewModel;
    private User user;
    private PSDialogMsg psDialogMsg;
    public boolean isLogout = false;
    Drawable notificationIconDrawable = null;
    ActionBarDrawerToggle drawerToggle;
    public String selectedLocationId, selectedLocationName, selected_lat, selected_lng;
    private String loginUserId;
    private String locationId;
    private String locationName;
    public String notificationItemId, notificationBuyerId, notificationSellerId, notificationMsg, notificationSenderName, notificationSenderUrl, userId;
    String receiverId = Constants.EMPTY_STRING;
    String receiverName = Constants.EMPTY_STRING;
    String receiverUrl = Constants.EMPTY_STRING;
    int requestCode = 0;
    String flag = Constants.EMPTY_STRING;
    private ConsentForm form;
    private String userIdToVerify;
    private GoogleSignInClient mGoogleSignInClient;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    NavigationController navigationController;

    public ActivityMainBinding binding;
    private PSCountViewModel psCountViewModel;
    private TextView messageNotificationTextView;
    private TextView notificationTextView;
    private ImageView notificationIconImageView;

    String notificationCount = "0";
    private int toolbarIconColor = Color.GRAY;
    //endregion

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    //region Override Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Base_PSTheme);

        super.onCreate(savedInstanceState);

        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initUIAndActions();

        initModels();

        initData();

        checkConsentStatus();

    }

    @Override
    protected void attachBaseContext(Context newBase) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String CURRENT_LANG_CODE = preferences.getString(Constants.LANGUAGE_CODE, Config.DEFAULT_LANGUAGE);
        String CURRENT_LANG_COUNTRY_CODE = preferences.getString(Constants.LANGUAGE_COUNTRY_CODE, Config.DEFAULT_LANGUAGE_COUNTRY_CODE);

        super.attachBaseContext(MyContextWrapper.wrap(newBase, CURRENT_LANG_CODE, CURRENT_LANG_COUNTRY_CODE, true));
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshUserData();

    }

    public void refreshPSCount() {
        if (!loginUserId.isEmpty()) {
            psCountViewModel.setPsCountObj(loginUserId, token);
        }
    }

    public void refreshUserData() {
        try {
            loginUserId = pref.getString(Constants.USER_ID, Constants.EMPTY_STRING);
        } catch (Exception e) {
            Utils.psErrorLog("", e);
        }

        refreshPSCount();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            String message = getBaseContext().getString(R.string.message__want_to_quit);
            String okStr = getBaseContext().getString(R.string.message__ok_close);
            String cancelStr = getBaseContext().getString(R.string.message__cancel_close);

            psDialogMsg.showConfirmDialog(message, okStr, cancelStr);

            psDialogMsg.show();

            psDialogMsg.okButton.setOnClickListener(view -> {

                psDialogMsg.cancel();
                finish();
                System.exit(0);
            });

            psDialogMsg.cancelButton.setOnClickListener(view -> psDialogMsg.cancel());

        }
        return true;
    }

    //endregion


    //region Private Methods

    /**
     * Initialize Models
     */
    private void initModels() {

        userViewModel = ViewModelProviders.of(this, viewModelFactory).get(UserViewModel.class);
        notificationViewModel = ViewModelProviders.of(this, viewModelFactory).get(NotificationViewModel.class);

    }


    /**
     * Show alert message to user.
     *
     * @param msg Message to show to user
     */
    private void showAlertMessage(String msg) {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.ps_dialog, null);

        builder.setView(view)
                .setPositiveButton(getString(R.string.app__ok), null);

        TextView message = view.findViewById(R.id.messageTextView);

        message.setText(msg);

        builder.create();

        builder.show();

    }


    /**
     * This function will initialize UI and Event Listeners
     */
    private void initUIAndActions() {

        psDialogMsg = new PSDialogMsg(this, false);

        initToolbar(binding.toolbar, Constants.EMPTY_STRING);

        userIdToVerify = pref.getString(Constants.USER_ID_TO_VERIFY, Constants.EMPTY_STRING);

        initDrawerLayout();

        initNavigationView();

        navigationController.navigateToCityList(this);
        showBottomNavigation();

        setSelectMenu(R.id.nav_home);

        getIntentData();
        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) binding.bottomNavigationView.getChildAt(0);
        View bTMView = bottomNavigationMenuView.getChildAt(3);
        BottomNavigationItemView itemView = (BottomNavigationItemView) bTMView;

        View badgeView = LayoutInflater.from(this)
                .inflate(R.layout.notification_badge, itemView, true);
        messageNotificationTextView = badgeView.findViewById(R.id.notifications_badge);
        messageNotificationTextView.setVisibility(View.GONE);

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.home_menu:
                    //layout_scrollFlags
                    Utils.addToolbarScrollFlag(binding.toolbar);
                    binding.addItemButton.setVisibility(View.VISIBLE);
                    navigationController.navigateToHome(MainActivity.this, false, selectedLocationId, selectedLocationName,false);
                    setToolbarText(binding.toolbar, Constants.EMPTY_STRING);

                    break;
                case R.id.message_menu:
                    Utils.addToolbarScrollFlag(binding.toolbar);
                    binding.addItemButton.setVisibility(View.GONE);

                    Utils.navigateOnUserVerificationAndMessageFragment(pref,user,navigationController,this);

                    break;

                case R.id.interest_menu:
                    Utils.addToolbarScrollFlag(binding.toolbar);
                    binding.addItemButton.setVisibility(View.GONE);
                    navigationController.navigateToInterest(MainActivity.this);
                    setToolbarText(binding.toolbar, getString(R.string.menu__interest));

                    break;

                /*case R.id.search_menu:
                    Utils.addToolbarScrollFlag(binding.toolbar);
                    binding.addItemButton.setVisibility(View.GONE);
                    navigationController.navigateToFilter(MainActivity.this);
                    setToolbarText(binding.toolbar, getString(R.string.menu__search));

                    break;*/

                case R.id.me_menu:
                    Utils.addToolbarScrollFlag(binding.toolbar);
                    binding.addItemButton.setVisibility(View.GONE);

                    Utils.navigateOnUserVerificationFragment(pref,user,navigationController,this);

                    break;

                default:


                    break;
            }

            return true;
        });

        binding.addItemButton.setTypeface(Utils.getTypeFace(this, Utils.Fonts.ROBOTO));
        binding.addItemButton.setOnClickListener(v -> {

            Utils.navigateOnUserVerificationActivity(userIdToVerify, loginUserId, psDialogMsg, this, navigationController, () -> {
                try {
                    locationId = pref.getString(Constants.SELECTED_LOCATION_ID, Constants.EMPTY_STRING);
                    locationName = pref.getString(Constants.SELECTED_LOCATION_NAME, Constants.EMPTY_STRING);

                } catch (Exception e) {
                    Utils.psErrorLog("", e);
                }

                navigationController.navigateToItemEntryActivity(MainActivity.this, Constants.ADD_NEW_ITEM, locationId, locationName);

            });

        });

        // Configure Google Sign In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    private void checkUserId() {
        if (!userId.isEmpty()) {
            if (userId.equals(notificationBuyerId)) {
                receiverId = notificationSellerId;
                receiverName = notificationSenderName;
                receiverUrl = notificationSenderUrl;
                requestCode = Constants.REQUEST_CODE__SELLER_CHAT_FRAGMENT;
                flag = Constants.CHAT_FROM_SELLER;
            }
            if (userId.equals(notificationSellerId)) {
                receiverId = notificationBuyerId;
                receiverName = notificationSenderName;
                receiverUrl = notificationSenderUrl;
                requestCode = Constants.REQUEST_CODE__BUYER_CHAT_FRAGMENT;
                flag = Constants.CHAT_FROM_BUYER;
            }

        }
    }

    private void getIntentData() {
        loginUserId = pref.getString(Constants.USER_ID, Constants.EMPTY_STRING);

        selectedLocationId = getIntent().getStringExtra(Constants.SELECTED_LOCATION_ID);
        selectedLocationName = getIntent().getStringExtra(Constants.SELECTED_LOCATION_NAME);
        selected_lat = getIntent().getStringExtra(Constants.LAT);
        selected_lng = getIntent().getStringExtra(Constants.LNG);

        pref.edit().putString(Constants.SELECTED_LOCATION_ID, selectedLocationId).apply();
        pref.edit().putString(Constants.SELECTED_LOCATION_NAME, selectedLocationName).apply();
        pref.edit().putString(Constants.LAT, selected_lat).apply();
        pref.edit().putString(Constants.LNG, selected_lng).apply();

        notificationItemId = getIntent().getStringExtra(Constants.NOTI_ITEM_ID);
        notificationMsg = getIntent().getStringExtra(Constants.NOTI_MSG);
        notificationBuyerId = getIntent().getStringExtra(Constants.NOTI_BUYER_ID);
        notificationSellerId = getIntent().getStringExtra(Constants.NOTI_SELLER_ID);
        notificationSenderName = getIntent().getStringExtra(Constants.NOTI_SENDER_NAME);
        notificationSenderUrl = getIntent().getStringExtra(Constants.NOTI_SENDER_URL);

        userId = pref.getString(Constants.USER_ID, Constants.EMPTY_STRING);

        checkUserId();

        if (notificationItemId != null) {
            if (loginUserId.isEmpty()) {

                psDialogMsg.showInfoDialog(getString(R.string.error_message__login_first), getString(R.string.app__ok));
                psDialogMsg.show();
                psDialogMsg.okButton.setOnClickListener(v1 -> {
                    psDialogMsg.cancel();
                    navigationController.navigateToUserLoginActivity(MainActivity.this);
                });

            } else {
                navigationController.navigateToChatActivity(MainActivity.this, notificationItemId, receiverId, receiverName, "", "", "",
                        "", "", flag, notificationSenderUrl, requestCode);
            }

        }

        if ((notificationItemId == null || notificationItemId.isEmpty()) && (notificationMsg != null)) {
            showAlertMessage(notificationMsg);
        }


    }

    private void initDrawerLayout() {

        drawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.app__drawer_open, R.string.app__drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        drawerToggle.setDrawerIndicatorEnabled(false);
        drawerToggle.setHomeAsUpIndicator(R.drawable.baseline_menu_grey_24);

        drawerToggle.setToolbarNavigationClickListener(view -> binding.drawerLayout.openDrawer(GravityCompat.START));

        binding.drawerLayout.addDrawerListener(drawerToggle);
        binding.drawerLayout.post(drawerToggle::syncState);

    }

    private void initNavigationView() {

        if (binding.navView != null) {

            // Updating Custom Fonts
            Menu m = binding.navView.getMenu();
            try {
                if (m != null) {

                    for (int i = 0; i < m.size(); i++) {
                        MenuItem mi = m.getItem(i);

                        //for applying a font to subMenu ...
                        SubMenu subMenu = mi.getSubMenu();
                        if (subMenu != null && subMenu.size() > 0) {
                            for (int j = 0; j < subMenu.size(); j++) {
                                MenuItem subMenuItem = subMenu.getItem(j);

                                subMenuItem.setTitle(subMenuItem.getTitle());
                                // update font

                                subMenuItem.setTitle(Utils.getSpannableString(getBaseContext(), subMenuItem.getTitle().toString(), Utils.Fonts.ROBOTO));

                            }
                        }

                        mi.setTitle(mi.getTitle());
                        // update font

                        mi.setTitle(Utils.getSpannableString(getBaseContext(), mi.getTitle().toString(), Utils.Fonts.ROBOTO));
                    }
                }
            } catch (Exception e) {
                Utils.psErrorLog("Error in Setting Custom Font", e);
            }

            binding.navView.setNavigationItemSelectedListener(menuItem -> {
                navigationMenuChanged(menuItem);
                return true;
            });

        }

        if (binding.bottomNavigationView != null) {

            // Updating Custom Fonts
            Menu m = binding.bottomNavigationView.getMenu();
            try {

                for (int i = 0; i < m.size(); i++) {
                    MenuItem mi = m.getItem(i);

                    //for applying a font to subMenu ...
                    SubMenu subMenu = mi.getSubMenu();
                    if (subMenu != null && subMenu.size() > 0) {
                        for (int j = 0; j < subMenu.size(); j++) {
                            MenuItem subMenuItem = subMenu.getItem(j);

                            subMenuItem.setTitle(subMenuItem.getTitle());
                            // update font

                            subMenuItem.setTitle(Utils.getSpannableString(getBaseContext(), subMenuItem.getTitle().toString(), Utils.Fonts.ROBOTO));

                        }
                    }

                    mi.setTitle(mi.getTitle());
                    // update font

                    mi.setTitle(Utils.getSpannableString(getBaseContext(), mi.getTitle().toString(), Utils.Fonts.ROBOTO));
                }
            } catch (Exception e) {
                Utils.psErrorLog("Error in Setting Custom Font", e);
            }

            binding.navView.setNavigationItemSelectedListener(menuItem -> {
                navigationMenuChanged(menuItem);
                return true;
            });

        }

    }


    public void hideBottomNavigation() {
        binding.bottomNavigationView.setVisibility(View.GONE);
        binding.addItemButton.setVisibility(View.GONE);

        Utils.removeToolbarScrollFlag(binding.toolbar);

    }

    private void showBottomNavigation() {
        binding.bottomNavigationView.setVisibility(View.VISIBLE);
        binding.addItemButton.setVisibility(View.VISIBLE);

        Utils.addToolbarScrollFlag(binding.toolbar);

    }

    private void navigationMenuChanged(MenuItem menuItem) {
        openFragment(menuItem.getItemId());

        if (menuItem.getItemId() != R.id.nav_logout_login) {
            menuItem.setChecked(true);
            binding.drawerLayout.closeDrawers();
        }
    }

    public void setSelectMenu(int id) {
        binding.navView.setCheckedItem(id);
    }

    private int menuId = 0;

    /**
     * Open Fragment
     *
     * @param menuId To know which fragment to open.
     */
    private void openFragment(int menuId) {

        this.menuId = menuId;
        switch (menuId) {
            case R.id.nav_home:
            case R.id.nav_home_login:

                setToolbarText(binding.toolbar, Constants.EMPTY_STRING);
                navigationController.navigateToHome(this, false, selectedLocationId, selectedLocationName,false);
                showBottomNavigation();
                break;

            case R.id.nav_category:
            case R.id.nav_category_login:
                setToolbarText(binding.toolbar, getString(R.string.menu__category));
                navigationController.navigateToCategory(this);
                hideBottomNavigation();
                break;

            case R.id.nav_latest:
            case R.id.nav_latest_login:

                setToolbarText(binding.toolbar, getString(R.string.menu__latest_item));
                navigationController.navigateToHomeLatestFiltering(MainActivity.this, new ItemParameterHolder().getRecentItem());
                hideBottomNavigation();
                break;

            case R.id.nav_popular:
            case R.id.nav_popular_login:
                setToolbarText(binding.toolbar, getString(R.string.menu__trending_item));
                navigationController.navigateToHomePopularFiltering(MainActivity.this, new ItemParameterHolder().getPopularItem());
                hideBottomNavigation();
                break;

            case R.id.nav_profile:
            case R.id.nav_profile_login:

                Utils.navigateOnUserVerificationFragment(pref,user,navigationController,this);

                Utils.psLog("nav_profile");

                hideBottomNavigation();

                break;

            case R.id.nav_favourite_news_login:

                setToolbarText(binding.toolbar, getString(R.string.menu__favourite_items));
                navigationController.navigateToFavourite(this);
                Utils.psLog("nav_favourite_news");

                hideBottomNavigation();
                break;

            case R.id.nav_user_history_login:
                setToolbarText(binding.toolbar, getString(R.string.menu__user_history));
                navigationController.navigateToHistory(this);
                Utils.psLog("nav_history");

                hideBottomNavigation();
                break;

            case R.id.nav_logout_login:

                psDialogMsg.showConfirmDialog(getString(R.string.edit_setting__logout_question), getString(R.string.app__ok), getString(R.string.app__cancel));

                psDialogMsg.show();

                psDialogMsg.okButton.setOnClickListener(view -> {

                    psDialogMsg.cancel();

                    hideBottomNavigation();

                    userViewModel.deleteUserLogin(user).observe(this, status -> {
                        if (status != null) {
                            this.menuId = 0;

                            setToolbarText(binding.toolbar, getString(R.string.app__app_name));

                            isLogout = true;

                            LoginManager.getInstance().logOut();
                        }
                    });

                    Utils.psLog("nav_logout_login");
                });

                psDialogMsg.cancelButton.setOnClickListener(view -> psDialogMsg.cancel());

                break;

            case R.id.nav_setting:
            case R.id.nav_setting_login:

                setToolbarText(binding.toolbar, getString(R.string.menu__setting));
                navigationController.navigateToSetting(this);
                Utils.psLog("nav_setting");

                hideBottomNavigation();
                break;

            case R.id.nav_language:
            case R.id.nav_language_login:

                setToolbarText(binding.toolbar, getString(R.string.menu__language));
                navigationController.navigateToLanguageSetting(this);
                Utils.psLog("nav_language");
                hideBottomNavigation();

                break;
            case R.id.nav_rate_this_app:
            case R.id.nav_rate_this_app_login:

                setToolbarText(binding.toolbar, getString(R.string.menu__rate));
                navigationController.navigateToPlayStore(this);
                hideBottomNavigation();

                break;

            case R.id.nav_contact_us:
            case R.id.nav_contact_us_login:

                setToolbarText(binding.toolbar, getString(R.string.menu__contact_us));
                navigationController.navigateToContactUs(this);
                hideBottomNavigation();

                break;

            case R.id.nav_privacy_policy:
            case R.id.nav_privacy_policy_login:

                setToolbarText(binding.toolbar, getString(R.string.menu__privacy_policy));
                navigationController.navigateToPrivacyPolicy(this);
                hideBottomNavigation();

                break;

        }

    }



    /**
     * Initialize Data
     */
    private void initData() {

        try {
            notificationSetting = pref.getBoolean(Constants.NOTI_SETTING, false);
            token = pref.getString(Constants.NOTI_TOKEN, "");

        } catch (NullPointerException ne) {
            Utils.psErrorLog("Null Pointer Exception.", ne);
        } catch (Exception e) {
            Utils.psErrorLog("Error in getting notification flag data.", e);
        }

        try {
            loginUserId = pref.getString(Constants.USER_ID, Constants.EMPTY_STRING);
        } catch (Exception e) {
            Utils.psErrorLog("", e);
        }

        userViewModel.getLoginUser().observe(this, data -> {

            if (data != null) {

                if (data.size() > 0) {
                    user = data.get(0).user;

                    pref.edit().putString(Constants.USER_ID, user.userId).apply();
                    pref.edit().putString(Constants.USER_NAME, user.userName).apply();
                    pref.edit().putString(Constants.USER_EMAIL, user.userEmail).apply();
                    pref.edit().putString(Constants.USER_PASSWORD, user.userPassword).apply();

                } else {
                    user = null;

                    pref.edit().remove(Constants.USER_ID).apply();
                    pref.edit().remove(Constants.USER_NAME).apply();
                    pref.edit().remove(Constants.USER_EMAIL).apply();
                    pref.edit().remove(Constants.USER_PASSWORD).apply();
                }

            } else {

                user = null;
                pref.edit().remove(Constants.USER_ID).apply();
                pref.edit().remove(Constants.USER_NAME).apply();
                pref.edit().remove(Constants.USER_EMAIL).apply();
                pref.edit().remove(Constants.USER_PASSWORD).apply();

            }
            updateMenu();

            if (isLogout) {
                navigationController.navigateToHome(MainActivity.this, false, selectedLocationId, selectedLocationName,true);
                showBottomNavigation();
                refreshUserData();
                isLogout = false;
                FirebaseAuth.getInstance().signOut();
                mGoogleSignInClient.revokeAccess()
                        .addOnCompleteListener(this, task -> {
                            // ...
                        });

                LoginManager.getInstance().logOut();

            }

        });


        registerNotificationToken(); // Just send "" because don't have token to sent. It will get token itself.

        psCountViewModel = ViewModelProviders.of(this, viewModelFactory).get(PSCountViewModel.class);

        LiveData<Resource<PSCount>> chatHistoryListData = psCountViewModel.getPSCount();

        if (chatHistoryListData != null) {

            chatHistoryListData.observe(this, psCountResource -> {
                if (psCountResource != null) {

                    Utils.psLog("Got Data" + psCountResource.message + psCountResource.toString());

                    switch (psCountResource.status) {
                        case LOADING:
                            // Loading State
                            // Data are from Local DB

                            if (psCountResource.data != null) {
                                // Update the data
                                // Notification
                                notificationCount = psCountResource.data.blogNotiUnreadCount;
                                if (notificationTextView != null) {
                                    if (notificationCount.equals("0")) {
                                        notificationTextView.setVisibility(View.GONE);
                                    } else {
                                        notificationTextView.setVisibility(View.VISIBLE);

                                        int count = Integer.valueOf(notificationCount);
                                        if (count > 9) {
                                            notificationTextView.setText("9+");
                                        } else {
                                            notificationTextView.setText(String.valueOf(count));
                                        }
                                    }
                                }


                                // Message
                                int sellerCount = Integer.valueOf(psCountResource.data.sellerUnreadCount);
                                int buyerCount = Integer.valueOf(psCountResource.data.buyerUnreadCount);
                                int totalCount = sellerCount + buyerCount;
                                if (totalCount == 0) {
                                    messageNotificationTextView.setVisibility(View.GONE);
                                } else {
                                    messageNotificationTextView.setVisibility(View.VISIBLE);

                                    if (totalCount > 9) {
                                        messageNotificationTextView.setText("9+");
                                    } else {
                                        messageNotificationTextView.setText(String.valueOf(totalCount));
                                    }
                                }

                            } else {
                                messageNotificationTextView.setVisibility(View.GONE);
                                if (notificationTextView != null) {
                                    notificationTextView.setVisibility(View.GONE);
                                }
                            }

                            break;

                        case SUCCESS:
                            // Success State
                            // Data are from Server

                            if (psCountResource.data != null) {
                                // Update the data
                                // Notification
                                notificationCount = psCountResource.data.blogNotiUnreadCount;
                                if (notificationTextView != null) {
                                    if (notificationCount.equals("0")) {
                                        notificationTextView.setVisibility(View.GONE);
                                    } else {
                                        notificationTextView.setVisibility(View.VISIBLE);

                                        int count = Integer.valueOf(notificationCount);
                                        if (count > 9) {
                                            notificationTextView.setText("9+");
                                        } else {
                                            notificationTextView.setText(String.valueOf(count));
                                        }
                                    }
                                }


                                // Message
                                int sellerCount = Integer.valueOf(psCountResource.data.sellerUnreadCount);
                                int buyerCount = Integer.valueOf(psCountResource.data.buyerUnreadCount);
                                int totalCount = sellerCount + buyerCount;
                                if (totalCount == 0) {
                                    messageNotificationTextView.setVisibility(View.GONE);
                                } else {
                                    messageNotificationTextView.setVisibility(View.VISIBLE);

                                    if (totalCount > 9) {
                                        messageNotificationTextView.setText("9+");
                                    } else {
                                        messageNotificationTextView.setText(String.valueOf(totalCount));
                                    }
                                }

                            } else {
                                messageNotificationTextView.setVisibility(View.GONE);
                                if (notificationTextView != null) {
                                    notificationTextView.setVisibility(View.GONE);
                                }
                            }

                            psCountViewModel.setLoadingState(false);

                            break;

                        case ERROR:
                            // Error State
                            messageNotificationTextView.setVisibility(View.GONE);
                            if (notificationTextView != null) {
                                notificationTextView.setVisibility(View.GONE);
                            }

                            psCountViewModel.setLoadingState(false);

                            break;
                        default:
                            // Default

                            break;
                    }

                } else {

                    // Init Object or Empty Data
                    Utils.psLog("Empty Data");

                    if (psCountViewModel.offset > 1) {
                        // No more data for this list
                        // So, Block all future loading
                        psCountViewModel.forceEndLoading = true;
                    }

                }

            });
        }
    }

    /**
     * This function will change the menu based on the user is logged in or not.
     */
    private void updateMenu() {

        if (user == null) {

            binding.navView.getMenu().setGroupVisible(R.id.group_before_login, true);
            binding.navView.getMenu().setGroupVisible(R.id.group_after_login, false);

            setSelectMenu(R.id.nav_home);

        } else {
            binding.navView.getMenu().setGroupVisible(R.id.group_after_login, true);
            binding.navView.getMenu().setGroupVisible(R.id.group_before_login, false);

            if (menuId == R.id.nav_profile) {
                setSelectMenu(R.id.nav_profile_login);
            } else if (menuId == R.id.nav_profile_login) {
                setSelectMenu(R.id.nav_profile_login);
            } else {
                setSelectMenu(R.id.nav_home_login);
            }

        }


    }

    private void registerNotificationToken() {
        /*
         * Register Notification
         */

        // Check already submit or not
        // If haven't, submit to server
        if (!notificationSetting) {

            if (this.token.equals("")) {

                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {

                                return;
                            }

                            // Get new Instance ID token
                            if (task.getResult() != null) {
                                token = task.getResult().getToken();
                            }

                            notificationViewModel.registerNotification(getBaseContext(), Constants.PLATFORM, token);
                        });


            }
        } else {
            Utils.psLog("Notification Token is already registered. Notification Setting : true.");
        }
    }

    //endregion

    Menu menu = null;
    public void updateToolbarIconColor(int color) {
        toolbarIconColor = color;
        updateMenuIconColor(menu, color);
    }

    private void updateMenuIconColor(Menu menu, int color) {
        if(menu != null) {
            for (int i = 0; i < menu.size(); i++) {
                ImageView notiImageView = menu.getItem(i).getActionView().findViewById(R.id.notiImageView);
                if (notiImageView != null) {
                    notiImageView.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                }
            }
        }
    }

    public void updateMenuIconWhite() {
        drawerToggle.setHomeAsUpIndicator(R.drawable.baseline_menu_white_24);
    }

    public void updateMenuIconGrey() {
        drawerToggle.setHomeAsUpIndicator(R.drawable.baseline_menu_grey_24);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notification_menu, menu);

        this.menu = menu;
        updateMenuIconColor(menu, toolbarIconColor);

        for(int i = 0; i< menu.size(); i++) {
            notificationIconDrawable = menu.getItem(i).getIcon();
            notificationIconDrawable.setColorFilter(toolbarIconColor, PorterDuff.Mode.SRC_ATOP);
        }
        View itemView = menu.getItem(0).getActionView();

        notificationTextView = itemView.findViewById(R.id.txtCount);
        notificationTextView.setText(notificationCount);

        if (notificationCount.equals("0")) {
            notificationTextView.setVisibility(View.GONE);
        } else {
            notificationTextView.setVisibility(View.VISIBLE);

            int count = Integer.valueOf(notificationCount);
            if (count > 9) {
                notificationTextView.setText("9+");
            }
        }
        notificationIconImageView = itemView.findViewById(R.id.notiImageView);
        notificationIconImageView.setOnClickListener(
                v -> navigationController.navigateToNotificationList(this)
        );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_notification) {

            navigationController.navigateToNotificationList(this);
        }
        return super.onOptionsItemSelected(item);
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }

    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void checkConsentStatus() {

        // For Testing Open this
        ConsentInformation.getInstance(this).
                setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);

        ConsentInformation consentInformation = ConsentInformation.getInstance(this);
        String[] publisherIds = {getString(R.string.adview_publisher_key)};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                // User's consent status successfully updated.

                Utils.psLog(consentStatus.name());

                if (!consentStatus.name().equals(pref.getString(Config.CONSENTSTATUS_CURRENT_STATUS, Config.CONSENTSTATUS_CURRENT_STATUS)) || consentStatus.name().equals(Config.CONSENTSTATUS_UNKNOWN)) {
                    collectConsent();
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                // User's consent status failed to update.

                Utils.psLog("Failed to update");
            }
        });
    }

    private void collectConsent() {
        URL privacyUrl = null;
        try {
            privacyUrl = new URL(Config.POLICY_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // Handle error.
        }

        form = new ConsentForm.Builder(this, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        // Consent form loaded successfully.

                        Utils.psLog("Form loaded");

                        if (form != null) {
                            form.show();
                        }
                    }

                    @Override
                    public void onConsentFormOpened() {
                        // Consent form was displayed.

                        Utils.psLog("Form Opened");
                    }

                    @Override
                    public void onConsentFormClosed(
                            ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        // Consent form was closed.

                        pref.edit().putString(Config.CONSENTSTATUS_CURRENT_STATUS, consentStatus.name()).apply();
                        pref.edit().putBoolean(Config.CONSENTSTATUS_IS_READY_KEY, true).apply();
                        Utils.psLog("Form Closed");
                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        // Consent form error.

                        pref.edit().putBoolean(Config.CONSENTSTATUS_IS_READY_KEY, false).apply();
                        Utils.psLog("Form Error " + errorDescription);
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .withAdFreeOption()
                .build();

        form.load();

    }
    //endregion

}
