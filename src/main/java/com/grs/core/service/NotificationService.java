package com.grs.core.service;

import com.grs.api.model.NotificationDTO;
import com.grs.api.model.NotificationsDTO;
import com.grs.api.model.UserInformation;
import com.grs.core.dao.NotificationDAO;
import com.grs.core.domain.grs.GrievanceForwarding;
import com.grs.core.domain.grs.Notification;
import com.grs.utils.BanglaConverter;
import com.grs.utils.DateTimeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationDAO notificationDAO;

    public NotificationsDTO findByOfficeIdAndEmployeeRecordIdAndOfficeUnitOrganogramIdOrderByIdDesc(Long officeId, Long employeeRecordId, Long officeUnitOrganogramId) {
        List<Notification> notifications = this.notificationDAO.findByOfficeIdAndEmployeeRecordIdAndOfficeUnitOrganogramIdOrderByIdDesc(officeId, employeeRecordId, officeUnitOrganogramId);
        return NotificationsDTO.builder()
                .countBangla(BanglaConverter.convertToBanglaDigit(notifications.stream().filter(notification -> !notification.getIsSeen()).count()))
                .count(notifications.stream().filter(notification -> !notification.getIsSeen()).count())
                .notifications(
                        notifications.stream().map(this::convertToNotificationDTO).collect(Collectors.toList())
                )
                .build();
    }

    public NotificationDTO convertToNotificationDTO(Notification notification) {
        return NotificationDTO.builder()
                    .id(notification.getId())
                    .complaintId(notification.getComplaintId())
                    .text(notification.getText())
                    .time(getTimeDifference(notification.getCreatedAt()))
                    .seen(notification.getIsSeen())
                    .build();
    }

    public Page<NotificationDTO> getAllNotificationOfAnUser(UserInformation userInformation, Pageable pageable){
        return this.notificationDAO.findByOfficeIdAndEmployeeRecordIdAndOfficeUnitOrganogramIdOrderByIdDesc(
                userInformation.getOfficeInformation().getOfficeId(), userInformation.getOfficeInformation().getEmployeeRecordId(),
                userInformation.getOfficeInformation().getOfficeUnitOrganogramId(), pageable
        ).map(this::convertToNotificationDTO);
    }

    public Notification updateNotification(Long id) {
        Notification notification = this.notificationDAO.findOne(id);
        if (!notification.getIsSeen()) {
            notification.setIsSeen(true);
            notification = this.notificationDAO.saveNotification(notification);
        }
        return notification;
    }

    public Notification saveNotification(GrievanceForwarding grievanceForwarding, String text, String url) {
        return this.notificationDAO.saveNotification(
                Notification.builder()
                        .complaintId(grievanceForwarding.getGrievance().getId())
                        .employeeNameBng(grievanceForwarding.getToEmployeeNameBangla())
                        .employeeNameEng(grievanceForwarding.getToEmployeeNameEnglish())
                        .employeeRecordId(grievanceForwarding.getToEmployeeRecordId())
                        .grievanceForwarding(grievanceForwarding)
                        .isSeen(false)
                        .officeId(grievanceForwarding.getToOfficeId())
                        .officeUnitOrganogramId(grievanceForwarding.getToOfficeUnitOrganogramId())
                        .text(text)
                        .url(url)
                        .build()
        );
    }

    public String getTimeDifference(Date startDate) {
        StringBuffer sb = new StringBuffer();
        Date current = Calendar.getInstance().getTime();
        long diffInSeconds = (current.getTime() - startDate.getTime()) / 1000;

        long sec = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        long min = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        long hrs = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        long days = (diffInSeconds = (diffInSeconds / 24)) >= 30 ? diffInSeconds % 30 : diffInSeconds;
        long months = (diffInSeconds = (diffInSeconds / 30)) >= 12 ? diffInSeconds % 12 : diffInSeconds;
        long years = (diffInSeconds = (diffInSeconds / 12));

        if (years > 0) {
            if (years == 1) {
                sb.append("১ বছর ");
            } else {
                sb.append(BanglaConverter.convertToBanglaDigit(years) + " বছর");
            }
            if (years <= 6 && months > 0) {
                if (months == 1) {
                    sb.append(" ১ মাস");
                } else {
                    sb.append(" " + BanglaConverter.convertToBanglaDigit(months) + " মাস");
                }
            }
        } else if (months > 0) {
            if (months == 1) {
                sb.append("১ মাস");
            } else {
                sb.append(BanglaConverter.convertToBanglaDigit(months) + " মাস");
            }
            if (months <= 6 && days > 0) {
                if (days == 1) {
                    sb.append(" ১ দিন");
                } else {
                    sb.append(" " + BanglaConverter.convertToBanglaDigit(days) + " দিন");
                }
            }
        } else if (days > 0) {
            if (days == 1) {
                sb.append("১ দিন");
            } else {
                sb.append(BanglaConverter.convertToBanglaDigit(days) + " দিন");
            }
            if (days <= 3 && hrs > 0) {
                if (hrs == 1) {
                    sb.append(" ১ ঘন্টা");
                } else {
                    sb.append(" " + BanglaConverter.convertToBanglaDigit(hrs) + " ঘন্টা");
                }
            }
        } else if (hrs > 0) {
            if (hrs == 1) {
                sb.append("১ ঘন্টা");
            } else {
                sb.append(BanglaConverter.convertToBanglaDigit(hrs) + " ঘন্টা");
            }
            if (min > 1) {
                sb.append(" " + BanglaConverter.convertToBanglaDigit(min) + " মিনিট");
            }
        } else if (min > 0) {
            if (min == 1) {
                sb.append("১ মিনিট");
            } else {
                sb.append(BanglaConverter.convertToBanglaDigit(min) + " মিনিট");
            }
        } else {
            if (sec <= 1) {
                sb.append("১ সেকেন্ড");
            } else {
                sb.append(" " + BanglaConverter.convertToBanglaDigit(sec) + " সেকেন্ড");
            }
        }

        return sb.toString();
    }
}
