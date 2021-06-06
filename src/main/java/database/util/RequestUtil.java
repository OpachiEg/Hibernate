package database.util;

public class RequestUtil {

    public static String deleteExtraComma(StringBuilder stringBuilder) {
        String sbString = stringBuilder.toString();
        if (sbString.charAt(sbString.length() - 1) == ',') {
            stringBuilder.deleteCharAt(sbString.length() - 1);
        }
        return stringBuilder.toString();
    }

}
