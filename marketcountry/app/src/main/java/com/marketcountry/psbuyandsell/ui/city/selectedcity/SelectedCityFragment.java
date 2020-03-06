package com.marketcountry.psbuyandsell.ui.city.selectedcity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdRequest;
import com.marketcountry.psbuyandsell.Config;
import com.marketcountry.psbuyandsell.GpsTracker;
import com.marketcountry.psbuyandsell.MainActivity;
import com.marketcountry.psbuyandsell.R;
import com.marketcountry.psbuyandsell.binding.FragmentDataBindingComponent;
import com.marketcountry.psbuyandsell.databinding.FragmentSelectedCityBinding;
import com.marketcountry.psbuyandsell.ui.category.adapter.CityCategoryAdapter;
import com.marketcountry.psbuyandsell.ui.common.DataBoundListAdapter;
import com.marketcountry.psbuyandsell.ui.common.PSFragment;
import com.marketcountry.psbuyandsell.ui.dashboard.adapter.DashBoardViewPagerAdapter;
import com.marketcountry.psbuyandsell.ui.item.adapter.ItemHorizontalListAdapter;
import com.marketcountry.psbuyandsell.utils.AutoClearedValue;
import com.marketcountry.psbuyandsell.utils.Constants;
import com.marketcountry.psbuyandsell.utils.PSDialogMsg;
import com.marketcountry.psbuyandsell.utils.Utils;
import com.marketcountry.psbuyandsell.viewmodel.blog.BlogViewModel;
import com.marketcountry.psbuyandsell.viewmodel.item.PopularItemViewModel;
import com.marketcountry.psbuyandsell.viewmodel.item.RecentItemViewModel;
import com.marketcountry.psbuyandsell.viewmodel.itemcategory.ItemCategoryViewModel;
import com.marketcountry.psbuyandsell.viewmodel.itemfromfollower.ItemFromFollowerViewModel;
import com.marketcountry.psbuyandsell.viewobject.Blog;
import com.marketcountry.psbuyandsell.viewobject.Item;
import com.marketcountry.psbuyandsell.viewobject.ItemCategory;
import com.marketcountry.psbuyandsell.viewobject.holder.ItemParameterHolder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import android.location.Address;
import android.widget.Toast;
//import com.panaceasoft.psbuyandsell.viewmodel.clearalldata.ClearAllDataViewModel;

public class SelectedCityFragment extends PSFragment implements DataBoundListAdapter.DiffUtilDispatchedInterface {

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private ItemCategoryViewModel itemCategoryViewModel;
    private PopularItemViewModel popularItemViewModel;
    private RecentItemViewModel recentItemViewModel;
    private BlogViewModel blogViewModel;
    private ImageView[] dots;
    private boolean layoutDone = false;
    private int loadingCount = 0;
    private PSDialogMsg psDialogMsg;
    //    private PSAPPLoadingViewModel psappLoadingViewModel;
    //    private PSAppInfoViewModel psAppInfoViewModel;
//    private ClearAllDataViewModel clearAllDataViewModel;
    private ItemFromFollowerViewModel itemFromFollowerViewModel;
    private ItemParameterHolder searchItemParameterHolder = new ItemParameterHolder().getRecentItem();

    private Runnable update;
    private int NUM_PAGES = 10;
    private int currentPage = 0;
    private boolean touched = false;
    private Timer unTouchedTimer;
    private Handler handler = new Handler();
    private boolean searchKeywordOnFocus = false;

    private GpsTracker gpsTracker;
    private String address;
    private double latitude;
    private double longitude;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    String isFirstEntryLocation;

