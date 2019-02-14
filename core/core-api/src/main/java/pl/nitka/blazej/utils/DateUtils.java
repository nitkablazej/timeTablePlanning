package pl.nitka.blazej.utils;

import pl.nitka.blazej.entity.employee.Vacation;
import pl.nitka.blazej.entity.employee.WorkTime;
import pl.nitka.blazej.enums.CompletedStudies;
import pl.nitka.blazej.enums.FreeStaticDays;
import pl.nitka.blazej.enums.MonthNames;
import pl.nitka.blazej.temporary.FreeDaysForMonth;
import pl.nitka.blazej.temporary.PreviousWorksScope;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Klasa dostarczająca statyczne metody operujące na datach.
 *
 * @author Blazej
 */
public final class DateUtils {

    public static final int FULL_VACATION = 26; // Liczba pełnych dni urlopowych
    public static final int NOT_FULL_VACATION = 20; // Liczba początkowych dni urlopowych
    public static final int DAYS_IN_YEAR = 365; // Liczba dni w roku
    private static final int DAYS_IN_MONTH = 30; // Liczba dni w miesiącu
    private static final int DAYS_TO_FULL_VACATION = 3650; // Liczba dni do osiągnięcia 26 dni urlopu
    private static final String DATE_PATTERN = "dd.MM.yyyy"; // Wzorzec do formatowania daty
    private static final String DATETIME_FILE_PATTERN = "yyyyMMddHHmmss";
    private static final String ONLY_TIME = "HH:mm";

    private DateUtils() {
        // Konstruktor domyślny
    }

    /**
     * Metoda formatująca datę na tekst.
     *
     * @param date - data
     * @return - tekstowa data
     */
    public static String formatDateToCalendar(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
        return simpleDateFormat.format(date);
    }

    /**
     * Wyliczenie daty, od której przysługuje 26 dni urlopowych.
     *
     * @param workTime - przepracowany okres przed rejestracją w systemie
     * @return - data, od której przysługuje 26 dni urlopu
     */
    public static Date getFullVacationDate(WorkTime workTime) {
        // Wyliczenie wszystkich przepracowanych dni
        int workedDays = workTime.getYears() * DAYS_IN_YEAR;
        workedDays += workTime.getMonths() * DAYS_IN_MONTH;
        workedDays += workTime.getDays();

        // Obliczenie dni pozostałych do osiągnięcia 26 dni urlopu
        int daysToFullVacation = DAYS_TO_FULL_VACATION - workedDays;

        // Pracownikowi już przysługuje 26 dni urlopu (przed rejestracją)
        if (daysToFullVacation < 0) {
            return DateUtils.getBeginOfWorldDate();
        }

        // Utworzenie daty, od której przysługuje 26 dni urlopu
        Calendar fullVacationDate = new GregorianCalendar();
        fullVacationDate.setTime(new Date());
        fullVacationDate.add(Calendar.DAY_OF_YEAR, daysToFullVacation);

        return fullVacationDate.getTime();
    }

    /**
     * Metoda obliczająca ilość dni pracujących z przekazanego zakresu dat.
     *
     * @param dateFrom - data początkowa
     * @param dateTo   - data końcowa
     * @return - ilość dni pracujących w zakresie
     */
    public static int getWorkingDaysNumberFromScope(Date dateFrom, Date dateTo) {
        // Obliczenie różnicy pomiędzy datami
        int differentBetweenDates = (int) getDifferentBetweenDates(dateFrom, dateTo);
        int workingDaysFromScope = 0;

        // Ustawienie daty początkowej
        Calendar calendarDate = Calendar.getInstance();
        calendarDate.setTime(dateFrom);

        // Iteracja po wszystkich dniach ze sprawdzeniem, czy dzień jest dniem wolnym
        for (int i = 0; i <= differentBetweenDates; i++) {
            if (!isFreeDay(calendarDate)) {
                workingDaysFromScope++;
            }
            calendarDate.add(Calendar.DAY_OF_YEAR, 1);
        }

        return workingDaysFromScope;
    }

