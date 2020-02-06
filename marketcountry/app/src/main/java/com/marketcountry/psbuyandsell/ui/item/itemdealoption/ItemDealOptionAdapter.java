package com.marketcountry.psbuyandsell.ui.item.itemdealoption;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.marketcountry.psbuyandsell.R;
import com.marketcountry.psbuyandsell.databinding.ItemItemDealOptionBinding;
import com.marketcountry.psbuyandsell.ui.common.DataBoundListAdapter;
import com.marketcountry.psbuyandsell.ui.common.DataBoundViewHolder;
import com.marketcountry.psbuyandsell.utils.Objects;
import com.marketcountry.psbuyandsell.viewobject.ItemDealOption;

public class ItemDealOptionAdapter extends DataBoundListAdapter<ItemDealOption, ItemItemDealOptionBinding> {

    private final androidx.databinding.DataBindingComponent dataBindingComponent;
    private final ItemDealOptionAdapter.NewsClickCallback callback;
    private DataBoundListAdapter.DiffUtilDispatchedInterface diffUtilDispatchedInterface = null;
    public String conditionTypeId = "";

    public ItemDealOptionAdapter(androidx.databinding.DataBindingComponent dataBindingComponent,
                               ItemDealOptionAdapter.NewsClickCallback callback,
                               DiffUtilDispatchedInterface diffUtilDispatchedInterface) {
        this.dataBindingComponent = dataBindingComponent;
        this.callback = callback;
        this.diffUtilDispatchedInterface = diffUtilDispatchedInterface;
    }

    public ItemDealOptionAdapter(androidx.databinding.DataBindingComponent dataBindingComponent,
                               ItemDealOptionAdapter.NewsClickCallback callback, String conditionTypeId) {
        this.dataBindingComponent = dataBindingComponent;
        this.callback = callback;
        this.conditionTypeId = conditionTypeId;
    }

    @Override
    protected ItemItemDealOptionBinding createBinding(ViewGroup parent) {
        ItemItemDealOptionBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.item_item_deal_option, parent, false,
                        dataBindingComponent);

        binding.getRoot().setOnClickListener(v -> {

            ItemDealOption itemCurrency = binding.getItemDealOption();

            if (itemCurrency != null && callback != null) {

                binding.groupview.setBackgroundColor(parent.getResources().getColor(R.color.md_green_50));

                callback.onClick(itemCurrency, itemCurrency.id);
            }
        });
        return binding;
    }

    @Override
    public void bindView(DataBoundViewHolder<ItemItemDealOptionBinding> holder, int position) {
        super.bindView(holder, position);

    }

    @Override
    protected void dispatched() {
        if (diffUtilDispatchedInterface != null) {
            diffUtilDispatchedInterface.onDispatched();
        }
    }

    @Override
    protected void bind(ItemItemDealOptionBinding binding, ItemDealOption item) {
        binding.setItemDealOption(item);

        if (conditionTypeId != null) {
            if (item.id.equals(conditionTypeId)) {
                binding.groupview.setBackgroundColor(binding.groupview.getResources().getColor((R.color.md_green_50)));
            }
        }

    }

    @Override
    protected boolean areItemsTheSame(ItemDealOption oldItem, ItemDealOption newItem) {
        return Objects.equals(oldItem.id, newItem.id);
    }

    @Override
    protected boolean areContentsTheSame(ItemDealOption oldItem, ItemDealOption newItem) {
        return Objects.equals(oldItem.id, newItem.id);
    }

    public interface NewsClickCallback {
        void onClick(ItemDealOption itemType, String id);
    }

}
