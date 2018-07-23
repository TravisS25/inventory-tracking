package expert.codinglevel.inventory_tracking.model;

import expert.codinglevel.inventory_tracking.enums.OperationType;

public class DbOperation {
    private String mQuery;
    private String[] mQueryArgs;
    private OperationType mOperationType;

    public DbOperation(String query, String[] queryArgs, OperationType operationType){
        mQuery = query;
        mQueryArgs = queryArgs;
        mOperationType = operationType;
    }

    public String getQuery(){return mQuery;}
    public String[] getQueryArguments(){return mQueryArgs;}
    public OperationType getOperationType(){return mOperationType;}
}
