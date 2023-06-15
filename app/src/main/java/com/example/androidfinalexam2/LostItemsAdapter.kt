package com.example.androidfinalexam2

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Base64

class LostItemsAdapter(private val lostItems: List<LostItem>) :
    RecyclerView.Adapter<LostItemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lost, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lostItems[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val id = item.id
            val intent = Intent(context, ItemDetail::class.java)
            intent.putExtra("id", id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return lostItems.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        private val textTime: TextView = itemView.findViewById(R.id.textTime)
        private val textLocation: TextView = itemView.findViewById(R.id.textLocation)
        private val imagePhoto: ImageView = itemView.findViewById(R.id.photo)
        private val imageCategory: ImageView = itemView.findViewById(R.id.itemCate)
        private val imageStatus: ImageView = itemView.findViewById(R.id.item_status)
        private val itemId: TextView = itemView.findViewById(R.id.item_id)

        fun bind(lostItem: LostItem) {
            textTitle.text = lostItem.title
            textTime.text = lostItem.find_time
            textLocation.text = lostItem.find_location
            displayImageFromBase64(lostItem.photo, imagePhoto)
            imageCategory.setImageResource(getCategoryIcon(lostItem.type))
            imageStatus.setImageResource(getCategoryIcon(lostItem.item_status))
            itemId.text = lostItem.id
        }
    }

    private fun displayImageFromBase64(base64Data: String, imageView: ImageView) {
        val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        imageView.setImageBitmap(bitmap)
    }

    private fun getCategoryIcon(category: String): Int {
        return when (category) {
            "书籍" -> R.drawable.shuji
            "证件" -> R.drawable.zhengjian
            "电子产品" -> R.drawable.dianzi
            "平板电脑" -> R.drawable.pingbandiannao
            "电脑" -> R.drawable.diannao
            "相机" -> R.drawable.xiangji
            "音乐播放器" -> R.drawable.yinyuebofangqi
            "耳机" -> R.drawable.erji
            "充电器" -> R.drawable.chongdianqi
            "1" -> R.drawable.yes
            "0" -> R.drawable.no
            else -> R.drawable.shuji
        }
    }
}
