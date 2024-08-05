package com.mfa.api.request

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class KodeJadwalRequest(
    val qrCodeData: String,
    val kodeJadwal: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(qrCodeData)
        parcel.writeString(kodeJadwal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<KodeJadwalRequest> {
        override fun createFromParcel(parcel: Parcel): KodeJadwalRequest {
            return KodeJadwalRequest(parcel)
        }

        override fun newArray(size: Int): Array<KodeJadwalRequest?> {
            return arrayOfNulls(size)
        }
    }
}
