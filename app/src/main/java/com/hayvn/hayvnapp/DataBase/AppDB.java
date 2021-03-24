package com.hayvn.hayvnapp.DataBase;

import androidx.annotation.VisibleForTesting;
import androidx.room.TypeConverters;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;

import com.hayvn.hayvnapp.Interface.AttachFileDAO;
import com.hayvn.hayvnapp.Interface.CaseNamesDAO;
import com.hayvn.hayvnapp.Interface.UserDAO;
import com.hayvn.hayvnapp.Interface.StoryDAO;
import com.hayvn.hayvnapp.Model.Attachedfile;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.User;
import com.hayvn.hayvnapp.Model.Story;
import com.hayvn.hayvnapp.Utilities.HayvnTypeConverters;


@Database(entities = {Case.class, Story.class, Attachedfile.class, User.class}, version = 3)
@TypeConverters(HayvnTypeConverters.class)
public abstract class AppDB extends RoomDatabase {

    private static AppDB instance;
    private final String TAG = "APPDB";

    public abstract CaseNamesDAO caseNamesDAO();

    public abstract StoryDAO storyDAO();

    public abstract AttachFileDAO attachFileDAO();

    public abstract UserDAO userDAO();

    @VisibleForTesting
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Room uses an own database hash to uniquely identify the database
            // Since version 1 does not use Room, it doesn't have the database hash associated.
            // By implementing a Migration class, we're telling Room that it should use the data
            // from version 1 to version 2.
            // If no migration is provided, then the tables will be dropped and recreated.
            // Since we didn't alter the table, there's nothing else to do here.
            database.execSQL("ALTER TABLE `user`  ADD COLUMN doctorId TEXT");
            database.execSQL("ALTER TABLE `case`  ADD COLUMN updatedFirebaseAt INTEGER");
            database.execSQL("ALTER TABLE `story`  ADD COLUMN updatedFirebaseAt INTEGER");
            database.execSQL("ALTER TABLE `attachedfile`  ADD COLUMN updatedFirebaseAt INTEGER");
            database.execSQL("ALTER TABLE `case`  ADD COLUMN patientId TEXT");
            database.execSQL("ALTER TABLE `case`  ADD COLUMN dateofbirth TEXT");
        }
    };

    @VisibleForTesting
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Room uses an own database hash to uniquely identify the database
            // Since version 1 does not use Room, it doesn't have the database hash associated.
            // By implementing a Migration class, we're telling Room that it should use the data
            // from version 1 to version 2.
            // If no migration is provided, then the tables will be dropped and recreated.
            // Since we didn't alter the table, there's nothing else to do here.
            database.execSQL("ALTER TABLE `user`  ADD COLUMN stringOne TEXT");
            database.execSQL("ALTER TABLE `user`  ADD COLUMN stringTwo TEXT");
            database.execSQL("ALTER TABLE `user`  ADD COLUMN stringThree TEXT");
            database.execSQL("ALTER TABLE `user`  ADD COLUMN string4 TEXT");
            database.execSQL("ALTER TABLE `user`  ADD COLUMN string5 TEXT");
            database.execSQL("ALTER TABLE `user`  ADD COLUMN string6 TEXT");
            database.execSQL("ALTER TABLE `user`  ADD COLUMN string7 TEXT");
            database.execSQL("ALTER TABLE `user`  ADD COLUMN string8 TEXT");
            database.execSQL("ALTER TABLE `user`  ADD COLUMN string9 TEXT");

            database.execSQL("ALTER TABLE `case`  ADD COLUMN string4 TEXT");
            database.execSQL("ALTER TABLE `case`  ADD COLUMN string5 TEXT");
            database.execSQL("ALTER TABLE `case`  ADD COLUMN string6 TEXT");
            database.execSQL("ALTER TABLE `case`  ADD COLUMN string7 TEXT");
            database.execSQL("ALTER TABLE `case`  ADD COLUMN string8 TEXT");
            database.execSQL("ALTER TABLE `case`  ADD COLUMN string9 TEXT");

            database.execSQL("ALTER TABLE `story`  ADD COLUMN string4 TEXT");
            database.execSQL("ALTER TABLE `story`  ADD COLUMN string5 TEXT");
            database.execSQL("ALTER TABLE `story`  ADD COLUMN string6 TEXT");
            database.execSQL("ALTER TABLE `story`  ADD COLUMN string7 TEXT");
            database.execSQL("ALTER TABLE `story`  ADD COLUMN string8 TEXT");
            database.execSQL("ALTER TABLE `story`  ADD COLUMN string9 TEXT");
        }
    };

    public static AppDB getAppDatabase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, AppDB.class, "database")
                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build();
        }
        return instance;
    }

}