package com.example;

import com.willowtreeapps.apptest.FormFactor;
import com.willowtreeapps.apptest.Platform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@java.lang.annotation.Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RunOn {
    Platform platform() default Platform.ALL;

    FormFactor formFactor() default FormFactor.ALL;
}
