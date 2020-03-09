package com.marketcountry.psbuyandsell.ui.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.kakao.auth.ApiResponseCallback;
import com.kakao.auth.ErrorCode;
import com.kakao.kakaotalk.callback.TalkResponseCallback;
import com.kakao.kakaotalk.response.KakaoTalkProfile;
import com.kakao.kakaotalk.v2.KakaoTalkService;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;

import com.kakao.util.helper.log.Logger;
import com.marketcountry.psbuyandsell.MainActivity;
import com.marketcountry.psbuyandsell.viewmodel.user.UserViewModel;

public class KakaoSignupActivity extends Activity{
    /**
     * Main으로 넘길지 가입 페이지를 그릴지 판단하기 위해 me를 호출한다.
     * @param savedInstanceState 기존 session 정보가 저장된 객체
     */

    String kakaoID;
    String kakaoNickname;
    String url;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestMe();
    }
    /**
     * 사용자의 상태를 알아 보기 위해 me API 호출을 한다.
     */
    protected void requestMe() { //유저의 정보를 받아오는 함수
        KakaoTalkService.getInstance().requestProfile(new TalkResponseCallback<KakaoTalkProfile>() {
            @Override
            public void onNotKakaoTalkUser() {

            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.d("카카오",errorResult.toString());
                redirectLoginActivity();
            }

            @Override
            public void onSuccess(KakaoTalkProfile talkProfile) {
                final String nickName = talkProfile.getNickName();
                final String profileImageURL = talkProfile.getProfileImageUrl();
                final String thumbnailURL = talkProfile.getThumbnailUrl();
                final String countryISO = talkProfile.getCountryISO();

                Log.d("카카오","nickname : "+nickName);
                Log.d("카카오","profileURL : "+profileImageURL);
                Log.d("카카오","thumbnailURL : "+thumbnailURL);
                Log.d("카카오","countryISO : "+countryISO);

                redirectMainActivity(profileImageURL,nickName);
            }
        },true);
    }
    private void redirectMainActivity(String url, String nickname) {
        Intent intent = new Intent();
        intent.putExtra("url", url);
        intent.putExtra("name", nickname);
        intent.putExtra("kakaoId",kakaoID);
        setResult(0,intent);
        finish();
    }
    protected void redirectLoginActivity() {
        final Intent intent = new Intent(this, UserLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }
}

