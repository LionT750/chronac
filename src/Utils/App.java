import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class App {
    public static void main(String[] args) {
        try {

       

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            
            CalendarParser lePlanilhaECriaUmaLista = new CsvCalendarParser();
           List<LocalDate> datas = lePlanilhaECriaUmaLista.parse("src\\calendario.csv");
            Semester semestre = new Semester(datas);
            
            System.out.println("Sucesso! Dias carregados: " + semestre.getValidClassDays().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}