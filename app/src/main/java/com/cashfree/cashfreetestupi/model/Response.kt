package com.cashfree.cashfreetestupi.model

import com.google.gson.annotations.SerializedName

data class SimulateRequest(
    @SerializedName("entity")
    val entity: String,
    @SerializedName("entity_id")
    val entityId: String,
    @SerializedName("entity_simulation")
    val entitySimulation: EntitySimulation,
)

data class EntitySimulation(
    @SerializedName("payment_status")
    val paymentStatus: String,
    @SerializedName("payment_error_code")
    val paymentErrorCode: String? = null,
)


data class SimulateResponse(
    @SerializedName("simulation_id")
    val simulationId: String,
    @SerializedName("entity")
    val entity: String,
    @SerializedName("entity_id")
    val entityId: String
)

