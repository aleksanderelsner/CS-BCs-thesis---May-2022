package com.example.virtualpetapp.model

import java.io.Serializable
import java.time.LocalDate

enum class PetClass {
    DOG, CAT
}

data class Pet(
    var name: String,
    var age: Int,
    var classification: PetClass,
    var money: Int,
    var hunger: Float,
    var bladder: Float,
    var energy: Float,
    var hygiene: Float,
    var loneliness: Float,
    var dateCreated: LocalDate
) : Serializable
