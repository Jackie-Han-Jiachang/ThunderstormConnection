package bnds.chinese.model;

public enum CharacterId {
    ZHOU_FANYI("周繁漪"),
    LU_GUI("鲁贵"),
    LU_SHIPING("鲁侍萍"),
    LU_SIFENG("鲁四凤"),
    LU_DAHAI("鲁大海"),
    ZHOU_PUYUAN("周朴园"),
    ZHOU_PING("周萍"),
    ZHOU_CHONG("周冲");

    private final String displayName;

    CharacterId(String displayName) {
        this.displayName = displayName;
    }

    public String getId() {
        return name();
    }

    public String getDisplayName() {
        return displayName;
    }
}
