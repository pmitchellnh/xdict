package mitchell.pete.xdict.db;

public class XDictSQLException extends Exception {
    String message;
    String statement;
    final String title = "SQL Error";

    XDictSQLException(String msg, String stmt) {
        message = msg;
        statement = stmt;
    }

    public String getMessage() {
        return message;
    }

    public String getStatement() {
        return statement;
    }

    public String getTitle() { return title; }

    public String toString() {
        return (title + ": " + message + "\nSQL Statement: " + statement + "\n");
    }
}
