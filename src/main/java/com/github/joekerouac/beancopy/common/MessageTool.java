package com.github.joekerouac.beancopy.common;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author JoeKerouac
 * @date 2024-09-01 10:29:52
 * @since 1.0.0
 */
public class MessageTool {

    public static void error(@Nullable Project project, @NotNull String msg) {
        msg(project, msg, NotificationType.ERROR);
    }

    public static void warning(@Nullable Project project, @NotNull String msg) {
        msg(project, msg, NotificationType.WARNING);
    }

    public static void info(@Nullable Project project, @NotNull String msg) {
        msg(project, msg, NotificationType.INFORMATION);
    }

    /**
     * 右下角消息通知
     *
     * @param project
     *            没有这个的话，仅仅是通知栏有文字，无Balloon
     */
    private static void msg(@Nullable Project project, @NotNull String msg, @NotNull NotificationType type) {
        Notification notification = new Notification(Constant.PLUGIN_NAME, Constant.PLUGIN_NAME, msg, type);
        Notifications.Bus.notify(notification, project);
    }
}
