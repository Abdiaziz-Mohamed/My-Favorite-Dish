package com.example.myfavdish.application

import android.app.Application
import com.example.myfavdish.Model.database.FavDishRepository
import com.example.myfavdish.Model.database.FavDishRoomDatabase

// TODO Step 5: Create a application class where we can define the variable scope to use through out the application.
/**
 * A application class where we can define the variable scope to use through out the application.
 */
class FavDishApplication : Application() {

    // TODO Step 6 Define the variables to create an instance of Room database and use it through out the application.
    // START
    /**Using by lazy so the database and the repository are only created when they're needed
     * rather than when the application starts.
     */
    /**
     * The "lazy" keyword used for creating a new instance that uses the specified initialization function
     * and the default thread-safety mode [LazyThreadSafetyMode.SYNCHRONIZED].
     *
     * If the initialization of a value throws an exception, it will attempt to reinitialize the value at next access.
     *
     * Note that the returned instance uses itself to synchronize on. Do not synchronize from external code on
     * the returned instance as it may cause accidental deadlock. Also this behavior can be changed in the future.
     */
    private val database by lazy { FavDishRoomDatabase.getDatabase(this@FavDishApplication) }

    // A variable for repository.
    val repository by lazy { FavDishRepository(database.favDishDao()) }
    // END
}