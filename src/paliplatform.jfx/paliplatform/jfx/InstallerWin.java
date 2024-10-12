/*
 * InstallerWin.java
 *
 * Copyright (C) 2023-2024 J. R. Bhaddacak 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package paliplatform.jfx;

import java.io.*;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.module.*;
import javax.swing.*;
import javax.swing.border.*;

/** 
 * The main window of JavaFX installer.
 * @author J.R. Bhaddacak
 * @version 3.0
 * @since 3.0
 */

public class InstallerWin extends JFrame {
	private static final String NIMBUS = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
	private static final String START = "Start";
	private static final double MEGA = 1024 * 1024;
	private static final String aboutStr = "JavaFX Installer for Pāli Platform 3" +
								"\n\nSimple guide:" +
								"\n0. JavaFX is the essential GUI toolkit for running the program." +
								"\n1. Available downloads are determined by the current system." +
								"\n2. If the machine is not recognized, it is better to do it manually by yourself." +
								"\n3. The installation is done locally not system-wide." +
								"\n4. When possible, use downloads from GLUON source." +
								"\n5. If the installation fails, consider using JRE with JavaFX included.";
	private final JPanel mainPanel = new JPanel();
	private final JTextArea messageArea = new JTextArea(3, 30);
	private final JScrollBar messageScroll;
	private final StatusBar statusBar = new StatusBar();
	private final List<TaskItem> taskList = new ArrayList<>();
	private final DownloadManager dlManager;
	private final JMenuItem launchMenuItem = new JMenuItem("Launch Pāli Platform");
	private final JButton launchButton = new JButton("Launch");
	private final JCheckBox cbSkipDownload = new JCheckBox("Skip download");
	private static final Integer[] VERSIONS	= { 13, 17, 21 };
	private final Map<Integer, JRadioButton> jfxVersionRadioMap = new LinkedHashMap<>();
	private final ButtonGroup jfxVersionGroup = new ButtonGroup();
	private final Map<SystemInfo.Source, JRadioButton> jfxSourceRadioMap = new EnumMap<>(SystemInfo.Source.class);
	private final ButtonGroup jfxSourceGroup = new ButtonGroup();
	private final Map<SystemInfo.Source, List<Runtime.Version>> availableJFXMap;
	private final Map<JRadioButton, Runtime.Version> radioVersionMap = new HashMap<>();
	private final JProgressBar progressBar = new JProgressBar();
	private final JButton startStopButton = new JButton(START);
	private Runtime.Version selectedVersion = SystemInfo.JAVAFX_21;
	private SystemInfo.Source selectedSource = SystemInfo.Source.GLUON;

