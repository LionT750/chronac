package br.com.chronac.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to start a schedule generation.
 *
 * <p>Today the only metadata needed is the demo identifier, which selects the
 * same demo problem solved at server startup. In the future this DTO will carry
 * the full data set required to build a real planning problem (courses,
 * teachers, rooms, semester window, solver parameters, ...).
 */
public record TimetableGenerateRequest(@NotBlank String demo) {
}
