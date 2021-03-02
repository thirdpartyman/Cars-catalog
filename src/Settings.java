import java.io.Serializable;

@SuppressWarnings("serial")
public class Settings implements Serializable {
    public boolean loadOnStartup = new Boolean(false);	//������������� ��������� ������ ��� ������ ������ ���������
    public boolean saveOnExit = new Boolean(false);		//������������� ��������� ������ ��� ���������� ���������
    public Boolean resetAfterSave = new Boolean(false);	//���������� � �������� ������ �������� ��� ������ ������ "���������" ������� ItemDialog
    public Integer sortingFieldIndex = 0;				//���������� ����� ���� ������ Car, �� �������� ������� ������������ ����������
    public CollectionComparator.Order sortingOrder = CollectionComparator.Order.Alphabetical;	
}
