package liuxf.live.exception;

import liuxf.live.enums.ValidTokenExceptionEnum;

import java.io.Serializable;

/**
 * @author liuxf
 * @version 1.0
 * @date 2020/12/29 15:31
 */
public class ValidTokenException extends RuntimeException implements Serializable {

    private ValidTokenExceptionEnum validTokenExceptionEnum;

    public ValidTokenException() {
    }

    public ValidTokenException(ValidTokenExceptionEnum validTokenExceptionEnum) {
        super(validTokenExceptionEnum.getDescription());
        this.validTokenExceptionEnum = validTokenExceptionEnum;
    }

    public ValidTokenExceptionEnum getValidTokenExceptionEnum() {
        return validTokenExceptionEnum;
    }
}
