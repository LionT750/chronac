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
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * A teacher, including their own schedule restrictions (formerly the separate
 * Timefold-only TeacherSchedule fact): weekdays they never teach on, and specific
 * dates they are unavailable.
 */
@Entity
@Table(name = "teacher")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String email;

    private String phone;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "teacher_invalid_weekday", joinColumns = @JoinColumn(name = "teacher_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private Set<DayOfWeek> invalidDayOfWeeks = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "teacher_unavailable_date", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "unavailable_date", nullable = false)
    private Set<LocalDate> specificUnavailableDates = new HashSet<>();

    public Teacher() {
    }

    public Teacher(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Set<DayOfWeek> getInvalidDayOfWeeks() {
        return invalidDayOfWeeks;
    }

    public void setInvalidDayOfWeeks(Set<DayOfWeek> invalidDayOfWeeks) {
        this.invalidDayOfWeeks = invalidDayOfWeeks == null ? new HashSet<>() : new HashSet<>(invalidDayOfWeeks);
    }

    public Set<LocalDate> getSpecificUnavailableDates() {
        return specificUnavailableDates;
    }

    public void setSpecificUnavailableDates(Set<LocalDate> specificUnavailableDates) {
        this.specificUnavailableDates = specificUnavailableDates == null
                ? new HashSet<>()
                : new HashSet<>(specificUnavailableDates);
    }
}
