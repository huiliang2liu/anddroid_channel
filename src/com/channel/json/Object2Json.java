package com.channel.json;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Object2Json {

    @SuppressWarnings("unchecked")
    public static String map2json(Map<String, Object> map) {
        StringBuffer sb = new StringBuffer("{");
        if (map != null && map.size() > 0) {
            Set<String> set = map.keySet();
            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                sb.append("\"").append(key).append("\":");
                Object object = map.get(key);
                if (object instanceof Map)
                    sb.append(map2json((Map<String, Object>) object));
                else if (object instanceof List)
                    sb.append(list2json((List<Object>) object));
                else if (object instanceof String || object instanceof Float
                        || object instanceof Double)
                    sb.append("\"").append(object).append("\"");
                else if (object instanceof Integer || object instanceof Long
                        || object instanceof Boolean)
                    sb.append(object);
                else
                    sb.append(object2json(object));
                sb.append(",");
            }
        }
        sb.delete(sb.length() - 1, sb.length());
        sb.append("}");
        return sb.toString();
    }
    public static String list2json(List<Object> list) {
        StringBuffer sb = new StringBuffer("[");
        if (list != null && list.size() > 0) {
            for (Object object : list) {
                if (object instanceof Map)
                    sb.append(map2json((Map<String, Object>) object));
                else if (object instanceof List)
                    sb.append(list2json((List<Object>) object));
                else if (object instanceof String || object instanceof Float
                        || object instanceof Double)
                    sb.append("\"").append(object).append("\"");
                else if (object instanceof Integer || object instanceof Long
                        || object instanceof Boolean)
                    sb.append(object);
                else
                    sb.append(object2json(object));
                sb.append(",");
            }
        }
        sb.delete(sb.length() - 1, sb.length());
        sb.append("]");
        return sb.toString();
    }

    public static String object2json(Object object) {
        if (object == null)
            return "";
        try {
            if (object instanceof Map)
                return map2json((Map<String, Object>) object);
            if (object instanceof List)
                return list2json((List<Object>) object);
            StringBuffer sb = new StringBuffer("{");
            Class c = object.getClass();
            Field[] fields = c.getFields();
            if (fields != null && fields.length > 0) {
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    field.setAccessible(true);
                    if (field.getName().equals("this$0"))
                        continue;
                    sb.append("\"").append(field.getName()).append("\"")
                            .append(":");
                    Object object2 = field.get(object);
                    if (Map.class.isAssignableFrom(field.getType())) {
                        sb.append(map2json((Map<String, Object>) object2));
                    } else if (List.class.isAssignableFrom(field.getType())) {
                        sb.append(list2json((List<Object>) object2));
                    } else {
                        if (String.class.isAssignableFrom(field.getType()) || Float.class.isAssignableFrom(field.getType()) || float.class.isAssignableFrom(field.getType()) || Double.class.isAssignableFrom(field.getType()) || double.class.isAssignableFrom(field.getType())) {
                            if (object2 == null)
                                sb.append("\"").append("\"");
                            else
                                sb.append("\"").append(object2).append("\"");
                        } else if (Character.class.isAssignableFrom(field.getType()) || char.class.isAssignableFrom(field.getType()) || Short.class.isAssignableFrom(field.getType()) || short.class.isAssignableFrom(field.getType()) || Integer.class.isAssignableFrom(field.getType()) || int.class.isAssignableFrom(field.getType()) || Long.class.isAssignableFrom(field.getType()) || long.class.isAssignableFrom(field.getType()) || Boolean.class.isAssignableFrom(field.getType()) || boolean.class.isAssignableFrom(field.getType())) {
                            if (object2 != null)
                                sb.append(object2);
                        } else {
                            if (object2 == null)
                                sb.append("null");
                            else {
                                System.out.println(field.getType());
                                if (field.getType().isEnum()) {
                                    sb.append(object2.toString());
                                } else
                                    sb.append(object2json(object2));
                            }

                        }
                    }
                    sb.append(",");
                }
            }
            sb.delete(sb.length() - 1, sb.length());
            sb.append("}");
            return sb.toString();

        } catch (Exception e) {
            // TODO: handle exception
        }
        return "";
    }

}
