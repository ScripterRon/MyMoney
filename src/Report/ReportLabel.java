package Report;

/**
 * The report label is a report element consisting of static text.
 */
public class ReportLabel extends AbstractElement {

    /** The label text */
    private String text;

    /**
     * Construct a new report label
     *
     * @param       text            The text for the report label
     */
    public ReportLabel(String text) {
        super();

        if (text == null)
            throw new NullPointerException("No label text provided");

        this.text = text;
    }

    /**
     * Return the text for the label
     *
     * @return                      The text string
     */
    public String getText() {
        return text;
    }
}

