package com.alvin.kotlinrecipeapp.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alvin.kotlinrecipeapp.R
import com.alvin.kotlinrecipeapp.models.Recipe
import com.alvin.kotlinrecipeapp.utils.Constants
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_update_recipe.*
import java.text.SimpleDateFormat
import java.util.*

class UpdateRecipeActivity : BaseActivity() {
    private val mFireStore = FirebaseFirestore.getInstance()
    private lateinit var mRecipeModel: Recipe
    private var mRecipeHashMap = HashMap<String, Any>()
    private var pickedImage: Uri? = null
    private var pickedBitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_recipe)
        setupActionBar()

        mRecipeModel = intent.getParcelableExtra("recipeModel")!!
        if (mRecipeModel != null) {
            etTitle.setText(mRecipeModel.title)
            etDescription.setText(mRecipeModel.description)
            spRecipeType.setText(mRecipeModel.type)

            val typeList = resources.getStringArray(R.array.recipe_types)
            val filteredRecipeList = typeList.filterIndexed { index, _ -> index > 0 }
            val adapter = ArrayAdapter(
                this,
                R.layout.dropdown_menu_popup_item,
                filteredRecipeList
            )
            spRecipeType.setAdapter(adapter)
            etIngredients.setText(mRecipeModel.ingredients)
            etSteps.setText(mRecipeModel.steps)

            // Picasso to assign URL into imageView
            Picasso.get()
                .load(mRecipeModel.recipeImage)
                .into(ivRecipePhoto);
            ivRecipePhoto.tag = "0";
        }
        btnUpdateImage.setOnClickListener {
            pickImage()
        }

        btnUpdateRecipe.setOnClickListener {
            if (mRecipeModel!!.title != etTitle.text.toString() || mRecipeModel.description != etDescription.text.toString()
                || mRecipeModel.type != spRecipeType.text.toString() || mRecipeModel.ingredients != etIngredients.text.toString()
                || mRecipeModel.steps != etSteps.text.toString() || ivRecipePhoto.tag == "imageUpdated"
            ) {
                if (validateRecipeDetails()) {
                    showProgressDialog()
                    if (ivRecipePhoto.tag == "imageUpdated") {
                        // setting file name
                        val imageUri = pickedImage
                        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
                        val now = Date()
                        val fileName = formatter.format(now)

                        val storageReference =
                            FirebaseStorage.getInstance().getReference("recipes/$fileName")

                        if (imageUri != null) {
                            storageReference.putFile(imageUri)
                                .addOnSuccessListener { taskSnapshot ->
                                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                                        val downloadUrl = uri.toString()
                                        mRecipeHashMap["recipeImage"] = downloadUrl
                                        updateFireStoreData()
                                    }
                                }.addOnFailureListener {
                                    hideProgressDialog()
                                    Toast.makeText(this, "Failed to upload", Toast.LENGTH_SHORT)
                                        .show()
                                }
                        }
                    } else {
                        updateFireStoreData()
                    }
                }

            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(tbUpdateRecipe)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeActionContentDescription(R.drawable.ic_baseline_arrow_back_24)
        }
        tbUpdateRecipe.setNavigationOnClickListener {
            finish()
            val intent =
                Intent(this@UpdateRecipeActivity, RecipeDetailsActivity::class.java)
            intent.putExtra("recipeId", mRecipeModel.id)
            startActivity(intent)
        }
    }

    private fun updateFireStoreData() {
        val newTitle = etTitle.text.toString()
        val newDescription = etDescription.text.toString()
        val newType = spRecipeType.text.toString()
        val newIngredients = etIngredients.text.toString()
        val newSteps = etSteps.text.toString()
        val date = Timestamp.now()

        mRecipeHashMap["title"] = newTitle
        mRecipeHashMap["description"] = newDescription
        mRecipeHashMap["type"] = newType
        mRecipeHashMap["ingredients"] = newIngredients
        mRecipeHashMap["steps"] = newSteps
        mRecipeHashMap["dateUpdated"] = date

        mFireStore.collection(Constants.RECIPES).document(mRecipeModel.id!!).update(mRecipeHashMap)
            .addOnSuccessListener {
                hideProgressDialog()
                finish()
                val intent =
                    Intent(this@UpdateRecipeActivity, RecipeDetailsActivity::class.java)
                intent.putExtra("recipeId", mRecipeModel.id)
                startActivity(intent)
                Toast.makeText(this, "Recipe updated successfully", Toast.LENGTH_SHORT)
                    .show()
            }.addOnFailureListener { e ->
                hideProgressDialog()
                Log.i("Update failed. Error: ", e.toString())
            }
    }

    private fun pickImage() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //No Permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        } else {
            openGallery()
        }
    }

    private fun openGallery() {
        val galleryIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(galleryIntent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                pickedImage = data?.data

                if (pickedImage != null) {
                    // set image to preview
                    if (Build.VERSION.SDK_INT >= 28) {
                        val source =
                            ImageDecoder.createSource(this.contentResolver, pickedImage!!)
                        pickedBitmap = ImageDecoder.decodeBitmap(source)
                        ivRecipePhoto.setImageBitmap(pickedBitmap)
                        ivRecipePhoto.tag = "imageUpdated"
                    } else {
                        pickedBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, pickedImage)
                        ivRecipePhoto.setImageBitmap(pickedBitmap)
                        ivRecipePhoto.tag = "imageUpdated"
                    }
                }
            }
        }

    fun validateRecipeDetails(): Boolean {
        return when {
            TextUtils.isEmpty(etTitle.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.errTitle), true)
                false
            }
            TextUtils.isEmpty(etDescription.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.errDescription), true)
                false
            }
            TextUtils.isEmpty(spRecipeType.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.errType), true)
                false
            }
            TextUtils.isEmpty(etIngredients.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.errIngredients), true)
                false
            }
            TextUtils.isEmpty(etSteps.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.errSteps), true)
                false
            }
            else -> {
                true
            }
        }
    }
}