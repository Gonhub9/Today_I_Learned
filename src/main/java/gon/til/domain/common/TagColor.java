package gon.til.domain.common;

import gon.til.global.exception.GlobalErrorCode;
import gon.til.global.exception.GlobalException;
import lombok.Getter;

@Getter
public enum TagColor {

    // 파스텔 톤 색상 팔레트
    PASTEL_RED("#FFADAD"),      // 연한 빨강
    PASTEL_ORANGE("#FFD6A5"),   // 연한 주황
    PASTEL_YELLOW("#FDFFB6"),   // 연한 노랑
    PASTEL_GREEN("#CAFFBF"),    // 연한 초록
    PASTEL_BLUE("#9BF6FF"),     // 연한 하늘색
    PASTEL_NAVY("#A0C4FF"),     // 연한 네이비
    PASTEL_PURPLE("#BDB2FF"),   // 연한 보라
    PASTEL_PINK("#FFC6FF"),     // 연한 분홍
    PASTEL_GRAY("#EAEAEA");     // 연한 회색

    private final String hexCode;

    TagColor(String hexCode) {
        this.hexCode = hexCode;
    }

    // 요청된 색상 이름(문자열)이 유효한지 확인하는 정적 메소드
    public static boolean isValidColor(String name) {
        for (TagColor color : TagColor.values()) {
            if (color.name().toUpperCase().contains(name.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    // 요청된 색상 이름(문자열)으로 적절한 TagColor enum 상수를 찾는 정적 메소드
    public static TagColor from(String name) {
        for (TagColor color : values()) {
            if (color.name().toUpperCase().contains(name.toUpperCase())) {
                return color;
            }
        }
        throw new GlobalException(GlobalErrorCode.INVALID_COLOR_NAME);
    }
}
