package com.example.myfavdish.view.Activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.myfavdish.Model.entities.FavDish
import com.example.myfavdish.R
import com.example.myfavdish.application.FavDishApplication
import com.example.myfavdish.databinding.ActivityAddUpdateDishBinding
import com.example.myfavdish.databinding.DialogCustomImageSelectionBinding
import com.example.myfavdish.databinding.DialogCustomListBinding
import com.example.myfavdish.utils.Constants
import com.example.myfavdish.view.adapters.CustomListItemAdapter
import com.example.myfavdish.view.fragments.AllDishesFragment
import com.example.myfavdish.viewmodel.FavDishViewModel
import com.example.myfavdish.viewmodel.FavDishViewModelFactory
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class AddUpdateDishActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mBinding: ActivityAddUpdateDishBinding
    private var mImagePath : String = ""

    private lateinit var mCustomListDialog: Dialog

    private var mFavDishDetails: FavDish? = null

    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((application as FavDishApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        if (intent.hasExtra(Constants.EXTRA_DISH_DETAILS)){
            mFavDishDetails = intent.getParcelableExtra(Constants.EXTRA_DISH_DETAILS)
        }

        setupActionBar()


        mFavDishDetails?.let {
            if (it.id != 0) {
                mImagePath = it.image

                // Load the dish image in the ImageView.
                Glide.with(this@AddUpdateDishActivity)
                        .load(mImagePath)
                        .centerCrop()
                        .into(mBinding.ivDishImage)

                mBinding.etTitle.setText(it.title)
                mBinding.etType.setText(it.type)
                mBinding.etCategory.setText(it.category)
                mBinding.etIngredients.setText(it.ingredients)
                mBinding.etCookingTime.setText(it.cookingTime)
                mBinding.etDirectionToCook.setText(it.directionToCook)

                mBinding.btnAddDish.text = resources.getString(R.string.lbl_update_dish)
            }
        }


        mBinding.ivAddDishImage.setOnClickListener(this@AddUpdateDishActivity)

        mBinding.etType.setOnClickListener(this)
        mBinding.etCategory.setOnClickListener(this)
        mBinding.etCookingTime.setOnClickListener(this)

        mBinding.btnAddDish.setOnClickListener(this)
    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.iv_add_dish_image -> {

                customImageSelectionDialog()

                return
            }
            R.id.et_type -> {
                customItemsListDialog(resources.getString(R.string.title_select_dish_type),
                        Constants.dishTypes(),
                        Constants.DISH_TYPE)
                return
            }
            R.id.et_category -> {
                customItemsListDialog(resources.getString(R.string.title_select_dish_category),
                        Constants.dishCategories(),
                        Constants.DISH_CATEGORY)
                return
            }

            R.id.et_cooking_time -> {
                customItemsListDialog(resources.getString(R.string.title_select_dish_cooking_time),
                        Constants.dishCookTime(),
                        Constants.DISH_COOKING_TIME)
                return
            }

            R.id.btn_add_dish -> {

                // Define the local variables and get the EditText values.
                // For Dish Image we have the global variable defined already.

                val title = mBinding.etTitle.text.toString().trim { it <= ' ' }
                val type = mBinding.etType.text.toString().trim { it <= ' ' }
                val category = mBinding.etCategory.text.toString().trim { it <= ' ' }
                val ingredients = mBinding.etIngredients.text.toString().trim { it <= ' ' }
                val cookingTimeInMinutes = mBinding.etCookingTime.text.toString().trim { it <= ' ' }
                val cookingDirection = mBinding.etDirectionToCook.text.toString().trim { it <= ' ' }

                when {

                    TextUtils.isEmpty(mImagePath) -> {
                        Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_image),
                                Toast.LENGTH_SHORT
                        ).show()
                    }

                    TextUtils.isEmpty(title) -> {
                        Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_title),
                                Toast.LENGTH_SHORT
                        ).show()
                    }

                    TextUtils.isEmpty(type) -> {
                        Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_type),
                                Toast.LENGTH_SHORT
                        ).show()
                    }

                    TextUtils.isEmpty(category) -> {
                        Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_category),
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                    TextUtils.isEmpty(ingredients) -> {
                        Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_ingredients),
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                    TextUtils.isEmpty(cookingTimeInMinutes) -> {
                        Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_cooking_time),
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                    TextUtils.isEmpty(cookingDirection) -> {
                        Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_cooking_instructions),
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        var dishID = 0
                        var imageSource = Constants.DISH_IMAGE_SOURCE_LOCAL
                        var favoriteDish = false

                        mFavDishDetails?.let {
                            if (it.id != 0) {
                                dishID = it.id
                                imageSource = it.imageSource
                                favoriteDish = it.favoriteDish
                            }
                        }


                        // TODO Step 3: Create an instance of the entity class and pass the required values to it. Remove the Toast Message.
                        // START
                        val favDishDetails: FavDish = FavDish(
                            mImagePath,
                            imageSource,
                            title,
                            type,
                            category,
                            ingredients,
                            cookingTimeInMinutes,
                            cookingDirection,
                            favoriteDish,
                            dishID
                        )

                        if(dishID == 0) {
                            mFavDishViewModel.insert(favDishDetails)

                            Toast.makeText(
                                    this@AddUpdateDishActivity,
                                    "You successfully added your favorite dish details.",
                                    Toast.LENGTH_SHORT
                            ).show()
                            Log.e("Insertion", "Success")
                        }else{
                            mFavDishViewModel.update(favDishDetails)

                            Toast.makeText(
                                    this@AddUpdateDishActivity,
                                    "You successfully updated your favorite dish details.",
                                    Toast.LENGTH_SHORT
                            ).show()

                            // You even print the log if Toast is not displayed on emulator
                            Log.e("Updating", "Success")
                        }
                        finish()
                        // END
                    }
                }
            }
        }
    }


    private fun setupActionBar() {
        setSupportActionBar(mBinding.toolbarAddDishActivity)
        if (mFavDishDetails != null && mFavDishDetails!!.id != 0){
            supportActionBar?.let {
                it.title = resources.getString(R.string.title_edit_dish)
            }

        }else {
            supportActionBar?.let {
                it.title = resources.getString(R.string.title_add_dish)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mBinding.toolbarAddDishActivity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun customImageSelectionDialog() {
        val dialog = Dialog(this@AddUpdateDishActivity)
        val binding: DialogCustomImageSelectionBinding =
                DialogCustomImageSelectionBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)

        binding.tvCamera.setOnClickListener {

            Dexter.withContext(this@AddUpdateDishActivity)
                    .withPermissions(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA
                    )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            // Here after all the permission are granted launch the CAMERA to capture an image.
                            report?.let {
                                if (report.areAllPermissionsGranted()) {

                                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                    startActivityForResult(intent, CAMERA)
                                }
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                                permissions: MutableList<PermissionRequest>?,
                                token: PermissionToken?
                        ) {
                            showRationalDialogForPermissions()
                        }
                    }).onSameThread()
                    .check()

            dialog.dismiss()
        }


        binding.tvGallery.setOnClickListener {

            // TODO Step 7: Ask for the permission while selecting the image from Gallery using Dexter Library. And Remove the toast message.
            Dexter.withContext(this@AddUpdateDishActivity)
                    .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse) {
                            // TODO Step 2: Launch the gallery for Image selection using the constant.
                            // START
                            val galleryIntent = Intent(
                                    Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            )

                            startActivityForResult(galleryIntent, GALLERY)
                            // END
                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse) {
                            Toast.makeText(
                                    this@AddUpdateDishActivity,
                                    "You have denied the storage permission to select image.",
                                    Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onPermissionRationaleShouldBeShown(
                                permission: PermissionRequest,
                                token: PermissionToken
                        ) {
                            showRationalDialogForPermissions()
                        }
                    })
                    .onSameThread()
                    .check()

            dialog.dismiss()
        }

        //Start the dialog and display it on screen.
        dialog.show()
    }

    fun  selectedListItem(item: String, selection: String){
        when(selection){
            Constants.DISH_TYPE ->{
                mCustomListDialog.dismiss()
                mBinding.etType.setText(item)
            }
            Constants.DISH_CATEGORY ->{
                mCustomListDialog.dismiss()
                mBinding.etCategory.setText(item)
            }
            else ->{
                mCustomListDialog.dismiss()
                mBinding.etCookingTime.setText(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == CAMERA){
                data?.extras?.let {
                    val thumbnail : Bitmap = data.extras!!.get("data") as Bitmap
                    //mBinding.ivDishImage.setImageBitmap(thumbnail)

                    Glide.with(this)
                            .load(thumbnail)
                            .centerCrop()
                            .into(mBinding.ivDishImage)

                    mImagePath = saveImageToInternalStorage(thumbnail)
                    Log.i("ImagePath", mImagePath)

                    mBinding.ivAddDishImage.setImageDrawable(
                            ContextCompat.getDrawable(this, R.drawable.ic_vector_edit))
                }
            }

            if (requestCode == GALLERY){
                data?.let {

                    val selectedPhotoUri = data.data

                    //mBinding.ivDishImage.setImageURI(selectedPhotoUri)

                    Glide.with(this)
                            .load(selectedPhotoUri)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .listener(object : RequestListener<Drawable>{
                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                    Log.e("TAG", "Error loading image", e)
                                    return false
                                }

                                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    resource?.let {
                                        val bitmap: Bitmap = resource.toBitmap()
                                        mImagePath = saveImageToInternalStorage(bitmap)
                                        Log.i("ImagePath", mImagePath)

                                    }
                                    return false
                                }

                            })
                            .into(mBinding.ivDishImage)

                    mBinding.ivAddDishImage.setImageDrawable(
                            ContextCompat.getDrawable(this, R.drawable.ic_vector_edit))
                }
            }
        }else if (resultCode == Activity.RESULT_CANCELED){
            Log.e("Cancelled", "User cancelled image selection")
        }
    }


    /**
     * A function used to show the alert dialog when the permissions are denied and need to allow it from settings app info.
     */
    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
                .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
                .setPositiveButton(
                        "GO TO SETTINGS"
                ) { _, _ ->
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.show()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap):String{
        val Wrapper = ContextWrapper(applicationContext)

        var file = Wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")

        try {
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }

        return file.absolutePath
    }
        private  fun customItemsListDialog(title: String, itemList: List<String>, selection: String){
            mCustomListDialog = Dialog(this)
            val binding : DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)

            mCustomListDialog.setContentView(binding.root)
            binding.tvTitle.text = title 

            binding.rvList.layoutManager = LinearLayoutManager(this)

            val adapter = CustomListItemAdapter(this@AddUpdateDishActivity, AllDishesFragment(), itemList, selection)
            binding.rvList.adapter = adapter
            mCustomListDialog.show()
        }


    companion object {
        private const val CAMERA = 1

        // TODO Step 1: Add the constant for Gallery.
        // START
        private const val GALLERY = 2

        private const val IMAGE_DIRECTORY ="FavDishImages"
        // END
    }
}