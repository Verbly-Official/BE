package verbly.spring.domain.user.enums;

public enum Level {
    LV1(1), LV2(2), LV3(3), LV4(4), LV5(5), LV6(6), LV7(7);

    private final int value;

    Level(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

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
