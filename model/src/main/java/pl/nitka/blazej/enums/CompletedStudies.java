package pl.nitka.blazej.enums;

import lombok.Getter;

/**
 * Enum udostępniający okresy nauki wliczane do stażu pracy.
 *
 * @author Blazej
 */
public enum CompletedStudies {

    /**
     * Zasadnicza szkoła zawodowa 2-letnia.
     */
    VOCATIONAL_SCHOOL_2_YEARS(2, "Zasadnicza szkoła zawodowa 2-letnia"),

    /**
     * Zasadnicza szkoła zawodowa 3-letnia.
     */
    VOCATIONAL_SCHOOL_3_YEARS(3, "Zasadnicza szkoła zawodowa 3-letnia"),

    /**
     * Technikum 4-letnie.
     */
    TECHNICAL_COLLEGE_4_YEARS(4, "Technikum 4-letnie"),

    /**
     * Technikum 5-letnie.
     */
    TECHNICAL_COLLEGE_5_YEARS(5, "Technikum 5-letnie"),

    /**
     * Technikum dla absolwentów szkoły zawodowej.
     */
    TECHNICAL_COLLEGE_FOR_VOCATIONAL_SHOOL_GRADUATES_5_YEARS(5, "Technikum dla absol. szkoły zawodowej"),

    /**
     * Liceum ogólnokształcące.
     */
    COMPREHENSIVE_SCHOOL_4_YEARS(4, "Liceum ogólnokształcące"),

    /**
     * Szkoła policealna.
     */
    EXTRA_HIGH_SCHOOL_6_YEARS(6, "Szkoła policealna"),

    /**
     * Szkoła wyższa
     */
    UNIVERSITY(8, "Szkoła wyższa"),

    /**
     * Brak ukończonej szkoły.
     */
    NONE(0, "Brak ukończonej szkoły");

    @Getter
    private final int learningTime;

    @Getter
    private final String schoolName;

    CompletedStudies(int learningTime, String schoolName) {
        this.learningTime = learningTime;
        this.schoolName = schoolName;
    }

    public static CompletedStudies getFromString(String schoolName) {
        for (CompletedStudies completedStudies : values()) {
            if (completedStudies.getSchoolName().equals(schoolName)) {
                return completedStudies;
            }
        }
        return null;
    }
}
