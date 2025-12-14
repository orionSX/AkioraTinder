package com.example.mobile_final.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_final.R
import com.example.mobile_final.dto.PlayerProfile

class RecommendationAdapter(
    private val profiles: List<PlayerProfile>,
    private val onProfileClick: (PlayerProfile) -> Unit
) : RecyclerView.Adapter<RecommendationAdapter.ProfileViewHolder>() {

    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        val profileName: TextView = itemView.findViewById(R.id.profile_name)
        val profileDescription: TextView = itemView.findViewById(R.id.profile_description)
        val cardView: CardView = itemView as CardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_card, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profile = profiles[position]
        holder.profileName.text = "${profile.userData.name}, ${profile.userData.age ?: "Возраст не указан"}"
        holder.profileDescription.text = profile.description

        // Handle click on profile for test handling
        holder.itemView.setOnClickListener {
            onProfileClick(profile)
        }
    }

    override fun getItemCount(): Int = profiles.size
}