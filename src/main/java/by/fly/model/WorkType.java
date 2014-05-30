package by.fly.model;

public enum WorkType {

    FILLING("Заправка"), RECOVERY("Восстановление"), REPAIR("Ремонт");

    private final String message;

    WorkType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}

