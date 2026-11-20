package com.lumina.luminahotels

import com.lumina.luminahotels.domain.validation.FormValidator
import com.lumina.luminahotels.domain.validation.ValidationResult
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class FormValidatorTest {

    @Test
    fun `validateName - nombres validos e invalidos`() {
        assertTrue(FormValidator.validateName("Carlos Alberto").isValid)
        assertTrue(FormValidator.validateName("María José Núñez").isValid)
        assertTrue(FormValidator.validateName("Jean-Luc O'Connor").isValid)

        assertFalse(FormValidator.validateName("").isValid)
        assertFalse(FormValidator.validateName("   ").isValid)
        assertFalse(FormValidator.validateName("A").isValid) // muy corto
        assertFalse(FormValidator.validateName("Carlos123").isValid) // números
        assertFalse(FormValidator.validateName("Carlos <script>").isValid) // script injection
    }

    @Test
    fun `validateEmail - formatos RFC correctos e incorrectos`() {
        assertTrue(FormValidator.validateEmail("recepcion@luminahotels.com").isValid)
        assertTrue(FormValidator.validateEmail("juan.perez+reservas@empresa.com.sv").isValid)

        assertFalse(FormValidator.validateEmail("").isValid)
        assertFalse(FormValidator.validateEmail("correo_sin_arroba.com").isValid)
        assertFalse(FormValidator.validateEmail("usuario@").isValid)
        assertFalse(FormValidator.validateEmail("@dominio.com").isValid)
        assertFalse(FormValidator.validateEmail("usuario@dominio").isValid)
    }

    @Test
    fun `validatePhone - telefonos validos e invalidos`() {
        assertTrue(FormValidator.validatePhone(null, isRequired = false).isValid)
        assertTrue(FormValidator.validatePhone("", isRequired = false).isValid)
        assertFalse(FormValidator.validatePhone("", isRequired = true).isValid)

        assertTrue(FormValidator.validatePhone("7123-4567").isValid)
        assertTrue(FormValidator.validatePhone("+503 2234-5678").isValid)
        assertTrue(FormValidator.validatePhone("+15551234567").isValid)

        assertFalse(FormValidator.validatePhone("123").isValid) // muy corto
        assertFalse(FormValidator.validatePhone("abc-1234").isValid)
    }

    @Test
    fun `validateDocumentId - DUI, pasaporte y cédulas`() {
        assertTrue(FormValidator.validateDocumentId("05123456-7").isValid)
        assertTrue(FormValidator.validateDocumentId("P12345678").isValid)

        assertFalse(FormValidator.validateDocumentId("").isValid)
        assertFalse(FormValidator.validateDocumentId("123").isValid) // corto
        assertFalse(FormValidator.validateDocumentId("DOC#1234$").isValid) // caracteres prohibidos
    }

    @Test
    fun `validateRoomNumber - numeros y letras de habitacion`() {
        assertTrue(FormValidator.validateRoomNumber("101").isValid)
        assertTrue(FormValidator.validateRoomNumber("302-A").isValid)
        assertTrue(FormValidator.validateRoomNumber("PH").isValid)

        assertFalse(FormValidator.validateRoomNumber("").isValid)
        assertFalse(FormValidator.validateRoomNumber("HABITACION_LARGA_12345").isValid)
    }

    @Test
    fun `validateFloor - pisos validos y limites`() {
        assertTrue(FormValidator.validateFloor("1").isValid)
        assertTrue(FormValidator.validateFloor("15").isValid)

        assertFalse(FormValidator.validateFloor("0").isValid)
        assertFalse(FormValidator.validateFloor("-2").isValid)
        assertFalse(FormValidator.validateFloor("abc").isValid)
        assertFalse(FormValidator.validateFloor("150").isValid) // límite 99
    }

    @Test
    fun `validateCapacity - capacidades permitidas`() {
        assertTrue(FormValidator.validateCapacity("1").isValid)
        assertTrue(FormValidator.validateCapacity("4").isValid)

        assertFalse(FormValidator.validateCapacity("0").isValid)
        assertFalse(FormValidator.validateCapacity("-1").isValid)
        assertFalse(FormValidator.validateCapacity("25").isValid) // límite 20
        assertFalse(FormValidator.validateCapacity("dos").isValid)
    }

    @Test
    fun `validatePrice - precios y redondeo seguro`() {
        assertTrue(FormValidator.validatePrice("115.00").isValid)
        assertTrue(FormValidator.validatePrice("175").isValid)
        assertTrue(FormValidator.validatePrice("310,50").isValid) // soporte coma decimal

        assertFalse(FormValidator.validatePrice("").isValid)
        assertFalse(FormValidator.validatePrice("0").isValid)
        assertFalse(FormValidator.validatePrice("-50.0").isValid)
        assertFalse(FormValidator.validatePrice("gratis").isValid)
        assertFalse(FormValidator.validatePrice("100000.00").isValid) // excede límite
    }

    @Test
    fun `validateDates - coherencia cronologica y formato ISO`() {
        val today = LocalDate.of(2026, 11, 20)
        val entrada = "2026-11-21"
        val salida = "2026-11-24"

        assertTrue(FormValidator.validateDates(entrada, salida, allowPastDates = false, today = today).isValid)

        // Fecha de salida igual o anterior a entrada
        val resMismaFecha = FormValidator.validateDates(entrada, entrada, allowPastDates = false, today = today)
        assertFalse(resMismaFecha.isValid)
        assertEquals("La fecha de salida debe ser posterior a la fecha de entrada.", (resMismaFecha as ValidationResult.Invalid).errorMessage)

        val resSalidaAntes = FormValidator.validateDates("2026-11-25", "2026-11-22", allowPastDates = false, today = today)
        assertFalse(resSalidaAntes.isValid)

        // Fecha en el pasado
        val resPasado = FormValidator.validateDates("2026-11-10", "2026-11-15", allowPastDates = false, today = today)
        assertFalse(resPasado.isValid)
        assertEquals("La fecha de entrada no puede ser anterior a hoy.", (resPasado as ValidationResult.Invalid).errorMessage)

        // Formatos malformados
        assertFalse(FormValidator.validateDates("21/11/2026", "24/11/2026", today = today).isValid)
    }

    @Test
    fun `validateGuests - cantidad y capacidad de habitacion`() {
        assertTrue(FormValidator.validateGuests("2", maxCapacity = 4).isValid)
        assertTrue(FormValidator.validateGuests("4", maxCapacity = 4).isValid)

        assertFalse(FormValidator.validateGuests("0", maxCapacity = 4).isValid)
        assertFalse(FormValidator.validateGuests("5", maxCapacity = 4).isValid)
        assertFalse(FormValidator.validateGuests("abc", maxCapacity = 4).isValid)
    }

    @Test
    fun `validateCard - tarjeta vencimiento y cvv`() {
        assertTrue(FormValidator.validateCardNumber("4532 1234 5678 9012").isValid)
        assertTrue(FormValidator.validateCardNumber("4532123456789012").isValid)
        assertFalse(FormValidator.validateCardNumber("").isValid)
        assertFalse(FormValidator.validateCardNumber("1234").isValid)

        assertTrue(FormValidator.validateCardExpiry("12/29").isValid)
        assertFalse(FormValidator.validateCardExpiry("").isValid)
        assertFalse(FormValidator.validateCardExpiry("15/28").isValid) // mes inválido
        assertFalse(FormValidator.validateCardExpiry("01/20").isValid) // año pasado

        assertTrue(FormValidator.validateCardCvv("123").isValid)
        assertTrue(FormValidator.validateCardCvv("1234").isValid)
        assertFalse(FormValidator.validateCardCvv("").isValid)
        assertFalse(FormValidator.validateCardCvv("12").isValid)
        assertFalse(FormValidator.validateCardCvv("12345").isValid)
    }
}
