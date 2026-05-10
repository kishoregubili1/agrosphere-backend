package com.agrosphere.service.impl;

import com.agrosphere.entity.Notification;
import com.agrosphere.entity.User;
import com.agrosphere.repository.NotificationRepository;
import com.agrosphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void send(User user, String title, String message, String type, String refId) {
        notificationRepository.save(Notification.builder()
            .user(user).title(title).message(message).type(type).referenceId(refId).build());
    }

    public List<Notification> getMyNotifications(Long userId) {
        User u = userRepository.findById(userId).orElseThrow();
        return notificationRepository.findByUserOrderByCreatedAtDesc(u);
    }

    public long getUnreadCount(Long userId) {
        User u = userRepository.findById(userId).orElseThrow();
        return notificationRepository.countByUserAndIsReadFalse(u);
    }

    @Transactional
    public void markAllRead(Long userId) {
        User u = userRepository.findById(userId).orElseThrow();
        List<Notification> list = notificationRepository.findByUserAndIsReadFalse(u);
        list.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(list);
    }
}
