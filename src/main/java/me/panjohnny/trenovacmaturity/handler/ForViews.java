package me.panjohnny.trenovacmaturity.handler;

import me.panjohnny.trenovacmaturity.View;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ForViews {
    View[] value();
}
