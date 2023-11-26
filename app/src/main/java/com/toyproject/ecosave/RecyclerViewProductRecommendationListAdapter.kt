package com.toyproject.ecosave

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.toyproject.ecosave.models.DeviceTypeList
import com.toyproject.ecosave.models.RecommendProductData
import com.toyproject.ecosave.utilities.getPowerOfConsumeUnit

import java.math.BigDecimal
import java.math.RoundingMode

class RecyclerViewProductRecommendationListAdapter constructor(
    private val context: Context,
    private val productRecommendationList: List<RecommendProductData>,
    private val deviceType: DeviceTypeList
) : RecyclerView.Adapter<RecyclerViewProductRecommendationListAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageDevice: ImageView = itemView.findViewById(R.id.imageDevice)
        val textProductName: TextView = itemView.findViewById(R.id.textProductName)
        val textPowerOfConsumeType: TextView = itemView.findViewById(R.id.textPowerOfConsumeType)
        val textPowerOfConsume: TextView = itemView.findViewById(R.id.textPowerOfConsume)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recommend_product_list_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        // 제품 이름 표시
        holder.textProductName.text = productRecommendationList[position].productName

        // 소비전력량 표시
        holder.textPowerOfConsumeType.text = getPowerOfConsumeUnit(deviceType)["description"]
        if (productRecommendationList[position].powerOfConsume != null) {
            val powerOfConsume = BigDecimal(productRecommendationList[position].powerOfConsume!!).setScale(1, RoundingMode.HALF_UP)
            holder.textPowerOfConsume.text = "$powerOfConsume " +
                    "${getPowerOfConsumeUnit(deviceType)["symbol"]}"
        } else {
            holder.textPowerOfConsume.text = "- ${getPowerOfConsumeUnit(deviceType)["symbol"]}"
        }
    }

    override fun getItemCount(): Int {
        return productRecommendationList.size
    }
}