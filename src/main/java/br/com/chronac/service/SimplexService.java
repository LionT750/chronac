package br.com.chronac.service;
import org.springframework.stereotype.Service;
import br.com.chronac.domain.Timetable;
import br.com.chronac.gateway.SimplexGateway;

@Service
public class SimplexService {
    

    private final SimplexGateway gateway;

    public SimplexService(SimplexGateway gateway) {
        this.gateway = gateway;
    }

    public Timetable requestTimetable(){
        return gateway.getTimetable();
    }

    
}
