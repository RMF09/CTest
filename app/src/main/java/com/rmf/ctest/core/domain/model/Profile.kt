package com.rmf.ctest.core.domain.model

import android.os.Parcelable
import com.rmf.ctest.core.data.dto.ProfileDto
import kotlinx.parcelize.Parcelize

@Parcelize
data class Profile(
    val email: String,
    val name: String,
    val image: String? = null
): Parcelable

fun ProfileDto.toProfile() =
    Profile(
        email = email?: "-",
        name = name ?: "Nama tidak diketahui",
        image = image
    )
