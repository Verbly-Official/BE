package verbly.spring.domain.user.enums;

public enum Level {
    LV1, LV2, LV3, LV4, LV5, LV6, LV7;

    public static Level fromPoint(long point) {
        if (point <= 100) return LV1;
        if (point <= 300) return LV2;
        if (point <= 600) return LV3;
        if (point <= 1000) return LV4;
        if (point <= 1500) return LV5;
        if (point <= 2100) return LV6;
        return LV7;
    }
}
