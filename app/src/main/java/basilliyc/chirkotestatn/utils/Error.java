package basilliyc.chirkotestatn.utils;

public class Error {

    public static String humanizeError(Throwable throwable) {
        return throwable.getMessage();
    }
}
