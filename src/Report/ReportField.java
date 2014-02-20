package Report;

/**
 * The report field is a report element consisting of text obtained from
 * the data column, expression or function identified by the field name.
 */
public class ReportField extends AbstractElement {

    /** The field name */
    private String field;

    /** The field renderer */
    private ReportRenderer renderer;

    /**
     * Construct a new report field using the value of the specified field
     *
     * @param       field           The field name
     */
    public ReportField(String field) {
        super();

        if (field == null)
            throw new NullPointerException("No field name provided");

        this.field = field;
    }

    /**
     * Set the renderer used to format the field value
     *
     * @param       renderer        The field renderer
     */
    public void setRenderer(ReportRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Return the string representation of the field value
     *
     * @return                      The string value
     */
    public String getText() {
        String text;

        //
        // Get the string representation of the field value
        //
        Object value = getState().getDataRow().getValue(field);
        if (value == null)
            text = new String();
        else if (renderer != null)
            text = renderer.format(this, value);
        else
            text = value.toString();

        return text;
    }
}

