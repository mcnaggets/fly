package by.fly.model;

public enum OrderStatus {

    CREATED("Создан"), IN_PROGRESS("В работе"), READY("Готов"), PAID("Оплачен");

    private final String message;

    OrderStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
