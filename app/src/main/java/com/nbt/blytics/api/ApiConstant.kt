package com.nbt.blytics.api


class ApiConstant {

    companion object {
        const val DEVICE_TYPE = 1
        const val HEADER_CONTENT_TYPE_JSON = "Content-Type:application/json"
        const val HEADER_AUTHORIZATION = "Authorization"
        const val BEARER = "Bearer "

        const val clientId: Int = 5
        const val clientSecret: String = ""
        const val grantTypePassword: String = "password"
        const val grantTypeRefreshToken: String = "refresh_token"

        const val SORT_DATE_DESC = "published_date-desc"
        const val SORT_DATE_ASC = "published_date-asc"

        const val PROMOTION_TYPE = "promotion"
        const val PROGRAM_TYPE = "program"

        const val SORT_PUBLISHED_AT = "published_at"
        const val SORT_EXPIRES_AT = "expires_at"
    }
}