package com.example.praticum.data.database;

import android.content.Context;

import androidx.room.Room;

public final class DatabaseProvider {

    private static volatile AppDatabase instance;

    private DatabaseProvider() {
        // Empêche l'instanciation de cette classe.
    }

    public static AppDatabase getDatabase(Context context) {
        if (instance == null) {
            synchronized (DatabaseProvider.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "practicum_database"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }

        return instance;
    }
}

