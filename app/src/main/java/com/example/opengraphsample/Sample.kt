package com.example.opengraphsample

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.util.*

fun getRandomName(array: Array<String>) : String = array[Random().nextInt(array.size)]
fun main() {
    val a = arrayOf("강종필", "이재원", "정일현")
    val b = arrayOf("천재", "멍청이", "병신")
    for(idx in a.indices)
        println(getRandomName(a).plus(" ${getRandomName(b)}"))

        val dt = DT(28, getRandomName(a))
        val dtt = DTT(28, getRandomName(a))
}

data class DT(
    val age: Int,
    val name: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.let {
            it.apply {
                writeInt(age)
                writeString(name)
            }
        }
    }

    companion object CREATOR : Parcelable.Creator<DT> {
        override fun createFromParcel(parcel: Parcel): DT {
            return DT(parcel)
        }

        override fun newArray(size: Int): Array<DT?> {
            return arrayOfNulls(size)
        }
    }
}

@Parcelize
data class DTT(
    val age: Int,
    val name: String
) : Parcelable

data class SDT(
    val age: Int,
    val name: String
) : Serializable