    /**
     * Metoda zwracająca listę dat z przekazanego zakresu.
     *
     * @param dateFrom - data początkowa
     * @param dateTo   - data końcowa
     * @return - lista dat z zakresu
     */
    public static List<Calendar> getDaysFromScope(Date dateFrom, Date dateTo) {
        // Obliczenie różnicy pomiędzy datami
        int differentBetweenDates = (int) getDifferentBetweenDates(dateFrom, dateTo);
        List<Calendar> resultList = new ArrayList<>();

        // Iteracja po wszystkich datach i dodanie ich do listy
        for (int i = 0; i <= differentBetweenDates; i++) {
            Calendar date = Calendar.getInstance();
            date.setTime(dateFrom);
            date.add(Calendar.DAY_OF_YEAR, i);
            resultList.add(date);
        }

        return resultList;
    }

    /**
     * Metoda przyjmująca zakres dat oraz listę urlopów, sprawdzająca, czy któraś z dat nie pokrywa się z urlopem.
     *
     * @param newVacationScope - zakres dodawanego urlopu
     * @param vacationList     - lista urlopów do porównania
     * @return - true, jeśli przynajmniej jeden dzień z zakresu pokrywa się z jednym dniem z urlopu
     */
    public static boolean isScopesOverlap(List<Calendar> newVacationScope, List<Vacation> vacationList) {
        // Iteracja po wszystkich pobranych urlopach
        for (Vacation vacation : vacationList) {
            int different = (int) DateUtils.getDifferentBetweenDates(vacation.getDateFromVacation(), vacation.getDateToVacation());
            Calendar firstVacationDate = Calendar.getInstance();
            firstVacationDate.setTime(vacation.getDateFromVacation());

            // Iteracja po wszystkich dniach z danego zakresu urlopu
            for (int i = 0; i <= different; i++) {

                // Sprawdzenie każdej daty z nowego zakresu z przekazanymi urlopami
                for (Calendar date : newVacationScope) {
                    if (compareDates(date, firstVacationDate)) {
                        return true;
                    }
                }
                firstVacationDate.add(Calendar.DAY_OF_YEAR, 1);
            }
        }
        return false;
    }

    /**
     * Metoda tworząca obiekt reprezentujący przepracowany czas przed rejestracją z uwzględnieniem ukończonej szkoły.
     *
     * @param completedStudies    - ukończona szkoła
     * @param previousWorksScopes - poprzednie okresy pracy
     * @param employmentDate - data zatrudnienia
     * @return - obiekt reprezentujący przepracowany czas
     */
    public static WorkTime createWorkTimeFromSchoolAndWorksScopes(CompletedStudies completedStudies, List<PreviousWorksScope> previousWorksScopes, Date employmentDate) {
        // Zbiór wszystkich przepracowanych dni
        Set<Date> workDaysSet = new HashSet<>();

        // Utworzenie nowej listy i dodanie dodatkowego poprzedniego okresu zatrudnienia jako zatrudnienia w firmie
        List<PreviousWorksScope> finalPreviousWorkScope = new ArrayList<>(previousWorksScopes);
        finalPreviousWorkScope.add(new PreviousWorksScope(employmentDate, new Date()));

        // Iteracja po zakresach
        for (PreviousWorksScope previousWorksScope : finalPreviousWorkScope) {
            // Utworzenie daty początkowej i końcowej zakresu
            Calendar startDate = DateUtils.createStartDate(previousWorksScope.getDateFrom());
            Calendar endTime = DateUtils.createEndDate(previousWorksScope.getDateTo());

            // Lista przepracowanych dni z zakresu
            List<Date> workDates = new ArrayList<>();

            // Iteracja po datach z zakresu, dodanie ich do listy dat
            while (startDate.getTime().before(endTime.getTime())) {
                workDates.add(startDate.getTime());
                startDate.add(Calendar.DATE, 1);
            }

            // Przpisanie listy z zakresu do zbioru dat ze wszystkich zakresów
            workDaysSet.addAll(workDates);
        }

        // Lista dat utworzona ze zbioru (aby móc sortować)
        List<Date> workDaysList = new ArrayList<>(workDaysSet);
        Collections.sort(workDaysList);

        // Lista dat z utworzonego zakresu zagregowanych dat
        List<Date> eachScopeDates = new ArrayList<>();
        List<TempWorkTime> tempWorkTimeList = new ArrayList<>();
        for (int i = 0; i < workDaysList.size(); i++) {
            Date thisDay = workDaysList.get(i);
            eachScopeDates.add(thisDay);

            // Ostatni element tablicy
            if (i + 1 == workDaysList.size()) {
                tempWorkTimeList.add(createTempWorkTimeFromScope(eachScopeDates));
            }
            // Są jeszcze elementy w tablicy, sprawdzenie, czy kolejna data jest rzeczywiście kolejną w kalendarzu
            else if (!DateUtils.isNextDay(thisDay, workDaysList.get(i + 1))) {
                tempWorkTimeList.add(createTempWorkTimeFromScope(eachScopeDates));
                eachScopeDates = new ArrayList<>();
            }
        }

        // Utworzenie przepracowanego okresu i dodanie lat na podstawie ukończonej szkoły
        WorkTime workTime = DateUtils.createWorkTimeFromTempScopes(tempWorkTimeList);
        workTime.addYears(completedStudies == null ? 0 : completedStudies.getLearningTime());

        return workTime;
    }

