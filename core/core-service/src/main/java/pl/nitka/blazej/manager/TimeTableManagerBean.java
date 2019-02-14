package pl.nitka.blazej.manager;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pl.nitka.blazej.entity.TimeTableFile;
import pl.nitka.blazej.entity.employee.Employee;
import pl.nitka.blazej.entity.employee.LastExitFromWork;
import pl.nitka.blazej.entity.employee.Vacation;
import pl.nitka.blazej.enums.EmployeeIdentifiers;
import pl.nitka.blazej.enums.WorkingHolidayDayName;
import pl.nitka.blazej.manager.timetable.Day;
import pl.nitka.blazej.manager.timetable.DayType;
import pl.nitka.blazej.manager.timetable.WorkingEmployee;
import pl.nitka.blazej.manager.timetable.XlsxFileUtils;
import pl.nitka.blazej.temporary.FreeDaysForMonth;
import pl.nitka.blazej.temporary.GeneratedMonth;
import pl.nitka.blazej.utils.DateUtils;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Bean z logiką generowania harmonogramu.
 *
 * @author Blazej
 */
@Stateless
public class TimeTableManagerBean implements TimeTableManager {

    private static final Logger LOGGER = Logger.getLogger(TimeTableManagerBean.class);

    @EJB
    private EmployeeManager employeeManagerBean;

    @EJB
    private LastExitFromWorkManagerBean lastExitFromWorkManagerBean;

