package me.n4th4not.dynamicfps.util;

import me.n4th4not.dynamicfps.DynamicFPSMod;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

public class IdleHandler {
    private static boolean ACTIVE = false;
    private static boolean WAS_IDLE = false;

    private static long PREVIOUS_ACTIVITY = 0L;

    private static Vec3 PREV_POSITION = Vec3.ZERO;
    private static Vec3 PREV_LOOK_ANGLE = Vec3.ZERO;

    private static @Nullable GLFWKeyCallback PREVIOUS_KEY_CALLBACK;
    private static @Nullable GLFWScrollCallback PREVIOUS_SCROLL_CALLBACK;
    private static @Nullable GLFWCursorPosCallback PREVIOUS_CURSOR_POS_CALLBACK;
    private static @Nullable GLFWMouseButtonCallback PREVIOUS_MOUSE_CLICK_CALLBACK;

    public static void init() {
        if (ACTIVE) return;

        if (DynamicFPSMod.MOD_CONFIG.idleTime() == 0) return;

        ACTIVE = true;
        MinecraftForge.EVENT_BUS.addListener(IdleHandler::checkActivity);
        if (DynamicFPSMod.getWindow() != null) setWindow(DynamicFPSMod.getWindow().address());
    }

    public static void setWindow(long address) {
        if (ACTIVE) {
            PREVIOUS_KEY_CALLBACK = GLFW.glfwSetKeyCallback(address, IdleHandler::onKey);
            PREVIOUS_SCROLL_CALLBACK = GLFW.glfwSetScrollCallback(address, IdleHandler::onScroll);
            PREVIOUS_CURSOR_POS_CALLBACK = GLFW.glfwSetCursorPosCallback(address, IdleHandler::onMove);
            PREVIOUS_MOUSE_CLICK_CALLBACK = GLFW.glfwSetMouseButtonCallback(address, IdleHandler::onPress);
        }
    }

    public static void onActivity() {
        PREVIOUS_ACTIVITY = Util.getEpochMillis();
    }

    public static boolean isIdle() {
        long idleTime = DynamicFPSMod.MOD_CONFIG.idleTime();

        if (idleTime == 0) return false;
        return (Util.getEpochMillis() - PREVIOUS_ACTIVITY) >= idleTime * 1000;
    }

    private static void checkActivity(TickEvent.ClientTickEvent event) {
        if (DynamicFPSMod.MOD_CONFIG.detectIdleMovement())
            checkPlayerActivity();

        boolean idle = isIdle();

        if (idle != WAS_IDLE) {
            WAS_IDLE = idle;
            DynamicFPSMod.onStatusChanged(!idle);
        }
    }

    private static void checkPlayerActivity() {
        var player = Minecraft.getInstance().player;

        if (player == null) return;

        var position = player.position();
        var lookAngle = player.getLookAngle();

        if (!position.equals(PREV_POSITION) || !lookAngle.equals(PREV_LOOK_ANGLE)) onActivity();

        PREV_POSITION = position;
        PREV_LOOK_ANGLE = lookAngle;
    }

    // Keyboard events

    private static void onKey(long address, int key, int scancode, int action, int mods) {
        onActivity();
        if (PREVIOUS_KEY_CALLBACK != null) PREVIOUS_KEY_CALLBACK.invoke(address, key, scancode, action, mods);
    }

    // Mouse events

    private static void onScroll(long address, double xOffset, double yOffset) {
        onActivity();
        if (PREVIOUS_SCROLL_CALLBACK != null) PREVIOUS_SCROLL_CALLBACK.invoke(address, xOffset, yOffset);
    }

    private static void onMove(long address, double x, double y) {
        onActivity();
        if (PREVIOUS_CURSOR_POS_CALLBACK != null) PREVIOUS_CURSOR_POS_CALLBACK.invoke(address, x, y);
    }

    private static void onPress(long address, int button, int action, int mods) {
        onActivity();
        if (PREVIOUS_MOUSE_CLICK_CALLBACK != null) PREVIOUS_MOUSE_CLICK_CALLBACK.invoke(address, button, action, mods);
    }
}