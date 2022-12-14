package com.glsx.plat.common.utils;

import com.glsx.plat.common.annotation.ReadOnly;
import com.glsx.plat.common.model.ParameterAnnotationHolder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReflectUtils {

    public static <T> List<T> castEntity(List<Object[]> list, Class<T> clazz) throws Exception {
        List<T> returnList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) return returnList;

        List<Class<?>> allFieldsList = getAllFieldsList(clazz);
        Class[] mod = allFieldsList.toArray(new Class[list.get(0).length]);
        for (Object[] co : list) {
            Constructor<T> constructor = clazz.getConstructor(mod);
            returnList.add(constructor.newInstance(co));
        }
        return returnList;
    }

    public static List<Class<?>> getAllFieldsList(final Class<?> cls) {
        Validate.isTrue(cls != null, "The class must not be null");
        final List<Class<?>> allFields = new ArrayList<>();
        Class<?> currentClass = cls;
        while (currentClass != null) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            for (final Field field : declaredFields) {
                allFields.add(field.getType());
            }
            currentClass = currentClass.getSuperclass();
        }
        return allFields;
    }

    public static <T, E> T getValueOfMethodAnnotation(Method method, Class<Annotation> annotation) {
        ParameterAnnotationHolder parameterAnnotationHolder = getAnnotationFromMethodParameters(method, annotation);
        Integer type = parameterAnnotationHolder.getType();
        if (type == 1) {
            String parameterName = parameterAnnotationHolder.getParameterName();

        } else if (type == 2) {
            return null;
        }
        return null;
    }

    /**
     * ??????????????????&??????????????????????????????????????????
     *
     * @param annotation
     * @param <T>
     * @return
     */
    public static <T> ParameterAnnotationHolder getAnnotationFromMethodParameters(Method method, Class<Annotation> annotation) {

        ParameterAnnotationHolder parameterAnnotationHolder = getAnnotationBeforeMethodParameters(method, annotation);

        return parameterAnnotationHolder == null ? getAnnotationFromMethodParametersEntityField(method, annotation) : parameterAnnotationHolder;
    }

    public static void main(String[] args) {
        ReflectUtils.testInParameter(new ParameterAnnotationHolder().setType(1));
    }

    @ReadOnly
    public static void testInParameter(ParameterAnnotationHolder inPara) {

    }

    /**
     * ????????????????????????????????????????????????????????????
     *
     * @param method
     * @param annotation
     * @return
     */
    public static ParameterAnnotationHolder getAnnotationFromMethodParametersEntityField(Method method, Class<Annotation> annotation) {

        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            Class<? extends Parameter> parameterClass = parameter.getClass();
            Field[] fields = parameterClass.getDeclaredFields();

            for (Field field : fields) {
                if (field.getDeclaredAnnotation(annotation) != null) {
                    field.setAccessible(true);
                    return new ParameterAnnotationHolder().setType(2).setParameterName(parameter.getName()).setFieldName(field.getName());
                }
            }

            Class<?> superClazz = parameterClass.getSuperclass();
            if (superClazz != null) {
                Field[] superFields = superClazz.getDeclaredFields();
                for (Field superField : superFields) {
                    if (superField.getDeclaredAnnotation(annotation) != null) {
                        superField.setAccessible(true);
                        return new ParameterAnnotationHolder().setType(2).setParameterName(parameter.getName()).setFieldName(superField.getName());
                    }
                }
            }
        }

        return null;
    }

    /**
     * ??????????????????????????????????????????????????????????????????
     *
     * @param method
     * @param annotation
     * @return
     */
    public static ParameterAnnotationHolder getAnnotationFromMethodParametersGenericField(Method method, Class<Annotation> annotation) {

        return null;
    }

    /**
     * ??????????????????????????????
     * list<string> or string
     *
     * @param method
     * @param annotation
     * @return
     */
    public static ParameterAnnotationHolder getAnnotationBeforeMethodParameters(Method method, Class<Annotation> annotation) {
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            if (null != parameter.getDeclaredAnnotation(annotation)) {
//                parameter.getType()
                return new ParameterAnnotationHolder().setType(1).setParameterName(parameter.getName());
            }
        }
        return null;
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param method
     * @param annotation
     * @return
     */
    public static ParameterAnnotationHolder getAnnotationBeforeMethodGenericParameters(Method method, Class<Annotation> annotation) {
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            if (null != parameter.getDeclaredAnnotation(annotation)) {

                return new ParameterAnnotationHolder().setType(1).setParameterName(parameter.getName());
            }
        }
        return null;
    }


    public static void getObjectValue(Object object) throws Exception {
        if (object != null) {
            Class<?> clz = object.getClass();
            Field[] fields = clz.getDeclaredFields();

            for (Field field : fields) {
                if (field.getGenericType().toString().equals(
                        "class java.lang.String")) {
                    // ??????type??????????????????????????????"class "????????????????????? ??????????????????gettet??????
                    Method m = object.getClass().getMethod(
                            "get" + getMethodName(field.getName()));

                    // ??????getter?????????????????????
                    String val = (String) m.invoke(object);
                    if (val != null) {
                        System.out.println("String type:" + val);
                    }
                }

                // ???????????????Integer
                if (field.getGenericType().toString().equals(
                        "class java.lang.Integer")) {
                    Method m = (Method) object.getClass().getMethod(
                            "get" + getMethodName(field.getName()));
                    Integer val = (Integer) m.invoke(object);
                    if (val != null) {
                        System.out.println("Integer type:" + val);
                    }

                }

                // ???????????????Double
                if (field.getGenericType().toString().equals(
                        "class java.lang.Double")) {
                    Method m = (Method) object.getClass().getMethod(
                            "get" + getMethodName(field.getName()));
                    Double val = (Double) m.invoke(object);
                    if (val != null) {
                        System.out.println("Double type:" + val);
                    }

                }

                // ???????????????Boolean ????????????
                if (field.getGenericType().toString().equals(
                        "class java.lang.Boolean")) {
                    Method m = (Method) object.getClass().getMethod(
                            field.getName());
                    Boolean val = (Boolean) m.invoke(object);
                    if (val != null) {
                        System.out.println("Boolean type:" + val);
                    }

                }

                // ???????????????boolean ??????????????????????????? ???????????????????????????????????? isXXX??? ???????????????isXXX???
                // ???????????????getter????????????
                if (field.getGenericType().toString().equals("boolean")) {
                    Method m = (Method) object.getClass().getMethod(
                            field.getName());
                    Boolean val = (Boolean) m.invoke(object);
                    if (val != null) {
                        System.out.println("boolean type:" + val);
                    }

                }
                // ???????????????Date
                if (field.getGenericType().toString().equals(
                        "class java.util.Date")) {
                    Method m = (Method) object.getClass().getMethod(
                            "get" + getMethodName(field.getName()));
                    Date val = (Date) m.invoke(object);
                    if (val != null) {
                        System.out.println("Date type:" + val);
                    }

                }
                // ???????????????Short
                if (field.getGenericType().toString().equals(
                        "class java.lang.Short")) {
                    Method m = (Method) object.getClass().getMethod(
                            "get" + getMethodName(field.getName()));
                    Short val = (Short) m.invoke(object);
                    if (val != null) {
                        System.out.println("Short type:" + val);
                    }

                }
                // ????????????????????????????????????????????????

            }//for() --end

        }//if (object!=null )  ----end
    }

    private static String getMethodName(String fildeName) throws Exception {
        byte[] items = fildeName.getBytes();
        items[0] = (byte) ((char) items[0] - 'a' + 'A');
        return new String(items);
    }
}
