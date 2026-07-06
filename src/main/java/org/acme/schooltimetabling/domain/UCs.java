package org.acme.schooltimetabling.domain;

import java.util.Map;

public class UCs {

    public Map<String, Subject> subjects;
    
    UCs() {
        this.subjects = Map.of(
            "OOP", new Subject("OOP", 96, "Alisson"),
            "Algoritmos", new Subject("Algoritmos", 108, "Rodolfo"),
            "Banco de dados", new Subject("Banco de dados", 72, "Nelma"),
            "Requisitos", new Subject("Requisitos", 108, "Vanessa")
        );
    }
}