    @VisibleForTesting
    private AutoClearedValue<FragmentSelectedCityBinding> binding;
    private AutoClearedValue<ItemHorizontalListAdapter> popularItemListAdapter;
    private AutoClearedValue<ItemHorizontalListAdapter> recentItemListAdapter;
    private AutoClearedValue<ItemHorizontalListAdapter> followerItemListAdapter;
    private AutoClearedValue<DashBoardViewPagerAdapter> dashBoardViewPagerAdapter;
    private AutoClearedValue<CityCategoryAdapter> cityCategoryAdapter;
    private AutoClearedValue<ViewPager> viewPager;
    private AutoClearedValue<LinearLayout> pageIndicatorLayout;

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(), REQUIRED_PERMISSIONS[0])) {
                pref.edit().putString("isFirstEntryLocation","0").apply();
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(this.getActivity(), "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(this.getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);

            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                pref.edit().putString("isFirstEntryLocation","0").apply();
                ActivityCompat.requestPermissions(this.getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }

    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
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
        LocationManager locationManager = (LocationManager) this.getActivity().getSystemService(getContext().LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        FragmentSelectedCityBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_selected_city, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);

        binding.get().setLoadingMore(connectivity.isConnected());
//
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }else {
            checkRunTimePermission();
            //pref.edit().putString("isFirstEntryLocation","0").apply();
        }

        gpsTracker = new GpsTracker(this.getActivity());

        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();

        address = getCurrentAddress(latitude, longitude);
        isFirstEntryLocation = pref.getString("isFirstEntryLocation","0");
        Log.v("알림 pref 1 ", isFirstEntryLocation);
        if(isFirstEntryLocation.equals("0") && !address.equals("주소 미발견"))
        {
            Log.v("알림 gps ", address);
            pref.edit().putString("isFirstEntryLocation","1").apply();
            Log.v("알림 pref 2 ", pref.getString("isFirstEntryLocation","0"));
            pref.edit().putString(Constants.SELECTED_LOCATION_NAME,address).apply();

        }
//

        return binding.get().getRoot();
    }
    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this.getActivity(), Locale.KOREA);

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    10);

        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this.getActivity(), "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this.getActivity(), "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this.getActivity(), "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }

        Address address = addresses.get(0);

        String cut[] = addresses.get(0).toString().split(" ");
        for(int i=0; i<cut.length; i++){
            System.out.println("cut["+i+"] : " + cut[i]);
        }

        /*for (int i=0;i <= address.getMaxAddressLineIndex();i++) {

            //여기서 변환된 주소 확인할  수 있음

            Log.v("알림", "AddressLine(" + i + ")" + address.getAddressLine(i) + "\n");
            Log.v("알림", cut[1] + " " + cut[2] + " " + cut[3]);
        }*/

        String convertAddr = cut[1].substring(0,2);
        return convertAddr;
    }
    @Override
    protected void initUIAndActions() {

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) this.getActivity()).binding.toolbar.setBackgroundColor(getResources().getColor(R.color.layout__primary_background));
            ((MainActivity) getActivity()).updateToolbarIconColor(Color.GRAY);
            ((MainActivity) getActivity()).updateMenuIconGrey();
            ((MainActivity) getActivity()).refreshPSCount();
        }

        getIntentData();

        if (Config.SHOW_ADMOB && connectivity.isConnected()) {
            AdRequest adRequest2 = new AdRequest.Builder()
                    .build();
            binding.get().adView2.loadAd(adRequest2);
        } else {
            binding.get().adView2.setVisibility(View.GONE);
        }

        psDialogMsg = new PSDialogMsg(getActivity(), false);

        viewPager = new AutoClearedValue<>(this, binding.get().blogViewPager);

        pageIndicatorLayout = new AutoClearedValue<>(this, binding.get().pagerIndicator);

        binding.get().blogViewAllTextView.setOnClickListener(v -> navigationController.navigateToBlogList(getActivity()));

        binding.get().popularViewAllTextView.setOnClickListener(v -> navigationController.navigateToHomeFilteringActivity(getActivity(), popularItemViewModel.popularItemParameterHolder, getString(R.string.selected_city_popular_item), selectedCityLat, selectedCityLng, Constants.MAP_MILES));

        binding.get().followerViewAllTextView.setOnClickListener(v -> navigationController.navigateToItemListFromFollower(getActivity()));

        binding.get().recentItemViewAllTextView.setOnClickListener(v -> navigationController.navigateToHomeFilteringActivity(getActivity(), recentItemViewModel.recentItemParameterHolder, getString(R.string.selected_city_recent), selectedCityLat, selectedCityLng, Constants.MAP_MILES));

        binding.get().categoryViewAllTextView.setOnClickListener(v -> navigationController.navigateToCategoryActivity(getActivity()));

        binding.get().addItemButton.setOnClickListener(v -> {

            Utils.navigateOnUserVerificationActivity(userIdToVerify, loginUserId, psDialogMsg, getActivity(), navigationController, new Utils.NavigateOnUserVerificationActivityCallback() {
                @Override
                public void onSuccess() {
                    navigationController.navigateToItemEntryActivity(SelectedCityFragment.this.getActivity(), Constants.ADD_NEW_ITEM, recentItemViewModel.locationId, recentItemViewModel.locationName);
                }
            });

        });

        binding.get().locationTextView.setOnClickListener(v -> navigationController.navigateToLocationActivity(getActivity(), Constants.SELECT_LOCATION_FROM_HOME,selected_location_id));


