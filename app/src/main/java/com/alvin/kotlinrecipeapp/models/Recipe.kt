package com.alvin.kotlinrecipeapp.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import kotlinx.android.parcel.Parcelize

@Parcelize
class Recipe (
    val title: String = "",
    val description: String = "",
    val type: String = "",
    val ingredients: String = "",
    val steps: String = "",
    val recipeImage: String = "",
    val authorName: String = "",
    val authorId: String = "",
    val dateCreated: Timestamp? = null,
    val dateUpdated: Timestamp? = null,
    @DocumentId
    val id: String?= "",
): Parcelable