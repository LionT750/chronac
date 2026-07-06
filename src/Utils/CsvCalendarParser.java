import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvCalendarParser implements CalendarParser {

    @Override
    public List<LocalDate> parse(String filePath) throws Exception {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            return lines
                    .skip(1) // Ignora o cabeçalho "date"
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(line -> LocalDate.parse(line, formatter))
                    .collect(Collectors.toList());
        }
    }
}