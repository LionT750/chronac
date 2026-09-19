package br.com.chronac.rest.dto;

import br.com.chronac.domain.Turno;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

public record TurmaRequest(
        String name,
        Turno turno,
        Integer capacity,
        String roomName,
        Set<DayOfWeek> validWeekdays,
        List<Long> teacherIds) {
}
