package eu.decentsoftware.holograms.api.utils.reflect;

import eu.decentsoftware.holograms.api.utils.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @deprecated For removal with {@link eu.decentsoftware.holograms.api.nms.NMS}.
 */
@Deprecated
public class ReflectConstructor {

    private final Class<?> clazz;
    private final Class<?>[] parameterTypes;

    private Constructor<?> constructor;

    public ReflectConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        this.clazz = clazz;
        this.parameterTypes = parameterTypes;
    }

    private void init() {
        if (constructor != null) return;
        try {
            constructor = clazz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            Log.error("无法为类 %s 找到具有参数类型 %s 的构造函数",
                    e, clazz.getName(), parameterTypesToString());
        }
    }

    public <T> T newInstance(Object... args) {
        this.init();

        Object object = null;
        try {
            object = constructor.newInstance(args);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            Log.error("无法为类 %s 创建具有参数类型 %s 的新实例",
                    e, clazz.getName(), parameterTypesToString());
        }
        return object == null ? null : (T) object;
    }

    private String parameterTypesToString() {
        StringBuilder builder = new StringBuilder();
        for (Class<?> parameterType : parameterTypes) {
            builder.append(parameterType.getName()).append(", ");
        }
        return builder.toString();
    }

}