//        binding.get().blogViewPager.setOnFocusChangeListener((v, hasFocus) -> {
//            if (hasFocus) {
//                binding.get().searchBoxEditText.clearFocus();
//            }
//        });

        binding.get().searchBoxEditText.setOnFocusChangeListener((v, hasFocus) -> {

            searchKeywordOnFocus = hasFocus;
            Utils.psLog("Focus " + hasFocus);
        });
        binding.get().searchBoxEditText.setOnKeyListener((v, keyCode, event) -> {

            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                binding.get().searchBoxEditText.clearFocus();
                searchKeywordOnFocus = false;
                callSearchList();
                Utils.psLog("Down");

                return false;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {

                Utils.psLog("Up");
            }
            return false;
        });
        binding.get().searchImageButton.setOnClickListener(v -> SelectedCityFragment.this.callSearchList());

        if (viewPager.get() != null && viewPager.get() != null && viewPager.get() != null) {
            viewPager.get().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    if (searchKeywordOnFocus) {
                        binding.get().searchBoxEditText.clearFocus();
                    }
                }

                @Override
                public void onPageSelected(int position) {

                    currentPage = position;

                    if (pageIndicatorLayout.get() != null) {

                        setupSliderPagination();
                    }

                    for (ImageView dot : dots) {
                        if (dots != null) {
                            dot.setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));
                        }
                    }

                    if (dots != null && dots.length > position) {
                        dots[position].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));
                    }

                    touched = true;

                    handler.removeCallbacks(update);

                    setUnTouchedTimer();

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        startPagerAutoSwipe();

        if (force_update) {
            navigationController.navigateToForceUpdateActivity(this.getActivity(), force_update_title, force_update_msg);
        }
    }

    private void callSearchList() {

        searchItemParameterHolder.keyword = binding.get().searchBoxEditText.getText().toString();

        navigationController.navigateToHomeFilteringActivity(getActivity(), searchItemParameterHolder, searchItemParameterHolder.keyword, selectedCityLat, selectedCityLng, Constants.MAP_MILES);

    }


    @Override
    protected void initViewModels() {
        itemCategoryViewModel = ViewModelProviders.of(this, viewModelFactory).get(ItemCategoryViewModel.class);
        recentItemViewModel = ViewModelProviders.of(this, viewModelFactory).get(RecentItemViewModel.class);
        popularItemViewModel = ViewModelProviders.of(this, viewModelFactory).get(PopularItemViewModel.class);
        blogViewModel = ViewModelProviders.of(this, viewModelFactory).get(BlogViewModel.class);
        itemFromFollowerViewModel = ViewModelProviders.of(this, viewModelFactory).get(ItemFromFollowerViewModel.class);
//        psAppInfoViewModel = ViewModelProviders.of(this, viewModelFactory).get(PSAppInfoViewModel.class);
//        clearAllDataViewModel = ViewModelProviders.of(this, viewModelFactory).get(ClearAllDataViewModel.class);
    }

    @Override
    protected void initAdapters() {


        DashBoardViewPagerAdapter nvAdapter3 = new DashBoardViewPagerAdapter(dataBindingComponent, blog -> navigationController.navigateToBlogDetailActivity(SelectedCityFragment.this.getActivity(), blog.id));

        this.dashBoardViewPagerAdapter = new AutoClearedValue<>(this, nvAdapter3);
        viewPager.get().setAdapter(dashBoardViewPagerAdapter.get());

        CityCategoryAdapter cityCategoryAdapter = new CityCategoryAdapter(dataBindingComponent,
                category -> navigationController.navigateToSubCategoryActivity(getActivity(), category.id, category.name), this);

        this.cityCategoryAdapter = new AutoClearedValue<>(this, cityCategoryAdapter);
        binding.get().cityCategoryRecyclerView.setAdapter(cityCategoryAdapter);


        ItemHorizontalListAdapter followerItemListAdapter = new ItemHorizontalListAdapter(dataBindingComponent, item -> navigationController.navigateToItemDetailActivity(SelectedCityFragment.this.getActivity(), item.id, item.title), this);
        this.followerItemListAdapter = new AutoClearedValue<>(this, followerItemListAdapter);
        binding.get().followerRecyclerView.setAdapter(followerItemListAdapter);

        ItemHorizontalListAdapter popularAdapter = new ItemHorizontalListAdapter(dataBindingComponent, item -> navigationController.navigateToItemDetailActivity(SelectedCityFragment.this.getActivity(), item.id, item.title), this);

        this.popularItemListAdapter = new AutoClearedValue<>(this, popularAdapter);
        binding.get().popularItemRecyclerView.setAdapter(popularAdapter);

        ItemHorizontalListAdapter recentAdapter = new ItemHorizontalListAdapter(dataBindingComponent, item ->
                navigationController.navigateToItemDetailActivity(this.getActivity(), item.id, item.title), this);

        this.recentItemListAdapter = new AutoClearedValue<>(this, recentAdapter);
        binding.get().recentItemRecyclerView.setAdapter(recentAdapter);


    }

    private void replaceItemFromFollowerList(List<Item> itemList) {
        this.followerItemListAdapter.get().replace(itemList);
        binding.get().executePendingBindings();
    }

    private void replaceRecentItemList(List<Item> itemList) {
        this.recentItemListAdapter.get().replace(itemList);
        binding.get().executePendingBindings();
    }

    private void replacePopularItemList(List<Item> itemList) {
        this.popularItemListAdapter.get().replace(itemList);
        binding.get().executePendingBindings();
    }

    private void replaceCityCategory(List<ItemCategory> categories) {
        cityCategoryAdapter.get().replace(categories);
        binding.get().executePendingBindings();
    }


    @Override
    protected void initData() {

        showItemFromFollower();

//        clearAllDataViewModel.getDeleteAllDataData().observe(this, result -> {
//
//            if (result != null) {
//                switch (result.status) {
//
//                    case ERROR:
//                        break;
//
//                    case SUCCESS:
//                        break;
//                }
//            }
//        });

        loadProducts();
    }

    private void showItemFromFollower() {
        if (loginUserId.isEmpty()) {
            hideForFollower();
        } else {
            showForFollower();
        }
    }

    private void showForFollower() {

        binding.get().followerConstraintLayout.setVisibility(View.VISIBLE);
        binding.get().followerTitleTextView.setVisibility(View.VISIBLE);
        binding.get().followerViewAllTextView.setVisibility(View.VISIBLE);
        binding.get().followerDescTextView.setVisibility(View.VISIBLE);
        binding.get().followerRecyclerView.setVisibility(View.VISIBLE);
    }

    private void hideForFollower() {

        binding.get().followerConstraintLayout.setVisibility(View.GONE);
        binding.get().followerTitleTextView.setVisibility(View.GONE);
        binding.get().followerViewAllTextView.setVisibility(View.GONE);
        binding.get().followerDescTextView.setVisibility(View.GONE);
        binding.get().followerRecyclerView.setVisibility(View.GONE);
    }

    private void getIntentData() {

        if (getActivity() != null) {
//            recentItemViewModel.locationId = getActivity().getIntent().getStringExtra(Constants.SELECTED_LOCATION_ID);
//            recentItemViewModel.locationName = getActivity().getIntent().getStringExtra(Constants.SELECTED_LOCATION_NAME);
//
//            if (getArguments() != null) {
//                recentItemViewModel.locationId = getArguments().getString(Constants.SELECTED_LOCATION_ID);
//                recentItemViewModel.locationName = getArguments().getString(Constants.SELECTED_LOCATION_NAME);
//                recentItemViewModel.locationLat = getArguments().getString(Constants.LAT);
//                recentItemViewModel.locationLng = getArguments().getString(Constants.LNG);
//            }

            recentItemViewModel.locationId = selected_location_id;
            recentItemViewModel.locationName = selected_location_name;
            recentItemViewModel.locationLat = selectedLat;
            recentItemViewModel.locationLng = selectedLng;

            recentItemViewModel.recentItemParameterHolder.location_id = recentItemViewModel.locationId;
            popularItemViewModel.popularItemParameterHolder.location_id = recentItemViewModel.locationId;
            searchItemParameterHolder.location_id = recentItemViewModel.locationId;

            binding.get().locationTextView.setText(recentItemViewModel.locationName);

        }
    }

    private void loadProducts() {

        //Blog

        blogViewModel.setNewsFeedObj(String.valueOf(Config.LIST_NEW_FEED_COUNT_PAGER), String.valueOf(blogViewModel.offset));

        blogViewModel.getNewsFeedData().observe(this, result -> {

            if (result != null) {
                switch (result.status) {
                    case SUCCESS:
                        replaceNewsFeedList(result.data);
                        blogViewModel.setLoadingState(false);
                        break;

                    case LOADING:
                        replaceNewsFeedList(result.data);
                        break;

                    case ERROR:

                        blogViewModel.setLoadingState(false);
                        break;
                }
            }

        });

        //Blog


        //City Category

        itemCategoryViewModel.setCategoryListObj(String.valueOf(Config.LIST_CATEGORY_COUNT), Constants.ZERO);

        itemCategoryViewModel.getCategoryListData().observe(this, listResource -> {

            if (listResource != null) {

                switch (listResource.status) {
                    case SUCCESS:

                        if (listResource.data != null) {

                            if (listResource.data.size() > 0) {
                                replaceCityCategory(listResource.data);
                            }

                        }
                        itemCategoryViewModel.setLoadingState(false);

                        break;

                    case LOADING:

                        if (listResource.data != null) {

                            if (listResource.data.size() > 0) {
                                replaceCityCategory(listResource.data);
                            }

                        }

                        break;

                    case ERROR:
                        itemCategoryViewModel.setLoadingState(false);
                        break;
                }
            }
        });

        //Popular Item

        popularItemViewModel.setPopularItemListByKeyObj(Utils.checkUserId(loginUserId), Config.LIMIT_FROM_DB_COUNT, Constants.ZERO, popularItemViewModel.popularItemParameterHolder);

        popularItemViewModel.getPopularItemListByKeyData().observe(this, listResource -> {

            if (listResource != null) {
                switch (listResource.status) {
                    case SUCCESS:

                        if (listResource.data != null) {
                            if (listResource.data.size() > 0) {
                                replacePopularItemList(listResource.data);
                            }
                        }

                        break;

                    case LOADING:

                        if (listResource.data != null) {
                            if (listResource.data.size() > 0) {
                                replacePopularItemList(listResource.data);
                            }
                        }

                        break;

                    case ERROR:
                        break;
                }
            }
        });

        //Popular Item

        //Recent Item

        recentItemViewModel.setRecentItemListByKeyObj(Utils.checkUserId(loginUserId), Config.LIMIT_FROM_DB_COUNT, Constants.ZERO, recentItemViewModel.recentItemParameterHolder);

        recentItemViewModel.getRecentItemListByKeyData().observe(this, listResource -> {

            if (listResource != null) {
                switch (listResource.status) {
                    case SUCCESS:

                        if (listResource.data != null) {
                            if (listResource.data.size() > 0) {
                                SelectedCityFragment.this.replaceRecentItemList(listResource.data);
                            }
                        }

                        break;

                    case LOADING:

                        if (listResource.data != null) {
                            if (listResource.data.size() > 0) {
                                SelectedCityFragment.this.replaceRecentItemList(listResource.data);
                            }
                        }
                        recentItemViewModel.setLoadingState(false);
                        break;

                    case ERROR:
                        break;
                }
            }
        });

        // Item from follower

        itemFromFollowerViewModel.setItemFromFollowerListObj(Utils.checkUserId(loginUserId), Config.LIMIT_FROM_DB_COUNT, Constants.ZERO);

        itemFromFollowerViewModel.getItemFromFollowerListData().observe(this, listResource -> {

            if (listResource != null) {
                switch (listResource.status) {
                    case LOADING:

                        if (listResource.data != null) {
                            if (listResource.data.size() > 0) {
                                replaceItemFromFollowerList(listResource.data);
                            }
                        }

                        break;
                    case SUCCESS:

                        if (listResource.data != null) {
                            if (listResource.data.size() > 0) {
                                replaceItemFromFollowerList(listResource.data);
                                showForFollower();
                            }
                        } else {
                            hideForFollower();
                        }
                        itemFromFollowerViewModel.setLoadingState(false);
                        break;

                    case ERROR:
                        break;
                }
            }
        });

        //endregion


        viewPager.get().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {


                if (binding.get() != null && viewPager.get() != null) {
                    if (viewPager.get().getChildCount() > 0) {
                        layoutDone = true;
                        loadingCount++;
                        hideLoading();
                        viewPager.get().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            }
        });
    }


    @Override
    public void onDispatched() {

//        if (homeLatestProductViewModel.loadingDirection == Utils.LoadingDirection.top) {
//
//            LinearLayoutManager layoutManager = (LinearLayoutManager)
//                    binding.get().productList.getLayoutManager();
//
//            if (layoutManager != null) {
//                layoutManager.scrollToPosition(0);
//            }
//
//        }
//
//        if (homeSearchProductViewModel.loadingDirection == Utils.LoadingDirection.top) {
//
//            GridLayoutManager layoutManager = (GridLayoutManager)
//                    binding.get().discountList.getLayoutManager();
//
//            if (layoutManager != null) {
//                layoutManager.scrollToPosition(0);
//            }
//
//        }
//
//        if (homeTrendingProductViewModel.loadingDirection == Utils.LoadingDirection.top) {
//
//            GridLayoutManager layoutManager = (GridLayoutManager)
//                    binding.get().trendingList.getLayoutManager();
//
//            if (layoutManager != null) {
//                layoutManager.scrollToPosition(0);
//            }
//
//        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void setupSliderPagination() {

        int dotsCount = dashBoardViewPagerAdapter.get().getCount();

        if (dotsCount > 0 && dots == null) {

            dots = new ImageView[dotsCount];

            if (binding.get() != null) {
                if (pageIndicatorLayout.get().getChildCount() > 0) {
                    pageIndicatorLayout.get().removeAllViewsInLayout();
                }
            }

            for (int i = 0; i < dotsCount; i++) {
                dots[i] = new ImageView(getContext());
                dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                params.setMargins(4, 0, 4, 0);

                pageIndicatorLayout.get().addView(dots[i], params);
            }

            dots[0].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));

        }

    }

    private void hideLoading() {

        if (loadingCount == 3 && layoutDone) {

            binding.get().loadingView.setVisibility(View.GONE);
            binding.get().loadHolder.setVisibility(View.GONE);
        }
    }

    private void startPagerAutoSwipe() {

        update = () -> {
            if (!touched) {
                if (currentPage == NUM_PAGES) {
                    currentPage = 0;
                }

                if (viewPager.get() != null) {
                    viewPager.get().setCurrentItem(currentPage++, true);
                }

            }
        };

        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!searchKeywordOnFocus) {
                    handler.post(update);
                }
            }
        }, 1000, 3000);
    }

    private void setUnTouchedTimer() {

        if (unTouchedTimer == null) {
            unTouchedTimer = new Timer();
            unTouchedTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    touched = false;
                    if (!searchKeywordOnFocus) {
                        handler.post(update);
                    }
                }
            }, 3000, 6000);
        } else {
            unTouchedTimer.cancel();
            unTouchedTimer.purge();

            unTouchedTimer = new Timer();
            unTouchedTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    touched = false;
                    if (!searchKeywordOnFocus) {
                        handler.post(update);
                    }
                }
            }, 3000, 6000);
        }
    }

    private void replaceNewsFeedList(List<Blog> blogs) {
        this.dashBoardViewPagerAdapter.get().replaceNewsFeedList(blogs);
        binding.get().executePendingBindings();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (requestCode == Constants.REQUEST_CODE__SELECTED_CITY_FRAGMENT
                    && resultCode == Constants.RESULT_CODE__SEARCH_WITH_ITEM_LOCATION_TYPE) {

                recentItemViewModel.locationId = data.getStringExtra(Constants.ITEM_LOCATION_TYPE_ID);
                recentItemViewModel.locationName = data.getStringExtra(Constants.ITEM_LOCATION_TYPE_NAME);
                recentItemViewModel.locationLat = data.getStringExtra(Constants.LAT);
                recentItemViewModel.locationLng = data.getStringExtra(Constants.LNG);

                pref.edit().putString(Constants.SELECTED_LOCATION_ID, recentItemViewModel.locationId).apply();
                pref.edit().putString(Constants.SELECTED_LOCATION_NAME, recentItemViewModel.locationName).apply();
                Log.v("알림 : locationName", recentItemViewModel.locationName);
                pref.edit().putString(Constants.LAT, recentItemViewModel.locationLat).apply();
                pref.edit().putString(Constants.LNG, recentItemViewModel.locationLng).apply();


                if (getActivity() != null) {

                    navigationController.navigateToHome((MainActivity) getActivity(), true, recentItemViewModel.locationId,
                            recentItemViewModel.locationName,false);
                }
            }
        }
    }

    @Override
    public void onResume() {
        loadLoginUserId();
        super.onResume();
    }

}
