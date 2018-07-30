package expert.codinglevel.inventory_tracking.model;

/**
 *  Filter is for the json representation of server side filtering
 */
public class Filter {
    private String field;
    private String operator;
    private String value;

    public Filter(String field, String operator, String value){
        this.field = field;
        this.operator = operator;
        this.value = value;
    }
}
