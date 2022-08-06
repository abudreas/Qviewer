package com.abudreas.qviewer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView


class Adapter(private val dataSet: List<Category>,val context:Context) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvCatg = view.findViewById<TextView>(R.id.tv_catgName)
            val tvTotal = view.findViewById<TextView>(R.id.tv_totalNumber)
            val tvAtemp = view.findViewById<TextView>(R.id.tv_attempted)
            val tvCorrect = view.findViewById<TextView>(R.id.tv_correct)
            val tvCont = view.findViewById<TextView>(R.id.tv_promt)
            val card = view.findViewById<ConstraintLayout>(R.id.card)
           fun bind(catg:Category){
                tvCatg.text = catg.name
               tvTotal.text = "Number: " + catg.total.toString()
               tvAtemp.text = "Attempted: " + catg.attempted.toString()
               tvCorrect.text ="Correct: " + catg.correct
               if (catg.attempted == 0){
                   tvCont.text = "Start"
               }else{
                   tvCont.text="Continue"
               }
                if(catg.name=="All Questions"){
                    card.setBackgroundResource(R.drawable.card_all)
                }else{
                    card.setBackgroundResource(R.drawable.card)
                }
           }
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.catg_card, viewGroup, false)

            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.bind( dataSet[position])
            viewHolder.card.setOnClickListener {
                val intent = Intent(context,Questions::class.java)

                intent.putExtra("catg",dataSet[position].name)
                intent.putExtra("tableName",dataSet[position].tableName)
                context.startActivity(intent)
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size




}