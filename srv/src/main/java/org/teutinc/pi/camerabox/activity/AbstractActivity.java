package org.teutinc.pi.camerabox.activity;

import java.util.UUID;

/**
 * @author apeyrard
 */
public abstract class AbstractActivity implements Activity {
    private final String id;
    private final String name;

    protected AbstractActivity(String id, String name) {
        this.id = id != null ? id : UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
    }

    protected AbstractActivity(String name) {
        this(null, name);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractActivity)) return false;

        AbstractActivity that = (AbstractActivity) o;

        return id.equals(that.id) && name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
