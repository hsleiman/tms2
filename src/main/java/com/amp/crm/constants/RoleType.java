package com.amp.crm.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum RoleType {
    SYSTEM_ADMIN(1, "System Administrator", "System Administrator"),
    SETTLEIT_AGENT(110, "Agent", "Collection Agent"),
    SETTLEIT_AGENT_SUPERVISOR(111, "Settleit Agent - Supervisor", "Agent - Supervisor"),
    SETTLEIT_AGENT_MANAGER(112, "Agent - Manager", "Agent - Manager"),
    SETTLEIT_AGENT_BACKEND(113, "Agent - Backend", "Agent - Backend"),
    SETTLEIT_SERVICE(120, "Service", "Service Agent"),
    PAYMENT_PROCESSOR(160, "Payment Processor", "Payment Processor"),
    PAYMENT_PROCESSOR_SUPERVISOR(161, "Payment Processor - Supervisor", "Payment Processor - Supervisor"),
    AUDITOR(240, "Auditor - Read Only", "Auditor - Read Only"),
    TMS_ADMIN(750, "Admin", "Admin"),
    TMS_MANAGER(751, "Manager", "Manager");

    private static Map<Integer, RoleType> map = new HashMap<>();

    static {
        for (RoleType val : RoleType.values()) {
            map.put(val.getId(), val);
        }
    }

    //**************************************************************************
    private final int id;
    private final String name;
    private final String description;

    private RoleType(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @JsonCreator
    public static RoleType fromId(int id) {
        return map.get(id);
    }

    @JsonValue
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
