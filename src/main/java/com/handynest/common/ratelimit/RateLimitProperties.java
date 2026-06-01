package com.handynest.common.ratelimit;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    private Duration cleanupInterval = Duration.ofMinutes(10);
    private Duration idleEntryTtl = Duration.ofHours(2);
    private Limit register = new Limit(3, Duration.ofHours(1));
    private Limit login = new Limit(5, Duration.ofMinutes(10));
    private Limit refresh = new Limit(60, Duration.ofMinutes(1));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getCleanupInterval() {
        return cleanupInterval;
    }

    public void setCleanupInterval(Duration cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }

    public Duration getIdleEntryTtl() {
        return idleEntryTtl;
    }

    public void setIdleEntryTtl(Duration idleEntryTtl) {
        this.idleEntryTtl = idleEntryTtl;
    }

    public Limit getRegister() {
        return register;
    }

    public void setRegister(Limit register) {
        this.register = register;
    }

    public Limit getLogin() {
        return login;
    }

    public void setLogin(Limit login) {
        this.login = login;
    }

    public Limit getRefresh() {
        return refresh;
    }

    public void setRefresh(Limit refresh) {
        this.refresh = refresh;
    }

    public static class Limit {

        private long capacity;
        private Duration window;

        public Limit() {
        }

        public Limit(long capacity, Duration window) {
            this.capacity = capacity;
            this.window = window;
        }

        public long getCapacity() {
            return capacity;
        }

        public void setCapacity(long capacity) {
            this.capacity = capacity;
        }

        public Duration getWindow() {
            return window;
        }

        public void setWindow(Duration window) {
            this.window = window;
        }
    }
}
