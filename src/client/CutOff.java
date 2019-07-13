package client;


public enum CutOff {
    NEWYORK1000("NYC"),
    TOKYO1500("TOK"),
    MUMBAI1230("INR"),
    SAOPAOLO1315("BRL"),
    BEIJING0915("CNY"),
    SINGAPORE1100_IDR("JISDOR"),
    SEOUL1530("KRW1530"),
    SINGAPORE1100_MYRFIX("MYRFIX"),
    MANILA1130("PHF"),
    TAIPEI100("TWD"),
    MOSCOW1230("EMTA-NDF");
    private final String value;

    CutOff(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CutOff fromValue(String v) {
        for (CutOff c : CutOff.values()) {
            if (c.value.equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
