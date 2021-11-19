package com.webproject.ourpoint.utils;

import com.webproject.ourpoint.errors.NotAcceptableException;
import org.springframework.http.HttpStatus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

public class PasswordValidation {

    public static boolean isValidPassword(String password) {
        // 최소 8자, 최대 20자 상수 선언
        final int MIN = 8;
        final int MAX = 20;

        // 영어, 숫자, 특수문자 포함한 MIN to MAX 글자 정규식
        final String REGEX =
                "^((?=.*\\d)(?=.*[a-zA-Z])(?=.*[\\W]).{" + MIN + "," + MAX + "})$";
        // 3자리 연속 문자 정규식
        final String SAMEPT = "(\\w)\\1\\1";
        // 공백 문자 정규식
        final String BLANKPT = "(\\s)";

        // 정규식 검사객체
        Matcher matcher;

        // null 체크
        checkArgument(password != null, "Detected: No Password.", HttpStatus.BAD_REQUEST);

        // ASCII 문자 비교를 위한 UpperCase
        String tmpPw = password.toUpperCase();
        // 문자열 길이
        int strLen = tmpPw.length();

        // 글자 길이 체크
        checkArgument(strLen <= MAX && strLen >= MIN, "Detected: Incorrect Length(Length: " + strLen + ").", HttpStatus.NOT_ACCEPTABLE);

        // 공백 체크
        matcher = Pattern.compile(BLANKPT).matcher(tmpPw);
        checkArgument(!matcher.find(), "Detected: Blank.", HttpStatus.NOT_ACCEPTABLE);

        // 비밀번호 정규식 체크
        matcher = Pattern.compile(REGEX).matcher(tmpPw);
        checkArgument(matcher.find(), "Detected: Wrong Regex.", HttpStatus.NOT_ACCEPTABLE);

        // 동일한 문자 3개 이상 체크
        matcher = Pattern.compile(SAMEPT).matcher(tmpPw);
        checkArgument(!matcher.find(), "Detected: Same Word x3.", HttpStatus.NOT_ACCEPTABLE);
        // 연속된 문자 / 숫자 3개 이상 체크

        // ASCII Char를 담을 배열 선언
        int[] tmpArray = new int[strLen];

        // Make Array
        for (int i = 0; i < strLen; i++) {
            tmpArray[i] = tmpPw.charAt(i);
        }

        // Validation Array
        for (int i = 0; i < strLen - 2; i++) {
            // 첫 글자 A-Z / 0-9
            if ((tmpArray[i] > 47
                    && tmpArray[i + 2] < 58)
                    || (tmpArray[i] > 64
                    && tmpArray[i + 2] < 91)) {
                // 배열의 연속된 수 검사
                // 3번째 글자 - 2번째 글자 = 1, 3번째 글자 - 1번째 글자 = 2
                checkArgument(!(Math.abs(tmpArray[i + 2] - tmpArray[i + 1]) == 1
                        && Math.abs(tmpArray[i + 2] - tmpArray[i]) == 2), "Detected: Continuous Pattern.", HttpStatus.NOT_ACCEPTABLE);

            }
        }

        return true;
    }
}
