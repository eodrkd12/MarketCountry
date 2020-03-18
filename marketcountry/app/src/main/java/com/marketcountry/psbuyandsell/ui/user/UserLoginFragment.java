package com.marketcountry.psbuyandsell.ui.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.marketcountry.psbuyandsell.Config;
import com.marketcountry.psbuyandsell.MainActivity;
import com.marketcountry.psbuyandsell.R;
import com.marketcountry.psbuyandsell.binding.FragmentDataBindingComponent;
import com.marketcountry.psbuyandsell.databinding.FragmentUserLoginBinding;
import com.marketcountry.psbuyandsell.ui.common.PSFragment;
import com.marketcountry.psbuyandsell.utils.AutoClearedValue;
import com.marketcountry.psbuyandsell.utils.Constants;
import com.marketcountry.psbuyandsell.utils.PSDialogMsg;
import com.marketcountry.psbuyandsell.utils.Utils;
import com.marketcountry.psbuyandsell.viewmodel.user.UserViewModel;
import com.marketcountry.psbuyandsell.viewobject.User;

import org.json.JSONException;

import java.util.Arrays;

import static androidx.constraintlayout.motion.widget.MotionScene.TAG;

import com.kakao.auth.ISessionCallback;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

/**
 * UserLoginFragment
 */
public class UserLoginFragment extends PSFragment {


    //region Variables

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private UserViewModel userViewModel;

    private PSDialogMsg psDialogMsg;

    private boolean checkFlag;

    @VisibleForTesting
    private AutoClearedValue<FragmentUserLoginBinding> binding;

    private AutoClearedValue<ProgressDialog> prgDialog;

    private CallbackManager callbackManager;

    private String number, userName;
    private String token;

    //Firebase test
    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

    //google login

    private final int GOOGLE_SIGN = 123;

    private GoogleSignInClient mGoogleSignInClient;

    //endregion


    //region Override Methods

    //kakao
    private ISessionCallback callback;

    private final int KAKAO_SIGN=124;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FacebookSdk.addLoggingBehavior(LoggingBehavior.REQUESTS);
        FacebookSdk.sdkInitialize(getContext());

        callbackManager = CallbackManager.Factory.create();

