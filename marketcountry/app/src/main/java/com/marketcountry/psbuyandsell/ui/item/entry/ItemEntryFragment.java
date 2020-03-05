package com.marketcountry.psbuyandsell.ui.item.entry;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.marketcountry.psbuyandsell.Config;
import com.marketcountry.psbuyandsell.GpsTracker;
import com.marketcountry.psbuyandsell.MainActivity;
import com.marketcountry.psbuyandsell.R;
import com.marketcountry.psbuyandsell.binding.FragmentDataBindingComponent;
import com.marketcountry.psbuyandsell.databinding.FragmentItemEntryBinding;
import com.marketcountry.psbuyandsell.databinding.ItemEntryBottomBoxBinding;
import com.marketcountry.psbuyandsell.ui.common.DataBoundListAdapter;
import com.marketcountry.psbuyandsell.ui.common.PSFragment;
import com.marketcountry.psbuyandsell.utils.AutoClearedValue;
import com.marketcountry.psbuyandsell.utils.Constants;
import com.marketcountry.psbuyandsell.utils.PSDialogMsg;
import com.marketcountry.psbuyandsell.utils.Utils;
import com.marketcountry.psbuyandsell.viewmodel.image.ImageViewModel;
import com.marketcountry.psbuyandsell.viewmodel.item.ItemViewModel;
import com.marketcountry.psbuyandsell.viewobject.Image;
import com.marketcountry.psbuyandsell.viewobject.Item;
import com.marketcountry.psbuyandsell.viewobject.common.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//
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
//

/**
 * A simple {@link Fragment} subclass.
 */
public class ItemEntryFragment extends PSFragment implements DataBoundListAdapter.DiffUtilDispatchedInterface {

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    private String catId = Constants.EMPTY_STRING;
    private String subCatId = Constants.EMPTY_STRING;
    private String typeId = Constants.EMPTY_STRING;
    private String priceTypeId = Constants.EMPTY_STRING;
    private String dealOptionId = Constants.EMPTY_STRING;
    private String conditionId = Constants.EMPTY_STRING;
    private String locationId = Constants.EMPTY_STRING;
    private String currencyId = Constants.EMPTY_STRING;
    private String businessMode = Constants.EMPTY_STRING;

    private String firstImageId = Constants.EMPTY_STRING;
    private String secImageId = Constants.EMPTY_STRING;
    private String thirdImageId = Constants.EMPTY_STRING;
    private String fouthImageId = Constants.EMPTY_STRING;
    private String fifthImageId = Constants.EMPTY_STRING;
    private String sixthImageId = Constants.EMPTY_STRING;
    private String seventhImageId = Constants.EMPTY_STRING;
    private String eighthImageId = Constants.EMPTY_STRING;

    private boolean isFirstImageSelected = false;
    private boolean isSecImageSelected = false;
    private boolean isThirdImageSelected = false;
    private boolean isFouthImageSelected = false;
    private boolean isFifthImageSelected = false;
    private boolean isSixthImageSelected = false;
    private boolean isSeventhImageSelected = false;
    private boolean isEighthImageSelected = false;

    private PSDialogMsg psDialogMsg;
    private ItemViewModel itemViewModel;
    private ImageViewModel imageViewModel;
    private String imagePath = "";
    private GoogleMap map;
    private Marker marker;
    private List<String> imagePathList = new ArrayList<>();
    private boolean selected = false;
    private int imageCount = 0;
    private ProgressDialog progressDialog;
    private boolean isUploadSuccess = false;

    private String typeTemp;
    private String conditionTemp;
    private GpsTracker gpsTracker;


