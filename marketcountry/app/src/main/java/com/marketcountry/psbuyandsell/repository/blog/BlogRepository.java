package com.marketcountry.psbuyandsell.repository.blog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.marketcountry.psbuyandsell.AppExecutors;
import com.marketcountry.psbuyandsell.Config;
import com.marketcountry.psbuyandsell.api.ApiResponse;
import com.marketcountry.psbuyandsell.api.PSApiService;
import com.marketcountry.psbuyandsell.db.BlogDao;
import com.marketcountry.psbuyandsell.db.PSCoreDb;
import com.marketcountry.psbuyandsell.repository.common.NetworkBoundResource;
import com.marketcountry.psbuyandsell.repository.common.PSRepository;
import com.marketcountry.psbuyandsell.utils.Utils;
import com.marketcountry.psbuyandsell.viewobject.Blog;
import com.marketcountry.psbuyandsell.viewobject.common.Resource;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BlogRepository extends PSRepository {

    private final BlogDao blogDao;

    @Inject
    BlogRepository(PSApiService psApiService, AppExecutors appExecutors, PSCoreDb db, BlogDao blogDao) {
        super(psApiService, appExecutors, db);
        this.blogDao = blogDao;
    }

    public LiveData<Resource<List<Blog>>> getNewsFeedList(String limit, String offset) {
        return new NetworkBoundResource<List<Blog>, List<Blog>>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull List<Blog> itemList) {
                Utils.psLog("SaveCallResult of getNewsFeedList.");

//                db.beginTransaction();

//                try {
//                    blogDao.deleteAll();
//                    blogDao.insertAll(itemList);
//
//                    db.setTransactionSuccessful();
//
//                } catch (Exception e) {
//                    Utils.psErrorLog("Error in doing transaction of getNewsFeedList.", e);
//                } finally {
//                    db.endTransaction();
//                }

                try {
                    db.runInTransaction(() -> {
                        blogDao.deleteAll();
                        blogDao.insertAll(itemList);
                    });
                } catch (Exception ex) {
                    Utils.psErrorLog("Error at getNewsFeedList", ex);
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Blog> data) {

                // Recent news always load from server
                return connectivity.isConnected();

            }

            @NonNull
            @Override
            protected LiveData<List<Blog>> loadFromDb() {
                Utils.psLog("Load getNewsFeedList From Db");
                if (limit.equals(String.valueOf(Config.LIST_NEW_FEED_COUNT_PAGER))) {
                    return blogDao.getAllNewsFeedByLimit(limit);
                } else {
                    return blogDao.getAllNewsFeed();
                }
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<Blog>>> createCall() {
                Utils.psLog("Call API Service to getNewsFeedList.");

                return psApiService.getAllNewsFeed(Config.API_KEY, limit, offset);
            }

            @Override
            protected void onFetchFailed(String message) {
                Utils.psLog("Fetch Failed (getNewsFeedList) : " + message);
            }
        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> getNextPageNewsFeedList(String apiKey, String limit, String offset) {

        final MediatorLiveData<Resource<Boolean>> statusLiveData = new MediatorLiveData<>();

        LiveData<ApiResponse<List<Blog>>> apiResponse = psApiService.getAllNewsFeed(apiKey, limit, offset);

        statusLiveData.addSource(apiResponse, response -> {

            statusLiveData.removeSource(apiResponse);

            //noinspection Constant Conditions
            if (response.isSuccessful()) {

                appExecutors.diskIO().execute(() -> {

//                    try {
//
//                        db.beginTransaction();
//                        blogDao.deleteAll();
//                        db.blogDao().insertAll(response.body);
//
//                        db.setTransactionSuccessful();
//
//                    } catch (NullPointerException ne) {
//                        Utils.psErrorLog("Null Pointer Exception : ", ne);
//                    } catch (Exception e) {
//                        Utils.psErrorLog("Exception : ", e);
//                    } finally {
//                        db.endTransaction();
//                    }

                    try {
                        db.runInTransaction(() -> {
                            blogDao.deleteAll();
                            db.blogDao().insertAll(response.body);
                        });
                    } catch (Exception ex) {
                        Utils.psErrorLog("Error at ", ex);
                    }

                    statusLiveData.postValue(Resource.success(true));
                });

            } else {
                statusLiveData.postValue(Resource.error(response.errorMessage, null));
            }
        });

        return statusLiveData;

    }

    public LiveData<Resource<Blog>> getBlogById(String id) {
        return new NetworkBoundResource<Blog, Blog>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull Blog blog) {
                Utils.psLog("SaveCallResult of getBlogById.");

//                db.beginTransaction();
//
//                try {
//
//                    blogDao.insert(blog);
//
//                    db.setTransactionSuccessful();
//
//                } catch (Exception e) {
//                    Utils.psErrorLog("Error in doing transaction of getBlogById.", e);
//                } finally {
//                    db.endTransaction();
//                }

                try {
                    db.runInTransaction(() -> blogDao.insert(blog));
                } catch (Exception ex) {
                    Utils.psErrorLog("Error at ", ex);
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable Blog blog) {

                // Recent news always load from server
                return connectivity.isConnected();

            }

            @NonNull
            @Override
            protected LiveData<Blog> loadFromDb() {

                Utils.psLog("Load getBlogById From Db");
                return blogDao.getBlogById(id);

            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<Blog>> createCall() {

                Utils.psLog("Call API Service to getBlogById.");
                return psApiService.getNewsById(Config.API_KEY, id);

            }

            @Override
            protected void onFetchFailed(String message) {
                Utils.psLog("Fetch Failed (getBlogById) : " + message);
            }
        }.asLiveData();
    }

}