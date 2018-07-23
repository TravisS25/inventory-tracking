package expert.codinglevel.inventory_tracking.interfaces;

import expert.codinglevel.inventory_tracking.view.TextValue;

public interface IMachine {
    TextValue getBuilding();
    TextValue getFloor();
    TextValue getDepartment();
    TextValue getRoom();
    TextValue getMachineStatus();
    void setBuilding(TextValue building);
    void setFloor(TextValue floor);
    void setDepartment(TextValue department);
    void setRoom(TextValue room);
    void setMachineStatus(TextValue machineStatus);
}
