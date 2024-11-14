    package com.nbt.blytics.modules.payment

    import com.google.gson.annotations.SerializedName

    data class TransactionHomeResponse(
        @SerializedName("status")
        val status: String,
        @SerializedName("data")
        val data: Data?
    ) {
        data class Data(
            @SerializedName("count")
            val count: Int,
            @SerializedName("next")
            val next: String?,
            @SerializedName("previous")
            val previous: String?,
            @SerializedName("txn_list")
            val txnList: List<Txn>
        ) {
            data class Txn(
                @SerializedName("bank_type")
                val bankType: String,
                @SerializedName("type")
                val type: String,
                @SerializedName("amount")
                val amount: String?,
                @SerializedName("uuid")
                val uuid: String,
                @SerializedName("user_id")
                val userId: Int,
                @SerializedName("user_name")
                val userName: String,
                @SerializedName("mob_no")
                val mobNo: String,
                @SerializedName("image_url")
                val imageUrl: String,
                @SerializedName("closing_balance_date")
                val closingBalanceDate: String,
                @SerializedName("closing_balance")
                val closingBalance: String,
                @SerializedName("reference")
                val reference: String,
                @SerializedName("currency")
                val currency: String,
                @SerializedName("txn_id")
                val txnId: String,
                @SerializedName("status")
                val status: String,
                @SerializedName("date")
                val date: String
            )
        }
    }
