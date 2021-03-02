import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

//Generic компаратор
//—равнивает объекты по выбранному полю, которое задаетс€ текстовой строкой
//недостаток - корректно работает только дл€ полей примитивных типов
public class CollectionComparator implements Comparator<Object> {
 
	private String field;			//им€ пол€
    private int order;				//пор€док сортировки: 1 - по возрастанию, -1 - по убыванию
    private String methodPrefix;	//префикс метода получени€ пол€ (чаще всего - get)
    
    
    public CollectionComparator(String field, Order order) {
        this.field = field;
        this.order = order.getValue();
        this.methodPrefix = "get";
    }
 
    public CollectionComparator(String field, Order order, String prefix) {
        this.field = field;
        this.order = order.getValue();
        this.methodPrefix = prefix;
    }
 
    
	//сеттеры и геттеры
///////////////////////////////////////////////////////////////////////////////	
    public void setField(String field)
    {
        this.field = field;
    }
    
    public void setOrder(Order order)
    {
        this.order = order.getValue();
    }
    
    public void setMethodPrefix(String prefix)
    {
        this.methodPrefix = prefix;
    }
    
    public String getField()
    {
    	return field;
    }
    
    public Order getOrder()
    {
    	return Order.valueOf(order);
    }
    
    public String getMethodPrefix()
    {
        return methodPrefix;
    }
///////////////////////////////////////////////////////////////////////////////	
    
    
    @Override
    public int compare(Object one, Object two) {
 
        Method method = getMethod(one); 
        Class type = wrap(method.getReturnType());

        try {
        	
            int res = 0;
            if (type == Integer.class) {
            	res = ((Integer) method.invoke(one, null)).compareTo((Integer) method.invoke(two, null));
            } else if (type == Short.class) {
            	res = ((Short) method.invoke(one, null)).compareTo((Short) method.invoke(two, null));
            } else if (type == Long.class) {
            	res = ((Long) method.invoke(one, null)).compareTo((Long) method.invoke(two, null));
            } else if (type == Byte.class) {
            	res = ((Byte) method.invoke(one, null)).compareTo((Byte) method.invoke(two, null));
            } else if (type == Float.class) {
            	res = ((Float) method.invoke(one, null)).compareTo((Float) method.invoke(two, null));
            } else if (type == Double.class) {
            	res = ((Double) method.invoke(one, null)).compareTo((Double) method.invoke(two, null));
            } else if (type == Character.class) {
            	res = ((Character) method.invoke(one, null)).compareTo((Character) method.invoke(two, null));
            } else if (type == Boolean.class) {
            	res = ((Boolean) method.invoke(one, null)).compareTo((Boolean) method.invoke(two, null));
            } else if (type == String.class) {
            	res = ((String) method.invoke(one, null)).compareTo((String) method.invoke(two, null));
            } 
            else return (method.invoke(one, null)).toString().compareTo((method.invoke(two, null)).toString());
        
            return res * order;
            
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
 
        return 0;
    }
 
    //получение метода обекта по имени
    private Method getMethod(Object object) {
 
        Method method = null;     
        Method[] methods = object.getClass().getMethods();
 
        String methodName = methodPrefix + Character.toUpperCase(field.charAt(0)) + field.substring(1);
       
        for (int i = 0; i < methods.length; i++) {
 
            if (methodName.equals(methods[i].getName())) {
                method = methods[i];
                break;
            }
        }
        return method;
    }
    
    
    //получение ссылочного эквивалента примитивного типа
	private static <T> Class<T> wrap(Class<T> c) {
        return c.isPrimitive() ? (Class<T>) PRIMITIVES_TO_WRAPPERS.get(c) : c;
      }

    private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS
    = new HashMap<Class<?>, Class<?>>() {{
       put(boolean.class, Boolean.class);
       put(byte.class, Byte.class);
       put(char.class, Character.class);
       put(double.class, Double.class);
       put(float.class, Float.class);
       put(int.class, Integer.class);
       put(long.class, Long.class);
       put(short.class, Short.class);
       put(void.class, Void.class);
   }};
    
   
   //перечисление "ѕор€док сортировки"
   public enum Order{
   	Alphabetical(1),	//по возрастанию
   	Reverse(-1);		//по убыванию

       private int value;
       private static Map map = new HashMap<>();

       private Order(int value) {
           this.value = value;
       }

       static {
           for (Order order : Order.values()) {
               map.put(order.value, order);
           }
       }

       public static Order valueOf(int order) {
           return (Order) map.get(order);
       }

       public int getValue() {
           return value;
       }
   }

}