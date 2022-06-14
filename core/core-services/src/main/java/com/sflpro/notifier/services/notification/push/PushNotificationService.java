package com.sflpro.notifier.services.notification.push;

import com.sflpro.notifier.db.entities.notification.push.PushNotification;
import com.sflpro.notifier.services.notification.AbstractNotificationService;
import com.sflpro.notifier.services.notification.dto.push.PushNotificationDto;
import com.sflpro.notifier.services.notification.dto.push.PushNotificationRecipientsParameters;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * User: Ruben Dilanyan
 * Company: SFL LLC
 * Date: 8/14/15
 * Time: 11:42 AM
 */
public interface PushNotificationService extends AbstractNotificationService<PushNotification> {

    /**
     * Creates new push notification for provided recipient and DTO
     *
     * @param pushNotificationRecipientId
     * @param pushNotificationDto
     * @return pushNotification
     */
    @Nonnull
    PushNotification createNotification(@Nonnull final Long pushNotificationRecipientId, @Nonnull final PushNotificationDto pushNotificationDto);

    /**
     * Create push notifications for active user devices
     *
     * @param userId
     * @param pushNotificationDto
     * @return pushNotifications
     */
    @Nonnull
    List<PushNotification> createNotificationsForRecipients(@Nonnull final PushNotificationRecipientsParameters recipientsParameters, @Nonnull final PushNotificationDto pushNotificationDto);

    /**
     * Returns the notification to be processed
     *
     * @param notificationId
     * @return notification
     */
    PushNotification getPushNotificationForProcessing(final Long notificationId);
}
