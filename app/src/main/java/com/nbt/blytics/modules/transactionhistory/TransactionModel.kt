package com.nbt.blytics.modules.transactionhistory
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName


/**
 * Created bynbton 30-10-2021
 */
data class TransactionResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("status")
    val status: String
) {
    data class Data(
        @SerializedName("account_no")
        val accountNo: String,
        @SerializedName("data_remaining")
        val dataRemaining: String,
        @SerializedName("total_txn")
        val totalTxn: String,
        @SerializedName("txn_list")
        val txnList: List<Txn>,
        @SerializedName("amount_sent")
        val amountSent: String="",
        @SerializedName("amount_receive")
        val amountReceive: String="",
        @SerializedName("total_charges")
        val totalCharges: String="",

    ) {

        data class Txn(
            @SerializedName("amount")
            val amount: String?,
            @SerializedName("currency")
            val currency: String,
            @SerializedName("date")
            val date: String,
            @SerializedName("txn_id")
            val txnId: String,
            @SerializedName("type")
            val type: String,
            @SerializedName("user_id")
            val userId: String,
            @SerializedName("image_url")
            val userImage: String,
            @SerializedName("user_name")
            val userName: String,
            @SerializedName("uuid")
            val uuid: String,
            @SerializedName("status")
            val status: String,
            @SerializedName("mob_no")
            val mobNo: String,
            var isSelected:Boolean= false,
            @SerializedName("bank_type")
            val bankType :String ="",
            val bank_acc:String ="",
            val bank_code:String="",
            val reference:String="",
            var lytType:Int=-1,
            val closing_balance:String =""

            )
    }
}
data class TransactionRequest(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_token")
    val userToken: String,
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("frequent_payee")
    val frequentPayee:String,
    @SerializedName("account")
    val account: String="",
    @SerializedName("bank")
    val bank: String="",
    @SerializedName("month")
    val month: String="",
    @SerializedName("offset")
    val offset: String="",
    @SerializedName("page")
    val page: Int=1,
    @SerializedName("reverse")
    val reverse: Int=0,
    @SerializedName("sort_by")
    val sortBy: String="",
    @SerializedName("status")
    val status: String="",
    @SerializedName("type")
    val type: String="",
    @SerializedName("transact_user_id")
    val transactuserid : String="",
    @SerializedName("name")
    val name: String="",
    val date: String="",
    @SerializedName("acc_number")
    val accNumber :String =""
)