    @VisibleForTesting
    private AutoClearedValue<FragmentItemEntryBinding> binding;
    private AutoClearedValue<BottomSheetDialog> mBottomSheetDialog;
    private AutoClearedValue<ItemEntryBottomBoxBinding> bottomBoxLayoutBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentItemEntryBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_item_entry, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);
        setHasOptionsMenu(true);
        initializeMap(savedInstanceState);

        //
        gpsTracker = new GpsTracker(this.getActivity());

        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        String address = getCurrentAddress(latitude, longitude);
        binding.get().addressEditText.setText(address);

        Toast.makeText(this.getActivity(), "현재위치 \n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();

        //

        return binding.get().getRoot();
    }


    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this.getActivity(), Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
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
        return address.getAddressLine(0).toString()+"\n";

    }

    private void initializeMap(Bundle savedInstanceState) {
        try {
            if (this.getActivity() != null) {
                MapsInitializer.initialize(this.getActivity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*binding.get().mapView.onCreate(savedInstanceState);
        bindMap(selectedLat, selectedLng);*/

    }

    /*private void bindMap(String latValue, String lngValue) {
        binding.get().mapView.onResume();

        binding.get().mapView.getMapAsync(googleMap -> {
            map = googleMap;

            try {
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.valueOf(latValue), Double.valueOf(lngValue)))
                        .title("City Name"));

                //zoom
                if (!latValue.isEmpty() && !lngValue.isEmpty()) {
                    int zoomlevel = 15;
                    // Animating to the touched position
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(latValue), Double.parseDouble(lngValue)), zoomlevel));
                }
            } catch (Exception e) {
                Utils.psErrorLog("", e);
            }

        });
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE__SEARCH_FRAGMENT && resultCode == Constants.RESULT_CODE__SEARCH_WITH_CATEGORY) {

            this.catId = data.getStringExtra(Constants.CATEGORY_ID);
            binding.get().categoryTextView.setText(data.getStringExtra(Constants.CATEGORY_NAME));
            itemViewModel.holder.cat_id = this.catId;

            navigationController.navigateToSearchActivityCategoryFragment(this.getActivity(), Constants.SUBCATEGORY, catId, subCatId); //added

            /*this.subCatId = "";
            itemViewModel.holder.sub_cat_id = this.subCatId;
            binding.get().subCategoryTextView.setText("");*/

        } else if (requestCode == Constants.REQUEST_CODE__SEARCH_FRAGMENT && resultCode == Constants.RESULT_CODE__SEARCH_WITH_SUBCATEGORY) {

            this.subCatId = data.getStringExtra(Constants.SUBCATEGORY_ID);
            binding.get().subCategoryTextView.setText(data.getStringExtra(Constants.SUBCATEGORY_NAME));
            itemViewModel.holder.sub_cat_id = this.subCatId;
        } /*else if (requestCode == Constants.REQUEST_CODE__SEARCH_VIEW_FRAGMENT && resultCode == Constants.RESULT_CODE__SEARCH_WITH_ITEM_TYPE) {

            this.typeId = data.getStringExtra(Constants.ITEM_TYPE_ID);
            binding.get().typeTextView.setText(data.getStringExtra(Constants.ITEM_TYPE_NAME));
            itemViewModel.holder.type_id = this.typeId;
        }*/ /*else if (requestCode == Constants.REQUEST_CODE__SEARCH_VIEW_FRAGMENT && resultCode == Constants.RESULT_CODE__SEARCH_WITH_ITEM_PRICE_TYPE) {

            this.priceTypeId = data.getStringExtra(Constants.ITEM_PRICE_TYPE_ID);
            binding.get().priceTypeTextView.setText(data.getStringExtra(Constants.ITEM_PRICE_TYPE_NAME));
            itemViewModel.holder.price_type_id = this.priceTypeId;
        } *//*else if (requestCode == Constants.REQUEST_CODE__SEARCH_VIEW_FRAGMENT && resultCode == Constants.RESULT_CODE__SEARCH_WITH_ITEM_CURRENCY_TYPE) {

            this.currencyId = data.getStringExtra(Constants.ITEM_CURRENCY_TYPE_ID);
            binding.get().priceTextView.setText(data.getStringExtra(Constants.ITEM_CURRENCY_TYPE_NAME));
            itemViewModel.holder.currency_id = this.currencyId;
        }*/ else if (requestCode == Constants.REQUEST_CODE__SEARCH_VIEW_FRAGMENT && resultCode == Constants.RESULT_CODE__SEARCH_WITH_ITEM_OPTION_TYPE) {

            this.dealOptionId = data.getStringExtra(Constants.ITEM_OPTION_TYPE_ID);
//            binding.get().dealOptionTextView.setText(data.getStringExtra(Constants.ITEM_OPTION_TYPE_NAME));
            itemViewModel.holder.deal_option_id = this.dealOptionId;
        } /*else if (requestCode == Constants.REQUEST_CODE__SEARCH_VIEW_FRAGMENT && resultCode == Constants.RESULT_CODE__SEARCH_WITH_ITEM_CONDITION_TYPE) {

            this.conditionId = data.getStringExtra(Constants.ITEM_CONDITION_TYPE_ID);
            binding.get().itemConditionTextView.setText(data.getStringExtra(Constants.ITEM_CONDITION_TYPE_NAME));
            itemViewModel.holder.condition_id = this.conditionId;
        }*/ else if (requestCode == Constants.REQUEST_CODE__SEARCH_VIEW_FRAGMENT && resultCode == Constants.RESULT_CODE__SEARCH_WITH_ITEM_LOCATION_TYPE) {

            this.locationId = data.getStringExtra(Constants.ITEM_LOCATION_TYPE_ID);
            itemViewModel.latValue = data.getStringExtra(Constants.LAT);
            itemViewModel.lngValue = data.getStringExtra(Constants.LNG);
            binding.get().locationTextView.setText(data.getStringExtra(Constants.ITEM_LOCATION_TYPE_NAME));
            itemViewModel.holder.location_id = this.locationId;

            itemViewModel.mapLat = itemViewModel.latValue;
            itemViewModel.mapLng = itemViewModel.lngValue;

//            bindMap(itemViewModel.latValue, itemViewModel.lngValue);
        } else if (requestCode == Constants.RESULT_CODE__TO_MAP_VIEW && resultCode == Constants.RESULT_CODE__FROM_MAP_VIEW) {

            itemViewModel.latValue = data.getStringExtra(Constants.LAT);
            itemViewModel.lngValue = data.getStringExtra(Constants.LNG);

            changeCamera();

            //bindingLatLng(itemViewModel.latValue, itemViewModel.lngValue);
        }

        //image  gallery upload

        if ((requestCode == Constants.REQUEST_CODE__FIRST_GALLERY || requestCode == Constants.REQUEST_CODE__SEC_GALLERY || requestCode == Constants.REQUEST_CODE__THIRD_GALLERY ||
                requestCode == Constants.REQUEST_CODE__FOURTH_GALLERY || requestCode == Constants.REQUEST_CODE__FIFTH_GALLERY || requestCode == Constants.REQUEST_CODE__SIXTH_GALLERY
                || requestCode == Constants.REQUEST_CODE__SEVENTH_GALLERY || requestCode == Constants.REQUEST_CODE__EIGHTH_GALLERY)
                && resultCode == Constants.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            if (requestCode == Constants.REQUEST_CODE__FIRST_GALLERY) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().firstImageView, selectedImage);
                itemViewModel.firstImagePath = convertToImagePath(selectedImage, filePathColumn);
                isFirstImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__SEC_GALLERY) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().secImageView, selectedImage);
                itemViewModel.secImagePath = convertToImagePath(selectedImage, filePathColumn);
                isSecImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__THIRD_GALLERY) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().thirdImageView, selectedImage);
                itemViewModel.thirdImagePath = convertToImagePath(selectedImage, filePathColumn);
                isThirdImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__FOURTH_GALLERY) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().fouthImageView, selectedImage);
                itemViewModel.fouthImagePath = convertToImagePath(selectedImage, filePathColumn);
                isFouthImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__FIFTH_GALLERY) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().fifthImageView, selectedImage);
                itemViewModel.fifthImagePath = convertToImagePath(selectedImage, filePathColumn);
                isFifthImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__SIXTH_GALLERY) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().sixthImageView, selectedImage);
                itemViewModel.sixthImagePath = convertToImagePath(selectedImage, filePathColumn);
                isSixthImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__SEVENTH_GALLERY) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().seventhImageView, selectedImage);
                itemViewModel.seventhImagePath = convertToImagePath(selectedImage, filePathColumn);
                isSeventhImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__EIGHTH_GALLERY) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().eighthImageView, selectedImage);
                itemViewModel.eighthImagePath = convertToImagePath(selectedImage, filePathColumn);
                isEighthImageSelected = true;
            }


        }

        //image camera

        if ((requestCode == Constants.REQUEST_CODE__FIRST_CAMERA || requestCode == Constants.REQUEST_CODE__SEC_CAMERA || requestCode == Constants.REQUEST_CODE__THIRD_CAMERA ||
                requestCode == Constants.REQUEST_CODE__FOURTH_CAMERA || requestCode == Constants.REQUEST_CODE__FIFTH_CAMERA
                || requestCode == Constants.REQUEST_CODE__SIXTH_CAMERA || requestCode == Constants.REQUEST_CODE__SEVENTH_CAMERA || requestCode == Constants.REQUEST_CODE__EIGHTH_CAMERA)
                && resultCode == Constants.RESULT_OK) {
            selected = true;

            if (requestCode == Constants.REQUEST_CODE__FIRST_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().firstImageView, navigationController.photoURI);
                itemViewModel.firstImagePath = Utils.currentPhotoPath;
                isFirstImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__SEC_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().secImageView, navigationController.photoURI);
                itemViewModel.secImagePath = Utils.currentPhotoPath;//photoURI.getPath();
                isSecImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__THIRD_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().thirdImageView, navigationController.photoURI);
                itemViewModel.thirdImagePath = Utils.currentPhotoPath;
                isThirdImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__FOURTH_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().fouthImageView, navigationController.photoURI);
                itemViewModel.fouthImagePath = Utils.currentPhotoPath;
                isFouthImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__FIFTH_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().fifthImageView, navigationController.photoURI);
                itemViewModel.fifthImagePath = Utils.currentPhotoPath;
                isFifthImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__SIXTH_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().sixthImageView, navigationController.photoURI);
                itemViewModel.sixthImagePath = Utils.currentPhotoPath;
                isSixthImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__SEVENTH_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().seventhImageView, navigationController.photoURI);
                itemViewModel.seventhImagePath = Utils.currentPhotoPath;
                isSeventhImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__EIGHTH_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindFullImageUri(binding.get().eighthImageView, navigationController.photoURI);
                itemViewModel.eighthImagePath = Utils.currentPhotoPath;
                isEighthImageSelected = true;
            }
        }

        //custom camera

        if ((requestCode == Constants.REQUEST_CODE__FIRST_CUSTOM_CAMERA || requestCode == Constants.REQUEST_CODE__SEC_CUSTOM_CAMERA || requestCode == Constants.REQUEST_CODE__THIRD_CUSTOM_CAMERA ||
                requestCode == Constants.REQUEST_CODE__FOURTH_CUSTOM_CAMERA || requestCode == Constants.REQUEST_CODE__FIFTH_CUSTOM_CAMERA
                || requestCode == Constants.REQUEST_CODE__SIXTH_CUSTOM_CAMERA || requestCode == Constants.REQUEST_CODE__SEVENTH_CUSTOM_CAMERA || requestCode == Constants.REQUEST_CODE__EIGHTH_CUSTOM_CAMERA)
                && resultCode == Constants.RESULT_CODE__ITEM_ENTRY_WITH_CUSTOM_CAMERA) {

            itemViewModel.customImageUri = data.getStringExtra(Constants.IMAGE_PATH);
            selected = true;

            if (requestCode == Constants.REQUEST_CODE__FIRST_CUSTOM_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindStorageImageUri(binding.get().firstImageView, itemViewModel.customImageUri);
                itemViewModel.firstImagePath = itemViewModel.customImageUri;
                isFirstImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__SEC_CUSTOM_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindStorageImageUri(binding.get().secImageView, itemViewModel.customImageUri);
                itemViewModel.secImagePath = itemViewModel.customImageUri;
                isSecImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__THIRD_CUSTOM_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindStorageImageUri(binding.get().thirdImageView, itemViewModel.customImageUri);
                itemViewModel.thirdImagePath = itemViewModel.customImageUri;
                isThirdImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__FOURTH_CUSTOM_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindStorageImageUri(binding.get().fouthImageView, itemViewModel.customImageUri);
                itemViewModel.fouthImagePath = itemViewModel.customImageUri;
                isFouthImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__FIFTH_CUSTOM_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindStorageImageUri(binding.get().fifthImageView, itemViewModel.customImageUri);
                itemViewModel.fifthImagePath = itemViewModel.customImageUri;
                isFifthImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__SIXTH_CUSTOM_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindStorageImageUri(binding.get().sixthImageView, itemViewModel.customImageUri);
                itemViewModel.sixthImagePath = itemViewModel.customImageUri;
                isSixthImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__SEVENTH_CUSTOM_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindStorageImageUri(binding.get().seventhImageView, itemViewModel.customImageUri);
                itemViewModel.seventhImagePath = itemViewModel.customImageUri;
                isSeventhImageSelected = true;
            }
            if (requestCode == Constants.REQUEST_CODE__EIGHTH_CUSTOM_CAMERA) {
                dataBindingComponent.getFragmentBindingAdapters().bindStorageImageUri(binding.get().eighthImageView, itemViewModel.customImageUri);
                itemViewModel.eighthImagePath = itemViewModel.customImageUri;
                isEighthImageSelected = true;
            }
        }
        //endregion


    }

    private String convertToImagePath(Uri selectedImage, String[] filePathColumn) {

        if (getActivity() != null && selectedImage != null) {
            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);

            selected = true;
            if (cursor != null) {
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imagePath = cursor.getString(columnIndex);

                cursor.close();
            }
        }
        return imagePath;
    }

    @Override
    public void onDispatched() {


    }

    @Override
    protected void initUIAndActions() {

        itemViewModel.latValue = selectedLat;
        itemViewModel.lngValue = selectedLng;

        if (getActivity() instanceof MainActivity) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            ((MainActivity) this.getActivity()).binding.toolbar.setBackgroundColor(getResources().getColor(R.color.global__primary));
            ((MainActivity) getActivity()).updateMenuIconWhite();
        }

        if (getContext() != null) {

            BottomSheetDialog mBottomSheetDialog2 = new BottomSheetDialog(getContext());
            mBottomSheetDialog = new AutoClearedValue<>(this, mBottomSheetDialog2);

            ItemEntryBottomBoxBinding bottomBoxLayoutBinding2 = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.item_entry_bottom_box, null, false);
            bottomBoxLayoutBinding = new AutoClearedValue<>(this, bottomBoxLayoutBinding2);
            mBottomSheetDialog.get().setContentView(bottomBoxLayoutBinding.get().getRoot());

        }

        psDialogMsg = new PSDialogMsg(getActivity(), false);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.message__loading));
        progressDialog.setCancelable(false);

        if (Config.SHOW_ADMOB && connectivity.isConnected()) {
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            binding.get().adView.loadAd(adRequest);
        } else {
            binding.get().adView.setVisibility(View.GONE);
        }

        binding.get().titleEditText.setHint(R.string.search__notSet);
        binding.get().categoryTextView.setHint(R.string.search__notSet);
        binding.get().subCategoryTextView.setHint(R.string.search__notSet);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        AutoClearedValue<AlertDialog.Builder> alertDialog = new AutoClearedValue<>(this, builder);
        alertDialog.get().setTitle(getResources().getString(R.string.Feature_UI__search_alert_cat_title));

        binding.get().categoryTextView.setText("");
        binding.get().subCategoryTextView.setText("");

        binding.get().categorySelectionView.setOnClickListener(view -> navigationController.navigateToSearchActivityCategoryFragment(this.getActivity(), Constants.CATEGORY, catId, subCatId));

        binding.get().subCategorySelectionView.setOnClickListener(view -> {

            if (catId.equals(Constants.NO_DATA) || catId.isEmpty()) {

                psDialogMsg.showWarningDialog(getString(R.string.error_message__choose_category), getString(R.string.app__ok));

                psDialogMsg.show();

            } else {
                navigationController.navigateToSearchActivityCategoryFragment(this.getActivity(), Constants.SUBCATEGORY, catId, subCatId);
            }
        });

        //binding.get().typeCardView.setOnClickListener(view -> navigationController.navigateToSearchViewActivity(this.getActivity(), Constants.ITEM_TYPE, typeId, priceTypeId, conditionId, dealOptionId, currencyId, locationId));

        binding.get().typeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int id) {
                switch (id){
                    case R.id.typeOption1 :
                        typeTemp = binding.get().typeOption1.getText().toString();
                        break;
                    case R.id.typeOption2 :
                        typeTemp = binding.get().typeOption2.getText().toString();
                        break;
                    case R.id.typeOption3 :
                        typeTemp = binding.get().typeOption3.getText().toString();
                        break;
                    case R.id.typeOption4 :
                        typeTemp = binding.get().typeOption4.getText().toString();
                        break;
                    case R.id.typeOption5 :
                        typeTemp = binding.get().typeOption5.getText().toString();
                        break;
                }

            }

        });

        //binding.get().itemConditionCardView.setOnClickListener(view -> navigationController.navigateToSearchViewActivity(this.getActivity(), Constants.ITEM_CONDITION_TYPE, typeId, priceTypeId, conditionId, dealOptionId, currencyId, locationId));

        binding.get().conditionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int id) {
                switch (id){
                    case R.id.conditionOption1 :
                        conditionTemp = binding.get().conditionOption1.getText().toString();
                        break;
                    case R.id.conditionOption2 :
                        conditionTemp = binding.get().conditionOption2.getText().toString();
                        break;
                    case R.id.conditionOption3 :
                        conditionTemp = binding.get().conditionOption3.getText().toString();
                        break;
                    case R.id.conditionOption4 :
                        conditionTemp = binding.get().conditionOption4.getText().toString();
                        break;

                }
            }

        });

        binding.get().priceTypeCardView.setOnClickListener(view -> navigationController.navigateToSearchViewActivity(this.getActivity(), Constants.ITEM_PRICE_TYPE, typeId, priceTypeId, conditionId, dealOptionId, currencyId, locationId));

        binding.get().dealOptionCardView.setOnClickListener(view -> navigationController.navigateToSearchViewActivity(this.getActivity(), Constants.ITEM_DEAL_OPTION_TYPE, typeId, priceTypeId, conditionId, dealOptionId, currencyId, locationId));

        binding.get().locationCardView.setOnClickListener(view -> navigationController.navigateToSearchViewActivity(this.getActivity(), Constants.ITEM_LOCATION_TYPE, typeId, priceTypeId, conditionId, dealOptionId, currencyId, locationId));

        binding.get().priceCardView.setOnClickListener(view -> navigationController.navigateToSearchViewActivity(this.getActivity(), Constants.ITEM_CURRENCY_TYPE, typeId, priceTypeId, conditionId, dealOptionId, currencyId, locationId));

        /*binding.get().mapViewButton.setOnClickListener(v -> {

            map.clear();

            if(itemViewModel.itemId.equals(Constants.ADD_NEW_ITEM)){
                navigationController.navigateToMapActivity(ItemEntryFragment.this.getActivity(), selectedLng, selectedLat, Constants.MAP_PICK);
            }else {
                navigationController.navigateToMapActivity(ItemEntryFragment.this.getActivity(), itemViewModel.mapLng, itemViewModel.mapLat, Constants.MAP_PICK);
            }


        });*/

        binding.get().submitButton.setOnClickListener(view -> {

            if (itemViewModel.firstImagePath == null && itemViewModel.secImagePath == null && itemViewModel.thirdImagePath == null && itemViewModel.fouthImagePath == null && itemViewModel.fifthImagePath == null
                && itemViewModel.sixthImagePath == null && itemViewModel.seventhImagePath == null && itemViewModel.eighthImagePath == null) {
                psDialogMsg.showWarningDialog(getString(R.string.item_entry_need_image), getString(R.string.app__ok));
                psDialogMsg.show();
            } else if (binding.get().titleEditText.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog(getString(R.string.item_entry_need_list_title), getString(R.string.app__ok));
                psDialogMsg.show();
            } else if (binding.get().categoryTextView.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog(getString(R.string.item_entry_need_category), getString(R.string.app__ok));
                psDialogMsg.show();
            } else if (binding.get().subCategoryTextView.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog(getString(R.string.item_entry_need_subcategory), getString(R.string.app__ok));
                psDialogMsg.show();
            } else if (binding.get().typeRadioGroup.isSelected()) {
                psDialogMsg.showWarningDialog(getString(R.string.item_entry_need_type), getString(R.string.app__ok));
                psDialogMsg.show();
            } else if (binding.get().conditionRadioGroup.isSelected()) {
                psDialogMsg.showWarningDialog(getString(R.string.item_entry_need_item_condition), getString(R.string.app__ok));
                psDialogMsg.show();
            } /*else if (binding.get().dealOptionTextView.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog(getString(R.string.item_entry_need_deal_option), getString(R.string.app__ok));
                psDialogMsg.show();
            }*/ else if (binding.get().descEditText.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog(getString(R.string.item_entry_need_description), getString(R.string.app__ok));
                psDialogMsg.show();
            } else if (binding.get().priceEditText.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog(getString(R.string.item_entry_need_price), getString(R.string.app__ok));
                psDialogMsg.show();
            } /*else if (binding.get().priceTextView.getText().toString().isEmpty()) {
                psDialogMsg.showWarningDialog(getString(R.string.item_entry_need_currency_symbol), getString(R.string.app__ok));
                psDialogMsg.show();
            }*/ else {

                isUploadSuccess = false;

                getImagePathList();

                checkIsShop();

                if (itemViewModel.itemId != null) {
                    if (!itemViewModel.itemId.equals(Constants.ADD_NEW_ITEM)) {//edit
                        itemViewModel.setUploadItemObj(this.catId, this.subCatId, this.typeTemp, this.priceTypeId, this.currencyId, this.conditionTemp, this.locationId,
                                "", binding.get().descEditText.getText().toString(),
                                /*binding.get().highlightInfoEditText.getText().toString()*/"", binding.get().priceEditText.getText().toString(), this.dealOptionId,
                                binding.get().brandEditText.getText().toString(), businessMode, itemViewModel.is_sold_out, binding.get().titleEditText.getText().toString(), binding.get().addressEditText.getText().toString(),
                                itemViewModel.latValue, itemViewModel.lngValue, itemViewModel.itemId, loginUserId);
                    } else {//add new item
                        itemViewModel.setUploadItemObj(this.catId, this.subCatId, this.typeTemp, this.priceTypeId, this.currencyId, this.conditionTemp, this.locationId,
                                "", binding.get().descEditText.getText().toString(),
                                /*binding.get().highlightInfoEditText.getText().toString()*/"", binding.get().priceEditText.getText().toString(), this.dealOptionId,
                                binding.get().brandEditText.getText().toString(), businessMode, "", binding.get().titleEditText.getText().toString(), binding.get().addressEditText.getText().toString(),
                                itemViewModel.latValue, itemViewModel.lngValue, "", loginUserId);
                    }

                }

                progressDialog.show();
            }

        });

        binding.get().firstImageView.setOnClickListener(v -> {
            mBottomSheetDialog.get().show();
            ButtonSheetClick(Constants.ONE);
        });

        binding.get().secImageView.setOnClickListener(v -> {
            mBottomSheetDialog.get().show();
            ButtonSheetClick(Constants.TWO);
        });

        binding.get().thirdImageView.setOnClickListener(v -> {
            mBottomSheetDialog.get().show();
            ButtonSheetClick(Constants.THREE);
        });

        binding.get().fouthImageView.setOnClickListener(v -> {
            mBottomSheetDialog.get().show();
            ButtonSheetClick(Constants.FOUR);
        });

        binding.get().fifthImageView.setOnClickListener(v -> {
            mBottomSheetDialog.get().show();
            ButtonSheetClick(Constants.FIVE);
        });


        binding.get().sixthImageView.setOnClickListener(v -> {
            mBottomSheetDialog.get().show();
            ButtonSheetClick(Constants.SIX);
        });


        binding.get().seventhImageView.setOnClickListener(v -> {
            mBottomSheetDialog.get().show();
            ButtonSheetClick(Constants.SEVEN);
        });


        binding.get().eighthImageView.setOnClickListener(v -> {
            mBottomSheetDialog.get().show();
            ButtonSheetClick(Constants.EIGHT);
        });


    }

    private void getImagePathList() {

        if (!itemViewModel.firstImagePath.isEmpty()) {
            imagePathList.add(itemViewModel.firstImagePath);
        }
        if (!itemViewModel.secImagePath.isEmpty()) {
            imagePathList.add(itemViewModel.secImagePath);
        }
        if (!itemViewModel.thirdImagePath.isEmpty()) {
            imagePathList.add(itemViewModel.thirdImagePath);
        }
        if (!itemViewModel.fouthImagePath.isEmpty()) {
            imagePathList.add(itemViewModel.fouthImagePath);
        }
        if (!itemViewModel.fifthImagePath.isEmpty()) {
            imagePathList.add(itemViewModel.fifthImagePath);
        }
        if (!itemViewModel.sixthImagePath.isEmpty()) {
            imagePathList.add(itemViewModel.sixthImagePath);
        }
        if (!itemViewModel.seventhImagePath.isEmpty()) {
            imagePathList.add(itemViewModel.seventhImagePath);
        }
        if (!itemViewModel.eighthImagePath.isEmpty()) {
            imagePathList.add(itemViewModel.eighthImagePath);
        }
    }

    private void checkIsShop() {

        if (binding.get().isShopCheckBox.isChecked()) {
            businessMode = Constants.ONE;
        } else {
            businessMode = Constants.ZERO;
        }
    }

    private void ButtonSheetClick(String flag) {
        bottomBoxLayoutBinding.get().cameraButton.setOnClickListener(v -> {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                openCamera(flag);
            } else {
                if (getActivity() != null) {
                    if ((getActivity()).checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                            ((getActivity()).checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)) {

                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, Constants.REQUEST_CODE__PERMISSION_CODE);
                    } else {
                        //granted
//                        navigationController.navigateToCamera(getActivity());
                        openCamera(flag);
                    }
                }
            }

            mBottomSheetDialog.get().dismiss();
        });

        bottomBoxLayoutBinding.get().galleryButton.setOnClickListener(v -> {

//            bottomBoxLayoutBinding.get().galleryButton.setBackgroundResource(R.color.button__select_green);
//            bottomBoxLayoutBinding.get().cameraButton.setBackgroundResource(R.color.md_white_1000);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                navigationController.navigateToGallery(getActivity(), flag);
            } else {
                if (getActivity() != null) {
                    if ((getActivity()).checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                            ((getActivity()).checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)) {

                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, Constants.REQUEST_CODE__PERMISSION_CODE);
                    } else {
                        //granted
                        navigationController.navigateToGallery(getActivity(), flag);
                    }
                }
            }

            mBottomSheetDialog.get().dismiss();

        });


    }

