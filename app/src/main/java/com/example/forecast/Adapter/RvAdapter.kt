package com.example.forecast.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.forecast.data.forecastModels.ForecastData
import com.example.forecast.databinding.RvItemLayoutBinding
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RvAdapter( private val forecastArray: ArrayList<ForecastData>) : RecyclerView.Adapter<RvAdapter.ViewHolder>() {

    class ViewHolder(val binding : RvItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RvItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = forecastArray[position]
        holder.binding.apply {
            val imageIcon = currentItem.weather[0].icon
            val imageUrl = "https://openweathermap.org/img/w/$imageIcon.png"

            Picasso.get().load(imageUrl).into(imgItem)

            tvItemTemp.text = "${currentItem.main.temp.toInt()} °C"
            tvItemStatus.text = "${currentItem.weather[0].description}"
            tvItemTime.text = displayTime(currentItem.dt_txt)

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayTime(dtTxt: String): CharSequence? {
        val input = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val output = DateTimeFormatter.ofPattern("MM-dd HH:mm")
        val dateTime = LocalDateTime.parse(dtTxt,input)
        return output.format(dateTime)

    }

    override fun getItemCount(): Int {
        return forecastArray.size
    }
}
