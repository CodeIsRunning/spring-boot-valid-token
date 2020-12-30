package liuxf.live.annotation;

import java.lang.annotation.*;

/**
 * @author liuxf
 * @version 1.0
 * @date 2020/12/29 14:53
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidToken {

    /**
     * 角色
     * @return
     */
    String [] roles() default "";

}