//    Uri photoURI;
//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        // Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
//            // Create the File where the photo should go
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                // Error occurred while creating the File
//
//            }
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                photoURI = FileProvider.getUriForFile(getContext(),
//                        "com.panaceasoft.psbuyandsell.fileprovider",
//                        photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityForResult(takePictureIntent,  Constants.REQUEST_CODE__SEC_CAMERA);
//            }
//        }
//    }

//    private File createImageFile() throws IOException {
//        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
//
//        // Save a file: path for use with ACTION_VIEW intents
//        currentPhotoPath = image.getAbsolutePath();
//        return image;
//    }

    private void openCamera(String flag) {
        if (getActivity() != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.TITLE, "New Picture");
            contentValues.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");

            if (cameraType.equals(Constants.DEFAULT_CAMERA)) {
                navigationController.navigateToCamera(getActivity(), flag);
//                dispatchTakePictureIntent();
            } else {
                navigationController.navigateToCustomCamera(getActivity(), flag);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_CODE__PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //open camera
//                navigationController.navigateToCamera(getActivity());
                openCamera(Constants.ZERO);
            } else {
                //permission denied
                Toast.makeText(getContext(), "permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void initViewModels() {
        itemViewModel = ViewModelProviders.of(this, viewModelFactory).get(ItemViewModel.class);
        imageViewModel = ViewModelProviders.of(this, viewModelFactory).get(ImageViewModel.class);

    }

    @Override
    protected void initAdapters() {

    }

    @Override
    protected void initData() {

        getIntentData();

        //bindingLatLng(itemViewModel.latValue, itemViewModel.lngValue);

        getItemDetail();

        getImageList();

        itemViewModel.getUploadItemData().observe(this, result -> {

            if (result != null) {

                switch (result.status) {
                    case SUCCESS:
                        if (result.data != null) {
                            if (selected) {

                                progressDialog.cancel();
                                itemViewModel.itemId = result.data.id;

                                if (isFirstImageSelected) {//reload
                                    if (imagePathList.size() > 1) {//multi image from start
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(0), itemViewModel.itemId, firstImageId);
                                    } else {//single image from end for last update
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(imagePathList.size() - 1), itemViewModel.itemId, firstImageId);
                                        Utils.psLog("dididididididid");
                                    }
                                    progressDialog.show();
                                    isFirstImageSelected = false;
                                } else if (isSecImageSelected) {
                                    if (imagePathList.size() > 1) {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(0), itemViewModel.itemId, secImageId);
                                    } else {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(imagePathList.size() - 1), itemViewModel.itemId, secImageId);
                                    }
                                    progressDialog.show();
                                    isSecImageSelected = false;
                                } else if (isThirdImageSelected) {
                                    if (imagePathList.size() > 1) {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(0), itemViewModel.itemId, thirdImageId);
                                    } else {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(imagePathList.size() - 1), itemViewModel.itemId, thirdImageId);
                                    }
                                    progressDialog.show();
                                    isThirdImageSelected = false;
                                } else if (isFouthImageSelected) {
                                    if (imagePathList.size() > 1) {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(0), itemViewModel.itemId, fouthImageId);
                                    } else {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(imagePathList.size() - 1), itemViewModel.itemId, fouthImageId);
                                    }
                                    progressDialog.show();
                                    isFouthImageSelected = false;
                                } else if (isFifthImageSelected) {
                                    if (imagePathList.size() > 1) {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(0), itemViewModel.itemId, fifthImageId);
                                    } else {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(imagePathList.size() - 1), itemViewModel.itemId, fifthImageId);
                                    }
                                    progressDialog.show();
                                    isFifthImageSelected = false;
                                } else if (isSixthImageSelected) {
                                    if (imagePathList.size() > 1) {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(0), itemViewModel.itemId, sixthImageId);
                                    } else {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(imagePathList.size() - 1), itemViewModel.itemId, sixthImageId);
                                    }
                                    progressDialog.show();
                                    isSixthImageSelected = false;
                                } else if (isSeventhImageSelected) {
                                    if (imagePathList.size() > 1) {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(0), itemViewModel.itemId, seventhImageId);
                                    } else {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(imagePathList.size() - 1), itemViewModel.itemId, seventhImageId);
                                    }
                                    progressDialog.show();
                                    isSeventhImageSelected = false;
                                } else if (isEighthImageSelected) {
                                    if (imagePathList.size() > 1) {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(0), itemViewModel.itemId, eighthImageId);
                                    } else {
                                        itemViewModel.setUploadItemImageObj(imagePathList.get(imagePathList.size() - 1), itemViewModel.itemId, eighthImageId);
                                    }
                                    progressDialog.show();
                                    isEighthImageSelected = false;
                                }


                            } else {
                                Toast.makeText(getActivity(), "상품이 성공적으로 업로드되었습니다", Toast.LENGTH_SHORT).show();
                                progressDialog.cancel();

                                if (Config.CLOSE_ENTRY_AFTER_SUBMIT) {
                                    if (getActivity() != null) {
                                        getActivity().finish();
                                    }
                                }

                            }

                        }

                        break;

                    case ERROR:
                        progressDialog.cancel();
                        psDialogMsg.showErrorDialog(getString(R.string.error_message__item_cannot_upload), getString(R.string.app__ok));
                        psDialogMsg.show();
                        break;
                }
            }

        });

        itemViewModel.getUploadItemImageData().observe(this, result -> {

            if (result != null) {
                switch (result.status) {
                    case SUCCESS:
                        progressDialog.cancel();
                        if (!isUploadSuccess) {
//                            int toastImageCount = imageCount+1;
//                            Toast.makeText(ItemEntryFragment.this.getActivity(), "Success image : "+toastImageCount+" uploaded", Toast.LENGTH_SHORT).show();
                            imageCount += 1;

                            if (imagePathList.size() > imageCount) {
                                ItemEntryFragment.this.callImageUpload(imageCount);//first is one
                            } else {

                                isUploadSuccess = true;
                                imageViewModel.setImageParentId(Constants.IMAGE_TYPE_PRODUCT, itemViewModel.itemId);

                                if(Config.CLOSE_ENTRY_AFTER_SUBMIT){
                                    if(getActivity()!=null){
                                        getActivity().finish();
                                    }
                                }


                            }
                        }
//                        else {
//                            if (ItemEntryFragment.this.getActivity() != null) {
//                                ItemEntryFragment.this.getActivity().finish();
//                            }
//                        }

                        break;

                    case ERROR:

                        Toast.makeText(ItemEntryFragment.this.getActivity(), "Error", Toast.LENGTH_SHORT).show();
                        progressDialog.cancel();
                        psDialogMsg.showErrorDialog(ItemEntryFragment.this.getString(R.string.error_message__image_cannot_upload), ItemEntryFragment.this.getString(R.string.app__ok));
                        psDialogMsg.show();
                        break;
                }

            }
        });
    }

    private void getImageList() {
        LiveData<Resource<List<Image>>> imageListLiveData = imageViewModel.getImageListLiveData();
        imageViewModel.setImageParentId(Constants.IMAGE_TYPE_PRODUCT, itemViewModel.itemId);
        imageListLiveData.observe(this, listResource -> {
            // we don't need any null checks here for the adapter since LiveData guarantees that
            // it won't call us if fragment is stopped or not started.
            if (listResource != null && listResource.data != null) {
                Utils.psLog("Got Data");

                //fadeIn Animation
                fadeIn(binding.get().getRoot());

                // Update the data
                bindingImageListData(listResource.data);
//                this.binding.get().executePendingBindings();

            } else {
                //noinspection Constant Conditions
                Utils.psLog("Empty Data");
            }
        });
    }

    private void bindingImageListData(List<Image> imageList) {

        if (imageList.size() != 0) {
            if (imageList.size() == 1) {
                firstImageId = imageList.get(0).imgId;
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().firstImageView, imageList.get(0).imgPath);
            }
            if (imageList.size() == 2) {
                firstImageId = imageList.get(0).imgId;
                secImageId = imageList.get(1).imgId;
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().firstImageView, imageList.get(0).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().secImageView, imageList.get(1).imgPath);
            }
            if (imageList.size() == 3) {
                firstImageId = imageList.get(0).imgId;
                secImageId = imageList.get(1).imgId;
                thirdImageId = imageList.get(2).imgId;
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().firstImageView, imageList.get(0).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().secImageView, imageList.get(1).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().thirdImageView, imageList.get(2).imgPath);
            }
            if (imageList.size() == 4) {
                firstImageId = imageList.get(0).imgId;
                secImageId = imageList.get(1).imgId;
                thirdImageId = imageList.get(2).imgId;
                fouthImageId = imageList.get(3).imgId;
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().firstImageView, imageList.get(0).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().secImageView, imageList.get(1).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().thirdImageView, imageList.get(2).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fouthImageView, imageList.get(3).imgPath);
            }
            if (imageList.size() == 5) {
                firstImageId = imageList.get(0).imgId;
                secImageId = imageList.get(1).imgId;
                thirdImageId = imageList.get(2).imgId;
                fouthImageId = imageList.get(3).imgId;
                fifthImageId = imageList.get(4).imgId;
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().firstImageView, imageList.get(0).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().secImageView, imageList.get(1).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().thirdImageView, imageList.get(2).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fouthImageView, imageList.get(3).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fifthImageView, imageList.get(4).imgPath);
            }
            if (imageList.size() == 6) {
                firstImageId = imageList.get(0).imgId;
                secImageId = imageList.get(1).imgId;
                thirdImageId = imageList.get(2).imgId;
                fouthImageId = imageList.get(3).imgId;
                fifthImageId = imageList.get(4).imgId;
                sixthImageId = imageList.get(5).imgId;
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().firstImageView, imageList.get(0).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().secImageView, imageList.get(1).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().thirdImageView, imageList.get(2).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fouthImageView, imageList.get(3).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fifthImageView, imageList.get(4).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().sixthImageView, imageList.get(5).imgPath);
            }
            if (imageList.size() == 7) {
                firstImageId = imageList.get(0).imgId;
                secImageId = imageList.get(1).imgId;
                thirdImageId = imageList.get(2).imgId;
                fouthImageId = imageList.get(3).imgId;
                fifthImageId = imageList.get(4).imgId;
                sixthImageId = imageList.get(5).imgId;
                seventhImageId = imageList.get(6).imgId;
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().firstImageView, imageList.get(0).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().secImageView, imageList.get(1).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().thirdImageView, imageList.get(2).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fouthImageView, imageList.get(3).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fifthImageView, imageList.get(4).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().sixthImageView, imageList.get(5).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().seventhImageView, imageList.get(6).imgPath);
            }
            if (imageList.size() == 8) {
                firstImageId = imageList.get(0).imgId;
                secImageId = imageList.get(1).imgId;
                thirdImageId = imageList.get(2).imgId;
                fouthImageId = imageList.get(3).imgId;
                fifthImageId = imageList.get(4).imgId;
                sixthImageId = imageList.get(5).imgId;
                seventhImageId = imageList.get(6).imgId;
                eighthImageId = imageList.get(7).imgId;
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().firstImageView, imageList.get(0).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().secImageView, imageList.get(1).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().thirdImageView, imageList.get(2).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fouthImageView, imageList.get(3).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().fifthImageView, imageList.get(4).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().sixthImageView, imageList.get(5).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().seventhImageView, imageList.get(6).imgPath);
                dataBindingComponent.getFragmentBindingAdapters().bindFullImage(binding.get().eighthImageView, imageList.get(7).imgPath);
            }
        }
    }

    private void getItemDetail() {

        LiveData<Item> historyItemList = itemViewModel.getItemDetailFromDBByIdData();
        if (historyItemList != null) {
            historyItemList.observe(this, listResource -> {
                if (listResource != null) {
                    bindingItemDetailData(listResource);

                }

            });
        }
    }

    private void bindingItemDetailData(Item item) {
        binding.get().titleEditText.setText(item.title);
        itemViewModel.holder.cat_id = item.catId;
        itemViewModel.holder.sub_cat_id = item.subCatId;
        //itemViewModel.holder.type_id = item.itemTypeId;
        //itemViewModel.holder.condition_id = item.conditionOfItem;
        itemViewModel.holder.price_type_id = item.itemPriceTypeId;
        itemViewModel.holder.currency_id = item.itemCurrencyId;
        itemViewModel.holder.location_id = item.itemLocation.id;
        itemViewModel.holder.deal_option_id = item.dealOptionId;
        itemViewModel.is_sold_out = item.isSoldOut;
        this.catId = item.catId;
        this.subCatId = item.subCatId;
        //this.typeId = item.itemTypeId;
        //this.conditionId = item.conditionOfItem;
        this.priceTypeId = item.itemPriceTypeId;
        this.currencyId = item.itemCurrencyId;
        this.locationId = item.itemLocation.id;
        this.dealOptionId = item.dealOptionId;
//        binding.get().dealOptionTextView.setText(item.itemDealOption.name);
        binding.get().categoryTextView.setText(item.category.name);
        binding.get().subCategoryTextView.setText(item.subCategory.name);
        //binding.get().typeTextView.setText(item.itemType.name);
        //binding.get().itemConditionTextView.setText(item.itemCondition.name);
        //binding.get().priceTypeTextView.setText(item.itemPriceType.name);
        //binding.get().priceTextView.setText(item.itemCurrency.currencySymbol);
        binding.get().locationTextView.setText(item.itemLocation.name);
//        bindMap(item.lat, item.lng);
        itemViewModel.mapLat = item.lat;
        itemViewModel.mapLng = item.lng;
        //bindingLatLng(item.lat, item.lng);
        binding.get().brandEditText.setText(item.brand);
        binding.get().priceEditText.setText(item.price);
        //binding.get().highlightInfoEditText.setText(item.highlightInfo);
        binding.get().descEditText.setText(item.description);
//        binding.get().remarkEditText.setText(item.dealOptionRemark);
        bindingIsShop(item.businessMode);
        binding.get().addressEditText.setText(item.address);

    }

    private void bindingIsShop(String businessMode) {
        if (businessMode.equals(Constants.ONE)) {
            binding.get().isShopCheckBox.setChecked(true);
        } else {
            binding.get().isShopCheckBox.setChecked(false);
        }
    }

    private void getIntentData() {
        try {
            if (getActivity() != null) {
                if (getActivity().getIntent().getExtras() != null) {

                    itemViewModel.itemId = getActivity().getIntent().getExtras().getString(Constants.ITEM_ID);
                    this.locationId = getActivity().getIntent().getExtras().getString(Constants.SELECTED_LOCATION_ID);
                    itemViewModel.holder.location_id = this.locationId;
                    String locationName = getActivity().getIntent().getExtras().getString(Constants.SELECTED_LOCATION_NAME);
                    binding.get().locationTextView.setText(locationName);

                    if (itemViewModel.itemId != null) {
                        if (!itemViewModel.itemId.equals(Constants.ADD_NEW_ITEM)) {//edit

                            itemViewModel.setItemDetailFromDBById(itemViewModel.itemId);

                        }
                    }
                }
            }
        } catch (Exception e) {
            Utils.psErrorLog("", e);
        }
    }

    private void callImageUpload(int imageCount) {

        if (isSecImageSelected) {
            itemViewModel.setUploadItemImageObj(imagePathList.get(imageCount), itemViewModel.itemId, secImageId);
            isSecImageSelected = false;
        } else if (isThirdImageSelected) {
            itemViewModel.setUploadItemImageObj(imagePathList.get(imageCount), itemViewModel.itemId, thirdImageId);
            isThirdImageSelected = false;
        } else if (isFouthImageSelected) {
            itemViewModel.setUploadItemImageObj(imagePathList.get(imageCount), itemViewModel.itemId, fouthImageId);
            isFouthImageSelected = false;
        } else if (isFifthImageSelected) {
            itemViewModel.setUploadItemImageObj(imagePathList.get(imageCount), itemViewModel.itemId, fifthImageId);
            isFifthImageSelected = false;
        } else if (isSixthImageSelected) {
            itemViewModel.setUploadItemImageObj(imagePathList.get(imageCount), itemViewModel.itemId, sixthImageId);
            isSixthImageSelected = false;
        } else if (isSeventhImageSelected) {
            itemViewModel.setUploadItemImageObj(imagePathList.get(imageCount), itemViewModel.itemId, seventhImageId);
            isSeventhImageSelected = false;
        } else if (isEighthImageSelected) {
            itemViewModel.setUploadItemImageObj(imagePathList.get(imageCount), itemViewModel.itemId, eighthImageId);
            isEighthImageSelected = false;
        }

    }

    /*private void bindingLatLng(String latValue, String lngValue) {
        binding.get().latitudeEditText.setText(latValue);
        binding.get().lngEditText.setText(lngValue);
    }*/

    private void changeCamera() {

        if (marker != null) {
            marker.remove();
        }

        map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(Double.valueOf(itemViewModel.latValue), Double.valueOf(itemViewModel.lngValue))).zoom(10).bearing(10).tilt(10).build()));

        marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(Double.valueOf(itemViewModel.latValue), Double.valueOf(itemViewModel.lngValue)))
                .title("Shop Name"));
    }

}
