package com.nbt.blytics.utils

import android.os.Parcel
import android.os.Parcelable
import com.google.android.material.datepicker.CalendarConstraints.DateValidator


/**
 * Created bynbton 09-07-2021
 */
internal class RangeValidator : DateValidator {
    var minDate: Long
    var maxDate: Long

    constructor(minDate: Long, maxDate: Long) {
        this.minDate = minDate
        this.maxDate = maxDate
    }

    constructor(parcel: Parcel) {
        minDate = parcel.readLong()
        maxDate = parcel.readLong()
    }

    override fun isValid(date: Long): Boolean {
        return !(minDate > date || maxDate < date)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(minDate)
        dest.writeLong(maxDate)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<RangeValidator?> =
            object : Parcelable.Creator<RangeValidator?> {
                override fun createFromParcel(parcel: Parcel): RangeValidator? {
                    return RangeValidator(parcel)
                }

                override fun newArray(size: Int): Array<RangeValidator?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
