package by.fly.model;

import org.springframework.data.annotation.Id;

public abstract class AbstractModel {

    @Id
    private String id;

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractModel that = (AbstractModel) o;

        return id.equals(that.id);

    }

    public boolean isNew() {
        return null == getId();
    }


    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id='" + id + '\'' +
                '}';
    }


}
