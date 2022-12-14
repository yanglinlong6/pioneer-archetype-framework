package com.glsx.plat.mybatis.interceptor;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.util.StringUtils;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Properties;
import java.util.Set;

//@Component
//@Intercepts({@org.apache.ibatis.plugin.Signature(type = Executor.class, method = "query",
//        args = {
//                MappedStatement.class,
//                Object.class,
//                RowBounds.class,
//                ResultHandler.class,
//                CacheKey.class,
//                BoundSql.class})})
public class QueryPermitLinkInterceptor implements Interceptor {


//    private SubTableHandler subTableHandler;

    CCJSqlParserManager parserManager = new CCJSqlParserManager();
    private Set<String> handleSqlIds;

    private static void modify(Object object, String fieldName, Object newFieldValue) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(object, newFieldValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String resetSql(String sql) {
        return "";
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        Method method = invocation.getMethod();

//        LinkPermitQuery readWrite = method.getAnnotation(LinkPermitQuery.class);
//        if (readWrite == null){
//            return invocation.proceed();
//        }

        StatementHandler handler = (StatementHandler) invocation.getTarget();

        MetaObject statementHandler = SystemMetaObject.forObject(handler);
        //mappedStatement???????????????????????????id
        MappedStatement mappedStatement = (MappedStatement) statementHandler.getValue("delegate.mappedStatement");

        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();

        //???????????? select ,??????????????????
        if (sqlCommandType != SqlCommandType.SELECT) {
            return invocation.proceed();
        }

        //?????? sql ?????? ,??????????????????
        BoundSql boundSql = handler.getBoundSql();
        String sql = boundSql.getSql();
        Select select = (Select) parserManager.parse(new StringReader(sql));
        SelectBody selectBody = select.getSelectBody();
        PlainSelect plainSelect = (PlainSelect) selectBody;

        //?????? sql
        Expression where = plainSelect.getWhere();
        if (where == null) {
            // ?????? ?????? where ,??????????????????????????????
            where = new StringValue("1");
        }
        Expression parenthesis = new Parenthesis(where);
        EqualsTo equalsTo = new EqualsTo();
//        equalsTo.accept();
        AndExpression andExpression = new AndExpression(parenthesis, equalsTo);
        plainSelect.setWhere(andExpression);
        //???????????????sql??????
        statementHandler.setValue("delegate.boundSql.sql", select.toString());

        resetSql(invocation);
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    private void resetSql(Invocation invocation) {
        final Object[] args = invocation.getArgs();
        BoundSql boundSql = (BoundSql) args[5];
        if (!StringUtils.isEmpty(boundSql.getSql())) {
            modify(boundSql, "sql", resetSql(boundSql.getSql()));
        }
    }
}
