package net.querz.worldpruner.ui;

import net.querz.worldpruner.Main;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;

public class AboutDialog {

	public static void show(Component parent) {
		JEditorPane aboutText = new JEditorPane();
		aboutText.setContentType("text/html");
		aboutText.setBackground(parent.getBackground());
		aboutText.setText("""
				<body style="font-family: Sans-Serif; font-size: 12;">
					<b style="font-size: 16;">WorldPruner by Querz and henkelmax</b>
					<br/>
					<span>Version %s</span>
					<br/>
					<br/>
					<span>Licensed under the <a href="https://github.com/Querz/WorldPruner/blob/main/LICENSE">MIT</<a> license.</span>
					<br/>
					<span>Check out the source code on <a href="https://github.com/Querz/WorldPruner">GitHub</<a>.</span>
					<br/>
					<br/>
					<b>Credits</b>
					<br/>
					<span><a href="https://commons.apache.org/proper/commons-cli/">Apache Commons CLI</a></span>
					<br/>
					<span><a href="https://fastutil.di.unimi.it/">fastutil</a></span>
					<br/>
					<span><a href="https://logging.apache.org/log4j/2.x/">Log4j</a></span>
					<br/>
					<span><a href="https://github.com/ctongfei/progressbar">progressbar</a></span>
				</body>
				""".formatted(Main.getVersion()));
		aboutText.setEditable(false);
		aboutText.addHyperlinkListener(e -> {
			if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
				Desktop desktop = Desktop.getDesktop();
				try {
					desktop.browse(e.getURL().toURI());
				} catch (Exception ignored) {
				}
			}
		});
		JOptionPane.showOptionDialog(parent, new JComponent[]{aboutText}, "About", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
	}
}
