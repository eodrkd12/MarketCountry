package com.marketcountry.psbuyandsell.ui.chathistory.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.google.android.gms.dynamic.IFragmentWrapper;
import com.marketcountry.psbuyandsell.Config;
import com.marketcountry.psbuyandsell.R;
import com.marketcountry.psbuyandsell.databinding.ItemSellerChatHistoryListAdapterBinding;
import com.marketcountry.psbuyandsell.ui.chathistory.MessageFragment;
import com.marketcountry.psbuyandsell.ui.common.DataBoundListAdapter;
import com.marketcountry.psbuyandsell.ui.common.DataBoundViewHolder;
import com.marketcountry.psbuyandsell.utils.Constants;
import com.marketcountry.psbuyandsell.utils.Objects;
import com.marketcountry.psbuyandsell.utils.Utils;
import com.marketcountry.psbuyandsell.viewobject.ChatHistory;

public class SellerChatHistoryListAdapter extends DataBoundListAdapter<ChatHistory, ItemSellerChatHistoryListAdapterBinding> {

    private final androidx.databinding.DataBindingComponent dataBindingComponent;
    private final ChatHistoryClickCallback callback;
    private DataBoundListAdapter.DiffUtilDispatchedInterface diffUtilDispatchedInterface;

    public SellerChatHistoryListAdapter(androidx.databinding.DataBindingComponent dataBindingComponent,
                                        ChatHistoryClickCallback callback,
                                        DiffUtilDispatchedInterface diffUtilDispatchedInterface) {
        this.dataBindingComponent = dataBindingComponent;
        this.callback = callback;
        this.diffUtilDispatchedInterface = diffUtilDispatchedInterface;
    }

    @Override
    protected ItemSellerChatHistoryListAdapterBinding createBinding(ViewGroup parent) {

        ItemSellerChatHistoryListAdapterBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.item_seller_chat_history_list_adapter, parent, false,
                        dataBindingComponent);

        binding.getRoot().setOnClickListener(v -> {

            ChatHistory chatHistory = binding.getChatHistory();

            if (chatHistory != null && callback != null) {
                callback.onClick(chatHistory, chatHistory.id);
            }
        });

        return binding;

    }

    @Override
    public void bindView(DataBoundViewHolder<ItemSellerChatHistoryListAdapterBinding> holder, int position) {
        super.bindView(holder, position);

    }

    @Override
    protected void dispatched() {
        if (diffUtilDispatchedInterface != null) {
            diffUtilDispatchedInterface.onDispatched();
        }
    }

    @Override
    protected void bind(ItemSellerChatHistoryListAdapterBinding binding, ChatHistory chatHistory) {
        binding.setChatHistory(chatHistory);

        if (/*!chatHistory.item.itemCurrency.currencySymbol.equals("") && */!chatHistory.item.price.equals("")) {
            String currencySymbol = chatHistory.item.itemCurrency.currencySymbol;
            String price;
            try {
               price = Utils.format(Double.parseDouble(chatHistory.item.price));
            }
            catch (Exception e){
                price = chatHistory.item.price;
            }
            String currencyPrice;
            if (Config.SYMBOL_SHOW_FRONT) {
                currencyPrice = currencySymbol + " " + price;
            } else {
                currencyPrice = price + " " + currencySymbol;
            }
            binding.priceTextView.setText(price);
        }
            binding.itemConditionTextView.setText(binding.getRoot().getResources().getString(R.string.item_condition__type, chatHistory.item.conditionOfItem));

        binding.countTextView.setVisibility(View.GONE);
        binding.countTextView2.setVisibility(View.GONE);

        if(chatHistory.buyerUserId.equals(MessageFragment.userId)) {
                if (chatHistory.buyerUnreadCount.equals(Constants.ZERO)) {
                    binding.countTextView.setVisibility(View.GONE);
                } else {
                    binding.countTextView.setVisibility(View.VISIBLE);
                }
            }else if(chatHistory.sellerUserId.equals(MessageFragment.userId)){
                if (chatHistory.sellerUnreadCount.equals(Constants.ZERO)) {
                    binding.countTextView2.setVisibility(View.GONE);
                } else {
                    binding.countTextView2.setVisibility(View.VISIBLE);
                }
            }
        Log.d("확인 bCount",chatHistory.buyerUnreadCount);
        Log.d("확인 sCount",chatHistory.sellerUnreadCount);

            if (chatHistory.item.isSoldOut.equals(Constants.ONE)) {
                binding.soldTextView.setVisibility(View.VISIBLE);
            } else {
                binding.soldTextView.setVisibility(View.GONE);
            }
        }


    @Override
    protected boolean areItemsTheSame(ChatHistory oldItem, ChatHistory newItem) {
        return Objects.equals(oldItem.id, newItem.id) &&
                oldItem.addedDate.equals(newItem.addedDate) &&
                oldItem.buyerUnreadCount.equals(newItem.buyerUnreadCount) &&
                oldItem.sellerUnreadCount.equals(newItem.sellerUnreadCount);
    }

    @Override
    protected boolean areContentsTheSame(ChatHistory oldItem, ChatHistory newItem) {
        return Objects.equals(oldItem.id, newItem.id) &&
                oldItem.addedDate.equals(newItem.addedDate) &&
                oldItem.buyerUnreadCount.equals(newItem.buyerUnreadCount) &&
                oldItem.sellerUnreadCount.equals(newItem.sellerUnreadCount);
    }

    public interface ChatHistoryClickCallback {
        void onClick(ChatHistory chatHistory, String id);
    }


}
