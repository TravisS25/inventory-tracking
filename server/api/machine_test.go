package api

import (
	"encoding/json"
	"net/http"
	"strconv"
	"testing"
	"time"

	"github.com/TravisS25/httputil/confutil"

	"github.com/TravisS25/httputil/apiutil"
	"github.com/TravisS25/inventory-tracking/src/server/forms"
	"github.com/TravisS25/inventory-tracking/src/server/models"
	"github.com/go-ozzo/ozzo-validation"
)

func TestMachineUploadIntegrationTest(t *testing.T) {
	testCase1 := apiutil.TestCase{
		TestName:       "machineUpload1",
		Method:         "GET",
		RequestURL:     "/api/machine/upload/",
		ExpectedStatus: http.StatusOK,
		ExpectedBody:   "",
		Handler:        integrationTestRouter,
	}

	machines := []forms.MachineForm{
		forms.MachineForm{
			RoomID:          1,
			MachineStatusID: 1,
			MachineName:     "Machine1",
		},
		forms.MachineForm{
			RoomID:          1,
			MachineStatusID: 1,
			MachineName:     "Machine2",
		},
	}

	testCase2 := testCase1
	testCase2.TestName = "machineUpload2"
	testCase2.Method = "POST"
	testCase2.Form = machines

	emptyMachines := []forms.MachineForm{}
	testCase3 := testCase2
	testCase3.TestName = "machineUpload3"
	testCase3.ExpectedStatus = http.StatusNotAcceptable
	testCase3.ExpectedBody = "Can't have empty list"
	testCase3.Form = emptyMachines

	formValidator := forms.NewMachineValidator(TestFormValidation)
	requiredMachineErrors := make([]validation.Errors, 0)
	requiredMachines := []forms.MachineForm{
		forms.MachineForm{},
	}

	for _, v := range requiredMachines {
		err := formValidator.Validate(v)
		requiredMachineErrors = append(requiredMachineErrors, err.(validation.Errors))
	}

	for _, v := range requiredMachineErrors {
		if v["machineName"].Error() != "machineName is required" {
			t.Errorf("Should have machineName required error; got %s", v["machineName"])
		}
		if v["machineStatusID"].Error() != "machineStatusID is required" {
			t.Errorf("Should have machineStatusID required error; got %s", v["machineName"])
		}
		if v["roomID"].Error() != "roomID is required" {
			t.Errorf("Should have roomID required error; got %s", v["machineName"])
		}
	}

	requiredJSON, _ := json.Marshal(requiredMachineErrors)

	testCase4 := testCase3
	testCase4.TestName = "machineUpload4"
	testCase4.ExpectedBody = string(requiredJSON)
	testCase4.Form = requiredMachines

	apiutil.RunTestCases(t, []apiutil.TestCase{
		testCase1,
		testCase2,
		testCase3,
		testCase4,
	})

	for _, v := range machines {
		machine, err := models.QueryMachine(
			TestDB,
			"select * from machine where machine_name = $1;",
			v.MachineName,
		)

		if err != nil {
			t.Errorf("Could not delete machine %s", v.MachineName)
		}

		err = machine.Delete(TestDB)

		if err != nil {
			t.Errorf("Could not delete machine %s", v.MachineName)
		}
	}
}

func TestMachineDetailsIntegrationTest(t *testing.T) {
	testCase1 := apiutil.TestCase{
		TestName:       "machineDetails1",
		Method:         "GET",
		RequestURL:     "/api/machine/details/",
		ExpectedStatus: http.StatusOK,
		ExpectedBody:   "",
		Handler:        integrationTestRouter,
	}

	insertMachine := models.Machine{
		MachineName:     "MachineDetails",
		MachineStatusID: 1,
		RoomID:          1,
		ScannedTime:     time.Now().UTC().Format(confutil.DateTimeLayout),
	}
	err := insertMachine.Insert(TestDB)

	if err != nil {
		t.Fatal("Unable to insert machine")
	}

	testCase1.RequestURL += strconv.Itoa(insertMachine.ID) + "/"
	apiutil.RunTestCases(t, []apiutil.TestCase{
		testCase1,
	})

	machine, err := models.QueryMachine(
		TestDB,
		"select * from machine where id = $1;",
		insertMachine.ID,
	)

	if err != nil {
		t.Fatal("Could not find machine")
	}

	err = machine.Delete(TestDB)

	if err != nil {
		t.Fatal("Could not delete machine")
	}
}

func TestMachineEditIntegrationTest(t *testing.T) {
	insertMachine := models.Machine{
		MachineName:     "Machine1",
		MachineStatusID: 1,
		RoomID:          1,
		ScannedTime:     time.Now().UTC().Format(confutil.DateTimeLayout),
	}
	updateMachine := models.Machine{
		MachineName:     "Updated Machine1",
		MachineStatusID: 2,
		RoomID:          2,
	}

	err := insertMachine.Insert(TestDB)

	if err != nil {
		t.Fatal("Could not insert machine")
	}

	testCase1 := apiutil.TestCase{
		TestName:       "machineEdit1",
		Method:         "GET",
		RequestURL:     "/api/machine/edit/" + strconv.Itoa(insertMachine.ID) + "/",
		ExpectedStatus: http.StatusOK,
		ExpectedBody:   "",
		Handler:        integrationTestRouter,
	}

	testCase2 := testCase1
	testCase2.TestName = "machineEdit2"
	testCase2.Method = "PUT"
	testCase2.ExpectedStatus = http.StatusNotAcceptable
	testCase2.Form = forms.MachineForm{}

	testCase3 := testCase2
	testCase3.TestName = "machineEdit3"
	testCase3.ExpectedStatus = http.StatusOK
	testCase3.Form = updateMachine

	apiutil.RunTestCases(t, []apiutil.TestCase{
		testCase1,
		testCase2,
		testCase3,
	})

	machine, err := models.QueryMachine(
		TestDB,
		"select * from machine where id = $1;",
		insertMachine.ID,
	)

	if err != nil {
		t.Fatal("Could not query machine")
	}

	if machine.MachineName != updateMachine.MachineName {
		t.Errorf("machineName shuold be %s, got %s", updateMachine.MachineName, machine.MachineName)
	}

	if machine.MachineStatusID != updateMachine.MachineStatusID {
		t.Errorf("machineStatusID should be %d; got %d", updateMachine.MachineStatusID, machine.MachineStatusID)
	}

	if machine.RoomID != updateMachine.RoomID {
		t.Errorf("roomID should be %d, got %d;", updateMachine.RoomID, machine.RoomID)
	}

	err = insertMachine.Delete(TestDB)

	if err != nil {
		t.Fatalf("Could not delete machine")
	}
}

func TestMachineSearchIntegrationTest(t *testing.T) {
	testCase1 := apiutil.TestCase{
		TestName:       "machineSearch1",
		Method:         "GET",
		RequestURL:     "/api/machine/search/?take=20&skip=0",
		ExpectedStatus: http.StatusOK,
		ExpectedBody:   "",
		Handler:        integrationTestRouter,
	}

	apiutil.RunTestCases(t, []apiutil.TestCase{
		testCase1,
	})

}
