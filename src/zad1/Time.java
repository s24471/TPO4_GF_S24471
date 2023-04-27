/**
 *
 *  @author Gocławski Filip S24471
 *
 */

package zad1;


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Locale;

public class Time {
    public static String passed(String from, String to) {
        try {

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.forLanguageTag("pl"));
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.forLanguageTag("pl"));
            Temporal fromTemporal;
            Temporal toTemporal;

            if (from.contains("T") && to.contains("T")) {
                fromTemporal = LocalDateTime.parse(from, dateTimeFormatter);
                toTemporal = LocalDateTime.parse(to, dateTimeFormatter);
            } else {
                fromTemporal = LocalDate.parse(from, dateFormatter);
                toTemporal = LocalDate.parse(to, dateFormatter);
            }

            LocalDate fromDate = LocalDate.from(fromTemporal);
            LocalDate toDate = LocalDate.from(toTemporal);
            if (from.split("-")[1].equals("02") && from.split("-")[2].equals("29") && !fromDate.isLeapYear()) {
                return ("*** java.time.format.DateTimeParseException: Text '" + from + "' could not be parsed: Invalid date 'February 29' as '" + fromDate.getYear() + "' is not a leap year");
            }
            if (to.split("-")[1].equals("02") && to.split("-")[2].equals("29") && !toDate.isLeapYear()) {
                return ("*** java.time.format.DateTimeParseException: Text '" + to + "' could not be parsed: Invalid date 'February 29' as '" + toDate.getYear() + "' is not a leap year");
            }
            return generateTimeReport(fromTemporal, toTemporal);
        } catch (
                DateTimeParseException e) {
            return "*** " + e.toString();
        }

    }

    private static String generateCalendarReport(Temporal from, Temporal to) {
        LocalDate fromDate = LocalDate.from(from);
        LocalDate toDate = LocalDate.from(to);

        ZoneId zone = ZoneId.systemDefault();
        Period period = Period.between(fromDate, toDate.atStartOfDay(zone).toLocalDate());

        String ans = "";
        ans += (period.getYears() == 1 ? period.getYears() + " rok" : period.getYears() > 1 ? (period.getYears() > 4 ? period.getYears() + " lat" : period.getYears() + " lata") : "");
        if (period.getYears() > 0 && (period.getMonths() > 0 || period.getDays() > 0)) ans += ", ";
        ans += (period.getMonths() == 1 ? period.getMonths() + " miesiąc" : period.getMonths() >= 5 ? period.getMonths() + " miesięcy" : (period.getMonths() > 1?period.getMonths() + " miesiące":  ""));
        if (period.getMonths() > 0 && period.getDays() > 0) ans += ", ";
        ans += (period.getDays() == 1 ? period.getDays() + " dzień" : period.getDays() > 1 ? period.getDays() + " dni" : "");

        return ans;
    }

    private static String generateTimeReport(Temporal from, Temporal to) {
        // Dodatkowe formatery dla dni tygodnia i miesięcy w języku polskim
        DateTimeFormatter dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE", new Locale("pl", "PL"));
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM", new Locale("pl", "PL"));
        ZoneId zone;
        ZonedDateTime zonedDateTime;
        ZonedDateTime convertedZonedDateTime;
        LocalDate fromDate;
        LocalDate toDate;
        if (from instanceof LocalDateTime && to instanceof LocalDateTime) {
            zone = ZoneId.systemDefault();
            zonedDateTime = ZonedDateTime.now();
            convertedZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("Europe/Warsaw"));
            fromDate = LocalDate.from(((LocalDateTime) from).atZone(ZoneId.from(convertedZonedDateTime)));
            toDate = LocalDate.from(((LocalDateTime) to).atZone(ZoneId.from(convertedZonedDateTime)));
        } else {
            zone = ZoneId.systemDefault();
            zonedDateTime = ZonedDateTime.now();
            convertedZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("Europe/Warsaw"));
            fromDate = LocalDate.from(((LocalDate) from));
            toDate = LocalDate.from(((LocalDate) to));
        }
        String fromDateString = fromDate.getDayOfMonth() + " " + fromDate.format(monthFormatter) + " " + fromDate.getYear() + " (" + fromDate.format(dayOfWeekFormatter) + ")";
        String toDateString = toDate.getDayOfMonth() + " " + toDate.format(monthFormatter) + " " + toDate.getYear() + " (" + toDate.format(dayOfWeekFormatter) + ")";

        long daysBetween = ChronoUnit.DAYS.between(fromDate, toDate);
        double weeksBetween = (double) daysBetween / 7;
        String weeksBetweenString = weeksBetween % 1 == 0 ? String.format("%.0f", weeksBetween) : String.format("%.2f", weeksBetween);
        weeksBetweenString = weeksBetweenString.replace(',', '.');
        String calendarReport = generateCalendarReport(from, to);

        StringBuilder report = new StringBuilder();
        report.append("Od ").append(fromDateString);

        // Dodaj informacje o godzinach, jeśli dostępne
        if (from instanceof LocalDateTime) {
            LocalDateTime fromDateTime = LocalDateTime.from(from);
            ZonedDateTime fromZonedDateTime = fromDateTime.atZone(zone);
            report.append(" godz. ").append(fromZonedDateTime.getHour()).append(":").append(String.format("%02d", fromZonedDateTime.getMinute()));
        }

        report.append(" do ").append(toDateString);

        // Dodaj informacje o godzinach, jeśli dostępne
        if (to instanceof LocalDateTime) {
            LocalDateTime toDateTime = LocalDateTime.from(to);
            ZonedDateTime toZonedDateTime = toDateTime.atZone(zone);
            report.append(" godz. ").append(toZonedDateTime.getHour()).append(":").append(String.format("%02d", toZonedDateTime.getMinute()));
        }

        report.append("\n");
        report.append(" - mija: ").append(daysBetween == 1 ? "1 dzień" : daysBetween + " dni").append(", tygodni ").append(weeksBetweenString).append("\n");

        // Dodaj informacje o godzinach i minutach, jeśli dostępne
        if (from instanceof LocalDateTime && to instanceof LocalDateTime) {
            LocalDateTime fromDateTime = LocalDateTime.from(from);
            LocalDateTime toDateTime = LocalDateTime.from(to);

            ZonedDateTime fromZonedDateTime = fromDateTime.atZone(zone);
            ZonedDateTime toZonedDateTime = toDateTime.atZone(zone);

            long hoursBetween = ChronoUnit.HOURS.between(fromZonedDateTime, toZonedDateTime);
            long minutesBetween = ChronoUnit.MINUTES.between(fromZonedDateTime, toZonedDateTime);
            report.append(" - godzin: ").append(hoursBetween).append(", minut: ").append(minutesBetween).append("\n");
        }
        if (!calendarReport.equals("")) report.append(" - kalendarzowo: ").append(calendarReport).append("\n");

        return report.toString();


    }

    public static String now() {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        return currentTime.format(formatter);
    }


}
