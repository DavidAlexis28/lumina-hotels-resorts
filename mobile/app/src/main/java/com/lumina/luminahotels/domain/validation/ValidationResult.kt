package com.lumina.luminahotels.domain.validation

/**
 * Representa el resultado de una validación de campo en Lumina Hotels.
 * Permite tipado estricto y desacoplado de la interfaz de usuario.
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errorMessage: String) : ValidationResult()

    val isValid: Boolean get() = this is Valid
    val errorMessageOrNull: String? get() = (this as? Invalid)?.errorMessage
}
