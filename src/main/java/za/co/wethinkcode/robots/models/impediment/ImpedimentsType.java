package za.co.wethinkcode.robots.models.impediment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ImpedimentsType
{@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CannotGoThrough {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CanGoThrough {}}