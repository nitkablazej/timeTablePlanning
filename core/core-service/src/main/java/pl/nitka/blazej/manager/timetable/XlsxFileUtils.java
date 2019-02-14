package pl.nitka.blazej.manager.timetable;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Utilsy do generowania pliku .XLSX
 *
 * @author Blazej
 */
public class XlsxFileUtils {

    private XlsxFileUtils() {
        // Konstruktor domyślny
    }

    /**
     * Utworzenie stylu dla dat w grafiku (pogrubione, żółte komórki).
     *
     * @param workbook - arkusz
     * @return - styl dla dat
     */
    public static CellStyle createDatesCellStyle(XSSFWorkbook workbook) {
        CellStyle datesCellStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        datesCellStyle.setFont(boldFont);
        addBorderAndAlignment(datesCellStyle);
        datesCellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        datesCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return datesCellStyle;
    }

    /**
     * Utworzenie standardowego stylu (wyśrodkowanie tekstu).
     *
     * @param workbook - arkusz
     * @return - styl dla listy pracowników
     */
    public static CellStyle createNormalCellStyle(XSSFWorkbook workbook) {
        CellStyle normalCellStyle = workbook.createCellStyle();
        addBorderAndAlignment(normalCellStyle);
        normalCellStyle.setWrapText(true);
        return normalCellStyle;
    }

    /**
     * Utworzenie stylu komórki dla dnia weekendowego (szara komórka).
     *
     * @param workbook - arkusz
     * @return - styl dla weekendu
     */
    public static CellStyle createWeekendCellStyle(XSSFWorkbook workbook) {
        CellStyle weekendCellStyle = workbook.createCellStyle();
        addBorderAndAlignment(weekendCellStyle);
        weekendCellStyle.setWrapText(true);
        weekendCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        weekendCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return weekendCellStyle;
    }

    /**
     * Utworzenie stylu komórki dla dnia świątecznego (czerwona komórka).
     *
     * @param workbook - arkusz
     * @return - styl dla święta
     */
    public static CellStyle createHolidayCellStyle(XSSFWorkbook workbook) {
        CellStyle holidayCellStyle = workbook.createCellStyle();
        addBorderAndAlignment(holidayCellStyle);
        holidayCellStyle.setWrapText(true);
        holidayCellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        holidayCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return holidayCellStyle;
    }

    /**
     * Utworzenie stylu komórki dla godzin zmiany (niebieska komórka).
     *
     * @param workbook - arkusz
     * @return - styl dla godzin
     */
    public static CellStyle createWorkingHoursCellStyle(XSSFWorkbook workbook) {
        CellStyle workingHoursCellStyle = workbook.createCellStyle();
        addBorderAndAlignment(workingHoursCellStyle);
        workingHoursCellStyle.setWrapText(true);
        workingHoursCellStyle.setFillForegroundColor(IndexedColors.LIME.getIndex());
        workingHoursCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return workingHoursCellStyle;
    }

    /**
     * Dodanie obraowania i wyśrodkowania do stylu.
     *
     * @param cellStyle - modyfikowany styl.
     */
    private static void addBorderAndAlignment(CellStyle cellStyle) {
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    }
}
