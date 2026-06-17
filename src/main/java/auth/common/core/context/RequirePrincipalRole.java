package auth.common.core.context;

import auth.common.core.constant.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePrincipalRole {
    String[] anyOf() default {UserRole.USER};

    boolean requireUserKey() default true;
}