	public InstallerWin() {
		for (final UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if (info.getClassName().equals(NIMBUS)) {
				try {
					UIManager.setLookAndFeel(NIMBUS);
				} catch (Exception e) {
					System.err.println(e);
				}
			}
			break;
		}
		
		// set frame icon
		final URL imgurl = getClass().getResource("resources/images/cloud-arrow-down.png");
		setIconImage(new ImageIcon(imgurl).getImage());
		
		// initialize download manager
		dlManager = new DownloadManager(this, 1);

		// add menu
		final JMenuBar menuBar = new JMenuBar();
		// File
		final JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		launchMenuItem.setMnemonic('P');
		launchMenuItem.addActionListener(e -> launch());
		fileMenu.add(launchMenuItem);
		final JMenuItem deleteMenuItem = new JMenuItem("Remove existing JavaFX");
		deleteMenuItem.addActionListener(e -> deleteJfx());
		fileMenu.add(deleteMenuItem);
		fileMenu.addSeparator();
		final JMenuItem closeMenuItem = new JMenuItem("Close");
		closeMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
		closeMenuItem.setMnemonic('Q');
		closeMenuItem.addActionListener(e -> quit());
		fileMenu.add(closeMenuItem);
		menuBar.add(fileMenu);
		// Help
		final JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		final JMenuItem aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.setMnemonic('A');
		aboutMenuItem.addActionListener(e -> showAbout());
		helpMenu.add(aboutMenuItem);
		menuBar.add(helpMenu);
		setJMenuBar(menuBar);

		// add toolbar
		final JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		cbSkipDownload.setMargin(new Insets(0, 0, 0, 0));
		cbSkipDownload.setToolTipText("Skip download if the file already exists in the cache");
		cbSkipDownload.addActionListener(e -> refreshDisplay());
		toolBar.add(cbSkipDownload);
		toolBar.addSeparator();
		launchButton.setMargin(new Insets(0, 0, 0, 0));
		launchButton.setToolTipText("Launch Pāli Platform");
		launchButton.addActionListener(e -> launch());
		toolBar.add(launchButton);
		toolBar.addSeparator();
		final JButton aboutButton = new JButton("About");
		aboutButton.setMargin(new Insets(0, 0, 0, 0));
		aboutButton.setToolTipText("Show program information and simple help");
		aboutButton.addActionListener(e -> showAbout());
		toolBar.add(aboutButton);
		add(toolBar, BorderLayout.NORTH);

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

		// add system information detected
		final Box sysInfoBox = Box.createHorizontalBox();
		final Box labelBox = Box.createVerticalBox();
		final JLabel archLabel = new JLabel("System architecture: ");
		archLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		final JLabel versionLabel = new JLabel("Java version: ");
		versionLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		labelBox.add(archLabel);
		labelBox.add(versionLabel);
		sysInfoBox.add(labelBox);
		final Box infoBox = Box.createVerticalBox();
		final JLabel archInfo = new JLabel(" " + SystemInfo.INSTANCE.getArchString() + " ");
		archInfo.setBorder(BorderFactory.createLoweredSoftBevelBorder());
		infoBox.add(archInfo);
		final JLabel versionInfo = new JLabel(" " + SystemInfo.INSTANCE.getJavaVersion().toString() + " ");
		versionInfo.setBorder(BorderFactory.createLoweredSoftBevelBorder());
		infoBox.add(versionInfo);
		sysInfoBox.add(infoBox);
		mainPanel.add(sysInfoBox);
		mainPanel.add(Box.createVerticalStrut(10));
		// add version and source selectors
		availableJFXMap = SystemInfo.INSTANCE.getAvailableJavaFX();
		final Box optionBox = Box.createHorizontalBox();
		final Box verSelBox = Box.createVerticalBox();
		final Border verSelBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
									"JavaFX Version", TitledBorder.CENTER, TitledBorder.TOP);	
		verSelBox.setBorder(verSelBorder);
		verSelBox.setAlignmentY(Component.TOP_ALIGNMENT);
		for (final Integer v : VERSIONS) {
			jfxVersionRadioMap.put(v, new JRadioButton(v + "                     "));
		}
		for (final JRadioButton b : jfxVersionRadioMap.values()) {
			b.setAlignmentX(Component.LEFT_ALIGNMENT);
			b.setEnabled(false);
			verSelBox.add(b);
			jfxVersionGroup.add(b);
		}
		optionBox.add(verSelBox);
		final Box srcSelBox = Box.createVerticalBox();
		final Border srcSelBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
									"Source", TitledBorder.CENTER, TitledBorder.TOP);	
		srcSelBox.setBorder(srcSelBorder);
		srcSelBox.setAlignmentY(Component.TOP_ALIGNMENT);
		jfxSourceRadioMap.put(SystemInfo.Source.GLUON, new JRadioButton(SystemInfo.Source.GLUON.toString()));
		jfxSourceRadioMap.put(SystemInfo.Source.MAVEN, new JRadioButton(SystemInfo.Source.MAVEN.toString()));
		jfxSourceRadioMap.put(SystemInfo.Source.OTHER, new JRadioButton(SystemInfo.Source.OTHER.toString()));
		for (final SystemInfo.Source s : SystemInfo.Source.values) {
			final JRadioButton b = jfxSourceRadioMap.get(s);
			b.setEnabled(availableJFXMap.containsKey(s));
			b.setActionCommand(s.toString());
			b.addItemListener(this::sourceSelected);
			srcSelBox.add(b);
			jfxSourceGroup.add(b);
		}
		optionBox.add(srcSelBox);
		// set source selection
		for (final SystemInfo.Source s : SystemInfo.Source.values) {
			if (availableJFXMap.containsKey(s)) {
				selectedSource = s;
				jfxSourceRadioMap.get(s).getModel().setSelected(true);
				break;
			}
		}
		// set version selection
		if (!availableJFXMap.containsKey(selectedSource))
			selectedSource = availableJFXMap.keySet().stream().findFirst().orElse(null);
		if (selectedSource != null)
			updateVersionSelector(selectedSource);
		mainPanel.add(optionBox);
		// add progress bar and start/stop button
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(progressBar);
		startStopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		startStopButton.addActionListener(e -> toggleStartStopDownload());
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(startStopButton);

		// add message pane
		final JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new GridLayout(1,1));
		messageArea.setEditable(false);
		messageArea.setLineWrap(true);
		messageArea.setMargin(new Insets(5, 5, 0, 0));
		final JScrollPane messagePane = new JScrollPane(messageArea);
		messageScroll = messagePane.getVerticalScrollBar();
		messagePanel.add(messagePane);

		// split main panel and message pane into 2 boxes
		final Box contentBox = Box.createVerticalBox();
		contentBox.add(mainPanel);
		contentBox.add(messagePanel);
		add(contentBox, BorderLayout.CENTER);
		
		// add status bar
		add(statusBar, BorderLayout.SOUTH);

		// other initializations
		if (SystemInfo.INSTANCE.getOsType() == SystemInfo.OsType.MAC) {
			final String warnStr = "JavaFX installer does not work well with macOS." +
						"\nPlease install a suitable JRE with JavaFX included.";
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(this, warnStr, "Warning", JOptionPane.WARNING_MESSAGE));
		} else if (SystemInfo.INSTANCE.getProcessor() == SystemInfo.Processor.UNCERTAIN) {
			final String warnStr = "The program cannot recognize this machine." +
						"\nSo, it uses x86 achitecture as a fallback." +
						"\nPlease consider installing JavaFX manually by yourself," +
						"\nor using a suitable JRE with JavaFX included.";
			EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(this, warnStr, "Warning", JOptionPane.WARNING_MESSAGE));
		}
		setupTasks();
		refreshDisplay();
	}

	private void showAbout() {
		JOptionPane.showMessageDialog(this, aboutStr, "About", JOptionPane.INFORMATION_MESSAGE);
	}

	private void updateVersionSelector(final SystemInfo.Source src) {
		final List<Runtime.Version> verList = availableJFXMap.get(src);
		for (final Runtime.Version v : verList) {
			final Integer feaNum = v.feature();
			final JRadioButton b = jfxVersionRadioMap.get(feaNum);
			b.setEnabled(true);
			b.setText(v.toString());
			b.setActionCommand(v.toString());
			radioVersionMap.put(b, v);
		}
		for (int i = VERSIONS.length-1; i >= 0; i--) {
			final JRadioButton b = jfxVersionRadioMap.get(VERSIONS[i]);
			if (b.isEnabled()) {
				b.getModel().setSelected(true);
				break;
			}
		}
	}

	private void sourceSelected(final ItemEvent e) {
		if (e.getSource() instanceof AbstractButton) {
			final AbstractButton btn = (AbstractButton) e.getSource();
			final SystemInfo.Source src = SystemInfo.Source.valueOf(btn.getText());
			updateVersionSelector(src);
		}
	}

	public void setStatus(final String mess) {
		statusBar.setText(mess);
	}

	private void toggleStartStopDownload() {
		if (!dlManager.isRunning()) {
			start();
			startStopButton.setText("STOP");
		} else {
			stop();
			startStopButton.setText(START);
		}
	}

	private void setupTasks() {
		if (selectedSource == null) return;
		taskList.clear();
		final String src = jfxSourceGroup.getSelection().getActionCommand();
		final String ver = jfxVersionGroup.getSelection().getActionCommand();
		final List<String> urlList = SystemInfo.INSTANCE.getJfxUrlList(src, ver);
		for (final String url : urlList) {
			final String filename = url.substring(url.lastIndexOf("/") + 1);
			final File target = new File(Installer.ROOTDIR + Installer.CACHEDIR + filename);
			final File dest = new File(Installer.ROOTDIR + Installer.MODDIR);
			final TaskItem task = new TaskItem(url, target, dest);
			task.setDownloadSkipped(cbSkipDownload.getModel().isSelected());
			taskList.add(task);
		}
		final int count = taskList.size();
		final String s = count > 1 ? "s" : "";
		setStatus(count + " item" + s + " to download");
		startStopButton.setEnabled(count > 0);
	}

	private void start() {
		setupTasks();
		dlManager.setTaskList(taskList);
		dlManager.start();
	}

	private void stop() {
		dlManager.stopAll();
	}

	public void updateStatus(final TaskItem task, final long size) {
		final long total = task.getTotalSize();
		final String percent;
		if (total < 0) {
			percent = "";
			progressBar.setIndeterminate(true);
		} else {
			final int perc = (int)(100 * size / total);
			percent = perc + "%";
			progressBar.setIndeterminate(false);
			progressBar.setValue(perc);
		}
		final String sizeStr = String.format("%.2f MB ", size / MEGA);
		statusBar.setText(sizeStr + percent + " [" + task + "]");
	}

	public void addMessage(final String text) {
		messageArea.append("\n> " + text);
		messageScroll.setValue(messageScroll.getMaximum());
	}

	public void downloadFinished(final TaskItem.State state) {
		if (state == TaskItem.State.CANCELLED || state == TaskItem.State.FAILED) {
			refreshDisplay();
			return;
		}
		startStopButton.setEnabled(false);
		progressBar.setIndeterminate(true);
		statusBar.setText("Unpacking/installing... (please wait)");
		final List<TaskItem> tlist = taskList.stream().filter(TaskItem::isInstallable).collect(Collectors.toList());
		final InstallWorker iworker = new InstallWorker(tlist, res -> installFinished(res));
		iworker.execute();
	}

	private void installFinished(final DownloadResponse res) {
		if (res.isSuccess()) {
			statusBar.setText(res.getMessage());
		} else {
			addMessage(res.getMessage());
		}
		refreshDisplay();
	}

	private void refreshDisplay() {
		startStopButton.setText(START);
		startStopButton.setEnabled(selectedSource != null);
		progressBar.setIndeterminate(false);
		progressBar.setValue(0);
		final boolean hasJFX = isModuleAvailable("javafx.base");
		launchMenuItem.setEnabled(hasJFX);
		launchButton.setEnabled(hasJFX);
	}

	private boolean isModuleAvailable(final String modName) {
		final String moddir = Installer.ROOTDIR + Installer.MODDIR;
		final ModuleFinder finder = ModuleFinder.of(Path.of(moddir));
		final Optional<ModuleReference> omref = finder.find(modName);
		return omref.isPresent();
	}

	private void launch() {
		if (isModuleAvailable("paliplatform.base")) {
			try {
				final String cmd = System.getProperty("os.name").toLowerCase().contains("windows") ? "run.cmd" : "./run.sh";
				final Process proc = Runtime.getRuntime().exec(cmd);
				proc.waitFor();
				System.out.println(proc);
			} catch (IOException | InterruptedException e) {
				System.err.println(e.toString());
			}
		}
	}

	private void deleteJfx() {
		final int response = JOptionPane.showConfirmDialog(this,
				"The JavaFX in the program's modules will be deleted.\n" + 
				"This makes the program unable to run.\n" +
				"Are you sure?",
				"Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.YES_OPTION) {
			if (isModuleAvailable("javafx.base")) {
				final File moddir = new File(Installer.ROOTDIR + Installer.MODDIR);
				final File listFile = Installer.getJfxFileList(moddir);
				if (listFile.exists()) {
					Installer.removeFilesInList(moddir, listFile);
					if (SystemInfo.INSTANCE.getOsType() == SystemInfo.OsType.WINDOWS) {
						// in the case of Windows, the native lib resides in bin/ and not listed
						Installer.removeBinNativeLib();
					}
					final String mess = "Deletion completed.";
					JOptionPane.showMessageDialog(this, mess, "JavaFX Removed", JOptionPane.INFORMATION_MESSAGE);
				} else {
					// if other native lib is used, fix this part
					Installer.removeJavaFX(moddir);
					Installer.removeAllNativeLib(moddir);
					final String mess = "It seems that the existing JavaFX was installed manually." +
						"\nThe components are removed anyway.";
					JOptionPane.showMessageDialog(this, mess, "JavaFX Removed", JOptionPane.INFORMATION_MESSAGE);
				}
				refreshDisplay();
			} else {
				final String mess = "No JavaFX base module is found." +
					"\nNothing is removed.";
				JOptionPane.showMessageDialog(this, mess, "No JavaFX Found", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	private void quit() {
		System.exit(0);
	}
	
}

