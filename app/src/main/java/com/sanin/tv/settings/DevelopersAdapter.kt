package com.sanin.tv.settings
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sanin.tv.databinding.ItemDeveloperBinding
import com.sanin.tv.loadImage
import com.sanin.tv.openLinkInBrowser
import com.sanin.tv.setAnimation
class DevelopersAdapter(
private val developers: Array<Developer>) :    RecyclerView.Adapter<DevelopersAdapter.DeveloperViewHolder>() {    inner 
class DeveloperViewHolder(
val binding: ItemDeveloperBinding) :        RecyclerView.ViewHolder(binding.root) {        init {            itemView.setOnClickListener {                openLinkInBrowser(developers[bindingAdapterPosition].url)            }        }    }

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeveloperViewHolder {
return DeveloperViewHolder(            ItemDeveloperBinding.inflate(                LayoutInflater.from(parent.context),                parent,                false            )        )    }

override fun onBindViewHolder(holder: DeveloperViewHolder, position: Int) {
    val b = holder.binding        setAnimation(b.root.context, b.root)        
val dev = developers[position]        b.devName.text = dev.name        b.devProfile.loadImage(dev.pfp)        b.devRole.text = dev.role    }

override fun getItemCount(): Int = developers.size}