package com.example.examenfinal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import org.json.JSONArray
import org.json.JSONException

class ResponseActivity : AppCompatActivity() {
    private lateinit var codeflag: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_response)
        val bun = intent.extras
        codeflag = bun?.getString("output").toString()


        var url = "http://www.geognos.com/api/en/countries/info/${codeflag}.json"

        var request = StringRequest(
            Request.Method.GET, // method
            url, // url // json request
            {response -> // response listener
                try {
                    var JArray = JSONArray(response)
                    for (i in 0..JArray.length()-1) {
                        var JObject = JArray.getJSONObject(i)

                        val id: Int = JObject.getInt("id")
                        val name: String = JObject.getString("name")
                        val email: String = JObject.getString("email")
                        val gender: String = JObject.getString("gender")
                        val status: String = JObject.getString("status")


                    }

                }catch (e: JSONException){
                }
            },
            {error -> // error listener
            }
        )
        MySingleton.getInstance(applicationContext).addToRequestQueue(request)

        url = "http://www.geognos.com/api/en/countries/info/${codeflag}.png"

        request = StringRequest(
            Request.Method.GET, // method
            url, // url // json request
            {response -> // response listener
                try {
                    var JArray = JSONArray(response)
                    for (i in 0..JArray.length()-1) {
                        var JObject = JArray.getJSONObject(i)

                        val id: Int = JObject.getInt("id")
                        val name: String = JObject.getString("name")
                        val email: String = JObject.getString("email")
                        val gender: String = JObject.getString("gender")
                        val status: String = JObject.getString("status")


                    }

                }catch (e: JSONException){
                }
            },
            {error -> // error listener
            }
        )
        MySingleton.getInstance(applicationContext).addToRequestQueue(request)

    }
}