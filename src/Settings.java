import java.io.Serializable;

@SuppressWarnings("serial")
public class Settings implements Serializable {
    public boolean loadOnStartup = new Boolean(false);	//автоматически загружать список при начале работы программы
    public boolean saveOnExit = new Boolean(false);		//автоматически сохранять список при завершении программы
    public Boolean resetAfterSave = new Boolean(false);	//переходить к созданию нового элемента при нажати кнопки "Сохранить" диалога ItemDialog
    public Integer sortingFieldIndex = 0;				//порядковый номер поля класса Car, по которому следует осуществлять сортировку
    public CollectionComparator.Order sortingOrder = CollectionComparator.Order.Alphabetical;	
}
