package com.mfa.api.response

import com.google.gson.annotations.SerializedName

data class HadirResponse(
	@field:SerializedName("hadir")
	val hadir: Int? = null
)
