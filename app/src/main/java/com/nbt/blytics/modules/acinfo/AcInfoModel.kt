package com.nbt.blytics.modules.acinfo

data class AcInfoRequest(
    val user_id: String, val user_token: String
)

data class AcInfoResponse(
    val status: String, val list: List<Data>
) {
    data class Data(
        val acc_type: String = "",
        val acc_holder_name: String = "",
        val acc_no: String = "",
        val acc_uuid: String = "",
        val bank_code: String = "",
        val amount: String = "",
        val create_by: String = "",
        val create_for: String = "",
        val relation: String = "",
        val accid: String = "",
        val acc_name: String = ""
    )
}