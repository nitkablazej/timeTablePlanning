package pl.nitka.blazej.view.timetable;

import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import pl.nitka.blazej.manager.TimeTableManager;
import pl.nitka.blazej.temporary.FreeDaysForMonth;
import pl.nitka.blazej.temporary.GeneratedMonth;
import pl.nitka.blazej.utils.DateUtils;
import pl.nitka.blazej.enums.EmployeeIdentifiers;
import pl.nitka.blazej.enums.WorkingHolidayDayName;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Bean obsługujący ekran generowania harmonogramu.
 *
 * @author Blazej
 */
@Named
@ViewScoped
public class TimeTableGeneratePageService implements Serializable {

    /**
     * Liczba miesięcy w przód, z których będzie możliwość wygenerowania harmonogramu.
     */
    private static final int AVAILABLE_MONTHS = 6;

    @EJB
    private TimeTableManager timeTableManagerBean;

    /**
     * Obiekt harmonogramu.
     */
    private StreamedContent generatedTimetableFile;

    /**
     * Wybrany miesiąc, na który będzie generowany grafik.
     */
    @Getter
    private GeneratedMonth selectedMonthToGenerate;

    /**
     * Sobota lub niedziela oznaczona jako pracujący dzień.
     */
    @Getter
    @Setter
    private WorkingHolidayDayName workingHolidayDayName;

    /**
     * Informacja, czy w grafiku mają być uwzględniane imiona i nazwiska czy może identyfikatory.
     */
    @Getter
    @Setter
    private EmployeeIdentifiers employeeIdentifiers;

    /**
     * Lista wybranych dni świątecznych pracujących dodatkowo.
     */
    @Getter
    @Setter
    private List<FreeDaysForMonth> selectedFreeDaysToWork;

    /**
     * Flaga określająca, czy użytkownik uruchomił opcję zaznaczania weekendowych dni pracujących.
     */
    @Getter
    @Setter
    private boolean workingHoliday;

    /**
     * Liczba zmian w trakcie dnia.
     */
    @Getter
    @Setter
    private boolean oneChangesOnDay;

    /**
     * Minimalna godzina rozpoczęcia pierwszej zmiany.
     */
    @Getter
    @Setter
    private int startTimeMinHour;

    /**
     * Maksymalna godzina rozpoczęcia pierwszej zmiany.
     */
    @Getter
    @Setter
    private int startTimeMaxHour;

    /**
     * Godzina rozpoczęcia pierwszej zmiany
     */
    @Getter
    @Setter
    private Date startWorkTime;

    /**
     * Widoczność panelu z możliwością pobrania grafiku.
     */
    @Setter
    private boolean renderedTimeTableDownloadPanel;

    /**
     * Nazwa wygenerowanego grafiku
     */
    @Getter
    private String generatedTimeTableName;

    @PostConstruct
    public void init() {
        selectedFreeDaysToWork = new ArrayList<>();
        selectedMonthToGenerate = getAllAvailableMonths().get(0);  // Domyślne przypisanie pierwszego miesiąca z listy
        employeeIdentifiers = EmployeeIdentifiers.FIRST_AND_LAST_NAME;
        oneChangesOnDay = true;
        renderedTimeTableDownloadPanel = false;
        setStartWorkMinAndMaxHour();
        Calendar startWorkTimeCalendar = Calendar.getInstance();
        startWorkTimeCalendar.set(Calendar.HOUR_OF_DAY, startTimeMinHour);
        startWorkTimeCalendar.set(Calendar.MINUTE, 0);
        startWorkTime = startWorkTimeCalendar.getTime();
    }

    /**
     * Metoda wywołująca generowanie harmonogramu.
     */
    public void generateTimetable() {
        Date generatedDate = new Date();

        // Utworzenie nazwy pliku
        String fileName = String.format("Harmonogram_%s_%s_%s.xlsx", selectedMonthToGenerate.getMonthTranslate(),
                selectedMonthToGenerate.getYear(), DateUtils.formatDateTimeFile(generatedDate));

        // Generowanie harmonogramu
        InputStream generatedTimeTable = timeTableManagerBean.generateTimeTable(selectedMonthToGenerate, workingHolidayDayName,
                selectedFreeDaysToWork, employeeIdentifiers, oneChangesOnDay, startWorkTime, fileName, generatedDate);

        // Zwrócenie harmonogramu na ekran, jeśli został wygenerowany
        if (Objects.nonNull(generatedTimeTable)) {
            generatedTimetableFile = new DefaultStreamedContent(generatedTimeTable, "application/vnd.ms-excel", fileName);
            renderedTimeTableDownloadPanel = true;
            generatedTimeTableName = fileName;
            String header = "Wygenerowano harmonogram";
            String message = "Plik można pobrać pod przyciskiem lub w zakładce \"Lista grafików\"";

            // Informacja, że do miesiąca pozostało więcej niż 10 dni
            if (greaterThanTenDaysToTimeTable(selectedMonthToGenerate)) {
                String greaterThanTenDaysMessage = "Uwaga! Do wybranego miesiąca pozostało co najmniej 10 dni, co oznacza, że pracownicy mogą planować urlopy na dany okres!";
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, greaterThanTenDaysMessage, null));
            }

            PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage(FacesMessage.SEVERITY_INFO, header, message));
        }
    }

    /**
     * Metoda sprawdzająca, czy do generowanego miesiąca pozostało >= 10 dni.
     *
     * @param selectedMonthToGenerate - wybrany miesiąc do generowania
     * @return - true, jeśli prawda
     */
    private boolean greaterThanTenDaysToTimeTable(GeneratedMonth selectedMonthToGenerate) {
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        today.set(Calendar.HOUR, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        today.add(Calendar.DAY_OF_YEAR, 10);

        Calendar firstDayFromTimeTable = Calendar.getInstance();
        firstDayFromTimeTable.setTime(new Date());
        firstDayFromTimeTable.set(Calendar.YEAR, selectedMonthToGenerate.getYear());
        firstDayFromTimeTable.set(Calendar.MONTH, selectedMonthToGenerate.getMonthNumber());
        firstDayFromTimeTable.set(Calendar.DAY_OF_MONTH, 1);
        firstDayFromTimeTable.set(Calendar.HOUR, 0);
        firstDayFromTimeTable.set(Calendar.MINUTE, 0);
        firstDayFromTimeTable.set(Calendar.SECOND, 0);
        firstDayFromTimeTable.set(Calendar.MILLISECOND, 0);

        return today.before(firstDayFromTimeTable);
    }

    /**
     * Ustawienie maksymalnej i minimalnej godziny rozpoczęcia zmiany w zależności od liczby zmian.
     */
    public void setStartWorkMinAndMaxHour() {
        if (oneChangesOnDay) {
            startTimeMinHour = 5;
            startTimeMaxHour = 12;
        } else {
            startTimeMinHour = 5;
            startTimeMaxHour = 7;
        }
    }

    /**
     * Metoda zwraca 6 kolejnych miesięcy do wygenerowania grafiku.
     *
     * @return - lista 6 miesięcy
     */
    public List<GeneratedMonth> getAllAvailableMonths() {
        List<GeneratedMonth> resultList = new ArrayList<>();

        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        today.add(Calendar.MONTH, 1);

        for (int i = 0; i < AVAILABLE_MONTHS; i++) {
            resultList.add(new GeneratedMonth(today.get(Calendar.MONTH), today.get(Calendar.YEAR), DateUtils.getTextMonthName(today)));
            today.add(Calendar.MONTH, 1);
        }

        return resultList;
    }

    /** Dodanie komunikatu po wybraniu pracujących weekendów.
     */
    public void workingHolidayMessage() {
        workingHolidayDayName = null;
        if (workingHoliday) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Pracujące weekendy: Uwzględnieni zostaną tylko pracownicy ze zgodą na nadgodziny", null));
        }
    }

    /**
     * Czyszczenie wybranych świąt po zmianie miesiąca.
     */
    public void clearFreeDays() {
        this.selectedFreeDaysToWork = new ArrayList<>();
    }

    /**
     * Pobieranie wszystkich świątecznych dni na wybrany do generowania miesiąc.
     *
     * @return - lista świąt dla danego miesiąca
     */
    public List<FreeDaysForMonth> getAllFreeDaysForSelectedMonth() {
        List<FreeDaysForMonth> resultList = new ArrayList<>();
        if (Objects.nonNull(selectedMonthToGenerate)) {
            int selectedMonth = this.selectedMonthToGenerate.getMonthNumber();
            int selectedYear = this.selectedMonthToGenerate.getYear();

            resultList = DateUtils.generateAllFreeDaysForMonth(selectedMonth, selectedYear);
        }

        return resultList;
    }

    /**
     * Konwersja tekstu na obiekt generowanego miesiąca.
     *
     * @param itemLabel - etykieta z GUI
     * @return - obiekt miesiąca do generowania
     */
    public GeneratedMonth convertGeneratedMonth(String itemLabel) {
        for (GeneratedMonth availableMonth : getAllAvailableMonths()) {
            if (itemLabel.equals(availableMonth.getLabel())) {
                selectedMonthToGenerate = availableMonth;
                return availableMonth;
            }
        }
        selectedMonthToGenerate = null;
        return null;
    }

    /**
     * Konwersja tekstowego dnia wolnego do obiektu.
     *
     * @param itemLabel - etykieta z GUI
     * @return - obiekt wolnego dnia
     */
    public FreeDaysForMonth convertFreeDay(String itemLabel) {
        for (FreeDaysForMonth freeDaysForMonth : getAllFreeDaysForSelectedMonth()) {
            if (itemLabel.equals(freeDaysForMonth.getLabel())) {
                return freeDaysForMonth;
            }
        }
        return null;
    }

    public StreamedContent getGeneratedTimetableFile() {
        renderedTimeTableDownloadPanel = false;
        generatedTimeTableName = "";
        return generatedTimetableFile;
    }

    public void setSelectedMonthToGenerate(GeneratedMonth selectedMonthToGenerate) {
        this.selectedMonthToGenerate = selectedMonthToGenerate;
    }

    public boolean isRenderedTimeTableDownloadPanel() {
        return renderedTimeTableDownloadPanel;
    }
}
