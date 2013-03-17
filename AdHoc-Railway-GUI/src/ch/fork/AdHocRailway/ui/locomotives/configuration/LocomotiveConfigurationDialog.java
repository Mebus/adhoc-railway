/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id$
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

package ch.fork.AdHocRailway.ui.locomotives.configuration;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
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

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManager;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManagerException;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManagerListener;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.SwingUtils;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.SingleListSelectionAdapter;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.common.collect.ArrayListModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class LocomotiveConfigurationDialog extends JDialog implements
		LocomotiveManagerListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4043586436141605523L;

	private boolean okPressed;

	private JTable locomotivesTable;

	private JList locomotiveGroupList;

	private JButton addLocomotiveButton;

	private JButton removeLocomotiveButton;

	private JButton addGroupButton;

	private JButton removeGroupButton;

	private JButton okButton;

	private LocomotiveGroupSelectionHandler groupSelectionHandler;

	private final LocomotiveManager locomotivePersistence = AdHocRailway
			.getInstance().getLocomotivePersistence();

	private JButton editGroupButton;

	private SelectionInList<LocomotiveGroup> locomotiveGroupModel;
	private SelectionInList<Locomotive> locomotiveModel;
	private ArrayListModel<LocomotiveGroup> locomotiveGroups;
	private ArrayListModel<Locomotive> locomotives;

	public LocomotiveConfigurationDialog(final JFrame owner) {
		super(owner, "Locomotive Configuration", true);
		initGUI();
	}

	private void initGUI() {
		buildPanel();
		locomotivePersistence.addLocomotiveManagerListener(this);
		pack();
		setLocationRelativeTo(getParent());
		setVisible(true);
	}

	private void buildPanel() {
		initComponents();
		initEventHandling();

		final FormLayout layout = new FormLayout("pref, 10dlu, pref:grow",
				"pref, 3dlu, fill:pref:grow, 10dlu, pref, 10dlu, pref");

		final PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		final CellConstraints cc = new CellConstraints();

		builder.addSeparator("Locomotive Groups", cc.xyw(1, 1, 1));

		builder.add(new JScrollPane(locomotiveGroupList), cc.xy(1, 3));
		builder.add(buildGroupButtonBar(), cc.xy(1, 5));

		builder.addSeparator("Locomotives", cc.xyw(3, 1, 1));
		builder.add(new JScrollPane(locomotivesTable), cc.xy(3, 3));
		builder.add(buildLocomotiveButtonBar(), cc.xy(3, 5));

		builder.add(buildMainButtonBar(), cc.xyw(1, 7, 3));
		add(builder.getPanel());

	}

	private Component buildLocomotiveButtonBar() {
		return ButtonBarFactory.buildCenteredBar(addLocomotiveButton,
				removeLocomotiveButton);
	}

	private Component buildGroupButtonBar() {
		return ButtonBarFactory.buildCenteredBar(addGroupButton,
				editGroupButton, removeGroupButton);
	}

	private Component buildMainButtonBar() {
		return ButtonBarFactory.buildRightAlignedBar(okButton);
	}

	private void initComponents() {
		locomotiveGroups = new ArrayListModel<LocomotiveGroup>(
				locomotivePersistence.getAllLocomotiveGroups());

		locomotiveGroupModel = new SelectionInList<LocomotiveGroup>(
				(ListModel) locomotiveGroups);

		locomotiveGroupList = BasicComponentFactory
				.createList(locomotiveGroupModel);
		locomotiveGroupList
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		locomotiveGroupList
				.setCellRenderer(new LocomotiveGroupListCellRenderer());

		addGroupButton = new JButton(new AddLocomotiveGroupAction());
		editGroupButton = new JButton(new EditLocomotiveGroupAction());
		removeGroupButton = new JButton(new RemoveLocomotiveGroupAction());

		locomotives = new ArrayListModel<Locomotive>();
		locomotiveModel = new SelectionInList<Locomotive>();
		locomotiveModel.setList(locomotives);
		locomotivesTable = new JTable();
		locomotivesTable.setModel(new LocomotiveTableModel(locomotiveModel));
		locomotivesTable.setSelectionModel(new SingleListSelectionAdapter(
				locomotiveModel.getSelectionIndexHolder()));

		addLocomotiveButton = new JButton(new AddLocomotiveAction());
		removeLocomotiveButton = new JButton(new RemoveLocomotiveAction());

		okButton = new JButton("OK",
				ImageTools.createImageIconFromIconSet("ok.png"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				okPressed = true;
				setVisible(false);
				locomotivePersistence
						.removeLocomotiveManagerListenerInNextEvent(LocomotiveConfigurationDialog.this);
			}
		});
	}

	private void initEventHandling() {
		groupSelectionHandler = new LocomotiveGroupSelectionHandler();
		locomotiveGroupList.addListSelectionListener(groupSelectionHandler);
		locomotivesTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2
						&& e.getButton() == MouseEvent.BUTTON1) {
					new EditLocomotiveAction().actionPerformed(null);
				}
			}

		});
		SwingUtils.addEscapeListener(this);

	}

	/**
	 * Sets the selected group as bean in the details model.
	 */
	private final class LocomotiveGroupSelectionHandler implements
			ListSelectionListener {

		@Override
		public void valueChanged(final ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) {
				return;
			}
			if (locomotiveGroupList.getSelectedIndex() == -1) {
				locomotiveGroupList.setSelectedIndex(0);
			}
			final LocomotiveGroup selectedGroup = (LocomotiveGroup) locomotiveGroupList
					.getSelectedValue();
			if (selectedGroup == null) {
				return;
			}

			locomotives.clear();
			locomotives.addAll(selectedGroup.getLocomotives());
		}
	}

	private class AddLocomotiveGroupAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6754626146363603303L;

		public AddLocomotiveGroupAction() {
			super("Add", ImageTools.createImageIconFromIconSet("add.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {
			final String newGroupName = JOptionPane.showInputDialog(
					LocomotiveConfigurationDialog.this,
					"Enter the name of the new Locomotive-Group",
					"Add Locomotive-Group", JOptionPane.QUESTION_MESSAGE);
			final LocomotiveGroup newSection = new LocomotiveGroup(0,
					newGroupName);
			locomotivePersistence.addLocomotiveGroup(newSection);
		}
	}

	private class EditLocomotiveGroupAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5332973509659278121L;

		public EditLocomotiveGroupAction() {
			super("Edit Group");
		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {
			final LocomotiveGroup groupToEdit = locomotiveGroupModel
					.getSelection();

			locomotivePersistence.updateLocomotiveGroup(groupToEdit);

		}
	}

	private class RemoveLocomotiveGroupAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6014881261911976113L;

		public RemoveLocomotiveGroupAction() {
			super("Remove", ImageTools.createImageIconFromIconSet("remove.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {
			final LocomotiveGroup groupToDelete = (LocomotiveGroup) (locomotiveGroupList
					.getSelectedValue());
			final int response = JOptionPane.showConfirmDialog(
					LocomotiveConfigurationDialog.this,
					"Really remove Locomotive-Group '"
							+ groupToDelete.getName() + "' ?",
					"Remove Locomotive-Group", JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.YES_OPTION) {
				try {
					locomotivePersistence.deleteLocomotiveGroup(groupToDelete);
				} catch (final LocomotiveManagerException e) {
					ExceptionProcessor.getInstance().processException(e);
				}
			}
		}
	}

	private class AddLocomotiveAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7522044592371429190L;

		public AddLocomotiveAction() {
			super("Add", ImageTools.createImageIconFromIconSet("add.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final LocomotiveGroup selectedLocomotiveGroup = locomotiveGroupModel
					.getSelection();
			if (selectedLocomotiveGroup == null) {
				JOptionPane.showMessageDialog(
						LocomotiveConfigurationDialog.this,
						"Please select a locomotive group", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			final Locomotive newLocomotive = createDefaultLocomotive();

			new LocomotiveConfig(LocomotiveConfigurationDialog.this,
					newLocomotive, selectedLocomotiveGroup);
		}

		private Locomotive createDefaultLocomotive() {
			final Locomotive newLocomotive = new Locomotive();
			return newLocomotive;
		}
	}

	private class EditLocomotiveAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7081224522762457353L;

		public EditLocomotiveAction() {
			super("Edit");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final LocomotiveGroup selectedLocomotiveGroup = locomotiveGroupModel
					.getSelection();
			new LocomotiveConfig(LocomotiveConfigurationDialog.this,
					locomotiveModel.getSelection(), selectedLocomotiveGroup);
			final List<Locomotive> locomotives = new ArrayList<Locomotive>(
					selectedLocomotiveGroup.getLocomotives());
			locomotiveModel.setList(locomotives);
		}
	}

	private class RemoveLocomotiveAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1606368554698945245L;

		public RemoveLocomotiveAction() {
			super("Remove", ImageTools.createImageIconFromIconSet("remove.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final LocomotiveGroup selectedLocomotiveGroup = locomotiveGroupModel
					.getSelection();

			final int row = locomotivesTable.getSelectedRow();
			final Locomotive locomotiveToDelete = locomotiveModel
					.getElementAt(row);

			if (locomotiveToDelete == null) {
				JOptionPane.showMessageDialog(
						LocomotiveConfigurationDialog.this,
						"Please select a locomotive", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			final int response = JOptionPane.showConfirmDialog(
					LocomotiveConfigurationDialog.this,
					"Really remove Locomotive '" + locomotiveToDelete.getName()
							+ "' ?", "Remove Locomotive",
					JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.YES_OPTION) {
				locomotivePersistence.removeLocomotiveFromGroup(
						locomotiveToDelete, selectedLocomotiveGroup);
			}
		}
	}

	public boolean isOkPressed() {
		return okPressed;
	}

	@Override
	public void locomotiveAdded(final Locomotive locomotive) {
		if (locomotive.getGroup().equals(locomotiveGroupModel.getSelection())) {
			locomotives.add(locomotive);
		}
	}

	@Override
	public void locomotiveUpdated(final Locomotive locomotive) {
		if (locomotive.getGroup().equals(locomotiveGroupModel.getSelection())) {
			locomotives.remove(locomotive);
			locomotives.add(locomotive);
		}
	}

	@Override
	public void locomotiveRemoved(final Locomotive locomotive) {
		if (locomotive.getGroup().equals(locomotiveGroupModel.getSelection())) {
			locomotives.remove(locomotive);
		}
	}

	@Override
	public void locomotiveGroupAdded(final LocomotiveGroup group) {
		locomotiveGroups.add(group);

	}

	@Override
	public void locomotiveGroupUpdated(final LocomotiveGroup group) {
		locomotiveGroups.remove(group);
		locomotiveGroups.add(group);
	}

	@Override
	public void locomotiveGroupRemoved(final LocomotiveGroup group) {
		if (locomotiveGroupModel.getSelection().equals(group)) {
			locomotives.clear();
		}
		locomotiveGroups.remove(group);
	}

	@Override
	public void locomotivesUpdated(final List<LocomotiveGroup> locomotiveGroups) {

	}

	@Override
	public void failure(
			final LocomotiveManagerException locomotiveManagerException) {

	}
}