    @EJB
    private TimeTableFileManager timeTableFileManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream generateTimeTable(GeneratedMonth generatedMonth, WorkingHolidayDayName workingHolidayName,
                                         List<FreeDaysForMonth> selectedFreeDaysToWork, EmployeeIdentifiers employeeIdentifiers,
                                         boolean oneChangesOnDay, Date startWorkTime, String fileName, Date generatedDate) {
        // Harmonogram jako strumień zwracany
        InputStream byteArrayInputStream = null;

        try {
            // Otworzenie pliku template i wczytanie arkusza harmonogramu i pracowników
            InputStream templateFile = TimeTableManager.class.getClassLoader().getResourceAsStream("META-INF/timeTableTemplate.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(templateFile);
            XSSFSheet timeTableSheet = workbook.getSheetAt(0); // Arkusz do harmonogramu
            XSSFSheet summarySheet = workbook.getSheetAt(1); // Arkusz do podsumowania pracowników

            // Dodanie nagłówka z nazwą miesiąca nad tabelą
            XSSFRow row = timeTableSheet.getRow(1);
            XSSFCell cell = row.getCell(2);
            String changesOnDay = oneChangesOnDay ? "1 ZMIANA" : "2 ZMIANY";
            cell.setCellValue(String.format("%s - %s", generatedMonth.getLabel().toUpperCase(), changesOnDay));

            // Utworzenie potrzbnych styli
            CellStyle datesCellStyle = XlsxFileUtils.createDatesCellStyle(workbook);
            CellStyle normalCellStyle = XlsxFileUtils.createNormalCellStyle(workbook);
            CellStyle weekendCellStyle = XlsxFileUtils.createWeekendCellStyle(workbook);
            CellStyle holidayCellStyle = XlsxFileUtils.createHolidayCellStyle(workbook);
            CellStyle workingHoursCellStyle = XlsxFileUtils.createWorkingHoursCellStyle(workbook);

            // Pobranie listy wszystkich pracowników
            List<Employee> allEmployees = employeeManagerBean.getAll();
            List<WorkingEmployee> workingEmployeeList = createWorkingEmployeeList(allEmployees, employeeIdentifiers, generatedMonth);

            // Utworzenie listy dni pracowniczych na dany miesiąc
            List<Day> allDaysInMonth = generateAllDaysInMonthList(generatedMonth);

            // Numer wiersza, od którego zaczyna się wypisywanie dat w arkuszu
            int startRow = 3;
            // Odstęp od lewej strony arkusza
            int cellMargin = 1;

            // Informacja, czy w sobotę przypadło jakieś święto (jeśli tak, to będzie informacja w grafiku)
            boolean saturdayHoliday = false;

            // Maksymalna liczba pracowników na zmianę wykorzystywana do ustawienia wysokości wiersza
            int maxEmployeeNumberOnChange = 0;

            // Generowanie harmonogramu jednozmianowego
            if (oneChangesOnDay) {
                // Utworzenie dwóch pierwszych wierszy do dat oraz numerów pracowników
                XSSFRow dayDatesRow = timeTableSheet.createRow(startRow++);
                XSSFRow employeeNumbersRow = timeTableSheet.createRow(startRow++);

                // Utworzenie komórek dla dat oraz listy numerów pracowników w danym dniu
                XSSFCell dayDateCell;
                XSSFCell employeeNumbersCell;

                // Iteracja po komórkach, jeśli są dniami tygodnia przed pierwszym dniem miesiąca i wypełnienie ich kreskami
                for (int i = 1; i < allDaysInMonth.get(0).getDayOfWeekNumber(); i++) {
                    // Wypełnienie komórki z datą
                    dayDateCell = dayDatesRow.createCell(i + cellMargin);
                    dayDateCell.setCellValue("-");
                    dayDateCell.setCellStyle(datesCellStyle);

                    // Wypełnienie komórki z pracownikiem
                    employeeNumbersCell = employeeNumbersRow.createCell(i + cellMargin);
                    employeeNumbersCell.setCellValue("-");
                    employeeNumbersCell.setCellStyle(normalCellStyle);
                }

                // Dodanie początkowej komórki z godzinami zmiany
                XSSFCell workingHoursCell = employeeNumbersRow.createCell(1);
                workingHoursCell.setCellStyle(workingHoursCellStyle);
                workingHoursCell.setCellValue(DateUtils.getWorkingHoursFirstChange(startWorkTime));

                // Iteracja po wszystkich dniach miesiąca
                for (Day dayInMonth : allDaysInMonth) {

                    // Obiekt przetwarzanego dnia miesiąca z czasem rozpoczęcia zmiany
                    Calendar calendarToday = Calendar.getInstance();
                    calendarToday.setTime(dayInMonth.getDate());
                    Calendar startWorkTimeCalendar = Calendar.getInstance();
                    startWorkTimeCalendar.setTime(startWorkTime);
                    calendarToday.set(Calendar.HOUR_OF_DAY, startWorkTimeCalendar.get(Calendar.HOUR_OF_DAY));
                    calendarToday.set(Calendar.MINUTE, startWorkTimeCalendar.get(Calendar.MINUTE));
                    calendarToday.set(Calendar.SECOND, 0);
                    calendarToday.set(Calendar.MILLISECOND, 0);

                    // Dodanie początkowej komórki z godzinami zmiany, jeśli dzień to poniedziałek
                    if (calendarToday.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                        workingHoursCell = employeeNumbersRow.createCell(1);
                        workingHoursCell.setCellStyle(workingHoursCellStyle);
                        workingHoursCell.setCellValue(DateUtils.getWorkingHoursFirstChange(startWorkTime));
                    }

                    // Ustawienie flagi informującej, że któraś z sobót była pracująca
                    if (calendarToday.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && DayType.FREE_DAY.equals(dayInMonth.getDayType())) {
                        saturdayHoliday = true;
                    }

                    // Wpisanie daty dnia do odpowiedniej komórki
                    dayDateCell = dayDatesRow.createCell(dayInMonth.getDayOfWeekNumber() + cellMargin);
                    dayDateCell.setCellValue(DateUtils.formatDateToCalendar(dayInMonth.getDate()));
                    dayDateCell.setCellStyle(datesCellStyle);

                    // Sprawdzenie, czy dany dzień ma być w firmie pracujący
                    if (isWorkingDay(dayInMonth, workingHolidayName, selectedFreeDaysToWork)) {

                        // Iteracja po wszystkich dostępnych pracownikach
                        for (WorkingEmployee workingEmployee : workingEmployeeList) {

                            // Dodanie ich do dnia, jeśli nie mają urlopu i dzień jest pracujący (chyba, że nie jest ale mają zgodę na nadgodziny)
                            if (!employeeHasVacationOnThisDate(workingEmployee, dayInMonth.getDate()) &&
                                    (dayInMonth.isWorkingDay() || (!dayInMonth.isWorkingDay() && workingEmployee.getEmployee().getOvertimeAgreement()))) {

                                // Cofnięcie czasu o 11 godzin w celu sprawdzenia przerwy dla pracowników
                                calendarToday.add(Calendar.HOUR_OF_DAY, -11);
                                Calendar lastExitFromWork = Calendar.getInstance();
                                lastExitFromWork.setTime(workingEmployee.getLastExitFromWork());

                                // Pracownik zostanie przypisany do zmiany tylko w przypadku, gdy ma ponad 11 godzin przerwy od ostatniego wyjścia z pracy
                                if (lastExitFromWork.before(calendarToday) || lastExitFromWork.equals(calendarToday)) {
                                    // Przywrócenie normalnej godziny pracy
                                    calendarToday.add(Calendar.HOUR_OF_DAY, 11);

                                    // Dodanie pracownika do dnia
                                    dayInMonth.addEmployeeNumberToFirstChange(workingEmployee.getNumber());

                                    // Przypisanie pracownikowi godziny wyjścia ze zmiany
                                    Calendar startWork = Calendar.getInstance();
                                    startWork.setTime(startWorkTime);
                                    startWork.add(Calendar.HOUR, 8);
                                    startWork.set(Calendar.MONTH, generatedMonth.getMonthNumber());
                                    startWork.set(Calendar.YEAR, generatedMonth.getYear());
                                    startWork.set(Calendar.DAY_OF_MONTH, calendarToday.get(Calendar.DAY_OF_MONTH));
                                    workingEmployee.setLastExitFromWork(startWork.getTime());

                                    // Przypisanie mu przepracowanych godzin w zależności, czy był to dzień roboczy czy nie
                                    if (dayInMonth.isWorkingDay()) {
                                        workingEmployee.addBasicWorkingHours();
                                    } else {
                                        workingEmployee.addAdditionalWorkingHours();
                                    }
                                } else {
                                    // Przywrócenie normalnej godziny pracy
                                    calendarToday.add(Calendar.HOUR_OF_DAY, 11);
                                }
                            }
                        }

                        // Uzupełnienie komórki z numerami pracowników
                        employeeNumbersCell = employeeNumbersRow.createCell(dayInMonth.getDayOfWeekNumber() + cellMargin);
                        employeeNumbersCell.setCellValue(Arrays.toString(dayInMonth.getFirstChangeEmployeesList().toArray()));

                    } else {
                        // Jeśli dzień jest w firmie dniem wolnym, to wpisywana jest kreska
                        employeeNumbersCell = employeeNumbersRow.createCell(dayInMonth.getDayOfWeekNumber() + cellMargin);
                        employeeNumbersCell.setCellValue("-");
                    }

                    // Kolorowanie komórki w zależności od dnia
                    switch (dayInMonth.getDayType()) {
                        case NORMAL: {
                            employeeNumbersCell.setCellStyle(normalCellStyle);
                            break;
                        }
                        case FREE_DAY: {
                            employeeNumbersCell.setCellStyle(holidayCellStyle);
                            break;
                        }
                        case WEEKEND: {
                            employeeNumbersCell.setCellStyle(weekendCellStyle);
                        }
                    }

                    // Aktualizacja maksymalnej liczby pracowników na zmianie
                    maxEmployeeNumberOnChange = dayInMonth.getFirstChangeEmployeesList().size() <= maxEmployeeNumberOnChange ? maxEmployeeNumberOnChange : dayInMonth.getFirstChangeEmployeesList().size();

                    // Jeśli dzień jest ostatnim dniem tygodnia, to następuje przejście do kolejnej linii
                    if (calendarToday.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        // Dodanie początkowej komórki z godzinami zmiany
                        workingHoursCell = employeeNumbersRow.createCell(1);
                        workingHoursCell.setCellStyle(workingHoursCellStyle);
                        workingHoursCell.setCellValue(DateUtils.getWorkingHoursFirstChange(startWorkTime));

                        // Ustawienie wysokości wiersza
                        employeeNumbersRow.setHeight((short) ((15 * (maxEmployeeNumberOnChange / 5 + 1)) * 20));

                        dayDatesRow = timeTableSheet.createRow(startRow++);
                        employeeNumbersRow = timeTableSheet.createRow(startRow++);

                        // Ustawienie wysokości wiersza
                        employeeNumbersRow.setHeight((short) ((15 * (maxEmployeeNumberOnChange / 5 + 1)) * 20));

                        maxEmployeeNumberOnChange = 0;
                    }
                }
            }

            // Praca w trybie dwuzmianowym
            else {
                // Generator liczb pseudolosowych
                Random rand = new Random();

                // Utworzenie komórek dla godziny zmiany oraz listy numerów pracowników dla pierwszej i drugiej zmiany
                XSSFCell firstChangeTime;
                XSSFCell secondChangeTime;
                XSSFCell firstChangeEmployeesCell;
                XSSFCell secondChangeEmployeesCell;
                XSSFCell dayDateCell; // Komórka dla daty

                // Utworzenie wiersza dla uzupełnienia dat i dwóch zmian
                XSSFRow dayDatesRow = timeTableSheet.createRow(startRow++);
                XSSFRow firstChangeEmployeesRow = timeTableSheet.createRow(startRow++);
                XSSFRow secondChangeEmployeesRow = timeTableSheet.createRow(startRow++);

                // Iteracja po komórkach, jeśli są dniami tygodnia przed pierwszym dniem miesiąca i wypełnienie ich kreskami
                for (int i = 1; i < allDaysInMonth.get(0).getDayOfWeekNumber(); i++) {
                    // Wypełnienie komórki z datą
                    dayDateCell = dayDatesRow.createCell(i + cellMargin);
                    dayDateCell.setCellValue("-");
                    dayDateCell.setCellStyle(datesCellStyle);

                    // Wypełnienie komórki z pracownikiem
                    firstChangeEmployeesCell = firstChangeEmployeesRow.createCell(i + cellMargin);
                    firstChangeEmployeesCell.setCellValue("-");
                    firstChangeEmployeesCell.setCellStyle(normalCellStyle);

                    secondChangeEmployeesCell = secondChangeEmployeesRow.createCell(i + cellMargin);
                    secondChangeEmployeesCell.setCellValue("-");
                    secondChangeEmployeesCell.setCellStyle(normalCellStyle);
                }

                // Dodanie początkowych komórek z godzinami zmiany
                firstChangeTime = firstChangeEmployeesRow.createCell(1);
                firstChangeTime.setCellStyle(workingHoursCellStyle);
                firstChangeTime.setCellValue(DateUtils.getWorkingHoursFirstChange(startWorkTime));

                secondChangeTime = secondChangeEmployeesRow.createCell(1);
                secondChangeTime.setCellStyle(workingHoursCellStyle);
                secondChangeTime.setCellValue(DateUtils.getWorkingHoursSecondChange(startWorkTime));

                // Maksymalna liczba powtórzeń generowania miesiąca w przypadku błędnego harmonogramu (nierównej liczbie pracowników w sobotę)
                int repeatChances = 0;

                // Iteracja po wszystkich dniach miesiąca
                for (int dayIterator = 0; dayIterator < allDaysInMonth.size(); dayIterator++) {
                    Day dayInMonth = allDaysInMonth.get(dayIterator);

                    // Obiekt przetwarzanego dnia miesiąca z czasem rozpoczęcia zmiany
                    Calendar calendarToday = Calendar.getInstance();
                    calendarToday.setTime(dayInMonth.getDate());
                    Calendar startWorkTimeCalendar = Calendar.getInstance();
                    startWorkTimeCalendar.setTime(startWorkTime);
                    calendarToday.set(Calendar.HOUR_OF_DAY, startWorkTimeCalendar.get(Calendar.HOUR_OF_DAY));
                    calendarToday.set(Calendar.MINUTE, startWorkTimeCalendar.get(Calendar.MINUTE));
                    calendarToday.set(Calendar.SECOND, 0);
                    calendarToday.set(Calendar.MILLISECOND, 0);

                    if (calendarToday.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                        // Dodanie początkowej komórki z godzinami zmiany
                        firstChangeTime = firstChangeEmployeesRow.createCell(1);
                        firstChangeTime.setCellStyle(workingHoursCellStyle);
                        firstChangeTime.setCellValue(DateUtils.getWorkingHoursFirstChange(startWorkTime));

                        secondChangeTime = secondChangeEmployeesRow.createCell(1);
                        secondChangeTime.setCellStyle(workingHoursCellStyle);
                        secondChangeTime.setCellValue(DateUtils.getWorkingHoursSecondChange(startWorkTime));
                    }

                    // Ustawienie flagi informującej, że któraś z sobót była pracująca
                    if (calendarToday.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && DayType.FREE_DAY.equals(dayInMonth.getDayType())) {
                        saturdayHoliday = true;
                    }

                    // Wpisanie daty dnia do odpowiedniej komórki
                    dayDateCell = dayDatesRow.createCell(dayInMonth.getDayOfWeekNumber() + cellMargin);
                    dayDateCell.setCellValue(DateUtils.formatDateToCalendar(dayInMonth.getDate()));
                    dayDateCell.setCellStyle(datesCellStyle);

                    // Sprawdzenie, czy dany dzień ma być w firmie pracujący
                    if (isWorkingDay(dayInMonth, workingHolidayName, selectedFreeDaysToWork)) {

                        calendarToday.add(Calendar.HOUR_OF_DAY, -3);

                        // Lista pracowników niemających urlopu z min. 3 h przerwy od ostatniego wyjścia
                        List<WorkingEmployee> filteredWorkingEmployeeList = workingEmployeeList.stream()
                                .filter(workingEmployee -> !employeeHasVacationOnThisDate(workingEmployee, dayInMonth.getDate()))
                                .filter(workingEmployee -> (dayInMonth.isWorkingDay() || (!dayInMonth.isWorkingDay() && workingEmployee.getEmployee().getOvertimeAgreement())))
                                .filter(workingEmployee -> workingEmployee.getLastExitFromWork().before(calendarToday.getTime()))
                                .collect(Collectors.toList());

                        // Przywrócenie normalnej godziny rozpoczęcie pracy
                        calendarToday.add(Calendar.HOUR_OF_DAY, 3);

                        // Liczba wszystkich dostępnych pracowników oraz połowa (dla w miarę równego podziału na zmiany)
                        int filteredEmployeeListSize = filteredWorkingEmployeeList.size();
                        int halfOfAvailableEmployeeList = filteredEmployeeListSize / 2;

                        // Iteracja po wszystkich dostępnych pracownikach
                        for (int employeeIterator = 0; employeeIterator < filteredEmployeeListSize; employeeIterator++) {

                            // Wylosowanie pracownika do zmiany
                            WorkingEmployee randomEmployee = filteredWorkingEmployeeList.get(rand.nextInt(filteredWorkingEmployeeList.size()));
                            calendarToday.add(Calendar.HOUR_OF_DAY, -11);

                            // Przypisanie pracownikowi godziny wyjścia ze zmiany
                            Calendar startWork = Calendar.getInstance();
                            startWork.setTime(startWorkTime);

                            // Jeśli lista pracowników na 1. zmianę jest jeszcze otwarta i pracownik ma 11 h przerwy, to zostanie dodany
                            if (dayInMonth.getFirstChangeEmployeesList().size() < halfOfAvailableEmployeeList &&
                                    (randomEmployee.getLastExitFromWork().before(calendarToday.getTime()))) {
                                dayInMonth.addEmployeeNumberToFirstChange(randomEmployee.getNumber());
                                startWork.add(Calendar.HOUR, 8);
                            }
                            // W przeciwnym wypadku zostanie dodany do 2. zmiany, jeśli lista ma jeszcze miejsce
                            else if (dayInMonth.getSecondChangeEmployeesList().size() <= halfOfAvailableEmployeeList) {
                                dayInMonth.addEmployeeNumberToSecondChange(randomEmployee.getNumber());
                                startWork.add(Calendar.HOUR, 16);
                            }

                            // Przywrócenie prawidłowej godziny
                            calendarToday.add(Calendar.HOUR_OF_DAY, 11);

                            // Zapisanie pracownikowi godziny ostatniego wyjścia z pracy
                            startWork.set(Calendar.MONTH, generatedMonth.getMonthNumber());
                            startWork.set(Calendar.YEAR, generatedMonth.getYear());
                            startWork.set(Calendar.DAY_OF_MONTH, calendarToday.get(Calendar.DAY_OF_MONTH));
                            randomEmployee.setLastExitFromWork(startWork.getTime());

                            // Przypisanie mu przepracowanych godzin w zależności, czy był to dzień roboczy czy nie
                            if (dayInMonth.isWorkingDay()) {
                                randomEmployee.addBasicWorkingHours();
                            } else {
                                randomEmployee.addAdditionalWorkingHours();
                            }

                            // Usunięcie wylosowanego pracownika z listy
                            filteredWorkingEmployeeList.remove(randomEmployee);
                        }

                        // Obliczenie różnicy pomiędzy liczbą pracowników na 1. i 2. zmianie w sobotę
                        int differentBetweenFirstAndSecondChange = Math.abs(dayInMonth.getFirstChangeEmployeesList().size() - dayInMonth.getSecondChangeEmployeesList().size());
                        if ((calendarToday.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) && (differentBetweenFirstAndSecondChange > 1) && repeatChances < 500) {
                            // Jeśli różnica jest większa niż 1 pracownik i nie przekroczono jeszcze limitu powtórzeń, to następuje reset generowania
                            dayIterator = 0;
                            startRow = 6;
                            repeatChances++;
                            workingEmployeeList = createWorkingEmployeeList(allEmployees, employeeIdentifiers, generatedMonth);
                            allDaysInMonth = generateAllDaysInMonthList(generatedMonth);
                            LOGGER.info(String.format("Invalid saturday: %s - repeat number %s", DateUtils.formatDateToCalendar(dayInMonth.getDate()), repeatChances));
                            continue;
                        }

                        // Aktualizacja maksymalnej liczby pracowników na zmianie
                        maxEmployeeNumberOnChange = dayInMonth.getFirstChangeEmployeesList().size() <= maxEmployeeNumberOnChange ? maxEmployeeNumberOnChange : dayInMonth.getFirstChangeEmployeesList().size();
                        maxEmployeeNumberOnChange = dayInMonth.getSecondChangeEmployeesList().size() <= maxEmployeeNumberOnChange ? maxEmployeeNumberOnChange : dayInMonth.getSecondChangeEmployeesList().size();

                        // Uzupełnienie pierwszej zmiany
                        firstChangeEmployeesCell = firstChangeEmployeesRow.createCell(dayInMonth.getDayOfWeekNumber() + cellMargin);
                        firstChangeEmployeesCell.setCellValue(Arrays.toString(dayInMonth.getFirstChangeEmployeesList().toArray()));

                        // Uzupełnienie drugiej zmiany
                        secondChangeEmployeesCell = secondChangeEmployeesRow.createCell(dayInMonth.getDayOfWeekNumber() + cellMargin);
                        secondChangeEmployeesCell.setCellValue(Arrays.toString(dayInMonth.getSecondChangeEmployeesList().toArray()));
                    } else {
                        // Jeśli dzień jest w firmie dniem wolnym, to wpisywana jest kreska
                        firstChangeEmployeesCell = firstChangeEmployeesRow.createCell(dayInMonth.getDayOfWeekNumber() + cellMargin);
                        firstChangeEmployeesCell.setCellValue("-");
                        secondChangeEmployeesCell = secondChangeEmployeesRow.createCell(dayInMonth.getDayOfWeekNumber() + cellMargin);
                        secondChangeEmployeesCell.setCellValue("-");
                    }

                    // Kolorowanie komórki w zależności od dnia
                    switch (dayInMonth.getDayType()) {
                        case NORMAL: {
                            firstChangeEmployeesCell.setCellStyle(normalCellStyle);
                            secondChangeEmployeesCell.setCellStyle(normalCellStyle);
                            break;
                        }
                        case FREE_DAY: {
                            firstChangeEmployeesCell.setCellStyle(holidayCellStyle);
                            secondChangeEmployeesCell.setCellStyle(holidayCellStyle);
                            break;
                        }
                        case WEEKEND: {
                            firstChangeEmployeesCell.setCellStyle(weekendCellStyle);
                            secondChangeEmployeesCell.setCellStyle(weekendCellStyle);
                        }
                    }

                    // Jeśli dzień jest ostatnim dniem tygodnia, to następuje przejście do kolejnej linii
                    if (calendarToday.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        // Dodanie początkowej komórki z godzinami zmiany
                        firstChangeTime = firstChangeEmployeesRow.createCell(1);
                        firstChangeTime.setCellStyle(workingHoursCellStyle);
                        firstChangeTime.setCellValue(DateUtils.getWorkingHoursFirstChange(startWorkTime));

                        secondChangeTime = secondChangeEmployeesRow.createCell(1);
                        secondChangeTime.setCellStyle(workingHoursCellStyle);
                        secondChangeTime.setCellValue(DateUtils.getWorkingHoursSecondChange(startWorkTime));

                        // Ustawienie wysokości wiersza w zależności od liczby pracowników
                        firstChangeEmployeesRow.setHeight((short) ((15 * (maxEmployeeNumberOnChange / 5 + 1)) * 20));
                        secondChangeEmployeesRow.setHeight((short) ((15 * (maxEmployeeNumberOnChange / 5 + 1)) * 20));

                        dayDatesRow = timeTableSheet.createRow(startRow++);
                        firstChangeEmployeesRow = timeTableSheet.createRow(startRow++);
                        secondChangeEmployeesRow = timeTableSheet.createRow(startRow++);

                        // Ustawienie wysokości wiersza w zależności od liczby pracowników
                        firstChangeEmployeesRow.setHeight((short) ((15 * (maxEmployeeNumberOnChange / 5 + 1)) * 20));
                        secondChangeEmployeesRow.setHeight((short) ((15 * (maxEmployeeNumberOnChange / 5 + 1)) * 20));

                        maxEmployeeNumberOnChange = 0;
                    }
                }
            }

            // Wypisanie listy wszystkich świąt, które wystąpiły
            List<FreeDaysForMonth> freeDaysForMonth = DateUtils.generateAllFreeDaysForMonth(generatedMonth.getMonthNumber(), generatedMonth.getYear());
            if (!freeDaysForMonth.isEmpty()) {
                XSSFRow holidaysRow = timeTableSheet.createRow(++startRow);
                XSSFCell holidayCell = holidaysRow.createCell(6);
                holidayCell.setCellValue("Święta przypadające w danym miesiącu:");

                // Iteracja po wszystkich świętach
                for (FreeDaysForMonth freeDay : freeDaysForMonth) {
                    holidaysRow = timeTableSheet.createRow(++startRow);
                    holidayCell = holidaysRow.createCell(6);
                    holidayCell.setCellValue(freeDay.getLabel());
                }
            }

            // Utworzenie tabeli z podsumowaniem pracowników i przepracowanego czasu
            int lastRow = generateSummaryTable(summarySheet, workingEmployeeList, normalCellStyle);

            // Informacja, że była sobota ze świętem
            if (saturdayHoliday) {
                XSSFRow saturdayHolidayRow = summarySheet.createRow(lastRow);
                XSSFCell saturdayHolidayCell = saturdayHolidayRow.createCell(1);
                saturdayHolidayCell.setCellValue("Na jedną z sobót przypadało święto - pracownik ma z tego powodu możliwość wybrania dnia wolnego");
            }

            // Wygenerowanie ciągu bitowego
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            byteArrayInputStream = new ByteArrayInputStream(bytes);

            TimeTableFile timeTableFile = new TimeTableFile();
            timeTableFile.setFileContent(bytes);
            timeTableFile.setGenerateDate(generatedDate);
            timeTableFile.setFileName(fileName);
            timeTableFile.setOneChanges(oneChangesOnDay);
            timeTableFileManager.saveTimeTable(timeTableFile);

            // Aktualizacja listy zawierającej ostatnie godziny przepracowane w danym miesiącu
            updateLastExitFromWork(workingEmployeeList, generatedMonth);

            LOGGER.info("Successful save file");
        } catch (IOException e) {
            LOGGER.error("Error during generate time table", e);
        }

        return byteArrayInputStream;
    }

    /**
     * Metoda aktualizująca pracownikom listę przepracowanych ostatnich dni z miesiąca.
     *
     * @param workingEmployeeList - lista pracowników
     * @param generatedMonth - generowany miesiąc
     */
    private void updateLastExitFromWork(List<WorkingEmployee> workingEmployeeList, GeneratedMonth generatedMonth) {
        workingEmployeeList.stream().filter(workingEmployee -> (workingEmployee.getBasicWorkingHours() + workingEmployee.getBasicWorkingHours()) > 0)
                .forEach(workingEmployee -> {

                    // Iteracja po wszystkich pracownikach, którzy pracowali w danym miesiącu
                    Employee employee = workingEmployee.getEmployee();

                    // Pobranie jego listy ostatnich wyjść z pracy na miesiące
                    List<LastExitFromWork> lastExitFromWorkList = employee.getLastExitFromWork();

                    // Ostatnie wyjście w danym miesiącu
                    LastExitFromWork lastExitFromWorkPerMonth = null;

                    // Klucz danego miesiąca
                    String monthAndYear = DateUtils.parseMonthAndYear(generatedMonth.getMonthNumber(), generatedMonth.getYear());

                    // Iteracja po wszystkich obiektach, jeśli jakiś został odnaleziony, to jest pobrany i będzie zaktualizowany
                    for (LastExitFromWork lastExitFromWork : lastExitFromWorkList) {
                        if (lastExitFromWork.getMonthAndYear().equals(monthAndYear)) {
                            lastExitFromWorkPerMonth = lastExitFromWork;
                        }
                    }

                    // Jeśli nie został odnaleziony, to utworzony zostanie nowy
                    if (lastExitFromWorkPerMonth == null) {
                        lastExitFromWorkPerMonth = new LastExitFromWork();
                        lastExitFromWorkPerMonth.setMonthAndYear(monthAndYear);
                        lastExitFromWorkPerMonth.setWorkingEmployee(employeeManagerBean.findEmployee(employee.getId()));
                    }

                    // Przypisanie ostatniego wyjścia z pracy
                    lastExitFromWorkPerMonth.setLastExit(workingEmployee.getLastExitFromWork());
                    lastExitFromWorkManagerBean.saveEntity(lastExitFromWorkPerMonth);
                });
    }

    /**
     * Metoda generująca tabelę z podsumowaniem pracowników.
     *
     * @param summarySheet        - drugi arkusz z pliku
     * @param workingEmployeeList - lista pracujących pracowników
     * @param normalCellStyle     - styl komórki
     */
    private int generateSummaryTable(XSSFSheet summarySheet, List<WorkingEmployee> workingEmployeeList, CellStyle normalCellStyle) {
        // Pierwszy edytowany wiersz z kolei
        int summarySheetStartRow = 2;
        XSSFRow summaryRow = summarySheet.createRow(summarySheetStartRow++);
        XSSFCell summaryCell;

        for (WorkingEmployee workingEmployee : workingEmployeeList) {
            // Numer pracownika
            summaryCell = summaryRow.createCell(1);
            summaryCell.setCellStyle(normalCellStyle);
            summaryCell.setCellValue(workingEmployee.getNumber() + ".");

            // Etykieta pracownika (imię i nazwisko lub identyfikator)
            summaryCell = summaryRow.createCell(2);
            summaryCell.setCellStyle(normalCellStyle);
            summaryCell.setCellValue(workingEmployee.getEmployeeLabel());

            // Standardowe godziny przepracowane w miesiącu
            summaryCell = summaryRow.createCell(3);
            summaryCell.setCellStyle(normalCellStyle);
            summaryCell.setCellValue(workingEmployee.getBasicWorkingHours() + " h");

            // Dodatkowe godziny przepracowane w miesiącu (nadgodziny)
            summaryCell = summaryRow.createCell(4);
            summaryCell.setCellStyle(normalCellStyle);
            summaryCell.setCellValue(workingEmployee.getAdditionalWorkingHours() + " h");

            // Łączna liczba przepracowanych godzin
            summaryCell = summaryRow.createCell(5);
            summaryCell.setCellStyle(normalCellStyle);
            summaryCell.setCellValue((workingEmployee.getBasicWorkingHours() + workingEmployee.getAdditionalWorkingHours()) + " h");
            summaryRow = summarySheet.createRow(summarySheetStartRow++);
        }

        return summarySheetStartRow;
    }

    /**
     * Metoda sprawdzająca, czy jest to dzień pracujący w firmie.
     *
     * @param dayInMonth             - dzień miesiąca
     * @param workingHolidayName     - nazwa weekendowego dnia pracującego, jeśli występuje
     * @param selectedFreeDaysToWork - wybrane święta do pracy
     * @return - true, jeśli dzień pracujący
     */
    private boolean isWorkingDay(Day dayInMonth, WorkingHolidayDayName workingHolidayName, List<FreeDaysForMonth> selectedFreeDaysToWork) {
        int selectedFromGuiWorkingHolidayName = 0;
        if (workingHolidayName != null) {
            selectedFromGuiWorkingHolidayName = workingHolidayName.getDayOfWeekNumber();
        }

        boolean isDayInListFreeDaysToWork = false;
        Calendar checkedDate = Calendar.getInstance();
        checkedDate.setTime(dayInMonth.getDate());
        for (FreeDaysForMonth freeDaysForMonth : selectedFreeDaysToWork) {
            if (checkedDate.get(Calendar.DAY_OF_MONTH) == freeDaysForMonth.getDay() &&
                    checkedDate.get(Calendar.MONTH) == (freeDaysForMonth.getMonth() - 1)) {
                isDayInListFreeDaysToWork = true;
            }
        }

        if (DateUtils.isWeekend(checkedDate) && isDayInListFreeDaysToWork && dayInMonth.getDayOfWeekNumber() == selectedFromGuiWorkingHolidayName) {
            return true;
        }

        return dayInMonth.isWorkingDay() ||
                (!dayInMonth.isWorkingDay() && dayInMonth.getDayOfWeekNumber() == selectedFromGuiWorkingHolidayName) ||
                (!dayInMonth.isWorkingDay() && isDayInListFreeDaysToWork && !DateUtils.isWeekend(checkedDate));
    }

    /**
     * Sprawdzenie, czy pracownik ma urlop w danym dniu.
     *
     * @param workingEmployee - pracownik
     * @param date            - data
     * @return - true, jeśli tak
     */
    private boolean employeeHasVacationOnThisDate(WorkingEmployee workingEmployee, Date date) {
        List<Vacation> employeeVacationList = workingEmployee.getEmployee().getVacationList();

        for (Vacation vacation : employeeVacationList) {
            Date startVacation = DateUtils.getDayBegin(vacation.getDateFromVacation());
            Date endVacation = DateUtils.getDayEnd(vacation.getDateToVacation());

            if (date.after(startVacation) && date.before(endVacation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Metoda tworząca tymczasowe obiekty pracowników do generowania.
     *
     * @param allEmployees        - lista wszystkich pracowników
     * @param employeeIdentifiers - typ identyfikatora w pliku
     * @return - lista pracowników
     */
    private List<WorkingEmployee> createWorkingEmployeeList(List<Employee> allEmployees, EmployeeIdentifiers employeeIdentifiers, GeneratedMonth generatedMonth) {
        List<WorkingEmployee> resultList = new ArrayList<>();
        int previousMonth = generatedMonth.getMonthNumber() == 0 ? 11 : generatedMonth.getMonthNumber() - 1;
        String generatedMonthYearKey = DateUtils.parseMonthAndYear(previousMonth, generatedMonth.getYear());

        for (int i = 0; i < allEmployees.size(); i++) {
            WorkingEmployee workingEmployee = new WorkingEmployee();
            workingEmployee.setEmployee(allEmployees.get(i));
            workingEmployee.setNumber(i + 1);

            // Pobranie ostatniego wyjścia z pracy pracownika z poprzedniego miesiąca
            List<LastExitFromWork> lastExitFromWorks = allEmployees.get(i).getLastExitFromWork().stream()
                    .filter(lastExitFromWork -> lastExitFromWork.getMonthAndYear().equals(generatedMonthYearKey))
                    .collect(Collectors.toList());

            // Jeśli nie ma żadnego wpisu, tzn., że w poprzednim miesiącu nie pracował
            if (lastExitFromWorks.isEmpty()) {
                workingEmployee.setLastExitFromWork(DateUtils.getBeginOfWorldDate());
            } else {
                workingEmployee.setLastExitFromWork(lastExitFromWorks.get(0).getLastExit());
            }

            if (EmployeeIdentifiers.FIRST_AND_LAST_NAME.equals(employeeIdentifiers)) {
                workingEmployee.setEmployeeLabel(String.format("%s %s", allEmployees.get(i).getFirstName(), allEmployees.get(i).getLastName()));
            } else {
                workingEmployee.setEmployeeLabel(allEmployees.get(i).getUniqueEmployeeId());
            }

            resultList.add(workingEmployee);
        }

        return resultList;
    }

    /**
     * Wygenerowanie wszystkich dni na dany miesiąc.
     *
     * @param generatedMonth - wybrany z GUI miesiąc
     * @return - lista dni z miesiąca
     */
    private List<Day> generateAllDaysInMonthList(GeneratedMonth generatedMonth) {
        List<Day> resultList = new ArrayList<>();
        Calendar firstMonthDay = Calendar.getInstance();
        firstMonthDay.set(Calendar.YEAR, generatedMonth.getYear());
        firstMonthDay.set(Calendar.MONTH, generatedMonth.getMonthNumber());
        firstMonthDay.set(Calendar.DAY_OF_MONTH, 1);
        int lastMonthDayNumber = firstMonthDay.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < lastMonthDayNumber; i++) {
            Day day = new Day();

            day.setDate(firstMonthDay.getTime());
            day.setDayOfWeekNumber(firstMonthDay.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 7 : firstMonthDay.get(Calendar.DAY_OF_WEEK) - 1);

            if (DateUtils.isWeekend(firstMonthDay)) {
                day.setDayType(DayType.WEEKEND);
                day.setWorkingDay(false);
            }

            if (DateUtils.isHoliday(firstMonthDay)) {
                day.setDayType(DayType.FREE_DAY);
                day.setWorkingDay(false);
            }

            resultList.add(day);
            firstMonthDay.add(Calendar.DAY_OF_MONTH, 1);
        }

        return resultList;
    }

}
