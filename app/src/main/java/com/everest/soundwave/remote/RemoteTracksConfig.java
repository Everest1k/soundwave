package com.everest.soundwave.remote;

// ссылка на публичную папку Яндекс.Диска с треками
public final class RemoteTracksConfig {

    public static final String YANDEX_DISK_PUBLIC_LINK = "https://disk.yandex.com/d/97VBe8dYd2q_5A";

    private RemoteTracksConfig() {}

    public static boolean isConfigured() {
        return YANDEX_DISK_PUBLIC_LINK != null && !YANDEX_DISK_PUBLIC_LINK.trim().isEmpty();
    }
}
