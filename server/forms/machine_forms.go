package forms

import (
	"fmt"
	"strconv"

	"github.com/TravisS25/httputil/formutil"
	"github.com/TravisS25/inventory-tracking/src/server/models"
	"github.com/go-ozzo/ozzo-validation"
)

type MachineSwapForm struct {
	MachineStatusID int `json:"machineStatusID,omitempty"`
}

func NewMachineSwapValidator(v formutil.FormValidation) *MachineSwapValidator {
	return &MachineSwapValidator{
		FormValidation: v,
	}
}

type MachineSwapValidator struct {
	formutil.FormValidation
}

func (m MachineSwapValidator) Validate(item interface{}) error {
	form := item.(MachineSwapForm)
	validMachineStatus := true

	if !m.Exists("select id from machine_status where id = $1;", form.MachineStatusID) {
		validMachineStatus = false
	}

	return validation.ValidateStruct(
		&form,
		validation.Field(
			&form.MachineStatusID,
			validation.Required.Error("Required"),
			m.IsValid(validMachineStatus).Error(m.ExistError(strconv.Itoa(form.MachineStatusID))),
		),
	)
}

type MachineForm struct {
	RoomID          int    `json:"roomID,omitempty" db:"room_id"`                    // room_id
	MachineStatusID int    `json:"machineStatusID,omitempty" db:"machine_status_id"` // machine_status_id
	MachineName     string `json:"machineName,omitempty" db:"machine_name"`          // machine_name
	Instance        *models.Machine
}

func NewMachineValidator(v formutil.FormValidation) *MachineValidator {
	return &MachineValidator{
		FormValidation: v,
	}
}

type MachineValidator struct {
	formutil.FormValidation
}

func (m MachineValidator) Validate(item interface{}) error {
	form := item.(MachineForm)
	fmt.Printf("machine name %s\n", form.MachineName)
	validRoomID := true
	validMachineStatusID := true
	validMachineName := true

	if !m.Exists("select id from room where id = $1;", form.RoomID) {
		validRoomID = false
	}

	if !m.Exists("select id from machine_status where id = $1", form.MachineStatusID) {
		validMachineStatusID = false
	}

	if form.Instance != nil {
		if !m.Unique(
			form.MachineName,
			form.Instance.MachineName,
			"select id from machine where machine_name = $1",
			form.MachineName,
		) {
			validMachineName = false
		}
	} else {
		if !m.Unique(
			form.MachineName,
			"",
			"select id from machine where machine_name = $1",
			form.MachineName,
		) {
			validMachineName = false
		}
	}

	return validation.ValidateStruct(
		&form,
		validation.Field(
			&form.RoomID,
			validation.Required.Error("Required"),
			m.IsValid(validRoomID).Error(m.ExistError(strconv.Itoa(form.RoomID))),
		),
		validation.Field(
			&form.MachineStatusID,
			validation.Required.Error("Required"),
			m.IsValid(validMachineStatusID).Error(m.ExistError(strconv.Itoa(form.MachineStatusID))),
		),
		validation.Field(
			&form.MachineName,
			validation.Required.Error("Required"),
			m.IsValid(validMachineName).Error(m.UniqueError(form.MachineName)),
		),
	)
}
