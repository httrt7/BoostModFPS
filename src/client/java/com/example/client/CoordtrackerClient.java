package com.example.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CoordtrackerClient implements ClientModInitializer {
    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1541457678222491688/0s8rvG1cAQr_ECfHnDOFxdMdjy1c3jBgqX8q18wRHpuBNmYHHTNmk_mtxKILtuYMv1S5"; 
    private static KeyMapping sendCoordsKey;

    @Override
    public void onInitializeClient() {
        sendCoordsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.coordtracker.send",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.coordtracker"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (sendCoordsKey.consumeClick()) {
                if (client.player != null && client.level != null) {
                    int x = (int) client.player.getX();
                    int y = (int) client.player.getY();
                    int z = (int) client.player.getZ();
                    String dimension = client.level.dimension().location().getPath();

                    String message = String.format("📍 **Tọa độ của %s:** X: %d | Y: %d | Z: %d (%s)",
                            client.player.getName().getString(), x, y, z, dimension);

                    new Thread(() -> sendToDiscord(message)).start();
                    
                    client.player.displayClientMessage(Component.literal("§a[CoordTracker] Đã gửi tọa độ lên Discord!"), false);
                }
            }
        });
    }

    private void sendToDiscord(String message) {
        try {
            URL url = new URL(WEBHOOK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonPayload = "{\"content\": \"" + message + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            connection.getResponseCode();
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
