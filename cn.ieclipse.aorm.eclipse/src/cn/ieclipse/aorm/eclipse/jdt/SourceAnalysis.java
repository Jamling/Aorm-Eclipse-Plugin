/*
 * Copyright 2010 Android ORM projects.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ieclipse.aorm.eclipse.jdt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMemberValuePair;

import cn.ieclipse.aorm.eclipse.jdt.JavaSelection.TypeMapping;

/**
 * @author Jamling
 * 
 */
public class SourceAnalysis {
    public static final Map<String, String> signatures = new HashMap<String, String>();
    public static final Map<Class<?>, String> typeClasses = new HashMap<Class<?>, String>();
    public static final String LF = System.getProperty("line.separator");
    static {
        typeClasses.put(int.class, "Integer");
        typeClasses.put(boolean.class, "Integer");
        typeClasses.put(float.class, "Float");
        typeClasses.put(short.class, "Short");
        typeClasses.put(double.class, "Double");
        typeClasses.put(long.class, "Long");
        typeClasses.put(byte[].class, "Blob");
        typeClasses.put(Byte[].class, "Blob");

        typeClasses.put(Integer.class, "Integer");
        typeClasses.put(String.class, "String");
        typeClasses.put(Double.class, "Double");
        typeClasses.put(Float.class, "Float");
        typeClasses.put(Short.class, "Short");
        typeClasses.put(Long.class, "Long");
    }

    public static String getColumnBySig(String sig) {
        if (signatures.isEmpty()) {
            signatures.put("I", "Integer");
            signatures.put("QInteger;", "Integer");

            signatures.put("F", "Float");
            signatures.put("QFloat;", "Float");

            signatures.put("J", "Long");
            signatures.put("QLong;", "Long");

            signatures.put("S", "Short");
            signatures.put("QShort;", "Short");

            signatures.put("Z", "Boolean");
            signatures.put("QBoolean;", "Boolean");

            signatures.put("[B", "Blob");
            signatures.put("[QByte;", "Blob");

            signatures.put("QString;", "String");

            signatures.put("D", "Double");
            signatures.put("QDouble;", "Double");
        }
        String value = signatures.get(sig);
        return value;
    }

    public static String getSQL(TypeMapping typeMapping, boolean format) {
        StringBuilder sb = new StringBuilder();
        sb.append("create TABLE ");
        sb.append(typeMapping.getTable());
        sb.append(" (  ");
        if (format) {
            sb.append(LF);
        }
        try {
            IField[] fds = typeMapping.getType().getFields();
            for (IField iField : fds) {
                String fieldType = iField.getTypeSignature();
                IAnnotation nc = iField.getAnnotation("Column");
                if (nc != null && nc.exists()) {
                    ColumnMeta meta = new ColumnMeta();
                    meta.type = getColumnBySig(fieldType);
                    IMemberValuePair[] mvps = nc.getMemberValuePairs();
                    for (IMemberValuePair imvp : mvps) {
                        String mn = imvp.getMemberName();
                        Object mv = imvp.getValue();
                        if ("name".equals(mn)) {
                            meta.name = (String) mv;
                        } else if ("id".equals(mn)) {
                            meta.id = (Boolean) mv;
                            meta.haveId = true;
                        } else if ("notNull".equals(mn)) {
                            meta.notNull = (Boolean) mv;
                            meta.haveNotNull = true;
                        } else if ("defaultValue".equals(mn)) {
                            meta.defaultValue = (String) mv;
                            meta.haveDefaultValue = true;
                        }
                    }
                    sb.append(meta.toSQL());
                    sb.append(", ");
                    if (format) {
                        sb.append(LF);
                    }
                }// end if
            }// end for
            int len = sb.length() - 2;
            if (format) {
                len = len - LF.length();
            }
            sb.delete(len, sb.length());
            sb.append(")");
            sb.append(LF);

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return sb.toString();
    }

    private static class ColumnMeta {
        String name;
        String type;

        boolean haveDefaultValue;
        String defaultValue;

        boolean haveNotNull;
        boolean notNull;

        boolean haveId;
        boolean id;

        public String toSQL() {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            sb.append(' ');
            sb.append(type);

            if (haveId && id) {
                sb.append(' ');
                sb.append("Primary key autoincrement");
            }
            if (haveNotNull) {
                sb.append(' ');
                sb.append(notNull ? "NOT NULL " : "");
                sb.append("Default '");
                sb.append(defaultValue == null ? "" : defaultValue);
                sb.append("'");
            } else {
                if (haveDefaultValue) {
                    sb.append(' ');
                    sb.append("Default '");
                    sb.append(defaultValue == null ? "" : defaultValue);
                    sb.append("'");
                }
            }
            return sb.toString();
        }
    }
}