    /**
     * Metoda porównująca dwie daty wg roku, miesiąca i dnia.
     *
     * @param date1 - pierwsza data
     * @param date2 - druga data
     * @return - true, jeśli daty są takie same
     */
    public static boolean compareDates(Calendar date1, Calendar date2) {
        return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
                date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Data początku świata :)
     *
     * @return - bardzo stara data
     */
    public static Date getBeginOfWorldDate() {
        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
        return cal.getTime();
    }

    /**
     * Metoda sprawdzająca, czy dany dzień przypada w weekend.
     *
     * @param date - sprawdzany dzień
     * @return - true, jeśli weekend
     */
    public static boolean isWeekend(Calendar date) {
        // Utworzenie zmiennych, które mówią o dniu i miesiącu danej daty
        final int dayOfWeekFromDate = date.get(Calendar.DAY_OF_WEEK);

        // Sprawdzenie, czy dany dzień nie wypada w weekend
        return dayOfWeekFromDate == Calendar.SATURDAY || dayOfWeekFromDate == Calendar.SUNDAY;
    }

    /**
     * Metoda sprawdzająca, czy dany dzień wypada w święto.
     *
     * @param date - sprawdzany dzień
     * @return - true, jeśli święto
     */
    public static boolean isHoliday(Calendar date) {
        final int dayOfMonthFromDate = date.get(Calendar.DAY_OF_MONTH);
        final int monthOfYearFromDate = date.get(Calendar.MONTH);

        // Sprawdzenie, czy dzień nie wypada w któreś ze świąt ze stałą datą
        for (FreeStaticDays staticFreeDay : FreeStaticDays.values()) {
            int freeDayOfMonth = staticFreeDay.getDay();
            int freeMonthOfYear = staticFreeDay.getMonth();
            if (freeDayOfMonth == dayOfMonthFromDate && freeMonthOfYear == monthOfYearFromDate) {
                return true;
            }
        }

        // Wielkanoc
        Calendar easterDate = createEasterDate(date.get(Calendar.YEAR));
        if (dayOfMonthFromDate == easterDate.get(Calendar.DAY_OF_MONTH) && monthOfYearFromDate == easterDate.get(Calendar.MONTH)) {
            return true;
        }

        // Poniedziałek wielkanocny
        Calendar easterMonday = Calendar.getInstance();
        easterMonday.setTime(easterDate.getTime());
        easterMonday.add(Calendar.DAY_OF_MONTH, 1);
        if (dayOfMonthFromDate == easterMonday.get(Calendar.DAY_OF_MONTH) && monthOfYearFromDate == easterMonday.get(Calendar.MONTH)) {
            return true;
        }

        // Boże Ciało występuje 60 dni po Niedzieli Wielkanocnej
        Calendar corpusCristiDate = Calendar.getInstance();
        corpusCristiDate.setTime(easterDate.getTime());
        corpusCristiDate.add(Calendar.DAY_OF_MONTH, 60);
        return dayOfMonthFromDate == corpusCristiDate.get(Calendar.DAY_OF_MONTH) && monthOfYearFromDate == corpusCristiDate.get(Calendar.MONTH);
    }

