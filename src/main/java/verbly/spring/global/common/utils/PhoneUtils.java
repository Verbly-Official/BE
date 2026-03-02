package verbly.spring.global.common.utils;

public class PhoneUtils {
    private PhoneUtils() {}

    public static String normalize(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("-", "").replaceAll(" ", "");
    }
}
