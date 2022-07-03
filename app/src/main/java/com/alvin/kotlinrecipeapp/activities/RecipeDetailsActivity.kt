package com.alvin.kotlinrecipeapp.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.alvin.kotlinrecipeapp.R
import com.alvin.kotlinrecipeapp.models.Recipe
import com.alvin.kotlinrecipeapp.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_recipe_details.*
import java.text.SimpleDateFormat

class RecipeDetailsActivity : AppCompatActivity() {
    private val mFireStore = FirebaseFirestore.getInstance()
    var recipe: Recipe? = null
    var recipeId: String? = null
    var nav_menu: Menu? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recipeId = intent.getStringExtra("recipeId")

        if (recipeId != null) {
            getRecipeDetails(recipeId!!)
        }
    }

    fun getRecipeDetails(recipeDetails: String) {
        mFireStore.collection(Constants.RECIPES).document(recipeDetails).get()
            .addOnSuccessListener { document ->
                recipe = document.toObject(Recipe::class.java)!!

                val sharedPreferences = getSharedPreferences("userProfile", Context.MODE_PRIVATE)
                val userId = sharedPreferences.getString("userId", "")
                val firstName = sharedPreferences?.getString("firstName", "")
                val lastName = sharedPreferences?.getString("lastName", "")
                if (userId.toString() == recipe?.authorId.toString()) {
                    nav_menu?.findItem(R.id.editRecipe)?.isVisible = true
                    nav_menu?.findItem(R.id.deleteRecipe)?.isVisible = true
                }
                if (recipe!!.authorName == "$firstName $lastName") {
                    tvAuthorName.text = "Me"
                } else {
                    tvAuthorName.text = recipe!!.authorName
                }
                tvRecipeTitle.text = recipe!!.title
                tvRecipeType.text = recipe!!.type
                if (recipe!!.dateUpdated == null) {
                    val date = recipe!!.dateCreated?.toDate()
                    val sdf = SimpleDateFormat("dd/MM/yy hh:mm a")
                    val formattedDate = sdf.format(date)
                    tvDateTime.text = formattedDate
                } else {
                    val date = recipe!!.dateUpdated?.toDate()
                    val sdf = SimpleDateFormat("dd/MM/yy hh:mm a")
                    val formattedDate = sdf.format(date)
                    tvDateTime.text = formattedDate
                }
                tvRecipeDescription.text = recipe!!.description
                tvRecipeIngredients.text = recipe!!.ingredients
                tvRecipeSteps.text = recipe!!.steps

                // Picasso to assign URL into imageView
                Picasso.get()
                    .load(recipe!!.recipeImage)
                    .into(ivRecipePhoto);
            }.addOnFailureListener { e ->
                Log.i("Failed to fetch user", e.toString())
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        nav_menu = menu
        menuInflater.inflate(R.menu.recipe_details_nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.editRecipe -> {
                finish()
                val intent = Intent(this@RecipeDetailsActivity, UpdateRecipeActivity::class.java)
                intent.putExtra("recipeModel", recipe)
                startActivity(intent)
            }
            R.id.deleteRecipe -> {
                val builder = AlertDialog.Builder(this@RecipeDetailsActivity)
                builder.setTitle("Confirm Deletion")
                builder.setMessage("Delete this recipe?")
                builder.setPositiveButton(
                    "YES",
                    DialogInterface.OnClickListener { dialog, which -> // Delete recipe and close the dialog
                        Log.i("here", recipeId!!)
                        mFireStore.collection(Constants.RECIPES).document(recipeId!!).delete()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@RecipeDetailsActivity,
                                    "Recipe Deleted",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }.addOnFailureListener { e ->
                                Log.i("Error deleting document", e.toString())
                            }
                        dialog.dismiss()
                    })
                builder.setNegativeButton(
                    "NO",
                    DialogInterface.OnClickListener { dialog, which -> // Do nothing
                        dialog.dismiss()
                    })

                val alert: AlertDialog = builder.create()
                alert.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}