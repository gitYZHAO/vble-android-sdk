package com.viroyal.android.sdk.vble;

public class Settings {
    private boolean micFromBT = false;
    private boolean audioRouteBLE = false;
    private boolean audioForceToSpeaker = false;

    private volatile static Settings settings;

    public static Settings getInstance() {
        if (settings == null) {
            synchronized (Settings.class) {
                settings = new Settings();
            }
        }
        return settings;
    }

    public Settings() {
    }

    public boolean isMicFromBT() {
        return this.micFromBT;
    }

    public boolean isAudioRouteBLE() {
        return this.audioRouteBLE;
    }

    public boolean isAudioForceToSpeaker() {
        return this.audioForceToSpeaker;
    }


    public static class Builder {
        private boolean micFromBT = false;
        private boolean audioRouteBLE = false;
        private boolean audioForceToSpeaker = false;

        public Builder() {
        }

        public Settings.Builder setMicFromBT(boolean micFromBT) {
            this.micFromBT = micFromBT;
            return this;
        }

        public Settings.Builder setAudioRouteBLE(boolean audioRouteBLE) {
            this.audioRouteBLE = audioRouteBLE;
            return this;
        }

        public Settings.Builder setAudioForceToSpeaker(boolean audioForceToSpeaker) {
            this.audioForceToSpeaker = audioForceToSpeaker;
            return this;
        }

        public Settings build() {
            Settings settings = Settings.getInstance();
            this.apply(settings);
            return settings;
        }

        private void apply(Settings settings) {
            settings.micFromBT = this.micFromBT;
            settings.audioRouteBLE = this.audioRouteBLE;
            settings.audioForceToSpeaker = this.audioForceToSpeaker;
        }
    }
}
