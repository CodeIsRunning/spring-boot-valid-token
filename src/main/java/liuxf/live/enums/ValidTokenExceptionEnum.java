package liuxf.live.enums;

/**
 * @author liuxf
 * @version 1.0
 * @date 2020/12/29 15:33
 */
public enum  ValidTokenExceptionEnum {

    HTTP_401(401,"未授权"),HTTP_403(403,"拒绝访问");

    private Integer code;

    private String description;

    ValidTokenExceptionEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