        // Inflate the layout for this fragment
        FragmentUserLoginBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_login, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);

        callback=new ISessionCallback() {
            @Override
            public void onSessionOpened() {
                redirectSignupActivity();  // 세션 연결성공 시 redirectSignupActivity() 호출
            }

            @Override
            public void onSessionOpenFailed(KakaoException exception) {
                if(exception != null) {
                    Logger.e(exception);
                }
            }
        };

        Session.getCurrentSession().addCallback(callback);
        //Session.getCurrentSession().checkAndImplicitOpen();

        return binding.get().getRoot();
    }

    protected void redirectSignupActivity() {       //세션 연결 성공 시 SignupActivity로 넘김
        final Intent intent = new Intent(this.getActivity(), KakaoSignupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivityForResult(intent,KAKAO_SIGN);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        if (getActivity() != null) {
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(getActivity(), task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                userViewModel.setGoogleLoginUser(user.getUid(), user.getDisplayName(), user.getEmail(), String.valueOf(user.getPhotoUrl()), token);

                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(UserLoginFragment.this.getActivity(), "SignIn Failed", Toast.LENGTH_LONG).show();

                        }
                    });
        }
    }


    //firebase
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void initUIAndActions() {

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbarText(((MainActivity) getActivity()).binding.toolbar, getString(R.string.login__login));
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            ((MainActivity) this.getActivity()).binding.toolbar.setBackgroundColor(getResources().getColor(R.color.global__primary));
            ((MainActivity) getActivity()).updateToolbarIconColor(Color.WHITE);
            ((MainActivity) getActivity()).updateMenuIconWhite();
            ((MainActivity) getActivity()).refreshPSCount();
        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = firebaseAuth -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                int a;
//                Toast.makeText(UserLoginFragment.this.getContext(), "Successfully singned in with" + currentUser.getUid(), Toast.LENGTH_SHORT).show();
            }
        };
        //end

        // Configure Google Sign In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        if (getActivity() != null) {
            mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), googleSignInOptions);
        }
        //end

        binding.get().phoneLoginButton.setOnClickListener(v -> Utils.navigateAfterPhoneLogin(getActivity(),navigationController));

        //google login
        binding.get().googleLoginButton.setOnClickListener(v -> signIn());

        if (mAuth.getCurrentUser() != null) {
            FirebaseUser user = mAuth.getCurrentUser();

        }

        //for check privacy and policy
        binding.get().privacyPolicyCheckbox.setOnClickListener(v -> {
            if (binding.get().privacyPolicyCheckbox.isChecked()) {

                navigationController.navigateToPrivacyPolicyActivity(getActivity());
                checkFlag = true;
                binding.get().googleSignInView.setVisibility(View.GONE);
                binding.get().facebookSignInView.setVisibility(View.GONE);
                binding.get().phoneSignInView.setVisibility(View.GONE);
                binding.get().kakaoSinginView.setVisibility(View.GONE);
                binding.get().fbLoginButton.setEnabled(true);
                binding.get().googleLoginButton.setEnabled(true);
                binding.get().phoneLoginButton.setEnabled(true);
                binding.get().comKakaoLogin.setEnabled(true);
            } else {

                checkFlag = false;
                binding.get().googleSignInView.setVisibility(View.GONE);
                binding.get().facebookSignInView.setVisibility(View.GONE);
                binding.get().phoneSignInView.setVisibility(View.VISIBLE);
                binding.get().kakaoSinginView.setVisibility(View.VISIBLE);
                binding.get().fbLoginButton.setEnabled(false);
                binding.get().googleLoginButton.setEnabled(false);
                binding.get().phoneLoginButton.setEnabled(false);
                binding.get().comKakaoLogin.setEnabled(false);
            }
        });

        // For First Time Loading
        if (!checkFlag) {
            binding.get().googleSignInView.setVisibility(View.GONE);
            binding.get().facebookSignInView.setVisibility(View.GONE);
            binding.get().phoneSignInView.setVisibility(View.VISIBLE);
            binding.get().kakaoSinginView.setVisibility(View.VISIBLE);
            binding.get().fbLoginButton.setEnabled(false);
            binding.get().googleLoginButton.setEnabled(false);
            binding.get().phoneLoginButton.setEnabled(false);
            binding.get().comKakaoLogin.setEnabled(false);
        } else {
            binding.get().googleSignInView.setVisibility(View.GONE);
            binding.get().facebookSignInView.setVisibility(View.GONE);
            binding.get().phoneSignInView.setVisibility(View.GONE);
            binding.get().kakaoSinginView.setVisibility(View.GONE);
            binding.get().fbLoginButton.setEnabled(true);
            binding.get().googleLoginButton.setEnabled(true);
            binding.get().phoneLoginButton.setEnabled(true);
            binding.get().comKakaoLogin.setEnabled(true);
        }

        psDialogMsg = new PSDialogMsg(getActivity(), false);

        // Init Dialog
        prgDialog = new AutoClearedValue<>(this, new ProgressDialog(getActivity()));
        //prgDialog.get().setMessage(getString(R.string.message__please_wait));

        prgDialog.get().setMessage((Utils.getSpannableString(getContext(), getString(R.string.message__please_wait), Utils.Fonts.MM_FONT)));
        prgDialog.get().setCancelable(false);

        //fadeIn Animation
        fadeIn(binding.get().getRoot());

        binding.get().loginButton.setOnClickListener(view -> {

            Utils.hideKeyboard(getActivity());

            if (connectivity.isConnected()) {
                String userEmail = binding.get().emailEditText.getText().toString().trim();
                String userPassword = binding.get().passwordEditText.getText().toString().trim();

                Utils.psLog("Email " + userEmail);
                Utils.psLog("Password " + userPassword);

                if (userEmail.equals("")) {

                    psDialogMsg.showWarningDialog(getString(R.string.error_message__blank_email), getString(R.string.app__ok));
                    psDialogMsg.show();
                    return;
                }

                if (userPassword.equals("")) {

                    psDialogMsg.showWarningDialog(getString(R.string.error_message__blank_password), getString(R.string.app__ok));
                    psDialogMsg.show();
                    return;
                }

                if (!userViewModel.isLoading) {

                    updateLoginBtnStatus();

                    doSubmit(userEmail, userPassword);

                    //firebase
//                    mAuth.signInWithEmailAndPassword(binding.get().emailEditText.getText().toString(), binding.get().passwordEditText.getText().toString());

                }
            } else {

                psDialogMsg.showWarningDialog(getString(R.string.no_internet_error), getString(R.string.app__ok));
                psDialogMsg.show();
            }

        });

        binding.get().registerButton.setOnClickListener(view ->
                Utils.navigateAfterRegister(UserLoginFragment.this.getActivity(), navigationController));

        binding.get().forgotPasswordButton.setOnClickListener(view ->
                Utils.navigateAfterForgotPassword(UserLoginFragment.this.getActivity(), navigationController));

        if (Config.ENABLE_FACEBOOK_LOGIN) {
            binding.get().fbLoginButton.setVisibility(View.GONE);
        } else {
            binding.get().fbLoginButton.setVisibility(View.GONE);
        }
        if (Config.ENABLE_GOOGLE_LOGIN) {
            binding.get().googleLoginButton.setVisibility(View.GONE);
        } else {
            binding.get().googleLoginButton.setVisibility(View.GONE);
        }
        if (Config.ENABLE_PHONE_LOGIN) {
            binding.get().phoneLoginButton.setVisibility(View.VISIBLE);
        } else {
            binding.get().phoneLoginButton.setVisibility(View.GONE);
        }
        if (Config.ENABLE_KAKAO_LOGIN) {
            binding.get().comKakaoLogin.setVisibility(View.VISIBLE);
        } else {
            binding.get().comKakaoLogin.setVisibility(View.GONE);
        }


        if (Config.ENABLE_FACEBOOK_LOGIN || Config.ENABLE_GOOGLE_LOGIN || Config.ENABLE_PHONE_LOGIN || Config.ENABLE_KAKAO_LOGIN) {
            binding.get().privacyPolicyCheckbox.setVisibility(View.VISIBLE);
        } else {
            binding.get().privacyPolicyCheckbox.setVisibility(View.GONE);
        }

        View.OnClickListener onClickListener = v -> {

            psDialogMsg.showWarningDialog(getString(R.string.error_message__to_check_agreement), getString(R.string.app__ok));
            psDialogMsg.show();

        };

        binding.get().facebookSignInView.setOnClickListener(onClickListener);

        binding.get().phoneSignInView.setOnClickListener(onClickListener);

        binding.get().googleSignInView.setOnClickListener(onClickListener);
    }

    private void updateLoginBtnStatus() {
        if (userViewModel.isLoading) {
            binding.get().loginButton.setText(getResources().getString(R.string.message__loading));
        } else {
            binding.get().loginButton.setText(getResources().getString(R.string.login__login));
        }
    }

    public boolean mAuthSignIn() {
        if (mAuth != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            return user != null;
        }
        return false;
    }

    private void doSubmit(String email, String password) {

        String token = pref.getString(Constants.NOTI_TOKEN, Constants.USER_NO_DEVICE_TOKEN);

        //prgDialog.get().show();
        userViewModel.setUserLogin(new User(
                "",
                "",
                "",
                "",
                "",
                "",
                email,
                email,
                "",
                "",
                password,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                token,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                null));

        userViewModel.isLoading = true;

    }


    @Override
    protected void initViewModels() {
        userViewModel = ViewModelProviders.of(this, viewModelFactory).get(UserViewModel.class);
    }

    @Override
    protected void initAdapters() {

    }

    @Override
    protected void initData() {

        token = pref.getString(Constants.NOTI_TOKEN, Constants.USER_NO_DEVICE_TOKEN);

        userViewModel.getLoadingState().observe(this, loadingState -> {

            if (loadingState != null && loadingState) {
                prgDialog.get().show();
            } else {
                prgDialog.get().cancel();
            }

            updateLoginBtnStatus();

        });

        userViewModel.getGoogleLoginData().observe(this, listResource -> {

            if (listResource != null) {

                Utils.psLog("Got Data" + listResource.message + listResource.toString());

                switch (listResource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        prgDialog.get().show();

                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (listResource.data != null) {
                            try {

                                Utils.updateUserLoginData(pref,listResource.data.user);
                                Utils.navigateAfterUserLogin(getActivity(),navigationController);

                            } catch (NullPointerException ne) {
                                Utils.psErrorLog("Null Pointer Exception.", ne);
                            } catch (Exception e) {
                                Utils.psErrorLog("Error in getting notification flag data.", e);
                            }

                            userViewModel.isLoading = false;
                            prgDialog.get().cancel();

                        }

                        break;
                    case ERROR:
                        // Error State

                        userViewModel.isLoading = false;
                        prgDialog.get().cancel();

                        psDialogMsg.showErrorDialog(listResource.message, getString(R.string.app__ok));
                        psDialogMsg.show();

                        break;
                    default:
                        // Default

                        break;
                }

            } else {

                // Init Object or Empty Data
                Utils.psLog("Empty Data");

            }

        });

        userViewModel.getKakaoLoginData().observe(this, listResource -> {

            if (listResource != null) {

                Utils.psLog("Got Data" + listResource.message + listResource.toString());

                switch (listResource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        prgDialog.get().show();

                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (listResource.data != null) {
                            try {

                                Utils.updateUserLoginData(pref,listResource.data.user);
                                Utils.navigateAfterUserLogin(getActivity(),navigationController);

                            } catch (NullPointerException ne) {
                                Utils.psErrorLog("Null Pointer Exception.", ne);
                            } catch (Exception e) {
                                Utils.psErrorLog("Error in getting notification flag data.", e);
                            }

                            userViewModel.isLoading = false;
                            prgDialog.get().cancel();

                        }

                        break;
                    case ERROR:
                        // Error State

                        userViewModel.isLoading = false;
                        prgDialog.get().cancel();

                        psDialogMsg.showErrorDialog(listResource.message, getString(R.string.app__ok));
                        psDialogMsg.show();

                        break;
                    default:
                        // Default

                        break;
                }

            } else {

                // Init Object or Empty Data
                Utils.psLog("Empty Data");

            }

        });


        userViewModel.getUserLoginStatus().observe(this, listResource -> {

            if (listResource != null) {

                Utils.psLog("Got Data" + listResource.message + listResource.toString());

                switch (listResource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (listResource.data != null) {
                            try {

                                if (getActivity() != null) {

                                    Utils.updateUserLoginData(pref, listResource.data.user);
                                    Utils.navigateAfterUserLogin(getActivity(),navigationController);
                                }

                            } catch (NullPointerException ne) {
                                Utils.psErrorLog("Null Pointer Exception.", ne);
                            } catch (Exception e) {
                                Utils.psErrorLog("Error in getting notification flag data.", e);
                            }

                        }

                        userViewModel.setLoadingState(false);

                        break;
                    case ERROR:
                        // Error State

                        psDialogMsg.showErrorDialog(listResource.message, getString(R.string.app__ok));
                        psDialogMsg.show();

                        userViewModel.setLoadingState(false);

                        break;
                    default:
                        // Default

                        userViewModel.setLoadingState(false);

                        break;
                }

            } else {

                // Init Object or Empty Data
                Utils.psLog("Empty Data");

            }
        });

        String token = pref.getString(Constants.NOTI_TOKEN, Constants.USER_NO_DEVICE_TOKEN);

        binding.get().fbLoginButton.setFragment(this);
        binding.get().fbLoginButton.setPermissions(Arrays.asList("email"));
        binding.get().fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        (object, response) -> {

                            String name = "";
                            String email = "";
                            String id = "";
                            String imageURL = "";
                            try {
                                if (object != null) {

                                    name = object.getString("name");

                                }
                                //link.setText(object.getString("link"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                if (object != null) {

                                    email = object.getString("email");

                                }
                                //link.setText(object.getString("link"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                if (object != null) {

                                    id = object.getString("id");

                                }
                                //link.setText(object.getString("link"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            if (!id.equals("")) {
                                prgDialog.get().show();
                                userViewModel.registerFBUser(id, name, email, imageURL, token);
                            }

                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "email,name,id");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {

                Utils.psLog("OnCancel.");
            }

            @Override
            public void onError(FacebookException e) {
                e.printStackTrace();
                Utils.psLog("OnError." + e);
            }


        });


        userViewModel.getRegisterFBUserData().observe(this, listResource -> {

            if (listResource != null) {

                Utils.psLog("Got Data" + listResource.message + listResource.toString());

                switch (listResource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        prgDialog.get().show();

                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (listResource.data != null) {
                            try {
                                if (getActivity() != null) {

                                    Utils.updateUserLoginData(pref, listResource.data.user);
                                    Utils.navigateAfterUserLogin(getActivity(),navigationController);
                                }

                            } catch (NullPointerException ne) {
                                Utils.psErrorLog("Null Pointer Exception.", ne);
                            } catch (Exception e) {
                                Utils.psErrorLog("Error in getting notification flag data.", e);
                            }

                            userViewModel.isLoading = false;
                            prgDialog.get().cancel();

                        }

                        break;
                    case ERROR:
                        // Error State

                        userViewModel.isLoading = false;
                        prgDialog.get().cancel();

                        psDialogMsg.showErrorDialog(listResource.message, getString(R.string.app__ok));
                        psDialogMsg.show();

                        break;
                    default:
                        // Default
                        //userViewModel.isLoading = false;
                        //prgDialog.get().cancel();
                        break;
                }

            } else {

                // Init Object or Empty Data
                Utils.psLog("Empty Data");

            }

        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if(Session.getCurrentSession().handleActivityResult(requestCode,resultCode,data)){
            return;
        }

        if (requestCode == GOOGLE_SIGN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }else if(requestCode==KAKAO_SIGN) {
            String name=data.getStringExtra("name");
            String imgUrl=data.getStringExtra("url");
            String email=data.getStringExtra("email");

            userViewModel.setGoogleLoginUser(email,name,email,imgUrl,token);
        }
    }

}

//endregion

