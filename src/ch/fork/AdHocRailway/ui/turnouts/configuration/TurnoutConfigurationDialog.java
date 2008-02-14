/*------------------------------------------------------------------------
 * 
 * <./ui/switches/configuration/SwitchConfigurationDialog.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:21 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
 * language  : java
 * version   : $Id:TurnoutConfigurationDialog.java 130 2008-02-01 20:23:34Z fork_ch $
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/

package ch.fork.AdHocRailway.ui.turnouts.configuration;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.Turnout.TurnoutOrientation;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.TableResizer;
import ch.fork.AdHocRailway.ui.TutorialUtils;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.AbstractTableAdapter;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.SingleListSelectionAdapter;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TurnoutConfigurationDialog
		extends JDialog {

	private boolean							okPressed;

	private JList							turnoutGroupList;

	private JTable							turnoutsTable;

	private SelectionInList<Turnout>		turnoutModel;

	private TurnoutPersistenceIface			turnoutPersistence		=
																			AdHocRailway
																					.getInstance()
																					.getTurnoutPersistence();

	private JButton							addGroupButton;

	private JButton							removeGroupButton;

	private SelectionInList<TurnoutGroup>	turnoutGroupModel;

	private JButton							addTurnoutButton;

	private JButton							removeTurnoutButton;

	private JButton							okButton;

	private TurnoutGroupConfigPanel			turnoutGroupConfig;

	private TurnoutGroup					previousSelectedGroup	= null;

	public TurnoutConfigurationDialog(JFrame owner) {
		super(owner, "Turnout Configuration", true);
		initGUI();
	}

	private void initGUI() {
		buildPanel();
		pack();
		TutorialUtils.locateOnOpticalScreenCenter(this);
		setVisible(true);
	}

	private void buildPanel() {
		initComponents();
		initEventHandling();

		FormLayout layout =
				new FormLayout("pref, 10dlu, pref:grow",
						"pref, 3dlu, fill:pref:grow, 3dlu, pref, 3dlu, pref, 3dlu, pref");

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		builder.addSeparator("Turnout Groups", cc.xyw(1, 1, 1));

		builder.add(new JScrollPane(turnoutGroupList), cc.xy(1, 3));
		builder.add(turnoutGroupConfig, cc.xy(1, 5));
		builder.add(buildGroupButtonBar(), cc.xy(1, 7));

		builder.addSeparator("Turnouts", cc.xyw(3, 1, 1));
		builder.add(new JScrollPane(turnoutsTable), cc.xy(3, 3));
		builder.add(buildTurnoutButtonBar(), cc.xy(3, 7));

		builder.add(buildMainButtonBar(), cc.xyw(1, 9, 3));
		add(builder.getPanel());

	}

	private Component buildTurnoutButtonBar() {
		return ButtonBarFactory.buildCenteredBar(addTurnoutButton,
				removeTurnoutButton);
	}

	private Component buildGroupButtonBar() {
		return ButtonBarFactory.buildCenteredBar(addGroupButton,
				removeGroupButton);
	}

	private Component buildMainButtonBar() {
		return ButtonBarFactory.buildRightAlignedBar(okButton);
	}

	private void initComponents() {
		ArrayListModel<TurnoutGroup> turnoutGroups =
				turnoutPersistence.getAllTurnoutGroups();
		turnoutGroupModel =
				new SelectionInList<TurnoutGroup>((ListModel) turnoutGroups);

		turnoutGroupList = BasicComponentFactory.createList(turnoutGroupModel);
		turnoutGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		turnoutGroupList.setCellRenderer(new TurnoutGroupListCellRenderer());

		turnoutGroupConfig = new TurnoutGroupConfigPanel();

		addGroupButton = new JButton(new AddTurnoutGroupAction());
		removeGroupButton = new JButton(new RemoveTurnoutGroupAction());

		turnoutModel = new SelectionInList<Turnout>();
		turnoutsTable = new JTable();
		turnoutsTable.setModel(new TurnoutTableModel(turnoutModel));

		turnoutsTable.setSelectionModel(new SingleListSelectionAdapter(
				turnoutModel.getSelectionIndexHolder()));

		TableColumn typeColumn = turnoutsTable.getColumnModel().getColumn(1);
		typeColumn.setCellRenderer(new TurnoutTypeCellRenderer());

		TableColumn defaultStateColumn =
				turnoutsTable.getColumnModel().getColumn(8);
		defaultStateColumn
				.setCellRenderer(new TurnoutDefaultStateCellRenderer());
		turnoutsTable.setRowHeight(30);

		addTurnoutButton = new JButton(new AddTurnoutAction());
		removeTurnoutButton = new JButton(new RemoveTurnoutAction());

		okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					turnoutPersistence.flush();
				} catch (TurnoutPersistenceException e1) {
					ExceptionProcessor.getInstance().processException(e1);
				} finally {
					okPressed = true;
					setVisible(false);
				}
			}
		});
	}

	// TableModel *************************************************************

	/**
	 * Describes how to present an Album in a JTable.
	 */
	private static final class TurnoutTableModel
			extends AbstractTableAdapter<Turnout> {

		private static final String[]	COLUMNS	=
														{
																"#",
																"Type",
																"Bus 1",
																"Addr. 1",
																"Addr. 1 switched",
																"Bus 2",
																"Addr. 2",
																"Addr. 2 switched",
																"Default State",
																"Orientation",
																"Desc" };

		private TurnoutTableModel(ListModel listModel) {
			super(listModel, COLUMNS);
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Turnout turnout = getRow(rowIndex);
			switch (columnIndex) {
			case 0:
				return turnout.getNumber();
			case 1:
				return turnout.getTurnoutType();
			case 2:
				return turnout.getBus1();
			case 3:
				return turnout.getAddress1();
			case 4:
				return Boolean.valueOf(turnout.isAddress1Switched());
			case 5:
				return turnout.getBus2();
			case 6:
				return turnout.getAddress2();
			case 7:
				return Boolean.valueOf(turnout.isAddress2Switched());
			case 8:
				return turnout.getDefaultStateEnum();
			case 9:
				return turnout.getOrientationEnum();
			case 10:
				return turnout.getDescription();
			default:
				throw new IllegalStateException("Unknown column");
			}
		}

	}

	private void initEventHandling() {
		turnoutGroupList
				.addListSelectionListener(new TurnoutGroupSelectionHandler());
		turnoutsTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2
						&& e.getButton() == MouseEvent.BUTTON1) {
					new EditTurnoutAction().actionPerformed(null);
				}
			}

		});
	}

	/**
	 * Sets the selected group as bean in the details model.
	 */
	private final class TurnoutGroupSelectionHandler implements
			ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {

			if (e.getValueIsAdjusting())
				return;
			if (previousSelectedGroup != null) {
				turnoutPersistence.updateTurnoutGroup(previousSelectedGroup);

			}
			if (turnoutGroupList.getSelectedIndex() == -1)
				turnoutGroupList.setSelectedIndex(0);
			TurnoutGroup selectedGroup =
					(TurnoutGroup) turnoutGroupList.getSelectedValue();
			if (selectedGroup == null)
				return;
			previousSelectedGroup = selectedGroup;
			List<Turnout> turnouts =
					new ArrayList<Turnout>(selectedGroup.getTurnouts());
			turnoutGroupConfig.setTurnoutGroup(selectedGroup);
			turnoutModel.setList(turnouts);
			TableResizer.adjustColumnWidths(turnoutsTable, 5);
		}
	}

	/**
	 * Used to renders TurnoutGroups in JLists and JComboBoxes. If the combo box
	 * selection is null, an empty text <code>""</code> is rendered.
	 */
	private static final class TurnoutGroupListCellRenderer
			extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Component component =
					super.getListCellRendererComponent(list, value, index,
							isSelected, cellHasFocus);

			TurnoutGroup group = (TurnoutGroup) value;
			setText(group == null ? "" : (" " + group.getName()));
			return component;
		}
	}

	private class AddTurnoutGroupAction
			extends AbstractAction {
		public AddTurnoutGroupAction() {
			super("Add Group", ImageTools.createImageIcon("add.png"));
		}

		public void actionPerformed(ActionEvent arg0) {
			String newGroupName =
					JOptionPane.showInputDialog(
							TurnoutConfigurationDialog.this,
							"Enter the name of the new Turnout-Group",
							"Add Turnout-Group", JOptionPane.QUESTION_MESSAGE);
			TurnoutGroup newTurnoutGroup = new TurnoutGroup();
			newTurnoutGroup.setName(newGroupName);
			previousSelectedGroup = null;
			turnoutPersistence.addTurnoutGroup(newTurnoutGroup);
			turnoutGroupConfig.setTurnoutGroup(null);

		}
	}

	private class RemoveTurnoutGroupAction
			extends AbstractAction {
		public RemoveTurnoutGroupAction() {
			super("Remove Group", ImageTools.createImageIcon("remove.png"));
		}

		public void actionPerformed(ActionEvent arg0) {
			TurnoutGroup groupToDelete =
					(TurnoutGroup) (turnoutGroupList.getSelectedValue());
			if (groupToDelete == null) {
				JOptionPane.showMessageDialog(TurnoutConfigurationDialog.this,
						"Please select a Turnout-Group", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int response =
					JOptionPane.showConfirmDialog(
							TurnoutConfigurationDialog.this,
							"Really remove Turnout-Group '"
									+ groupToDelete.getName() + "' ?",
							"Remove Turnout-Group", JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.YES_OPTION) {
				try {
					previousSelectedGroup = null;
					turnoutPersistence.deleteTurnoutGroup(groupToDelete);
					turnoutGroupConfig.setTurnoutGroup(null);
				} catch (TurnoutException e) {
					ExceptionProcessor.getInstance().processExceptionDialog(e);
				}
			}
		}
	}

	private class AddTurnoutAction
			extends AbstractAction {
		public AddTurnoutAction() {
			super("Add", ImageTools.createImageIcon("add.png"));
		}

		public void actionPerformed(ActionEvent e) {
			TurnoutGroup selectedTurnoutGroup =
					(TurnoutGroup) (turnoutGroupList.getSelectedValue());
			if (selectedTurnoutGroup == null) {
				JOptionPane.showMessageDialog(TurnoutConfigurationDialog.this,
						"Please select a switch group", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			Turnout newTurnout = new Turnout();
			newTurnout.setNumber(turnoutPersistence.getNextFreeTurnoutNumber());
			newTurnout.setTurnoutGroup(selectedTurnoutGroup);
			newTurnout.setDefaultStateEnum(TurnoutState.STRAIGHT);
			newTurnout.setOrientationEnum(TurnoutOrientation.EAST);
			newTurnout.setTurnoutType(turnoutPersistence
					.getTurnoutType(TurnoutTypes.DEFAULT));
			TurnoutConfig switchConfig =
					new TurnoutConfig(TurnoutConfigurationDialog.this,
							newTurnout);
			if (switchConfig.isOkPressed()) {
				List<Turnout> turnouts =
						new ArrayList<Turnout>(selectedTurnoutGroup
								.getTurnouts());
				turnoutModel.setList(turnouts);
			}
		}
	}

	private class RemoveTurnoutAction
			extends AbstractAction {

		public RemoveTurnoutAction() {
			super("Remove", ImageTools.createImageIcon("remove.png"));
			;
		}

		public void actionPerformed(ActionEvent e) {
			if (turnoutsTable.isEditing())
				turnoutsTable.getCellEditor().stopCellEditing();

			TurnoutGroup selectedTurnoutGroup =
					(TurnoutGroup) (turnoutGroupList.getSelectedValue());
			int[] rows = turnoutsTable.getSelectedRows();
			int[] numbers = new int[rows.length];
			int i = 0;
			for (int row : rows) {
				numbers[i] = (Integer) turnoutsTable.getValueAt(row, 0);
				i++;
			}
			try {
				for (int number : numbers) {
					turnoutPersistence.deleteTurnout(turnoutPersistence
							.getTurnoutByNumber(number));
				}
				List<Turnout> turnouts =
						new ArrayList<Turnout>(selectedTurnoutGroup
								.getTurnouts());
				turnoutModel.setList(turnouts);

			} catch (TurnoutException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private class EditTurnoutAction
			extends AbstractAction {
		public void actionPerformed(ActionEvent e) {

			PresentationModel<Turnout> model =
					new PresentationModel<Turnout>(turnoutModel);
			if (model == null)
				return;
			new TurnoutConfig(TurnoutConfigurationDialog.this, model);
		}
	}

	public boolean isOkPressed() {
		return okPressed;
	}
}
