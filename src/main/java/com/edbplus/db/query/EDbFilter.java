package com.edbplus.db.query;


import java.io.Serializable;


/**
 * @program: EDbFilter
 * @description: 筛选辅助类
 * @author: 杨志佳
 * @create: 2020-04-19 15:05
 **/
public class EDbFilter implements Serializable {

    private static final long serialVersionUID = -8712382358441065075L;

    /**
     * 运算符
     */
    public enum Operator {

        /** 等于 */
        eq(" = "),

        /** 不等于 ，部分数据库不支持 != */
        ne(" <> "),

        /** 大于 */
        gt(" > "),

        /** 小于 */
        lt(" < "),

        /** 大于等于 */
        ge(" >= "),

        /** 小于等于 */
        le(" <= "),

        /** 类似 */
        like(" like "),

        /** 包含 */
        in(" in "),

        /**
         * 不包含
         */
        notIn(" not in "),

        /** 为Null */
        isNull(" is NULL "),

        /** 不为Null */
        isNotNull(" is not NULL ");

//        /** 正则表达式 */
//        reg(" REGEXP ");

        Operator(String operator) {
            this.operator = operator;
        }

        private String operator;

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }
    }


    /** 属性 */
    private String property;

    /** 运算符 */
    private EDbFilter.Operator operator;

    /** 值 */
    private Object value;


    /**
     * 构造方法
     */
    public EDbFilter() {
    }

    /**
     * 构造方法
     *
     * @param property
     *            属性
     * @param operator
     *            运算符
     * @param value
     *            值
     */
    public EDbFilter(String property, EDbFilter.Operator operator, Object value) {
        this.property = property;
        this.operator = operator;
        this.value = value;
    }


    /**
     * 返回等于筛选
     *
     * @param property
     *            属性
     * @param value
     *            值
     * @return 等于筛选
     */
    public static EDbFilter eq(String property, Object value) {
        return new EDbFilter(property, EDbFilter.Operator.eq, value);
    }


    /**
     * 返回不等于筛选
     *
     * @param property
     *            属性
     * @param value
     *            值
     * @return 不等于筛选
     */
    public static EDbFilter ne(String property, Object value) {
        return new EDbFilter(property, EDbFilter.Operator.ne, value);
    }



    /**
     * 返回大于筛选
     *
     * @param property
     *            属性
     * @param value
     *            值
     * @return 大于筛选
     */
    public static EDbFilter gt(String property, Object value) {
        return new EDbFilter(property, EDbFilter.Operator.gt, value);
    }

    /**
     * 返回小于筛选
     *
     * @param property
     *            属性
     * @param value
     *            值
     * @return 小于筛选
     */
    public static EDbFilter lt(String property, Object value) {
        return new EDbFilter(property, EDbFilter.Operator.lt, value);
    }

    /**
     * 返回大于等于筛选
     *
     * @param property
     *            属性
     * @param value
     *            值
     * @return 大于等于筛选
     */
    public static EDbFilter ge(String property, Object value) {
        return new EDbFilter(property, EDbFilter.Operator.ge, value);
    }

    /**
     * 返回小于等于筛选
     *
     * @param property
     *            属性
     * @param value
     *            值
     * @return 小于等于筛选
     */
    public static EDbFilter le(String property, Object value) {
        return new EDbFilter(property, EDbFilter.Operator.le, value);
    }

    /**
     * 返回相似筛选
     *
     * @param property
     *            属性
     * @param value
     *            值
     * @return 相似筛选
     */
    public static EDbFilter like(String property, Object value) {
        return new EDbFilter(property, EDbFilter.Operator.like, value);
    }


    /**
     * 返回包含筛选
     *
     * @param property
     *            属性
     * @param value
     *            值
     * @return 包含筛选
     */
    public static EDbFilter in(String property, Object value) {
        return new EDbFilter(property, EDbFilter.Operator.in, value);
    }

    /**
     * 返回不包含筛选
     * @param property 属性
     * @param value 值
     * @return 包含不筛选
     */
    public static EDbFilter notIn(String property, Object value) {
        return new EDbFilter(property, Operator.notIn, value);
    }

    /**
     * 返回为Null筛选
     *
     * @param property
     *            属性
     * @return 为Null筛选
     */
    public static EDbFilter isNull(String property) {
        return new EDbFilter(property, EDbFilter.Operator.isNull, null);
    }

    /**
     * 返回不为Null筛选
     *
     * @param property
     *            属性
     * @return 不为Null筛选
     */
    public static EDbFilter isNotNull(String property) {
        return new EDbFilter(property, EDbFilter.Operator.isNotNull, null);
    }

    /**
     * 返回忽略大小写筛选
     *
     * @return 忽略大小写筛选
     */
    public EDbFilter ignoreCase() {
        return this;
    }

    /**
     * 获取属性
     *
     * @return 属性
     */
    public String getProperty() {
        return property;
    }

    /**
     * 设置属性
     *
     * @param property
     *            属性
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * 获取运算符
     *
     * @return 运算符
     */
    public EDbFilter.Operator getOperator() {
        return operator;
    }

    /**
     * 设置运算符
     *
     * @param operator
     *            运算符
     */
    public void setOperator(EDbFilter.Operator operator) {
        this.operator = operator;
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public Object getValue() {
        return value;
    }

    /**
     * 设置值
     *
     * @param value
     *            值
     */
    public void setValue(Object value) {
        this.value = value;
    }





}

