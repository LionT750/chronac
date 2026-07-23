package org.acme.schooltimetabling.domain;

import java.util.Map;
import java.util.List;

public class Curriculum {

    public Map<String, Subject> subjects;
    
    Curriculum() {
        this.subjects = Map.of(
            "OOP", new Subject("OOP", 96, "Alisson",  List.of("Sala 114")),
            "Algoritmos", new Subject("Algoritmos", 108, "Rodolfo", List.of("Sala 114")),
            "Banco de dados", new Subject("Banco de dados", 72, "Nelma",List.of("Sala 114")),
            "Requisitos", new Subject("Requisitos", 108, "Vanessa", List.of("Sala 114"))
        );
    }
}
