import java.time.LocalDate;
import java.util.List;

public class Semester {
    private List<LocalDate> validClassDays;

    public Semester(List<LocalDate> validClassDays) {
        this.validClassDays = validClassDays;
    }

    public List<LocalDate> getValidClassDays() {
        return validClassDays;
    }
}
