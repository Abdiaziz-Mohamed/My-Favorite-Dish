package com.example.myfavdish.view.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.myfavdish.R
import com.example.myfavdish.application.FavDishApplication
import com.example.myfavdish.databinding.FragmentDishDetailsBinding
import com.example.myfavdish.viewmodel.FavDishViewModel
import com.example.myfavdish.viewmodel.FavDishViewModelFactory
import java.io.IOException
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DishDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DishDetailsFragment : Fragment() {

    private var mBinding: FragmentDishDetailsBinding? = null

    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory(((requireActivity().application)as FavDishApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        // TODO Step 8: Initialize the mBinding variable.
        // START
        mBinding = FragmentDishDetailsBinding.inflate(inflater, container, false)
        return mBinding!!.root
        // END
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: DishDetailsFragmentArgs by navArgs()
        // TODO Step 10: Remove the log and populate the data to UI.
        // START
        args.let {

            try {
                // Load the dish image in the ImageView.
                Glide.with(requireActivity())
                        .load(it.dishDetails.image)
                        .centerCrop()
                        .listener(object: RequestListener<Drawable>{
                            override fun onLoadFailed(e: GlideException?,
                                                      model: Any?,
                                                      target: Target<Drawable>?,
                                                      isFirstResource: Boolean
                            ): Boolean {
                                Log.e("TAG", "ERROR loading Image", e)
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?,
                                                         model: Any?,
                                                         target: Target<Drawable>?,
                                                         dataSource: DataSource?,
                                                         isFirstResource: Boolean
                            ): Boolean {
                                resource.let {
                                    Palette.from(resource!!.toBitmap()).generate(){
                                        Palette ->
                                        val intcolor = Palette?.vibrantSwatch?.rgb ?: 0
                                        mBinding!!.rlDishDetailMain.setBackgroundColor(intcolor)
                                    }
                                }
                        return false
                            }


                        })
                        .into(mBinding!!.ivDishImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            mBinding!!.tvTitle.text = it.dishDetails.title
            mBinding!!.tvType.text =
                    it.dishDetails.type.capitalize(Locale.ROOT) // Used to make first letter capital
            mBinding!!.tvCategory.text = it.dishDetails.category
            mBinding!!.tvIngredients.text = it.dishDetails.ingredients
            mBinding!!.tvCookingDirection.text = it.dishDetails.directionToCook
            mBinding!!.tvCookingTime.text =
                    resources.getString(R.string.lbl_estimate_cooking_time, it.dishDetails.cookingTime)

            if (args.dishDetails.favoriteDish){
                mBinding!!.ivFavoriteDish.setImageDrawable(ContextCompat.getDrawable(requireActivity(),
                        R.drawable.ic_favorite_selected))

            } else {
                mBinding!!.ivFavoriteDish.setImageDrawable(
                        ContextCompat.getDrawable(
                                requireActivity(),
                                R.drawable.ic_favorite_unselected
                        )
                )

            }
        }
        mBinding!!.ivFavoriteDish.setOnClickListener {
        args.dishDetails.favoriteDish = !args.dishDetails.favoriteDish

            mFavDishViewModel.update(args.dishDetails)

            if (args.dishDetails.favoriteDish){
                mBinding!!.ivFavoriteDish.setImageDrawable(ContextCompat.getDrawable(requireActivity(),
                R.drawable.ic_favorite_selected))
                Toast.makeText(
                        requireActivity(),
                        resources.getString(R.string.msg_added_to_favorites),
                        Toast.LENGTH_SHORT
                ).show()
            } else {
                mBinding!!.ivFavoriteDish.setImageDrawable(
                        ContextCompat.getDrawable(
                                requireActivity(),
                                R.drawable.ic_favorite_unselected
                        )
                )

                Toast.makeText(
                        requireActivity(),
                        resources.getString(R.string.msg_removed_from_favorite),
                        Toast.LENGTH_SHORT
                ).show()
            }
            // END
        }
        // END
    }

    // TODO Step 9: Override the onDestroy function to make the mBinding null that is avoid the memory leaks. This we have not done before because the AllDishesFragment because when in it the onDestroy function is called the app is killed. But this is the good practice to do it.
    // START
    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
    // END
}