package com.example.globalbit.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

//note:
//for the purposes of this exercise, i assume all fields are non-null
@JsonIgnoreProperties(ignoreUnknown = true)
class Country(@JsonProperty("name")
              val _name: String,
              @JsonProperty("nativeName")
              val _nativeName: String,
              @JsonProperty("flag")
              val _flag: String,
              @JsonProperty("capital")
              val _capital: String,
              @JsonProperty("borders")
              val _borders: List<String>)
    : Serializable