package com.webproject.ourpoint.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.regex.Pattern.matches;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class EmailFormatValidation {

    public static boolean checkAddress(String address) {
        checkArgument(isNotEmpty(address), "address must be provided.");

        return matches("[\\w~\\-.+]+@[\\w~\\-]+(\\.[\\w~\\-]+)+", address);
    }

}
