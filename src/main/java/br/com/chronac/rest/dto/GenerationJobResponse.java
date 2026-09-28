package br.com.chronac.rest.dto;

import br.com.chronac.domain.GenerationStatus;

public record GenerationJobResponse(String jobId, GenerationStatus status) {
}
