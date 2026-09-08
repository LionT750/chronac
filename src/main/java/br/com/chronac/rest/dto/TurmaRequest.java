package br.com.chronac.rest.dto;

import br.com.chronac.domain.Turno;

import java.util.List;

public record TurmaRequest(
        String name,
        Turno turno,
        Integer capacity,
        List<Long> teacherIds) {
}
