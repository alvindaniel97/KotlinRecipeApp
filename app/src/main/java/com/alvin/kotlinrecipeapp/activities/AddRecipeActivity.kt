package com.alvin.kotlinrecipeapp.activities

import android.app.Activity
import android.content.Context
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
import kotlinx.android.synthetic.main.activity_add_recipe.*
import java.text.SimpleDateFormat
import java.util.*

class AddRecipeActivity : BaseActivity() {
    private var pickedImage: Uri? = null
    private var pickedBitmap: Bitmap? = null
    private val mFireStore = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)
        setupActionBar()

        val typeList = resources.getStringArray(R.array.recipe_types)
        val filteredRecipeList = typeList.filterIndexed { index, _ -> index > 0 }
        val adapter = ArrayAdapter(
            this,
            R.layout.dropdown_menu_popup_item,
            filteredRecipeList
        )
        spRecipeType.setAdapter(adapter)

        btnUpload.setOnClickListener {
            pickImage()
        }

        btnSubmitRecipe.setOnClickListener {
            if (validateRecipeDetails()) {
                showProgressDialog()
                // setting file name
                val imageUri = pickedImage
                val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
                val now = Date()
                val fileName = formatter.format(now)

                val storageReference =
                    FirebaseStorage.getInstance().getReference("recipes/$fileName")

                if (imageUri != null) {
                    storageReference.putFile(imageUri).addOnSuccessListener { taskSnapshot ->
                        storageReference.downloadUrl.addOnSuccessListener { uri ->
                            val downloadUrl = uri.toString()
                            val date = Timestamp.now()

                            // Retrieve sharedPreference
                            val sharedPreferences =
                                getSharedPreferences("userProfile", Context.MODE_PRIVATE)
                            val userId = sharedPreferences.getString("userId", "").toString()
                            val firstName = sharedPreferences.getString("firstName", "").toString()
                            val lastName = sharedPreferences.getString("lastName", "").toString()

                            // Save data in fireStore
                            val recipe = Recipe(
                                etTitle.text.toString(),
                                etDescription.text.toString(),
                                spRecipeType.text.toString(),
                                etIngredients.text.toString(),
                                etSteps.text.toString(),
                                downloadUrl,
                                "$firstName $lastName",
                                userId,
                                date
                            )

                            mFireStore.collection(Constants.RECIPES).add(recipe)
                                .addOnSuccessListener {
                                    hideProgressDialog()
                                    Toast.makeText(
                                        this,
                                        "Recipe Added Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }.addOnFailureListener { e ->
                                    Log.i("Error adding recipe", e.toString())
                                }
                        }
                    }.addOnFailureListener {
                        hideProgressDialog()
                        Toast.makeText(this, "Failed to upload", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(tbAddRecipe)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeActionContentDescription(R.drawable.ic_baseline_arrow_back_24)
        }
        tbAddRecipe.setNavigationOnClickListener { onBackPressed() }
    }

    private fun pickImage() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //No Permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
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
                    } else {
                        pickedBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, pickedImage)
                        ivRecipePhoto.setImageBitmap(pickedBitmap)
                    }
                }
            }
        }

    fun validateRecipeDetails(): Boolean {
        return when {
            pickedImage == null -> {
                showSnackBar(resources.getString(R.string.errImage), true)
                false
            }
            TextUtils.isEmpty(etTitle.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.errType), true)
                false
            }
            TextUtils.isEmpty(etDescription.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.errType), true)
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