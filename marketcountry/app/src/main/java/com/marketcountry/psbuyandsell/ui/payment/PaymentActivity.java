package com.marketcountry.psbuyandsell.ui.payment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.marketcountry.psbuyandsell.R;
import com.marketcountry.psbuyandsell.databinding.ActivityItemEntryBinding;
import com.marketcountry.psbuyandsell.utils.Constants;

public class PaymentActivity extends Activity {

    private final int PAY_FOR_ACCOUNT=0;
    private final int PAY_FOR_KAKAO=1;

    private int countInt;
    private String countString;
    private int priceInt;
    private int finalPrice;

    @Override
    public void onBackPressed() {
        setResult(1);
        finish();
    }

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
        Button plusBtn=findViewById(R.id.countBtnPlus);
        Button minusBtn=findViewById(R.id.countBtnMinus);
        TextView countTextView=findViewById(R.id.countTextView);
        EditText editPrice=findViewById(R.id.edit_price);

        editPrice.setText(iPrice);

        textItem.setText("상품 : "+iName);
        textPrice.setText("가격 :");


        priceInt = Integer.parseInt(editPrice.getText().toString());
        countInt = 1; //init
        finalPrice = priceInt;
        countTextView.setText("1");
        plusBtn.setText("+");
        minusBtn.setText("-");

        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editPrice.getText().toString().equals("")){
                    Toast.makeText(PaymentActivity.this, "흥정한 가격을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                int price=Integer.parseInt(editPrice.getText().toString());
                Intent intent=new Intent(PaymentActivity.this,AccountActivity.class);
                intent.putExtra("iName",iName);
                intent.putExtra("iPrice",price);
                intent.putExtra("rName",rName);
                startActivityForResult(intent,PAY_FOR_ACCOUNT);
            }
        });

        btnKakaopay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(editPrice.getText().toString().equals("")){
                    Toast.makeText(PaymentActivity.this, "흥정한 가격을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                int price=Integer.parseInt(editPrice.getText().toString());
                Intent intent=new Intent(PaymentActivity.this,KakaopayActivity.class);
                intent.putExtra("iName",iName);
                intent.putExtra("iPrice",price);
                intent.putExtra("rName",rName);
                startActivityForResult(intent,PAY_FOR_KAKAO);
            }
        });

        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countInt++;
                countString = Integer.toString(countInt);
                countTextView.setText(countString);

                finalPrice = priceInt * countInt;

                editPrice.setText(Integer.toString(finalPrice));
            }
        });

        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(1 < countInt)
                {
                    countInt--;
                    countString = Integer.toString(countInt);
                    countTextView.setText(countString);

                    finalPrice = priceInt * countInt;

                    editPrice.setText(Integer.toString(finalPrice));
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==PAY_FOR_ACCOUNT){
            setResult(0,data);
        }
        else if(requestCode==PAY_FOR_KAKAO){
            setResult(0,data);
        }
        finish();
    }
}

