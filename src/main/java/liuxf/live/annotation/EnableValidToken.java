package liuxf.live.annotation;

import liuxf.live.aop.ValidTokenAspect;
import liuxf.live.service.UserTokenDetailService;
import liuxf.live.service.impl.UserTokenDetailServiceImpl;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author liuxf
 * @version 1.0
 * @date 2020/12/29 14:57
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import({ValidTokenAspect.class, UserTokenDetailServiceImpl.class})
public @interface EnableValidToken {
}
