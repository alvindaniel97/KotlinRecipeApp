package com.alvin.kotlinrecipeapp.activities.ui.myRecipe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alvin.kotlinrecipeapp.R
import com.alvin.kotlinrecipeapp.activities.RecipeDetailsActivity
import com.alvin.kotlinrecipeapp.databinding.FragmentMyRecipeBinding
import com.alvin.kotlinrecipeapp.models.Recipe
import com.alvin.kotlinrecipeapp.utils.Constants
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat

class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
class MyRecipeFragment : Fragment() {

    private var _binding: FragmentMyRecipeBinding? = null
    private var queryType: String = "noQuery"

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMyRecipeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val typeList = resources.getStringArray(R.array.recipe_types)
        val adapter = activity?.let {
            ArrayAdapter(
                it,
                R.layout.dropdown_menu_popup_item,
                typeList
            )
        }

        binding.spRecipeType.setAdapter(adapter)

        binding.spRecipeType.setOnItemClickListener { adapterView, view, i, l ->
            val query = adapterView.getItemAtPosition(i).toString()
            FirestoreChangeListener(query)
        }

        FirestoreChangeListener(queryType)

        return root
    }

    private fun FirestoreChangeListener(queryTpye: String) {
        Log.i("queryTpye", queryTpye)
        val sharedPreferences = activity?.getSharedPreferences("userProfile", Context.MODE_PRIVATE)
        val userId = sharedPreferences?.getString("userId", "")
        var query: Query
        if (queryTpye == "All" || queryTpye == "noQuery" || queryTpye == "") {
            query = FirebaseFirestore.getInstance().collection(Constants.RECIPES)
                .whereEqualTo("authorId", userId)
        } else {
            query = FirebaseFirestore.getInstance().collection(Constants.RECIPES)
                .whereEqualTo("type", queryTpye).whereEqualTo("authorId", userId)
        }

        val options = FirestoreRecyclerOptions.Builder<Recipe>().setQuery(query, Recipe::class.java)
            .setLifecycleOwner(this).build()

        val adapter = object : FirestoreRecyclerAdapter<Recipe, RecipeViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
                val view = LayoutInflater.from(activity)
                    .inflate(R.layout.list_recipe, parent, false)
                return RecipeViewHolder(view)
            }

            override fun onBindViewHolder(holder: RecipeViewHolder, position: Int, recipe: Recipe) {

                val tvRecipeTitle: TextView = holder.itemView.findViewById(R.id.tvRecipeTitle)
                val tvType: TextView = holder.itemView.findViewById(R.id.tvType)
                val ivRecipeListImage: ImageView =
                    holder.itemView.findViewById(R.id.ivRecipeListImage)
                val tvAuthorName: TextView = holder.itemView.findViewById(R.id.tvAuthorName)
                val tvDateTimeCreated: TextView =
                    holder.itemView.findViewById(R.id.tvDateTimeCreated)
                tvRecipeTitle.text = recipe.title
                tvType.text = recipe.type
                val sharedPreferences =
                    activity?.getSharedPreferences("userProfile", Context.MODE_PRIVATE)
                val firstName = sharedPreferences?.getString("firstName", "")
                val lastName = sharedPreferences?.getString("lastName", "")
                if (recipe.authorName == "$firstName $lastName") {
                    tvAuthorName.text = "Me"
                } else {
                    tvAuthorName.text = recipe.authorName
                }

                if (recipe.dateUpdated == null) {
                    val date = recipe.dateCreated?.toDate()
                    val sdf = SimpleDateFormat("dd/MM/yy hh:mm a")
                    val formattedDate = sdf.format(date)
                    tvDateTimeCreated.text = formattedDate
                } else {
                    val date = recipe.dateUpdated.toDate()
                    val sdf = SimpleDateFormat("dd/MM/yy hh:mm a")
                    val formattedDate = sdf.format(date)
                    tvDateTimeCreated.text = formattedDate
                }

                // Picasso to assign URL into imageView
                Picasso.get()
                    .load(recipe.recipeImage)
                    .into(ivRecipeListImage);

                holder.itemView.setOnClickListener {
                    recipe.id?.let { recipeId ->
                        val intent = Intent(activity, RecipeDetailsActivity::class.java)
                        intent.putExtra("recipeId", recipeId)
                        startActivity(intent)
                    }
                }

            }
        }
        binding.rvRecipeList.adapter = adapter
        binding.rvRecipeList.layoutManager = LinearLayoutManager(activity)
    }


    override fun onStart() {
        super.onStart()
        FirestoreChangeListener(queryType)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}