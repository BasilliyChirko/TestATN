package basilliyc.chirkotestatn.utils;

public class Error {

    public static final String FILE_NOT_SELECTED = "File not selected";
    public static final String FILE_NOT_EXISTS = "File not exists";
    public static final String FILE_ERROR_OPEN_STREAM = "File error open stream";
    public static final String DIR_NOT_SELECTED = "Dir not selected";
    public static final String DIR_NOT_EXISTS = "Dir not exists";

    public static String humanizeError(Throwable throwable) {
        return throwable.getMessage();
    }
}
