package MyMoney;

import java.text.ParseException;

import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.*;
import java.awt.*;

/**
 * EditInputVerifier will validate the text for a JFormattedTextField component.
 * The component must have an associated formatter to convert the text string
 * to a value.  The focus will not be allowed to leave the component if the
 * text string does not represent a valid value as defined by the formatter.
 */
public final class EditInputVerifier extends InputVerifier {

    /** TRUE if the field can be empty */
    private boolean optionalField;

    /**
     * Create a new input verifier
     *
     * @param   optionalField       TRUE if the field can be empty
     */
    public EditInputVerifier(boolean optionalField) {
        super();
        this.optionalField = optionalField;
    }

    /**
     * Verify the input text.  An empty text string is allowed for an optional field.
     * The text is not valid if the formatter throws a parse exception.
     *
     * @param       input       Input component
     * @return                  TRUE if the input text is valid
     */
    public boolean verify(JComponent input) {
        boolean allow = true;
        if (input instanceof JFormattedTextField) {
            JFormattedTextField textField = (JFormattedTextField)input;
            AbstractFormatter formatter = textField.getFormatter();
            if (formatter != null) {
                String value = textField.getText();
                if (value.length() != 0) {
                    try {
                        formatter.stringToValue(value);
                    } catch (ParseException exc) {
                        allow = false;
                    }
                } else if (!optionalField) {
                    allow = false;
                }
            }
        }

        return allow;
    }

    /**
     * Check if the component should yield the focus.  Error feedback
     * will be provided if the input text is not valid.  The verify()
     * method is used to test the input text.
     *
     * @param       input       Input component
     * @return                  TRUE if the component should yield the focus
     */
    public boolean shouldYieldFocus(JComponent input) {
        boolean allow = verify(input);
        if (!allow)
            UIManager.getLookAndFeel().provideErrorFeedback(input);

        return allow;
    }
}