    /**
     * Metoda zwracająca różnicę dni, jaka występuje pomiędzy dwiema datami.
     *
     * @param date1 - data początkowa
     * @param date2 - data końcowa
     * @return - różnica dni pomiędzy datami
     */
    public static long getDifferentBetweenDates(Date date1, Date date2) {
        long diff = date2.getTime() - date1.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    /**
     * Metoda zwraca polską nazwę miesiąca
     *
     * @param today - dzień
     * @return - tekstowa nazwa miesiąca
     */
    public static String getTextMonthName(Calendar today) {
        int month = today.get(Calendar.MONTH);
        MonthNames[] monthNames = {MonthNames.JANUARY, MonthNames.FEBRUARY, MonthNames.MARCH, MonthNames.APRIL, MonthNames.MAY,
                MonthNames.JUNE, MonthNames.JULY, MonthNames.AUGUST, MonthNames.SEPTEMBER, MonthNames.OCTOBER,
                MonthNames.NOVEMBER, MonthNames.DECEMBER};
        return monthNames[month].getTranslate();
    }

    /**
     * Metoda zwracająca datę jako sformatowany tekst do nazwy pliku.
     *
     * @param date - data
     * @return - data jako tekst
     */
    public static String formatDateTimeFile(Date date) {
        return new SimpleDateFormat(DATETIME_FILE_PATTERN).format(date);
    }

    /**
     * Metoda zwracająca koniec przekazanego dnia.
     *
     * @param date - dzień
     * @return - dzień z ustawioną końcową godziną
     */
    public static Date getDayEnd(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * Metoda zwracająca początek przekazanego dnia.
     *
     * @param date - dzień
     * @return - dzień z ustawioną początkową godziną
     */
    public static Date getDayBegin(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Metoda zwracająca tekstową reprezentację okresu pierwszej zmiany.
     *
     * @param startWorkTime - godzina rozpoczęcia pierwszej zmiany
     * @return - tekstowy okres pierwszej zmiany
     */
    public static String getWorkingHoursFirstChange(Date startWorkTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ONLY_TIME);
        String startTime = simpleDateFormat.format(startWorkTime);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startWorkTime);
        calendar.add(Calendar.HOUR, 8);
        String endTime = simpleDateFormat.format(calendar.getTime());

        return String.format("%s - %s", startTime, endTime);
    }

    /**
     * Metoda zwracająca tekstową reprezentację okresu drugiej zmiany.
     *
     * @param startWorkTime - godzina rozpoczęcia pierwszej zmiany
     * @return - tekstowy okres drugiej zmiany
     */
    public static String getWorkingHoursSecondChange(Date startWorkTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ONLY_TIME);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startWorkTime);
        calendar.add(Calendar.HOUR_OF_DAY, 8);
        String startTime = simpleDateFormat.format(calendar.getTime());

        calendar.add(Calendar.HOUR, 8);
        String endTime = simpleDateFormat.format(calendar.getTime());

