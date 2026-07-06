
import java.time.LocalDate;
import java.util.List;

public interface CalendarParser {
    List<LocalDate> parse(String filePath) throws Exception;
}

