package com.marketcountry.psbuyandsell.ui.payment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

        String iName=intent.getStringExtra(Constants.ITEM_NAME);
        String iPrice=intent.getStringExtra(Constants.ITEM_PRICE);
        String rName=intent.getStringExtra(Constants.RECEIVE_USER_NAME);

        Log.d("확인 페이먼트 iName",intent.getStringExtra(Constants.ITEM_NAME));
        Log.d("확인 페이먼트 iPrice",intent.getStringExtra(Constants.ITEM_PRICE));
        Log.d("확인 페이먼트 rName",intent.getStringExtra(Constants.RECEIVE_USER_NAME));

        TextView textItem=findViewById(R.id.text_item);
        TextView textPrice=findViewById(R.id.text_price);
        Button btnAccount=findViewById(R.id.btn_account);
        Button btnKakaopay=findViewById(R.id.btn_kakaopay);

        textItem.setText("상품 : "+iName);
        textPrice.setText("가격 : "+iPrice);

        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PaymentActivity.this,AccountActivity.class);
                intent.putExtra("iName",iName);
                intent.putExtra("iPrice",iPrice);
                intent.putExtra("rName",rName);
                startActivity(intent);
                finish();
            }
        });
        btnKakaopay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PaymentActivity.this,KakaopayActivity.class);
                intent.putExtra("iName",iName);
                intent.putExtra("iPrice",iPrice);
                intent.putExtra("rName",rName);
                startActivity(intent);
                finish();
            }
        });
    }
}

