package com.example.forecast

import android.net.http.HttpException
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity
import com.example.forecast.Utils.RetrofitInstances
import com.example.forecast.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {


    lateinit var binding : ActivityMainBinding
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getCurrentWeather()


        }
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun getCurrentWeather(){
        GlobalScope.launch(Dispatchers.IO) {
            val response = try {
                RetrofitInstances.api.getCurrentWeather("new york","metric",applicationContext.getString(R.string.api_key))


            }catch (e:IOException){
                Toast.makeText(applicationContext,"app error ${e.message}",Toast.LENGTH_SHORT).show()
                return@launch
            }catch (e:HttpException){
                Toast.makeText(applicationContext,"app error ${e.message}",Toast.LENGTH_SHORT).show()
                return@launch
            }
            if(response.isSuccessful&&response.body()!=null){
                withContext(Dispatchers.Main){
                  val data = response.body()!!
                    val iconId = data.weather[0].icon

                    val imgUrl = "https://openweathermap.org/img/w/$iconId.png"
                    Picasso.get().load(imgUrl).into(binding.imgWeather)
                    binding.tvSunrise.text=
                        SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(data.sys.sunrise*1000)//sunrise huda ko icon

                    binding.tvSunrise.text=
                        SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(data.sys.sunset*1000)//sunset huda ko icon
                    binding.apply{
                        tvStatus.text = data.weather[0].description
                        tvWind.text ="${data.wind.speed.toString()} km/hr"
                        tvLocation.text = "${data.name}\n${data.sys.country}"
                        tvTemp.text = "${data.main.temp.toInt()}°C"
                        tvFeelsLike.text= "Feels like:${data.main.feels_like.toInt()}°C "
                        tvMinTemp.text = "${data.main.temp_min.toInt()}°C"
                        tvMaxTemp.text = "${data.main.temp_max.toInt()}°C"
                        tvHumidity.text= "${data.main.humidity}%"
                        tvPressure.text= "${data.main.pressure} hPa"
                        tvUpdateTime.text= "Last update :${
                            SimpleDateFormat("hh:mm a", 
                                Locale.ENGLISH)
                                .format(data.dt*1000)
                        }"


                    }

                }
            }

        }
    }
    }