        return String.format("%s - %s", startTime, endTime);
    }

    /**
     * Metoda parsująca miesiąc i rok na odpowiedni ciąg tekstowy.
     *
     * @param generatedMonth - generowany miesiąc
     * @param generatedYear - generowany rok
     * @return - tekstowy ciąg
     */
    public static String parseMonthAndYear(int generatedMonth, int generatedYear) {
        return String.format("%s.%s", generatedMonth, generatedYear);
    }

    /**
     * Metoda zwracająca listę wszystkich dni wolnych w danym miesiącu.
     *
     * @param selectedMonth - wybrany miesiąc
     * @param selectedYear - wybrany rok
     * @return - lista świąt w danym miesiącu
     */
    public static List<FreeDaysForMonth> generateAllFreeDaysForMonth(int selectedMonth, int selectedYear) {
        List<FreeDaysForMonth> resultList = new ArrayList<>();

        Arrays.stream(FreeStaticDays.values()).filter(freeDay -> freeDay.getMonth() == selectedMonth)
                .forEach(freeDay -> resultList.add(new FreeDaysForMonth(freeDay.getMonth() + 1, freeDay.getDay(), freeDay.getTranslate())));

        // Wielkanoc
        Calendar easterDate = DateUtils.createEasterDate(selectedYear);
        if (easterDate.get(Calendar.MONTH) == selectedMonth) {
            resultList.add(new FreeDaysForMonth(selectedMonth + 1, easterDate.get(Calendar.DAY_OF_MONTH), "Wielkanoc"));
        }

        // Poniedziałek wielkanocny
        Calendar easterMonday = Calendar.getInstance();
        easterMonday.setTime(easterDate.getTime());
        easterMonday.add(Calendar.DAY_OF_MONTH, 1);
        if (easterMonday.get(Calendar.MONTH) == selectedMonth) {
            resultList.add(new FreeDaysForMonth(selectedMonth + 1, easterMonday.get(Calendar.DAY_OF_MONTH), "Poniedziałek Wielkanocny"));
        }

        // Boże ciało
        Calendar corpusCristiDate = Calendar.getInstance();
        corpusCristiDate.setTime(easterDate.getTime());
        corpusCristiDate.add(Calendar.DAY_OF_MONTH, 60);
        if (corpusCristiDate.get(Calendar.MONTH) == selectedMonth) {
            resultList.add(new FreeDaysForMonth(selectedMonth + 1, corpusCristiDate.get(Calendar.DAY_OF_MONTH), "Boże Ciało"));
        }

        return resultList;
    }

    /**
     * Metoda obliczająca datę Wielkanocy dla wskazanego roku (algorytm Meeusa-Jonesa-Butchera).
     *
     * @param year - rok, dla którego obliczana jest data
     * @return - data Wielkanocy w danym roku
     */
    private static Calendar createEasterDate(int year) {
        final int a = year % 19;
        final int b = year / 100;
        final int c = year % 100;
        final int d = b / 4;
        final int e = b % 4;
        final int f = ((b + 8) / 25);
        final int g = ((b - f + 1) / 3);
        final int h = (19 * a + b - d - g + 15) % 30;
        final int i = c / 4;
        final int k = c % 4;
        final int l = (32 + 2 * e + 2 * i - h - k) % 7;
        final int m = ((a + 11 * h + 22 * l) / 451);
        final int p = (h + l - 7 * m + 114) % 31;

        // Obliczenie dnia i miesiąca
        int day = p + 1;
        int month = (h + l - 7 * m + 114) / 31;

        // Utworzenie daty
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);

        return cal;

    }
    /**
     * Metoda zerująca czas w podanej dacie (na początek dnia).
     *
     * @param date - data
     * @return - data z wyzerowanym czasem
     */
    private static Calendar createStartDate(Date date) {
        Calendar startDate = new GregorianCalendar();
        startDate.setTime(date);
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);
        return startDate;
    }

    /**
     * Metoda ustawiająca ostatnią możliwą godzinę (czas) w podanej dacie.
     *
     * @param date - data
     * @return - data z przypisanem czasem końca dnia
     */
    private static Calendar createEndDate(Date date) {
        Calendar endTime = new GregorianCalendar();
        endTime.setTime(date);
        endTime.set(Calendar.HOUR_OF_DAY, 23);
        endTime.set(Calendar.MINUTE, 59);
        endTime.set(Calendar.SECOND, 59);
        endTime.set(Calendar.MILLISECOND, 999);
        return endTime;
    }

    /**
     * Metoda obliczająca przepracowany czas w latach, miesiącach i dniach na podstawie przepracowanych okresów.
     *
     * @param allTempWorkTimeScopes - lista zagregowanych przepracowanych okresów przed rejestracją
     * @return - obiekt reprezentujący okres przepracowany przed rejestracją
     */
    private static WorkTime createWorkTimeFromTempScopes(List<TempWorkTime> allTempWorkTimeScopes) {
        int years = 0;
        int months = 0;
        int days = 0;

        for (TempWorkTime workTimeScope : allTempWorkTimeScopes) {
            years += workTimeScope.getYears();
            months += workTimeScope.getMonths();
            days += workTimeScope.getDays();
        }

        if (days >= 30) {
            months += days / 30;
            days = days % 30;
        }

        if (months >= 12) {
            years += months / 12;
            months = months % 12;
        }

        return new WorkTime(years, months, days);
    }

    /**
     * Metoda sprawdzająca, czy dany dzień jest dniem wolnym od pracy.
     *
     * @param date - sprawdzany dzień
     * @return - true, jeśli dzień wypada w weekend lub jest świętem wolnym od pracy
     */
    private static boolean isFreeDay(Calendar date) {
        return isWeekend(date) || isHoliday(date);
    }

    /**
     * Metoda tworząca obiekt z latami, miesiącami i dniami przepracowanego okresu.
     *
     * @param eachScopeDates - zakres dat
     * @return - przepracowany czas
     */
    private static TempWorkTime createTempWorkTimeFromScope(List<Date> eachScopeDates) {
        Date startDate = Collections.min(eachScopeDates);
        Date endDate = Collections.max(eachScopeDates);
        return createTempWorkTimeFromDates(startDate, endDate);
    }

    public static TempWorkTime createTempWorkTimeFromDates(Date startDate, Date endDate) {
        Calendar startDateWorkspace = Calendar.getInstance();
        Calendar endDateWorkspace = Calendar.getInstance();
        startDateWorkspace.setTime(startDate);
        endDateWorkspace.setTime(endDate);

        int years = 0;
        int months = 0;

        while (startDateWorkspace.before(endDateWorkspace)) {
            startDateWorkspace.add(Calendar.YEAR, 1);
            if (startDateWorkspace.before(endDateWorkspace)) {
                years++;
            } else {
                startDateWorkspace.add(Calendar.YEAR, -1);
                break;
            }
        }

        while (startDateWorkspace.before(endDateWorkspace)) {
            startDateWorkspace.add(Calendar.MONTH, 1);
            if (startDateWorkspace.before(endDateWorkspace)) {
                months++;
            } else {
                startDateWorkspace.add(Calendar.MONTH, -1);
                break;
            }
        }

        long days = getDifferentBetweenDates(startDateWorkspace.getTime(), endDateWorkspace.getTime());
        days++; // Różnica + 1 dzień, ponieważ pierwszy z obliczanych też był przepracowany
        if (days >= 30) {
            days = 0;
            months++;
        }

        if (months == 12) {
            years++;
            months = 0;
        }

        return new TempWorkTime(years, months, (int) days);
    }

    /**
     * Sprawdzenie, czy podana data jest następną w kalendarzu.
     *
     * @param firstDate  - data, do której następuje porównanie
     * @param secondDate - data porównywana
     * @return - true, jeśli druga data jest kolejną w kalendarzu
     */
    private static boolean isNextDay(Date firstDate, Date secondDate) {
        Calendar firstDateCalendar = Calendar.getInstance();
        Calendar secondDateCalendar = Calendar.getInstance();

        firstDateCalendar.setTime(firstDate);
        secondDateCalendar.setTime(secondDate);

        firstDateCalendar.add(Calendar.DAY_OF_YEAR, 1);

        return firstDateCalendar.get(Calendar.YEAR) == secondDateCalendar.get(Calendar.YEAR) &&
                firstDateCalendar.get(Calendar.MONTH) == secondDateCalendar.get(Calendar.MONTH) &&
                firstDateCalendar.get(Calendar.DAY_OF_MONTH) == secondDateCalendar.get(Calendar.DAY_OF_MONTH);
    }
}
