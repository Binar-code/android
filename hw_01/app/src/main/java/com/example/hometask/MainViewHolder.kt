package com.example.hometask

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val text: TextView = view.findViewById<TextView>(R.id.text_1)
    private val img = view.findViewById<ImageView>(R.id.image_view)

    fun bind(number: Int) {
        text.text = "$number"

        if (number % 2 == 0)
            img.setBackgroundColor(Color.RED)
        else
            img.setBackgroundColor(Color.BLUE)
    }
}