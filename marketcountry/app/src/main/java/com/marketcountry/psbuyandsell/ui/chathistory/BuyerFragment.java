package com.marketcountry.psbuyandsell.ui.chathistory;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.marketcountry.psbuyandsell.Config;
import com.marketcountry.psbuyandsell.R;
import com.marketcountry.psbuyandsell.binding.FragmentDataBindingComponent;
import com.marketcountry.psbuyandsell.databinding.FragmentBuyerBinding;
import com.marketcountry.psbuyandsell.ui.chathistory.adapter.BuyerChatHistoryListAdapter;
import com.marketcountry.psbuyandsell.ui.chathistory.adapter.SellerChatHistoryListAdapter;
import com.marketcountry.psbuyandsell.ui.common.DataBoundListAdapter;
import com.marketcountry.psbuyandsell.ui.common.PSFragment;
import com.marketcountry.psbuyandsell.utils.AutoClearedValue;
import com.marketcountry.psbuyandsell.utils.Constants;
import com.marketcountry.psbuyandsell.utils.Utils;
import com.marketcountry.psbuyandsell.viewmodel.chathistory.ChatHistoryViewModel;
import com.marketcountry.psbuyandsell.viewobject.ChatHistory;
import com.marketcountry.psbuyandsell.viewobject.common.Resource;
import com.marketcountry.psbuyandsell.viewobject.common.Status;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class BuyerFragment extends PSFragment implements DataBoundListAdapter.DiffUtilDispatchedInterface {

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private ChatHistoryViewModel buyerChatHistoryViewModel;
    private ChatHistoryViewModel sellerChatHistoryViewModel;
    public String catId;

    @VisibleForTesting
    private AutoClearedValue<FragmentBuyerBinding> binding;
    private AutoClearedValue<BuyerChatHistoryListAdapter> buyerAdapter;
    private AutoClearedValue<SellerChatHistoryListAdapter> sellerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        FragmentBuyerBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_buyer, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);

        if (getActivity() != null) {
            Intent intent = getActivity().getIntent();
            this.catId = intent.getStringExtra(Constants.CATEGORY_ID);
        }
        return binding.get().getRoot();
    }

    @Override
    protected void initUIAndActions() {
        binding.get().buyerList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager)
                        recyclerView.getLayoutManager();

                if (layoutManager != null) {

                    int lastPosition = layoutManager
                            .findLastVisibleItemPosition();
                    if (lastPosition == buyerAdapter.get().getItemCount() - 1) {

                        if (!binding.get().getLoadingMore() && !buyerChatHistoryViewModel.forceEndLoading) {

                            if (connectivity.isConnected()) {

                                buyerChatHistoryViewModel.loadingDirection = Utils.LoadingDirection.bottom;

                                int limit = Config.LIST_CATEGORY_COUNT;
                                buyerChatHistoryViewModel.offset = buyerChatHistoryViewModel.offset + limit;

                                buyerChatHistoryViewModel.setNextPageChatHistoryFromSellerObj(loginUserId, buyerChatHistoryViewModel.holder.getBuyerHistoryList(), String.valueOf(Config.CHAT_HISTORY_COUNT), String.valueOf(buyerChatHistoryViewModel.offset));
                            }
                        }
                    }
                }
            }
        });
        binding.get().sellerList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager)
                        recyclerView.getLayoutManager();

                if (layoutManager != null) {

                    int lastPosition = layoutManager
                            .findLastVisibleItemPosition();
                    if (lastPosition == sellerAdapter.get().getItemCount() - 1) {

                        if (!binding.get().getLoadingMore() && !sellerChatHistoryViewModel.forceEndLoading) {

                            if (connectivity.isConnected()) {

                                sellerChatHistoryViewModel.loadingDirection = Utils.LoadingDirection.bottom;

                                int limit = Config.LIST_CATEGORY_COUNT;
                                sellerChatHistoryViewModel.offset = sellerChatHistoryViewModel.offset + limit;

                                sellerChatHistoryViewModel.setNextPageChatHistoryFromSellerObj(loginUserId, sellerChatHistoryViewModel.holder.getSellerHistoryList(), String.valueOf(Config.CHAT_HISTORY_COUNT), String.valueOf(sellerChatHistoryViewModel.offset));
                            }
                        }
                    }
                }
            }
        });

        binding.get().swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.view__primary_line));
        binding.get().swipeRefresh.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.global__primary));
        binding.get().swipeRefresh.setOnRefreshListener(() -> {

            buyerChatHistoryViewModel.loadingDirection = Utils.LoadingDirection.top;
            sellerChatHistoryViewModel.loadingDirection=Utils.LoadingDirection.top;

            // reset productViewModel.offset
            buyerChatHistoryViewModel.offset = 0;
            sellerChatHistoryViewModel.offset = 0;
            // reset productViewModel.forceEndLoading
            buyerChatHistoryViewModel.forceEndLoading = false;
            sellerChatHistoryViewModel.forceEndLoading=false;
            // update live data
            if (!loginUserId.isEmpty()) {
                buyerChatHistoryViewModel.setChatHistoryListObj(loginUserId, buyerChatHistoryViewModel.holder.getBuyerHistoryList(), String.valueOf(Config.CHAT_HISTORY_COUNT), String.valueOf(buyerChatHistoryViewModel.offset));
                sellerChatHistoryViewModel.setChatHistoryListObj(loginUserId, sellerChatHistoryViewModel.holder.getSellerHistoryList(), String.valueOf(Config.CHAT_HISTORY_COUNT), String.valueOf(sellerChatHistoryViewModel.offset));
            }
        });
    }

    @Override
    protected void initViewModels() {

        buyerChatHistoryViewModel = ViewModelProviders.of(this, viewModelFactory).get(ChatHistoryViewModel.class);
        sellerChatHistoryViewModel = ViewModelProviders.of(this,viewModelFactory).get(ChatHistoryViewModel.class);
    }

    @Override
    protected void initAdapters() {

        BuyerChatHistoryListAdapter buyerChatHistoryListAdapter = new BuyerChatHistoryListAdapter(dataBindingComponent,
                (chatHistoryFromBuyer, id) -> navigationController.navigateToChatActivity(BuyerFragment.this.getActivity(),
                        chatHistoryFromBuyer.itemId,
                        chatHistoryFromBuyer.buyerUserId,
                        chatHistoryFromBuyer.buyerUser.userName,
                        chatHistoryFromBuyer.item.defaultPhoto.imgPath,
                        chatHistoryFromBuyer.item.title,
                        chatHistoryFromBuyer.item.itemCurrency.currencySymbol,
                        chatHistoryFromBuyer.item.price,
                        chatHistoryFromBuyer.item.conditionOfItem,
                        Constants.CHAT_FROM_BUYER,
                        chatHistoryFromBuyer.buyerUser.userProfilePhoto,
                        Constants.REQUEST_CODE__BUYER_CHAT_FRAGMENT
                ), this);
        this.buyerAdapter = new AutoClearedValue<>(this, buyerChatHistoryListAdapter);
        binding.get().buyerList.setAdapter(buyerChatHistoryListAdapter);

        SellerChatHistoryListAdapter sellerChatHistoryListAdapter = new SellerChatHistoryListAdapter(dataBindingComponent,
                (chatHistoryFromSeller, id) -> navigationController.navigateToChatActivity(getActivity(),
                        chatHistoryFromSeller.itemId,
                        chatHistoryFromSeller.sellerUserId,
                        chatHistoryFromSeller.sellerUser.userName,
                        chatHistoryFromSeller.item.defaultPhoto.imgPath,
                        chatHistoryFromSeller.item.title,
                        chatHistoryFromSeller.item.itemCurrency.currencySymbol,
                        chatHistoryFromSeller.item.price,
                        chatHistoryFromSeller.item.conditionOfItem,
                        Constants.CHAT_FROM_SELLER,
                        chatHistoryFromSeller.sellerUser.userProfilePhoto,
                        Constants.REQUEST_CODE__SELLER_CHAT_FRAGMENT
                ), this);
        this.sellerAdapter = new AutoClearedValue<>(this, sellerChatHistoryListAdapter);
        binding.get().sellerList.setAdapter(sellerChatHistoryListAdapter);

    }

    @Override
    protected void initData() {
        loadCategory();
    }

    private void loadCategory() {

        // Load Category List
        if (!loginUserId.isEmpty()) {
            buyerChatHistoryViewModel.setChatHistoryListObj(loginUserId, buyerChatHistoryViewModel.holder.getBuyerHistoryList(), String.valueOf(Config.CHAT_HISTORY_COUNT), String.valueOf(buyerChatHistoryViewModel.offset));
            sellerChatHistoryViewModel.setChatHistoryListObj(loginUserId, sellerChatHistoryViewModel.holder.getBuyerHistoryList(), String.valueOf(Config.CHAT_HISTORY_COUNT), String.valueOf(sellerChatHistoryViewModel.offset));
        }

        LiveData<Resource<List<ChatHistory>>> buyerNews = buyerChatHistoryViewModel.getChatHistoryListData();
        LiveData<Resource<List<ChatHistory>>> sellerNews = sellerChatHistoryViewModel.getChatHistoryListData();

        if (buyerNews != null) {
            buyerNews.observe(this, listResource -> {
                if (listResource != null) {
                    Utils.psLog("Buyer Got Data" + listResource.message + listResource.toString());
                    switch (listResource.status) {
                        case LOADING:
                            // Data are from Local DB
                            if (listResource.data != null) {
                                //fadeIn Animation
                                fadeIn(binding.get().getRoot());
                                // Update the data
                                replaceBuyerData(listResource.data);
                            }
                            break;
                        case SUCCESS:
                            // Data are from Server
                            if (listResource.data != null) {
                                // Update the data
                                replaceBuyerData(listResource.data);
                            }
                            buyerChatHistoryViewModel.setLoadingState(false);
                            break;
                        case ERROR:
                            buyerChatHistoryViewModel.setLoadingState(false);
                            break;
                        default:
                            break;
                    }
                } else {
                    // Init Object or Empty Data
                    Utils.psLog("Empty Data");
                    if (buyerChatHistoryViewModel.offset > 1) {
                        // No more data for this list
                        // So, Block all future loading
                        buyerChatHistoryViewModel.forceEndLoading = true;
                    }
                }
            });
        }
        if (sellerNews != null) {
            sellerNews.observe(this, listResource -> {
                if (listResource != null) {
                    Utils.psLog("Seller Got Data" + listResource.message + listResource.toString());
                    switch (listResource.status) {
                        case LOADING:
                            // Data are from Local DB
                            if (listResource.data != null) {
                                //fadeIn Animation
                                fadeIn(binding.get().getRoot());
                                // Update the data
                                replaceSellerData(listResource.data);
                            }
                            break;
                        case SUCCESS:
                            // Data are from Server
                            if (listResource.data != null) {
                                // Update the data
                                replaceSellerData(listResource.data);
                            }
                            sellerChatHistoryViewModel.setLoadingState(false);
                            break;
                        case ERROR:
                            sellerChatHistoryViewModel.setLoadingState(false);
                            break;
                        default:
                            break;
                    }
                } else {
                    // Init Object or Empty Data
                    Utils.psLog("Empty Data");
                    if (sellerChatHistoryViewModel.offset > 1) {
                        // No more data for this list
                        // So, Block all future loading
                        sellerChatHistoryViewModel.forceEndLoading = true;
                    }
                }
            });
        }

        buyerChatHistoryViewModel.getNextPageChatHistoryListData().observe(this, state -> {
            if (state != null) {
                if (state.status == Status.ERROR) {
                    Utils.psLog("Next Page State : " + state.data);

                    buyerChatHistoryViewModel.setLoadingState(false);
                    buyerChatHistoryViewModel.forceEndLoading = true;
                }
            }
        });
        sellerChatHistoryViewModel.getNextPageChatHistoryListData().observe(this, state -> {
            if (state != null) {
                if (state.status == Status.ERROR) {
                    Utils.psLog("Next Page State : " + state.data);

                    sellerChatHistoryViewModel.setLoadingState(false);
                    sellerChatHistoryViewModel.forceEndLoading = true;
                }
            }
        });

        buyerChatHistoryViewModel.getLoadingState().observe(this, loadingState -> {

            binding.get().setLoadingMore(buyerChatHistoryViewModel.isLoading);

            if (loadingState != null && !loadingState) {
                binding.get().swipeRefresh.setRefreshing(false);
            }
        });
        sellerChatHistoryViewModel.getLoadingState().observe(this, loadingState -> {

            binding.get().setLoadingMore(sellerChatHistoryViewModel.isLoading);

            if (loadingState != null && !loadingState) {
                binding.get().swipeRefresh.setRefreshing(false);
            }
        });

    }

    private void replaceBuyerData(List<ChatHistory> chatHistoryList) {
        buyerAdapter.get().replace(chatHistoryList);
        binding.get().executePendingBindings();
    }
    private void replaceSellerData(List<ChatHistory> chatHistoryList) {
        sellerAdapter.get().replace(chatHistoryList);
        binding.get().executePendingBindings();
    }

    @Override
    public void onDispatched() {
        if (buyerChatHistoryViewModel.loadingDirection == Utils.LoadingDirection.bottom) {
            if (binding.get().buyerList != null) {
                LinearLayoutManager layoutManager = (LinearLayoutManager)
                        binding.get().buyerList.getLayoutManager();
                if (layoutManager != null) {
                    layoutManager.scrollToPosition(0);
                }
            }
        }
        if (sellerChatHistoryViewModel.loadingDirection == Utils.LoadingDirection.bottom) {
            if (binding.get().sellerList != null) {
                LinearLayoutManager layoutManager = (LinearLayoutManager)
                        binding.get().sellerList.getLayoutManager();
                if (layoutManager != null) {
                    layoutManager.scrollToPosition(0);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE__BUYER_CHAT_FRAGMENT) {
            buyerChatHistoryViewModel.loadingDirection = Utils.LoadingDirection.top;
            // reset productViewModel.offset
            buyerChatHistoryViewModel.offset = 0;
            // reset productViewModel.forceEndLoading
            buyerChatHistoryViewModel.forceEndLoading = false;
            // update live data
            if (!loginUserId.isEmpty()) {
                buyerChatHistoryViewModel.setChatHistoryListObj(loginUserId, buyerChatHistoryViewModel.holder.getBuyerHistoryList(), String.valueOf(Config.CHAT_HISTORY_COUNT), String.valueOf(buyerChatHistoryViewModel.offset));
            }

            sellerChatHistoryViewModel.loadingDirection = Utils.LoadingDirection.top;
            // reset productViewModel.offset
            sellerChatHistoryViewModel.offset = 0;
            // reset productViewModel.forceEndLoading
            sellerChatHistoryViewModel.forceEndLoading = false;
            // update live data
            if (!loginUserId.isEmpty()) {
                sellerChatHistoryViewModel.setChatHistoryListObj(loginUserId, sellerChatHistoryViewModel.holder.getSellerHistoryList(), String.valueOf(Config.CHAT_HISTORY_COUNT), String.valueOf(sellerChatHistoryViewModel.offset));
            }
        }
    }
}

