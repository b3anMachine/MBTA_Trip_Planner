import javax.swing.table.DefaultTableModel;

public class StopsTableModel extends DefaultTableModel implements Reorderable {
	private static final long serialVersionUID = 36544289767178149L;

	@Override
	public boolean isCellEditable(int row, int column) {
		//all cells false
		return false;
	}

	@Override
	public void reorder(int fromIndex, int toIndex) {
		
	}
}