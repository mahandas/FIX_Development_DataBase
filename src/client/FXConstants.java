package client;

public final class FXConstants {

    public static class HedgeTradeType_FI {
        public static final int NO_HEDGE = 1;
        public static final int SPOT = 2;
        public static final int FORWARD = 3;
    }

    public static class Settlement_Type {
        public static final int REGULAR = 0;
        public static final int CASH = 1;
    }

    public static class Account_Streaming {
        public static final int SUBSCRIBE = 1;
        public static final int UNSUBSCRIBE = 2;
        public static final String RFQ = "Y";
        public static final String RFS = "N";
    }
      
    /**
     * For Command Line Arguements Mapping
     */
    public static class Params_FI {
        //        public static final int ENTITY_ID = 0;
        //        public static final int PP_ID = 1;
        public static final int PROP_FILE_PATH = 0;
    }

    
}
