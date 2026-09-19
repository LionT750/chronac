package br.com.chronac.gateway;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.chronac.domain.Timetable;

@Component 
public class SimplexGateway {
    
    private final RestClient restClient = RestClient.create();

    public Timetable getTimetable() {
        return restClient.get()
            .uri("http://localhost:3000")
            .retrieve()
            .body(Timetable.class);
    }

}
