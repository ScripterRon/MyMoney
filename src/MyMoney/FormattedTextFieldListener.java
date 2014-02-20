package MyMoney;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A JFormattedTextField does not activate the default button when ENTER
 * is pressed and the text field has been modified.  This means that
 * ENTER must be pressed twice to activate the default button.  This is
 * confusing to the user since a JTextField will activate the default
 * button when ENTER is pressed whether or not the text field has been
 * modified.  So we define an action listener for a JFormattedTextField
 * and will click the button ourself.  Note that an action event is not
 * fired if the text field is not valid and focus will remain in the
 * formatted text field until a valid value is entered.
 */
public final class FormattedTextFieldListener implements ActionListener {
    
    /** Parent dialog */
    private JDialog dialog;
    
    /** 
     * Create a new formatted text field listener
     *
     * @param       dialog          The parent dialog for the JFormattedTextField
     */
    public FormattedTextFieldListener(JDialog dialog) {
        this.dialog = dialog;
    }

    /**
     * Action performed (ActionListener interface)
     *
     * @param   ae              Action event
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() instanceof JFormattedTextField) {
            JButton defaultButton = dialog.getRootPane().getDefaultButton();
            if (defaultButton != null)
                defaultButton.doClick();
        }
    }    
}
