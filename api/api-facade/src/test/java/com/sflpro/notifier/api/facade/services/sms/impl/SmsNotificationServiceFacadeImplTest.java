package com.sflpro.notifier.api.facade.services.sms.impl;

import com.sflpro.notifier.api.internal.facade.test.AbstractFacadeUnitTest;
import com.sflpro.notifier.api.model.common.result.ErrorType;
import com.sflpro.notifier.api.model.common.result.ResultResponseModel;
import com.sflpro.notifier.api.model.sms.SmsNotificationModel;
import com.sflpro.notifier.api.model.sms.request.CreateSmsNotificationRequest;
import com.sflpro.notifier.api.model.sms.response.CreateSmsNotificationResponse;
import com.sflpro.notifier.db.entities.notification.NotificationProviderType;
import com.sflpro.notifier.db.entities.notification.NotificationSendingPriority;
import com.sflpro.notifier.db.entities.notification.UserNotification;
import com.sflpro.notifier.db.entities.notification.sms.SmsNotification;
import com.sflpro.notifier.db.entities.user.User;
import com.sflpro.notifier.services.notification.UserNotificationService;
import com.sflpro.notifier.services.notification.dto.UserNotificationDto;
import com.sflpro.notifier.services.notification.dto.sms.SmsNotificationDto;
import com.sflpro.notifier.services.notification.event.sms.StartSendingNotificationEvent;
import com.sflpro.notifier.services.notification.sms.SmsNotificationService;
import com.sflpro.notifier.services.system.event.ApplicationEventDistributionService;
import com.sflpro.notifier.services.user.UserService;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * User: Ruben Dilanyan
 * Company: SFL LLC
 * Date: 1/13/16
 * Time: 5:57 PM
 */
public class SmsNotificationServiceFacadeImplTest extends AbstractFacadeUnitTest {


    /* Test subject and mocks */
    @TestSubject
    private SmsNotificationServiceFacadeImpl smsNotificationServiceFacade = new SmsNotificationServiceFacadeImpl();

    @Mock
    private SmsNotificationService smsNotificationService;

    @Mock
    private UserService userService;

    @Mock
    private UserNotificationService userNotificationService;

    @Mock
    private ApplicationEventDistributionService applicationEventDistributionService;


    /* Test methods */
    @Test
    public void testCreateSmsNotificationWithInvalidArguments() {
        // Reset
        resetAll();
        // Replay
        replayAll();
        // Run test scenario
        try {
            smsNotificationServiceFacade.createSmsNotification(null);
            fail("Exception should be thrown");
        } catch (final IllegalArgumentException ex) {
            // Expected
        }
        // Verify
        verifyAll();
    }

    @Test
    public void testCreateSmsNotificationWithValidationErrors() {
        // Test data
        final CreateSmsNotificationRequest request = new CreateSmsNotificationRequest();
        // Reset
        resetAll();
        // Replay
        replayAll();
        // Run test scenario
        final ResultResponseModel<CreateSmsNotificationResponse> result = smsNotificationServiceFacade.createSmsNotification(request);
        assertNotNull(result);
        assertNull(result.getResponse());
        assertNotNull(result.getErrors());
        assertErrorExists(result.getErrors(), ErrorType.NOTIFICATION_SMS_RECIPIENT_MISSING);
        // Verify
        verifyAll();
    }

    @Test
    public void testCreateSmsNotificationWithoutUser() {
        // Test data
        final CreateSmsNotificationRequest request = getServiceFacadeImplTestHelper().createCreateSmsNotificationRequest();
        request.setUserUuId(null);
        final NotificationSendingPriority sendingPriority = NotificationSendingPriority.valueOf(request.getSendingPriority().name());
        final SmsNotificationDto smsNotificationDto = new SmsNotificationDto(request.getRecipientNumber(), request.getBody(), request.getClientIpAddress(), NotificationProviderType.MSG_AM);
        smsNotificationDto.setSendingPriority(sendingPriority);
        final Long notificationId = 1L;
        final SmsNotification smsNotification = getServiceFacadeImplTestHelper().createSmsNotification();
        smsNotification.setId(notificationId);
        smsNotification.setSendingPriority(sendingPriority);
        // Reset
        resetAll();
        // Expectations
        expect(smsNotificationService.createSmsNotification(smsNotificationDto)).andReturn(smsNotification).once();
        applicationEventDistributionService.publishAsynchronousEvent(new StartSendingNotificationEvent(notificationId, request.getSecureProperties(), smsNotification.getSendingPriority()));
        expectLastCall().once();
        // Replay
        replayAll();
        // Run test scenario
        final ResultResponseModel<CreateSmsNotificationResponse> result = smsNotificationServiceFacade.createSmsNotification(request);
        assertNotNull(result);
        assertNotNull(result.getResponse());
        assertNotNull(result.getErrors());
        assertEquals(0, result.getErrors().size());
        // Assert notification model
        getServiceFacadeImplTestHelper().assertSmsNotificationModel(smsNotification, (SmsNotificationModel) result.getResponse().getNotification());
        // Verify
        verifyAll();
    }

    @Test
    public void testCreateSmsNotificationWithUser() {
        // Test data
        final CreateSmsNotificationRequest request = getServiceFacadeImplTestHelper().createCreateSmsNotificationRequest();
        final NotificationSendingPriority sendingPriority = NotificationSendingPriority.valueOf(request.getSendingPriority().name());
        final SmsNotificationDto smsNotificationDto = new SmsNotificationDto(request.getRecipientNumber(), request.getBody(), request.getClientIpAddress(),NotificationProviderType.MSG_AM);
        smsNotificationDto.setSendingPriority(sendingPriority);
        final Long notificationId = 1L;
        final SmsNotification smsNotification = getServiceFacadeImplTestHelper().createSmsNotification();
        smsNotification.setId(notificationId);
        smsNotification.setSendingPriority(sendingPriority);
        final Long userId = 2L;
        final User user = getServiceFacadeImplTestHelper().createUser();
        user.setId(userId);
        final Long userNotificationId = 3L;
        final UserNotification userNotification = getServiceFacadeImplTestHelper().createUserNotification();
        userNotification.setId(userNotificationId);
        // Reset
        resetAll();
        // Expectations
        expect(smsNotificationService.createSmsNotification(smsNotificationDto)).andReturn(smsNotification).once();
        expect(userService.getOrCreateUserForUuId(eq(request.getUserUuId()))).andReturn(user).once();
        expect(userNotificationService.createUserNotification(userId, notificationId, new UserNotificationDto())).andReturn(userNotification).once();
        applicationEventDistributionService.publishAsynchronousEvent(new StartSendingNotificationEvent(notificationId, request.getSecureProperties(), smsNotification.getSendingPriority()));
        expectLastCall().once();
        // Replay
        replayAll();
        // Run test scenario
        final ResultResponseModel<CreateSmsNotificationResponse> result = smsNotificationServiceFacade.createSmsNotification(request);
        assertNotNull(result);
        assertNotNull(result.getResponse());
        assertNotNull(result.getErrors());
        assertEquals(0, result.getErrors().size());
        // Assert notification model
        getServiceFacadeImplTestHelper().assertSmsNotificationModel(smsNotification, (SmsNotificationModel) result.getResponse().getNotification());
        // Verify
        verifyAll();
    }
}
