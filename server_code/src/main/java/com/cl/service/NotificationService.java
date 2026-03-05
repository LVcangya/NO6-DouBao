package com.cl.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.cl.entity.JiuzhentongzhiEntity;
import com.cl.entity.TongzhijiluEntity;
import com.cl.entity.YishengyuyueEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    @Autowired
    private JiuzhentongzhiService jiuzhentongzhiService;

    @Autowired
    private TongzhijiluService tongzhijiluService;

    @Autowired
    private YishengyuyueService yishengyuyueService;

    private static final int MAX_RETRY_COUNT = 3;

    @Transactional
    public void createNotificationAfterBooking(YishengyuyueEntity booking) {
        try {
            JiuzhentongzhiEntity notification = new JiuzhentongzhiEntity();
            notification.setTongzhibianhao(generateNotificationNo());
            notification.setYishengzhanghao(booking.getYishengzhanghao());
            notification.setDianhua(booking.getDianhua());
            notification.setJiuzhenshijian(booking.getYuyueshijian());
            notification.setZhanghao(booking.getZhanghao());
            notification.setShouji(booking.getShouji());
            notification.setTongzhibeizhu("预约成功通知：请准时就诊！");
            notification.setTongzhishijian(new Date());
            notification.setSendstatus(0);
            notification.setRetrycount(0);
            notification.setAddtime(new Date());

            jiuzhentongzhiService.insert(notification);

            sendNotification(notification, "系统");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public boolean sendNotification(JiuzhentongzhiEntity notification, String operator) {
        boolean sendSuccess = false;
        String failReason = null;
        TongzhijiluEntity record = new TongzhijiluEntity();
        record.setTongzhiid(notification.getId());
        record.setTongzhibianhao(notification.getTongzhibianhao());
        record.setSendtime(new Date());
        record.setOperator(operator);
        record.setAddtime(new Date());

        try {
            doSendNotification(notification);
            sendSuccess = true;
        } catch (Exception e) {
            failReason = e.getMessage();
        }

        record.setSendstatus(sendSuccess ? 1 : 0);
        record.setFailreason(failReason);
        tongzhijiluService.insert(record);

        notification.setLastsendtime(new Date());
        if (sendSuccess) {
            notification.setSendstatus(1);
        } else {
            notification.setRetrycount(notification.getRetrycount() + 1);
            notification.setFailreason(failReason);
            if (notification.getRetrycount() >= MAX_RETRY_COUNT) {
                notification.setSendstatus(2);
            } else {
                notification.setSendstatus(0);
            }
        }
        jiuzhentongzhiService.updateById(notification);

        return sendSuccess;
    }

    private void doSendNotification(JiuzhentongzhiEntity notification) throws Exception {
        System.out.println("正在发送通知给用户: " + notification.getZhanghao() + 
            ", 手机: " + notification.getShouji() + 
            ", 就诊时间: " + 
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(notification.getJiuzhenshijian()));
    }

    @Transactional
    public void retryFailedNotifications() {
        EntityWrapper<JiuzhentongzhiEntity> wrapper = new EntityWrapper<>();
        wrapper.eq("sendstatus", 0);
        wrapper.lt("retrycount", MAX_RETRY_COUNT);
        List<JiuzhentongzhiEntity> list = jiuzhentongzhiService.selectList(wrapper);
        
        for (JiuzhentongzhiEntity notification : list) {
            sendNotification(notification, "系统重试");
        }
    }

    private String generateNotificationNo() {
        return "TZ" + System.currentTimeMillis();
    }
}
