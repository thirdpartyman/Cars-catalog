import java.io.Serializable;
import java.lang.reflect.Field;

public class Car implements Serializable, Cloneable {
	private String brand;			//марка
	private String model;			//модель
	private short enginePower;		//мощность двигател€, л.с.
	private float engineVolume;		//объЄм двигател€, л
	private short maxSpeed;			//максимальна€ скорость, км/ч
	private float fuelConsumption;	//расход топлива, л/100км
	
	Car()
	{
		this.brand = "";
		this.model = "";
		this.enginePower = 0;
		this.engineVolume = 0;
		this.maxSpeed = 0;
		this.fuelConsumption = 0;
	}
	
	Car(String brand,
		String model,
		int enginePower,
		double engineVolume,
		int maxSpeed,
		double fuelConsumption) throws Exception
	{
		setBrand(brand);
		setModel(model);
		setEnginePower(enginePower);
		setEngineVolume(engineVolume);
		setMaxSpeed(maxSpeed);
		setFuelConsumption(fuelConsumption);
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) throws Exception {
		if (!brand.isEmpty())
			this.brand = brand;
		else throw new Exception("Ќазвание бренда не должно быть пустым");
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) throws Exception {
		if (!model.isEmpty())
			this.model = model;
		else throw new Exception("Ќазвание модели не должно быть пустым");
	}

	public short getEnginePower() {
		return enginePower;
	}

	public void setEnginePower(int enginePower) throws Exception {
		if (enginePower > 0 && enginePower <= Short.MAX_VALUE)
			this.enginePower = (short)enginePower;
		else throw new Exception("¬еличина расхода топлива должна быть положительным числом, не превышающим 32767");
	}

	public float getEngineVolume() {
		return engineVolume;
	}

	public void setEngineVolume(double engineVolume) throws Exception {
		if (engineVolume > 0 && engineVolume <= Float.MAX_VALUE)
			this.engineVolume = (float)engineVolume;
		else throw new Exception("¬еличина расхода топлива должна быть положительным числом");
	}

	public short getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) throws Exception {
		if (maxSpeed > 0 && maxSpeed <= Short.MAX_VALUE)
			this.maxSpeed = (short)maxSpeed;
		else throw new Exception("¬еличина расхода топлива должна быть положительным числом, не превышающим 32767");
	}

	public float getFuelConsumption() {
		return fuelConsumption;
	}

	public void setFuelConsumption(double fuelConsumption) throws Exception {
		if (fuelConsumption > 0 && fuelConsumption <= Float.MAX_VALUE)
			this.fuelConsumption = (float)fuelConsumption;
		else throw new Exception("¬еличина расхода топлива должна быть положительным числом");
	}

	
	public String toString() {
		return JSON.toString(this);
	}
	
	
	public Boolean isEqual(Car car)
	{
		return brand.equals(car.brand) 
			&& model.equals(car.model) 
			&& enginePower == car.enginePower
			&& engineVolume == car.engineVolume
			&& maxSpeed == car.maxSpeed
			&& fuelConsumption == car.fuelConsumption;
	}
	
	
	public Car clone() throws CloneNotSupportedException{     
		return (Car) super.clone();
	}
}



//сериализаци€ объект в json-строку
class JSON {
	static String tab = "    ";

	public static String toString(Object obj) {
		StringBuilder res = new StringBuilder();
		toString(obj, "", res);
		return res.toString();
	}

	private static void toString(Object obj, String tabString, StringBuilder res) {
		Class myClass = obj.getClass();
		res.append("{\n");
		Field[] fields = myClass.getDeclaredFields();
		for (Field field : fields) {
			Object value;
			try {
				value = field.get(obj);
			} catch (IllegalAccessException e) {
				value = null;
			}
			String name = field.getName();

			if (isPrimitive(field.getType()))
				res.append(String.format(tabString + tab + "%s : %s,\n", name, value));
			else {
				res.append(String.format(tabString + tab + "%s : ", name));
				toString(value, tabString + tab, res);
				res.append('\n');
			}
		}
		res.append(tabString + "}");
	}

	private static <T> boolean isPrimitive(Class<T> myclass) {
		return myclass.isPrimitive() || myclass.isEnum() || myclass == Integer.class || myclass == Long.class
				|| myclass == Float.class || myclass == Double.class || myclass == Boolean.class
				|| myclass == String.class;
	}
}
