package br.com.chronac.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

/**
 * A turma (class / student group / curso), e.g. "Tecnico em Desenvolvimento de
 * Sistemas" or "Jovem Programador". Each turma is designed for one room and
 * has its own set of valid weekdays for classes; which subject and how many
 * hours each teacher gives is future work (Materia/Aula persistence), not
 * modeled here yet.
 */
@Entity
@Table(name = "turma")
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Turno turno;

    private Integer capacity;

    private String roomName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "turma_valid_weekday", joinColumns = @JoinColumn(name = "turma_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private Set<DayOfWeek> validWeekdays = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "turma_teacher",
            joinColumns = @JoinColumn(name = "turma_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id"))
    private Set<Teacher> teachers = new HashSet<>();

    public Turma() {
    }

    public Turma(String name, Turno turno, Integer capacity) {
        this.name = name;
        this.turno = turno;
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Set<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(Set<Teacher> teachers) {
        this.teachers = teachers == null ? new HashSet<>() : new HashSet<>(teachers);
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Set<DayOfWeek> getValidWeekdays() {
        return validWeekdays;
    }

    public void setValidWeekdays(Set<DayOfWeek> validWeekdays) {
        this.validWeekdays = validWeekdays == null ? new HashSet<>() : new HashSet<>(validWeekdays);
    }
}
