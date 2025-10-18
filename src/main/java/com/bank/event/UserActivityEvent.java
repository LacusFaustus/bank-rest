package com.bank.event;

import com.bank.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserActivityEvent extends ApplicationEvent {
    private final User user;
    private final String activityType;
    private final String description;
    private final String ipAddress;

    public UserActivityEvent(Object source, User user, String activityType, String description, String ipAddress) {
        super(source);
        this.user = user;
        this.activityType = activityType;
        this.description = description;
        this.ipAddress = ipAddress;
    }
}
