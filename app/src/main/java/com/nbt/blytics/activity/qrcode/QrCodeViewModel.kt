package com.nbt.blytics.activity.qrcode

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.signin.model.FailResponse
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created bynbton 19-11-2021
 */
class QrCodeViewModel(application: Application): BaseViewModel(application) {
    private var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()

fun getQrCode(qr: QrRequest){
    val request = service.getQrCode(qr)
    request.enqueue(object : Callback<ResponseBody> {
        override fun onResponse(
            call: Call<ResponseBody>,
            response: Response<ResponseBody>
        ) {
            try {

                getQrCodeResponse(response.body()!!.string())

            } catch (ex: Exception) {
                observerResponse.value = FailResponse("", ex.message.toString())
            }

        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            observerResponse.value = FailResponse("", t.message.toString())
        }
        fun getQrCodeResponse(res:String){
            val json = JSONObject(res)
            val status = json.getString("status")
            val data = json.getJSONObject("data")
            val qrcode = data.getString("qrcode")

            observerResponse.value = QrResponse(status = status, data = QrResponse.Data(qrcode))



        }

    })
}

}