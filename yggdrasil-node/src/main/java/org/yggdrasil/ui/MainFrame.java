package org.yggdrasil.ui;

import org.yggdrasil.core.api.service.BlockchainService;
import org.yggdrasil.ui.forms.ApplicationForm;

import javax.swing.*;
import java.awt.*;

/**
 * The main JFrame container for the graphical portion of the
 * Yggdrasil node application.
 *
 * @author nathanielbunch
 */
public class MainFrame {

    public static JFrame frame;
    public static BlockchainService blockchainService;

    public MainFrame(String applicationName, BlockchainService blokService) {
        blockchainService = blokService;
        frame = new ApplicationForm(applicationName);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        frame.setLocation( ( screenSize.width - frameSize.width ) / 2, ( screenSize.height - frameSize.height ) / 2 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible(true);
    }

}
