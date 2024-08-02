package com.convelming.roadflow.handler.jdbc;

import com.alibaba.fastjson2.JSON;
import com.easy.query.core.basic.jdbc.executor.internal.merge.result.StreamResultSet;
import com.easy.query.core.basic.jdbc.executor.internal.props.JdbcProperty;
import com.easy.query.core.basic.jdbc.types.EasyParameter;
import com.easy.query.core.basic.jdbc.types.handler.JdbcTypeHandler;

import java.sql.SQLException;

public class JsonTypeHandler implements JdbcTypeHandler {

    private static final String DEFAULT = "{}";
    public JsonTypeHandler() {
    }

    @Override
    public Object getValue(JdbcProperty jdbcProperty, StreamResultSet streamResultSet) throws SQLException {
        String json = streamResultSet.getString(jdbcProperty.getJdbcIndex());
        if (json != null) {
            return JSON.parse(json);
        } else {
            return DEFAULT;
        }
    }

    @Override
    public void setParameter(EasyParameter parameter) throws SQLException {
        String json = JSON.toJSONString(parameter.getValue());
        parameter.getPs().setString(parameter.getIndex(), json);
    }
}
