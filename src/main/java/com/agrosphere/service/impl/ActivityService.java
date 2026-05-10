package com.agrosphere.service.impl;

import com.agrosphere.dto.request.ActivityRequest;
import com.agrosphere.entity.*;
import com.agrosphere.enums.ActivityStatus;
import com.agrosphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final CropActivityRepository activityRepository;
    private final CropRepository cropRepository;
    private final TenantRepository tenantRepository;

    public List<CropActivity> getMyActivities(Long tenantId) {
        return activityRepository.findByTenantIdOrderByScheduledDateAsc(tenantId);
    }

    public List<CropActivity> getActivitiesByDateRange(Long tenantId, LocalDate from, LocalDate to) {
        return activityRepository.findByTenantIdAndScheduledDateBetween(tenantId, from, to);
    }

    public List<CropActivity> getPendingActivities(Long tenantId) {
        return activityRepository.findByTenantIdAndStatus(tenantId, ActivityStatus.PENDING);
    }

    @Transactional
    public CropActivity create(ActivityRequest req, Long tenantId) {
        Crop crop = cropRepository.findByIdAndTenantId(req.getCropId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Crop not found"));
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        CropActivity activity = CropActivity.builder()
                .title(req.getTitle())
                .notes(req.getNotes())
                .scheduledDate(req.getScheduledDate())
                .activityType(req.getActivityType())
                .crop(crop)
                .tenant(tenant)
                .build();
        return activityRepository.save(activity);
    }

    @Transactional
    public CropActivity updateStatus(Long id, ActivityStatus status, Long tenantId) {
        CropActivity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        if (!activity.getTenant().getId().equals(tenantId))
            throw new RuntimeException("Access denied");
        activity.setStatus(status);
        if (status == ActivityStatus.COMPLETED)
            activity.setCompletedDate(LocalDate.now());
        return activityRepository.save(activity);
    }

    @Transactional
    public void delete(Long id, Long tenantId) {
        CropActivity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        if (!activity.getTenant().getId().equals(tenantId))
            throw new RuntimeException("Access denied");
        activityRepository.delete(activity);
    }
}
