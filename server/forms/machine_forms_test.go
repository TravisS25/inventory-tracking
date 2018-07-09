package forms

import (
	"strconv"
	"testing"
	"time"

	"github.com/TravisS25/httputil/confutil"
	"github.com/TravisS25/httputil/formutil"
	"github.com/TravisS25/inventory-tracking/src/server/models"
)

func TestMachineForm(t *testing.T) {
	insertMachine := models.Machine{
		MachineName:     "ExistingMachine",
		RoomID:          1,
		MachineStatusID: 1,
		ScannedTime:     time.Now().UTC().Format(confutil.FormDateLayout),
	}
	err := insertMachine.Insert(TestDB)

	if err != nil {
		t.Fatal("Can't insert machine")
	}

	testCase1 := formutil.FormTestCase{
		TestName: "machineForm1",
		ValidationErrors: map[string]string{
			"roomID":          "Required",
			"machineStatusID": "Required",
			"machineName":     "Required",
		},
		FormValidator: NewMachineValidator(TestFormValidation),
		Form:          MachineForm{},
	}

	invalidForm := MachineForm{
		RoomID:          -1,
		MachineStatusID: -1,
		MachineName:     insertMachine.MachineName,
	}

	testCase2 := formutil.FormTestCase{
		TestName: "machineForm2",
		ValidationErrors: map[string]string{
			"roomID":          strconv.Itoa(invalidForm.RoomID) + " does not exist",
			"machineStatusID": strconv.Itoa(invalidForm.MachineStatusID) + " does not exist",
			"machineName":     invalidForm.MachineName + " already exists",
		},
		FormValidator: NewMachineValidator(TestFormValidation),
		Form:          invalidForm,
	}

	validForm := MachineForm{
		RoomID:          2,
		MachineStatusID: 2,
		MachineName:     insertMachine.MachineName,
		Instance:        &insertMachine,
	}

	testCase3 := formutil.FormTestCase{
		TestName:      "machineForm3",
		IsValidForm:   true,
		FormValidator: NewMachineValidator(TestFormValidation),
		Form:          validForm,
	}

	formutil.RunFormTests(t, []formutil.FormTestCase{
		testCase1,
		testCase2,
		testCase3,
	})

	err = insertMachine.Delete(TestDB)

	if err != nil {
		t.Fatal("Can't delete machine")
	}
}

func TestMachineSwapForm(t *testing.T) {
	form1 := MachineSwapForm{
		MachineStatusID: -1,
	}
	form2 := MachineSwapForm{
		MachineStatusID: 1,
	}

	testCase1 := formutil.FormTestCase{
		TestName: "machineSwap1",
		ValidationErrors: map[string]string{
			"machineStatusID": "Required",
		},
		FormValidator: NewMachineSwapValidator(TestFormValidation),
		Form:          MachineSwapForm{},
	}
	testCase2 := formutil.FormTestCase{
		TestName: "machineSwap2",
		ValidationErrors: map[string]string{
			"machineStatusID": strconv.Itoa(form1.MachineStatusID) + " does not exist",
		},
		FormValidator: NewMachineSwapValidator(TestFormValidation),
		Form:          form1,
	}
	testCase3 := formutil.FormTestCase{
		TestName:      "machineSwap3",
		IsValidForm:   true,
		FormValidator: NewMachineSwapValidator(TestFormValidation),
		Form:          form2,
	}

	formutil.RunFormTests(t, []formutil.FormTestCase{
		testCase1,
		testCase2,
		testCase3,
	})
}
