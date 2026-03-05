package com.cl.scheduled;

import com.cl.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduledTask {

    @Autowired
    private NotificationService notificationService;

    @Scheduled(cron = "0 0/30 * * * ?")
    public void retryFailedNotifications() {
        System.out.println("开始执行通知重试任务...");
        notificationService.retryFailedNotifications();
        System.out.println("通知重试任务执行完成。");
    }
}
