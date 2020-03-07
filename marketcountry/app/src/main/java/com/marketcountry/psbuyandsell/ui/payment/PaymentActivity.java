package com.marketcountry.psbuyandsell.ui.payment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.marketcountry.psbuyandsell.R;
import com.marketcountry.psbuyandsell.databinding.ActivityItemEntryBinding;
import com.marketcountry.psbuyandsell.utils.Constants;

public class PaymentActivity extends Activity {


    //region Override Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payment);

        Intent intent = getIntent();

        Log.d("확인 페이먼트 iName",intent.getStringExtra(Constants.ITEM_NAME));
        Log.d("확인 페이먼트 iPrice",intent.getStringExtra(Constants.ITEM_PRICE));
        Log.d("확인 페이먼트 rName",intent.getStringExtra(Constants.RECEIVE_USER_NAME));

    }
}

