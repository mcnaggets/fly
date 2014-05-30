package by.fly.model;

public enum PrinterType {

    LASER("Лезерный"), JET("Струйный");

    private final String message;

    PrinterType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }


    public static PrinterType fromMessage(String message) {
        for (PrinterType printerType : values()) {
            if (message.equals(printerType.getMessage())) return printerType;
        }
        return null;
    }
}
