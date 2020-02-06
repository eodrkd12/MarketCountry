package com.marketcountry.psbuyandsell.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.marketcountry.psbuyandsell.db.common.Converters;
import com.marketcountry.psbuyandsell.viewobject.AboutUs;
import com.marketcountry.psbuyandsell.viewobject.Blog;
import com.marketcountry.psbuyandsell.viewobject.ChatHistory;
import com.marketcountry.psbuyandsell.viewobject.ChatHistoryMap;
import com.marketcountry.psbuyandsell.viewobject.City;
import com.marketcountry.psbuyandsell.viewobject.CityMap;
import com.marketcountry.psbuyandsell.viewobject.DeletedObject;
import com.marketcountry.psbuyandsell.viewobject.Image;
import com.marketcountry.psbuyandsell.viewobject.Item;
import com.marketcountry.psbuyandsell.viewobject.ItemCategory;
import com.marketcountry.psbuyandsell.viewobject.ItemCollection;
import com.marketcountry.psbuyandsell.viewobject.ItemCollectionHeader;
import com.marketcountry.psbuyandsell.viewobject.ItemCondition;
import com.marketcountry.psbuyandsell.viewobject.ItemCurrency;
import com.marketcountry.psbuyandsell.viewobject.ItemDealOption;
import com.marketcountry.psbuyandsell.viewobject.ItemFavourite;
import com.marketcountry.psbuyandsell.viewobject.ItemFromFollower;
import com.marketcountry.psbuyandsell.viewobject.ItemHistory;
import com.marketcountry.psbuyandsell.viewobject.ItemLocation;
import com.marketcountry.psbuyandsell.viewobject.ItemMap;
import com.marketcountry.psbuyandsell.viewobject.ItemPriceType;
import com.marketcountry.psbuyandsell.viewobject.ItemSpecs;
import com.marketcountry.psbuyandsell.viewobject.ItemSubCategory;
import com.marketcountry.psbuyandsell.viewobject.ItemType;
import com.marketcountry.psbuyandsell.viewobject.Noti;
import com.marketcountry.psbuyandsell.viewobject.PSAppInfo;
import com.marketcountry.psbuyandsell.viewobject.PSAppSetting;
import com.marketcountry.psbuyandsell.viewobject.PSAppVersion;
import com.marketcountry.psbuyandsell.viewobject.PSCount;
import com.marketcountry.psbuyandsell.viewobject.Rating;
import com.marketcountry.psbuyandsell.viewobject.User;
import com.marketcountry.psbuyandsell.viewobject.UserLogin;
import com.marketcountry.psbuyandsell.viewobject.UserMap;
import com.marketcountry.psbuyandsell.viewobject.messageHolder.Message;


/**
 * Created by Panacea-Soft on 11/20/17.
 * Contact Email : teamps.is.cool@gmail.com
 */

@Database(entities = {
        Image.class,
        User.class,
        UserLogin.class,
        AboutUs.class,
        ItemFavourite.class,
        Noti.class,
        ItemHistory.class,
        Blog.class,
        Rating.class,
        PSAppInfo.class,
        PSAppVersion.class,
        DeletedObject.class,
        City.class,
        CityMap.class,
        Item.class,
        ItemMap.class,
        ItemCategory.class,
        ItemCollectionHeader.class,
        ItemCollection.class,
        ItemSubCategory.class,
        ItemSpecs.class,
        ItemCurrency.class,
        ItemPriceType.class,
        ItemType.class,
        ItemLocation.class,
        ItemDealOption.class,
        ItemCondition.class,
        ItemFromFollower.class,
        Message.class,
        ChatHistory.class,
        ChatHistoryMap.class,
        PSAppSetting.class,
        UserMap.class,
        PSCount.class
}, version = 6, exportSchema = false)
// app version 2.3 = db version 6
// app version 2.2 = db version 6
// app version 2.1 = db version 6
// app version 2.0 = db version 6
// app version 1.9 = db version 6
// app version 1.8 = db version 5
// app version 1.7 = db version 4
// app version 1.6 = db version 4
// app version 1.5 = db version 4
// app version 1.4 = db version 3
// app version 1.3 = db version 3
// app version 1.2 = db version 2
// app version 1.0 = db version 1


@TypeConverters({Converters.class})

public abstract class PSCoreDb extends RoomDatabase {

    abstract public UserDao userDao();

    abstract public UserMapDao userMapDao();

    abstract public HistoryDao historyDao();

    abstract public SpecsDao specsDao();

    abstract public AboutUsDao aboutUsDao();

    abstract public ImageDao imageDao();

    abstract public ItemDealOptionDao itemDealOptionDao();

    abstract public ItemConditionDao itemConditionDao();

    abstract public ItemLocationDao itemLocationDao();

    abstract public ItemCurrencyDao itemCurrencyDao();

    abstract public ItemPriceTypeDao itemPriceTypeDao();

    abstract public ItemTypeDao itemTypeDao();

    abstract public RatingDao ratingDao();

    abstract public NotificationDao notificationDao();

    abstract public BlogDao blogDao();

    abstract public PSAppInfoDao psAppInfoDao();

    abstract public PSAppVersionDao psAppVersionDao();

    abstract public DeletedObjectDao deletedObjectDao();

    abstract public CityDao cityDao();

    abstract public CityMapDao cityMapDao();

    abstract public ItemDao itemDao();

    abstract public ItemMapDao itemMapDao();

    abstract public ItemCategoryDao itemCategoryDao();

    abstract public ItemCollectionHeaderDao itemCollectionHeaderDao();

    abstract public ItemSubCategoryDao itemSubCategoryDao();

    abstract public ChatHistoryDao chatHistoryDao();

    abstract public MessageDao messageDao();

    abstract public PSCountDao psCountDao();


//    /**
//     * Migrate from:
//     * version 1 - using Room
//     * to
//     * version 2 - using Room where the {@link } has an extra field: addedDateStr
//     */
//    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("ALTER TABLE news "
//                    + " ADD COLUMN addedDateStr INTEGER NOT NULL DEFAULT 0");
//        }
//    };

    /* More migration write here */
}