package com.marketcountry.psbuyandsell.repository.itemdealoption;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.marketcountry.psbuyandsell.AppExecutors;
import com.marketcountry.psbuyandsell.Config;
import com.marketcountry.psbuyandsell.api.ApiResponse;
import com.marketcountry.psbuyandsell.api.PSApiService;
import com.marketcountry.psbuyandsell.db.ItemDealOptionDao;
import com.marketcountry.psbuyandsell.db.PSCoreDb;
import com.marketcountry.psbuyandsell.repository.common.NetworkBoundResource;
import com.marketcountry.psbuyandsell.repository.common.PSRepository;
import com.marketcountry.psbuyandsell.utils.Utils;
import com.marketcountry.psbuyandsell.viewobject.ItemDealOption;
import com.marketcountry.psbuyandsell.viewobject.common.Resource;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ItemDealOptionRepository extends PSRepository {
    private ItemDealOptionDao itemDealOptionDao;

    @Inject
    ItemDealOptionRepository(PSApiService psApiService, AppExecutors appExecutors, PSCoreDb db, ItemDealOptionDao itemDealOptionDao) {

        super(psApiService, appExecutors, db);
        this.itemDealOptionDao = itemDealOptionDao;
    }

    public LiveData<Resource<List<ItemDealOption>>> getAllItemDealOptionList(String limit, String offset) {

        return new NetworkBoundResource<List<ItemDealOption>, List<ItemDealOption>>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull List<ItemDealOption> itemTypeList) {
                Utils.psLog("SaveCallResult of getAllCategoriesWithUserId");


//                try {
//                    db.beginTransaction();
//
//                    itemDealOptionDao.deleteAllItemDealOption();
//
//                    itemDealOptionDao.insertAll(itemTypeList);
//
//                    db.setTransactionSuccessful();
//
//                } catch (Exception e) {
//                    Utils.psErrorLog("Error in doing transaction of getAllCategoriesWithUserId", e);
//                } finally {
//                    db.endTransaction();
//                }

                try {
                    db.runInTransaction(() -> {
                        itemDealOptionDao.deleteAllItemDealOption();

                        itemDealOptionDao.insertAll(itemTypeList);
                    });
                } catch (Exception ex) {
                    Utils.psErrorLog("Error at ", ex);
                }
            }


            @Override
            protected boolean shouldFetch(@Nullable List<ItemDealOption> data) {

                return connectivity.isConnected();
            }

            @NonNull
            @Override
            protected LiveData<List<ItemDealOption>> loadFromDb() {

                Utils.psLog("Load From Db (All Categories)");

                return itemDealOptionDao.getAllItemDealOption();

            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<ItemDealOption>>> createCall() {
                Utils.psLog("Call Get All Categories webservice.");

                return psApiService.getItemDealOptionList(Config.API_KEY, limit, offset);
            }

            @Override
            protected void onFetchFailed(String message) {
                Utils.psLog("Fetch Failed of About Us");
            }

        }.asLiveData();
    }


}