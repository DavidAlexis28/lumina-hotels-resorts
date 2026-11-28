package com.lumina.luminahotels.domain.validation

import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.regex.Pattern

/**
 * Utilidad pura de validaciones para Lumina Hotels & Resorts.
 * Diseñada para ser desacoplada de Android Framework y 100% testeable en JVM.
 */
object FormValidator {

    // RFC 5322 regex simplificada y robusta para correos electrónicos
    private val EMAIL_REGEX = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    )

    // Solo letras, acentos, diéresis, espacios y guiones para nombres
    private val NAME_REGEX = Pattern.compile(
        "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s'-]+$"
    )

    // Números de teléfono: opcional + inicial, entre 7 y 15 dígitos con separadores opcionales
    private val PHONE_DIGITS_REGEX = Pattern.compile("^\\+?[0-9]{7,15}$")

    // Documento de identidad (DNI, Pasaporte, DUI): 6 a 20 caracteres alfanuméricos con guiones opcionales
    private val DOC_REGEX = Pattern.compile("^[a-zA-Z0-9-]{6,20}$")

    // Número de habitación: alfanumérico corto (ej. "101", "PH-A")
    private val ROOM_NUMBER_REGEX = Pattern.compile("^[a-zA-Z0-9-]{1,10}$")

    /**
     * Sanitiza una cadena eliminando espacios redundantes y caracteres de control.
     */
    fun sanitize(input: String?): String {
        if (input == null) return ""
        return input.trim().replace("\\s+".toRegex(), " ")
    }

    /**
     * Valida nombres y apellidos de personas.
     */
    fun validateName(rawName: String?, fieldName: String = "El nombre"): ValidationResult {
        val sanitized = sanitize(rawName)
        if (sanitized.isBlank()) {
            return ValidationResult.Invalid("$fieldName no puede estar vacío.")
        }
        if (sanitized.length < 2) {
            return ValidationResult.Invalid("$fieldName debe contener al menos 2 caracteres.")
        }
        if (sanitized.length > 60) {
            return ValidationResult.Invalid("$fieldName no puede exceder 60 caracteres.")
        }
        if (!NAME_REGEX.matcher(sanitized).matches()) {
            return ValidationResult.Invalid("$fieldName solo puede contener letras y espacios válidos.")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida dirección de correo electrónico según estándar RFC.
     */
    fun validateEmail(rawEmail: String?): ValidationResult {
        val email = sanitize(rawEmail)
        if (email.isBlank()) {
            return ValidationResult.Invalid("El correo electrónico es obligatorio.")
        }
        if (email.length > 100) {
            return ValidationResult.Invalid("El correo electrónico no puede exceder 100 caracteres.")
        }
        if (!EMAIL_REGEX.matcher(email).matches()) {
            return ValidationResult.Invalid("Ingrese un formato de correo válido (ej. usuario@dominio.com).")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida números de teléfono (locales e internacionales).
     */
    fun validatePhone(rawPhone: String?, isRequired: Boolean = false): ValidationResult {
        val phone = sanitize(rawPhone)
        if (phone.isBlank()) {
            return if (isRequired) {
                ValidationResult.Invalid("El número de teléfono es obligatorio.")
            } else {
                ValidationResult.Valid
            }
        }
        val digitsOnly = phone.replace("[\\s-]".toRegex(), "")
        if (!PHONE_DIGITS_REGEX.matcher(digitsOnly).matches()) {
            return ValidationResult.Invalid("Ingrese un número de teléfono válido (7 a 15 dígitos numéricos).")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida documento de identidad (DNI / Pasaporte / DUI).
     */
    fun validateDocumentId(rawDoc: String?): ValidationResult {
        val doc = sanitize(rawDoc)
        if (doc.isBlank()) {
            return ValidationResult.Invalid("El documento de identidad es obligatorio.")
        }
        if (doc.length < 6 || doc.length > 20) {
            return ValidationResult.Invalid("El documento debe tener entre 6 y 20 caracteres.")
        }
        if (!DOC_REGEX.matcher(doc).matches()) {
            return ValidationResult.Invalid("El documento contiene caracteres no permitidos.")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida el número de habitación.
     */
    fun validateRoomNumber(rawNumber: String?): ValidationResult {
        val num = sanitize(rawNumber)
        if (num.isBlank()) {
            return ValidationResult.Invalid("El número de habitación es obligatorio.")
        }
        if (!ROOM_NUMBER_REGEX.matcher(num).matches()) {
            return ValidationResult.Invalid("Número de habitación inválido (1-10 caracteres alfanuméricos).")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida el piso de una habitación.
     */
    fun validateFloor(rawFloor: String?): ValidationResult {
        val sanitized = sanitize(rawFloor)
        if (sanitized.isBlank()) {
            return ValidationResult.Invalid("El número de piso es obligatorio.")
        }
        val floor = sanitized.toIntOrNull()
            ?: return ValidationResult.Invalid("El piso debe ser un número entero válido.")

        if (floor <= 0 || floor > 99) {
            return ValidationResult.Invalid("El piso debe estar entre 1 y 99.")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida la capacidad de huéspedes de una habitación.
     */
    fun validateCapacity(rawCapacity: String?): ValidationResult {
        val sanitized = sanitize(rawCapacity)
        if (sanitized.isBlank()) {
            return ValidationResult.Invalid("La capacidad es obligatoria.")
        }
        val cap = sanitized.toIntOrNull()
            ?: return ValidationResult.Invalid("La capacidad debe ser un número entero.")

        if (cap <= 0 || cap > 20) {
            return ValidationResult.Invalid("La capacidad debe ser entre 1 y 20 huéspedes.")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida el precio por noche con formato decimal monetario.
     */
    fun validatePrice(rawPrice: String?): ValidationResult {
        val sanitized = sanitize(rawPrice).replace(",", ".")
        if (sanitized.isBlank()) {
            return ValidationResult.Invalid("El precio por noche es obligatorio.")
        }
        val price = try {
            BigDecimal(sanitized)
        } catch (_: NumberFormatException) {
            return ValidationResult.Invalid("Formato de precio inválido (ej. 115.00).")
        }

        if (price <= BigDecimal.ZERO) {
            return ValidationResult.Invalid("El precio debe ser un valor positivo mayor a 0.")
        }
        if (price > BigDecimal("99999.99")) {
            return ValidationResult.Invalid("El precio excede el límite permitido ($99,999.99).")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida las fechas de reserva asegurando formato ISO-8601 y orden cronológico.
     *
     * @param entradaStr Fecha de entrada en formato YYYY-MM-DD
     * @param salidaStr Fecha de salida en formato YYYY-MM-DD
     * @param allowPastDates Si es true (ej. para historial o edición), permite fechas pasadas
     */
    fun validateDates(
        entradaStr: String?,
        salidaStr: String?,
        allowPastDates: Boolean = false,
        today: LocalDate = LocalDate.now()
    ): ValidationResult {
        val entradaClean = sanitize(entradaStr)
        val salidaClean = sanitize(salidaStr)

        if (entradaClean.isBlank() || salidaClean.isBlank()) {
            return ValidationResult.Invalid("Las fechas de entrada y salida son obligatorias.")
        }

        val entrada = try {
            LocalDate.parse(entradaClean, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: DateTimeParseException) {
            return ValidationResult.Invalid("Formato de fecha de entrada inválido. Use AAAA-MM-DD.")
        }

        val salida = try {
            LocalDate.parse(salidaClean, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: DateTimeParseException) {
            return ValidationResult.Invalid("Formato de fecha de salida inválido. Use AAAA-MM-DD.")
        }

        if (!allowPastDates && entrada.isBefore(today)) {
            return ValidationResult.Invalid("La fecha de entrada no puede ser anterior a hoy.")
        }

        if (!salida.isAfter(entrada)) {
            return ValidationResult.Invalid("La fecha de salida debe ser posterior a la fecha de entrada.")
        }

        return ValidationResult.Valid
    }

    /**
     * Valida la cantidad de huéspedes para una reserva.
     */
    fun validateGuests(rawGuests: String?, maxCapacity: Int? = null): ValidationResult {
        val sanitized = sanitize(rawGuests)
        if (sanitized.isBlank()) {
            return ValidationResult.Invalid("La cantidad de huéspedes es obligatoria.")
        }
        val guests = sanitized.toIntOrNull()
            ?: return ValidationResult.Invalid("Ingrese un número entero válido de huéspedes.")

        if (guests <= 0) {
            return ValidationResult.Invalid("La cantidad de huéspedes debe ser mayor que cero.")
        }
        if (maxCapacity != null && guests > maxCapacity) {
            return ValidationResult.Invalid("Los huéspedes ($guests) exceden la capacidad máxima ($maxCapacity).")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida contraseña para inicio de sesión.
     */
    fun validatePassword(rawPassword: String?): ValidationResult {
        if (rawPassword.isNullOrBlank()) {
            return ValidationResult.Invalid("La contraseña es obligatoria.")
        }
        if (rawPassword.length < 6) {
            return ValidationResult.Invalid("La contraseña debe tener al menos 6 caracteres.")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida número de tarjeta de crédito/débito (15 o 16 dígitos numéricos).
     */
    fun validateCardNumber(rawCard: String?): ValidationResult {
        val sanitized = sanitize(rawCard).replace("[\\s-]".toRegex(), "")
        if (sanitized.isBlank()) {
            return ValidationResult.Invalid("El número de tarjeta es obligatorio.")
        }
        if (!sanitized.matches("^[0-9]{15,16}$".toRegex())) {
            return ValidationResult.Invalid("Ingrese un número de tarjeta válido (15 o 16 dígitos).")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida fecha de expiración de tarjeta en formato MM/AA.
     */
    fun validateCardExpiry(rawExpiry: String?): ValidationResult {
        val sanitized = sanitize(rawExpiry).replace(" ", "")
        if (sanitized.isBlank()) {
            return ValidationResult.Invalid("La fecha de vencimiento es obligatoria.")
        }
        val match = "^(0[1-9]|1[0-2])/([0-9]{2})$".toRegex().matchEntire(sanitized)
            ?: return ValidationResult.Invalid("Formato inválido. Use MM/AA (ej. 08/28).")

        val month = match.groupValues[1].toInt()
        val year = 2000 + match.groupValues[2].toInt()
        val currentYear = LocalDate.now().year
        val currentMonth = LocalDate.now().monthValue

        if (year < currentYear || (year == currentYear && month < currentMonth)) {
            return ValidationResult.Invalid("La tarjeta se encuentra vencida.")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida código de seguridad CVV (3 o 4 dígitos).
     */
    fun validateCardCvv(rawCvv: String?): ValidationResult {
        val sanitized = sanitize(rawCvv)
        if (sanitized.isBlank()) {
            return ValidationResult.Invalid("El código CVV es obligatorio.")
        }
        if (!sanitized.matches("^[0-9]{3,4}$".toRegex())) {
            return ValidationResult.Invalid("CVV inválido (3 o 4 dígitos).")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida número de pasaporte (6 a 12 caracteres alfanuméricos, mayúsculas).
     */
    fun validatePassportNumber(rawPassport: String?): ValidationResult {
        val passport = sanitize(rawPassport).uppercase()
        if (passport.isBlank()) {
            return ValidationResult.Invalid("El número de pasaporte es obligatorio.")
        }
        if (passport.length < 6 || passport.length > 12) {
            return ValidationResult.Invalid("El pasaporte debe tener entre 6 y 12 caracteres.")
        }
        if (!passport.matches("^[A-Z0-9]{6,12}$".toRegex())) {
            return ValidationResult.Invalid("El pasaporte solo puede contener letras y números.")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida dirección de residencia.
     */
    fun validateAddress(rawAddress: String?): ValidationResult {
        val address = sanitize(rawAddress)
        if (address.isBlank()) {
            return ValidationResult.Invalid("La dirección es obligatoria.")
        }
        if (address.length < 5) {
            return ValidationResult.Invalid("La dirección debe contener al menos 5 caracteres.")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida fecha de nacimiento (DD/MM/AAAA, no en el futuro y mayor de 18 años).
     */
    fun validateBirthDate(rawDate: String?, today: LocalDate = LocalDate.now()): ValidationResult {
        val dateStr = sanitize(rawDate)
        if (dateStr.isBlank()) {
            return ValidationResult.Invalid("La fecha de nacimiento es obligatoria.")
        }
        val date = try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (_: Exception) {
            return ValidationResult.Invalid("Formato de fecha inválido. Use DD/MM/AAAA.")
        }
        if (date.isAfter(today)) {
            return ValidationResult.Invalid("La fecha de nacimiento no puede ser futura.")
        }
        val eighteenYearsAgo = today.minusYears(18)
        if (date.isAfter(eighteenYearsAgo)) {
            return ValidationResult.Invalid("Debe tener al menos 18 años de edad.")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida fecha de vencimiento de pasaporte (DD/MM/AAAA, no en el pasado).
     */
    fun validatePassportExpiry(rawDate: String?, today: LocalDate = LocalDate.now()): ValidationResult {
        val dateStr = sanitize(rawDate)
        if (dateStr.isBlank()) {
            return ValidationResult.Invalid("La fecha de vencimiento del pasaporte es obligatoria.")
        }
        val date = try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (_: Exception) {
            return ValidationResult.Invalid("Formato de fecha inválido. Use DD/MM/AAAA.")
        }
        if (date.isBefore(today)) {
            return ValidationResult.Invalid("El pasaporte se encuentra vencido.")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida la capacidad de cupos para atracciones turísticas (entre 8 y 15 cupos).
     */
    fun validateMinibusCapacity(capacity: Int): ValidationResult {
        if (capacity < 8 || capacity > 15) {
            return ValidationResult.Invalid("La capacidad debe estar entre 8 y 15 cupos.")
        }
        return ValidationResult.Valid
    }
}
