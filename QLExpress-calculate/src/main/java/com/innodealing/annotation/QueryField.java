package com.innodealing.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 如果需要将一个java bean 作为查询参数去检索数据库，可以加上改注解
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface QueryField {
    /**
     * 数据model在数据库表中对应的列名称
     */
    String columnName() default "";

    /**
     * 过滤选项(等于(=)、大于(>)、小于(<)、LIKE、BETWEEN等)
     */
    String option() default "";

    /**
     * "连接条件(AND | OR),默认AND"
     */
    String joinType() default "AND";

    /**
     * 排序方式(ASC|DESC),默认ASC
     */
    String sort() default "ASC";
    
    /**
     * 字段所在表名
     */
    String tableName() default "";
    
    /**
     * 
     * 是否需要排序
     */
    boolean isSort() default false;

}
