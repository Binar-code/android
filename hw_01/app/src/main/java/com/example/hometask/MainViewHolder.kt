package com.example.hometask

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val text: TextView = view.findViewById<TextView>(R.id.text_1)
    val img = view.findViewById<ImageView>(R.id.image_view)

    fun bind(number: Int) {
        text.text = "$number"
    }
}