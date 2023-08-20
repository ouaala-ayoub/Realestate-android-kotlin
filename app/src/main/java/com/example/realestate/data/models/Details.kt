package com.example.realestate.data.models

import com.google.gson.annotations.SerializedName

data class Details(

    @SerializedName("propertyCondition") val propertyCondition: String,
    @SerializedName("numberOfRooms") val numberOfRooms: Int,
    @SerializedName("numberOfBathrooms") val numberOfBathrooms: Int,
    @SerializedName("numberOfFloors") val numberOfFloors: Int,
    @SerializedName("floorNumber") val floorNumber: Int,
    @SerializedName("space") val space: Int,
    @SerializedName("isFurnished") val isFurnished: Boolean,
    @SerializedName("hasBalcony") val hasBalcony: Boolean,
    @SerializedName("isNewProperty") val isNewProperty: Boolean,
    @SerializedName("hasSwimmingPool") val hasSwimmingPool: Boolean,
    @SerializedName("hasGym") val hasGym: Boolean,
    @SerializedName("hasParking") val hasParking: Boolean,
    @SerializedName("hasElevator") val hasElevator: Boolean,
    @SerializedName("hasSecurity") val hasSecurity: Boolean


) {

    fun getAvailableDetails(): List<Pair<String, Any>> {
        val validDetails = mutableListOf<Pair<String, Any>>()

        if (propertyCondition.isNotEmpty()) {
            validDetails.add("Property Condition" to propertyCondition)
        }

        validDetails.add("Number of rooms" to numberOfRooms)
        validDetails.add("Number of bathrooms" to numberOfBathrooms)
        validDetails.add("Number of floors in the building" to numberOfFloors)
        validDetails.add("Floor Number" to floorNumber)
        validDetails.add("Space" to space)

        if (hasBalcony) {
            validDetails.add("Balcony" to hasBalcony)
        }

        if (isFurnished) {
            validDetails.add("Furnished" to isFurnished)
        }

        if (isNewProperty) {
            validDetails.add("New" to isNewProperty)
        }

        if (hasElevator) {
            validDetails.add("Elevator" to hasElevator)
        }

        if (hasSecurity) {
            validDetails.add("Security" to hasSecurity)
        }

        if (hasGym) {
            validDetails.add("Gym" to hasGym)
        }

        if (hasSwimmingPool) {
            validDetails.add("Swimming Pool" to hasSwimmingPool)
        }

        if (hasParking) {
            validDetails.add("Parking" to hasParking)
        }

        return validDetails
    }

    fun getShortDetails(): MutableList<Pair<String, Any>> {
        val validDetails = mutableListOf<Pair<String, Any>>()
        if (propertyCondition.isNotEmpty()) {
            validDetails.add("Property Condition" to propertyCondition)
        }

        validDetails.add("numberOfRooms" to numberOfRooms)
        validDetails.add("numberOfBathrooms" to numberOfBathrooms)
        validDetails.add("numberOfFloors" to numberOfFloors)
        validDetails.add("floorNumber" to floorNumber)
        validDetails.add("space" to space)

        if (isFurnished) {
            validDetails.add("Furnished" to isFurnished)
        }
        if (hasBalcony) {
            validDetails.add("Balcony" to hasBalcony)
        }
        if (isNewProperty) {
            validDetails.add("New" to isNewProperty)
        }
        if (hasGym) {
            validDetails.add("Gym" to hasGym)
        }
        if (hasSwimmingPool) {
            validDetails.add("Swimming Pool" to hasSwimmingPool)
        }
        if (hasParking) {
            validDetails.add("Parking" to hasParking)
        }
        if (space.toDouble() > 0) {
            validDetails.add("Space" to "$space m²")
        }

        return validDetails
    }
}
