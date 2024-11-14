package com.nbt.blytics.modules.apps

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.squpdate.model.SqResponse
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppsViewModel (application: Application): BaseViewModel(application) {

}