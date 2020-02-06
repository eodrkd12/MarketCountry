package com.marketcountry.psbuyandsell.viewmodel.itemdealoption;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.marketcountry.psbuyandsell.repository.itemdealoption.ItemDealOptionRepository;
import com.marketcountry.psbuyandsell.utils.AbsentLiveData;
import com.marketcountry.psbuyandsell.utils.Utils;
import com.marketcountry.psbuyandsell.viewmodel.common.PSViewModel;
import com.marketcountry.psbuyandsell.viewobject.ItemDealOption;
import com.marketcountry.psbuyandsell.viewobject.common.Resource;
import com.marketcountry.psbuyandsell.viewobject.holder.CategoryParameterHolder;

import java.util.List;

import javax.inject.Inject;

public class ItemDealOptionViewModel extends PSViewModel {


    //region Variables

    private final LiveData<Resource<List<ItemDealOption>>> itemTypeListData;
    private MutableLiveData<ItemDealOptionViewModel.TmpDataHolder> itemTypeListObj = new MutableLiveData<>();

    public CategoryParameterHolder categoryParameterHolder = new CategoryParameterHolder();

    //endregion

    //region Constructors

    @Inject
    ItemDealOptionViewModel(ItemDealOptionRepository repository) {

        Utils.psLog("ItemDealOptionViewModel");

        itemTypeListData = Transformations.switchMap(itemTypeListObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }

            Utils.psLog("ItemDealOptionViewModel : categories");
            return repository.getAllItemDealOptionList(obj.limit, obj.offset);
        });

    }

    //endregion

    public void setItemDealOptionListObj(String limit, String offset) {
        if (!isLoading) {
            TmpDataHolder tmpDataHolder = new TmpDataHolder();
            tmpDataHolder.offset = offset;
            tmpDataHolder.limit = limit;
            itemTypeListObj.setValue(tmpDataHolder);

            // start loading
            setLoadingState(true);
        }
    }

    public LiveData<Resource<List<ItemDealOption>>> getItemDealOptionListData() {
        return itemTypeListData;
    }


    class TmpDataHolder {
        public String limit = "";
        public String offset = "";
        public String cityId = "";
    }
}
